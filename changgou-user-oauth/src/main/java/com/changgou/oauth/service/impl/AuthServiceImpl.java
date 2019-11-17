package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Autowired
    private  StringRedisTemplate stringRedisTemplate;

    @Value("${auth.ttl}")
    private long ttl;


    /**
     * 申请令牌
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //获取url  http://localhost:9200/oauth/token
        ServiceInstance choose = loadBalancerClient.choose("USER-AUTH");
        URI uri = choose.getUri();
        String url = uri+"/oauth/token";

        //封装requestEntity实例
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        MultiValueMap<String, String> hearder = new LinkedMultiValueMap<>();
            hearder.add("Authorization",this.getBasic(clientId,clientSecret));
        HttpEntity<MultiValueMap<String, String>> requestEntity=new HttpEntity<>(body,hearder);
        //对400 401异常处理
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
               if (response.getRawStatusCode()!=400&&response.getRawStatusCode()!=401){
                   super.handleError(response);
               }

            }
        });
        //1.发送请求
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map map = exchange.getBody();

        if (map==null||map.get("access_token")==null||map.get("refresh_token")==null||map.get("jti")==null){
            throw  new RuntimeException("申请令牌失败");
        }
        //封装 authToken
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken((String)map.get("access_token"));
        authToken.setJti((String)map.get("jti"));
        authToken.setRefreshToken((String)map.get("refresh_token"));
        //将jti作为redis的key jwt作为redis的value存放
        stringRedisTemplate.boundValueOps(authToken.getJti()).set(authToken.getAccessToken(),ttl, TimeUnit.SECONDS);

        return authToken;



    }
        //构建Basic64 编码   Basic Y2hhbmdnb3U6Y2hhbmdnb3U=
    private String getBasic(String clientId,String clientSecret ){
        //客户端id:客户端密码
        String string = clientId+":"+clientSecret;
        //编码
        byte[] encode = Base64Utils.encode(string.getBytes());

        return "Basic "+new String(encode);
    }

}
