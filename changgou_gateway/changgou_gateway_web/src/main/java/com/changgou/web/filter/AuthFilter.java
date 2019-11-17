package com.changgou.web.filter;

import com.changgou.web.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;



//网关过滤器
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_URL="http://localhost:8001/api/oauth/toLogin";

    @Autowired
    private AuthService authService;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取响应对象
        ServerHttpResponse response = exchange.getResponse();
        //1.判断当前请求路径是否为登录请求 如果是则直接放行
        String path = request.getURI().getPath();
        if ("/api/oauth/login".equals(path) || !UrlFilter.hasAuthorize(path)){
            //放行
            return chain.filter(exchange);
        }
        //2.从cookie中获取jti的值 如果该值不存在拒绝
        String jti = authService.getJti(request);
        if (StringUtils.isEmpty(jti)){
            //拒绝请求  登录页面

                return this.toLoginPage(LOGIN_URL+"?FROM="+request.getURI().getPath(),exchange);
        }

        //3.从redis中获取jwt中的值 如果不存在拒绝
        String jwt = authService.getJwt(jti);
        if (StringUtils.isEmpty(jwt)){
           return this.toLoginPage(LOGIN_URL+"?FROM="+request.getURI().getPath(),exchange);
        }
        //4.对当前请求进行增强 让它携带令牌信息
       request.mutate().header("Authorization", "Bearer " + jwt);

        return chain.filter(exchange);
    }

    /**
     * 登录跳转页面
     * @return
     */
    private Mono<Void> toLoginPage(String url,ServerWebExchange exchange) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("Location",url);
        return response.setComplete();
    }

    //设置过滤器的优先级
    @Override
    public int getOrder() {
        return 0;
    }
}
