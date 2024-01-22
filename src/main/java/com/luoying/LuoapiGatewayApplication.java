package com.luoying;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class LuoapiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LuoapiGatewayApplication.class, args);
    }

    /*@Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("toBaidu", r -> r.path("/baidu")
                        .uri("https://www.baidu.com"))
                .route("toTecent", r -> r.path("/tencent")
                        .uri("https://www.tencent.com"))
                .build();
    }*/
}
