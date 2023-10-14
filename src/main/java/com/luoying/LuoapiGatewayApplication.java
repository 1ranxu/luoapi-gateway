package com.luoying;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LuoapiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuoapiGatewayApplication.class, args);
    }

/*    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("toBaidu", r -> r.path("/baidu")
                        .uri("https://www.baidu.com"))
                .route("toTecent", r -> r.path("/tencent")
                        .uri("https://www.tencent.com"))
                .build();
    }*/
}
