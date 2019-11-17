package com.changgou.system.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 获取客户端的url
 */
@Component
public class UrlFilter implements GlobalFilter, Ordered {
    //具体的业务逻辑
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("经过第二个过滤器IpFilter");
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();
        System.out.println("url:"+url);
        //放行
        return chain.filter(exchange);
    }
    //过滤器的优先级 数值越小 优先级越高
    @Override
    public int getOrder() {
        return 2;
    }
}
