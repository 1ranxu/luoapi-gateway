package com.luoying;


import com.alibaba.nacos.common.utils.StringUtils;
import com.luoying.entity.InterfaceInfo;
import com.luoying.entity.User;
import com.luoying.provider.CommonService;
import com.luoying.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * 全局过滤
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    private static final List<String> IP_BLACK_LIST = Arrays.asList("10.10.10.10");
    private static final String GATEWAY_HOST = "http://gateway.luoapi.icu";

    @DubboReference
    private CommonService commonService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 记录请求日志
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().toString();
        String path = request.getPath().value();
        log.info("请求唯一标识：{}", request.getId());
        log.info("请求路径：{}", path);
        log.info("请求方法：{}", method);
        log.info("请求参数：{}", request.getQueryParams());
        log.info("请求地址来源：{}", request.getRemoteAddress());
        log.info("网关本地地址：{}", request.getLocalAddress());
        // 2. 判断ip是否在黑名单中
        ServerHttpResponse response = exchange.getResponse();
        if (IP_BLACK_LIST.contains(request.getRemoteAddress().getHostString())) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        // 3. 用户鉴权（判断ak和sk是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        String timestamp = headers.getFirst("timestamp");
        // 请求头参数必须都携带
        if (StringUtils.isAnyBlank(body, sign, accessKey, timestamp)) {
            return handleNoAuth(response);
        }
        // 过期时间不能在当前时间之前 或者 过期时间减当前时间不能超过一分钟
        Date expireTime = new Date(Long.parseLong(timestamp));
        Date currentTime = new Date();
        if (expireTime.before(currentTime) || (expireTime.getTime() - currentTime.getTime()) / 1000 > 60) {
            return handleNoAuth(response);
        }
        // 根据accessKey查询数据库，是否存在包含该accessKey的用户
        User invokeUser = null;
        try {
            invokeUser = commonService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error", e);
            return handleNoAuth(response);
        }
        // 用户角色不能是被ban用户
        if (invokeUser.getUserRole().equals("ban")) {
            return handleNoAuth(response);
        }
        // 根据记录获取secretKey
        String secretKey = invokeUser.getSecretKey();
        String dbSign = SignUtil.genSign(body, secretKey);
        if (!dbSign.equals(sign)) {
            return handleNoAuth(response);
        }
        // 4. 请求的模拟接口是否存在
        InterfaceInfo invokeInterfaceInfo = null;
        try {
            invokeInterfaceInfo = commonService.getInvokeInterfaceInfo(method, GATEWAY_HOST + path);
        } catch (Exception e) {
            log.error("getInvokeInterfaceInfo error", e);
            return handlInvokeError(response);
        }
        Long userId = invokeUser.getId();
        // 用户剩余积分不能小于接口扣减积分
        if (invokeUser.getScore() < invokeInterfaceInfo.getReduceScore()) {
            return handleNoAuth(response);
        }
        // 5. 请求转发，调用接口
        return responseLog(exchange, chain, userId, invokeInterfaceInfo);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handlInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

    public Mono<Void> responseLog(ServerWebExchange exchange, GatewayFilterChain chain, long userId, InterfaceInfo interfaceInfo) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓冲区工厂缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 获取响应码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                // 获取装饰后的Response，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 等模拟接口调用完成后，才会执行
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                // 6. 调用成功，扣减积分
                                try {
                                    commonService.invokeCount(userId, interfaceInfo.getId(), interfaceInfo.getReduceScore());
                                } catch (Exception e) {
                                    log.error("invokeCount error", e);
                                }
                                // content就是模拟接口的返回值
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                sb2.append("<--- {} {} \n");
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);
                                // 8. 记录响应日志
                                log.info("响应结果：{}", data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 放行调用模拟接口并设置 response 对象为装饰后的，调用完模拟接口就会拼接字符串
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }
}

