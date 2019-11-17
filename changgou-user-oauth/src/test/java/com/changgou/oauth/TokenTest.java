package com.changgou.oauth;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.net.URI;

import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TokenTest {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;


    //申请令牌测试
    @Test
    public void testCreateToken() throws Exception {

        //请求地址拼接  返回服务实例对象   http://localhost:9200/oauth/token
        ServiceInstance userauth = loadBalancerClient.choose("USER-AUTH");
        URI uri = userauth.getUri();
        String url = uri + "/oauth/token";

        //构建 body 指定认证类型 账号密码
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itheima");
        body.add("password","itheima");
        //构建  headers  header中存放 http basic认证信息
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        //进行 Base64编码 并将编码后的数据放入头文件中
        String httpBasic = httpbasic("changgou","changgou");
        headers.add("Authorization",httpBasic);

        //封装请求参数  body headers
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body,headers);

        //当后端出现了401 400 异常 后端对这两个异常不作处理 而是直接返回
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //不对400 和401进行处理
                if (response.getRawStatusCode()!=400&&response.getRawStatusCode()!=401){
                    super.handleError(response);
                }

            }
        });


        //发送请求  参数1请求路径 参数2 请求方式 HttpEntity 用于封装当前的请求参数  参数4 当前数据的返回类型

        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        Map map = exchange.getBody();
        System.out.println(map);
    }

    //进行 Base64编码 方法
    private String httpbasic(String client_id, String client_secret) {
        //将客户端id和客户端密码拼接 格式 "客户端id:客户端密码"
        String str = client_id+":"+client_secret;
        //进行base64编码
        byte[] encode = Base64Utils.encode(str.getBytes());
        return "Basic "+new String(encode);
    }


}
