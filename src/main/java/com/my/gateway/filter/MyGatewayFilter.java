package com.my.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * url
 * @author yangsm
 */
//@Component
@Slf4j
public class MyGatewayFilter implements GatewayFilter, Ordered  {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put("time", System.currentTimeMillis());
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute("time");
                    if (startTime != null) {
                        log.info("{}:{} ms",exchange.getRequest().getURI().getRawPath() , (System.currentTimeMillis() - startTime) );
                    }
                })
        );
    }

    @Override
    public int getOrder() {
        //最低优先级
        return Ordered.LOWEST_PRECEDENCE;
    }
}
