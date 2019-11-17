package com.changgou.web.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;


@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    //从cookie中获取jti
    public String getJti(ServerHttpRequest request) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie cookie = cookies.getFirst("uid");
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    //根据获取到的jti从redis中获取jwt
    public String getJwt(String jti) {
        BoundValueOperations<String, String> valueOps = redisTemplate.boundValueOps(jti);
        String jwt = valueOps.get();

        return jwt;
    }
}
