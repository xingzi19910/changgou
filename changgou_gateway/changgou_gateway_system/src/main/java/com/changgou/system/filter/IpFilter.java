package com.changgou.system.filter;


import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

//全局过滤器
@Component
public class IpFilter implements GlobalFilter, Ordered {

//具体业务逻辑
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("经过第一个过滤器IpFilter");
        //获取客户端ip
        ServerHttpRequest request = exchange.getRequest();
        InetSocketAddress address = request.getRemoteAddress();
        System.out.println("ip:"+address.getHostName());
        //放行
        return chain.filter(exchange);
    }
        //过滤器的优先级,当方法的返回值越小 优先级越高
    @Override
    public int getOrder() {
        return 1;
    }
}
