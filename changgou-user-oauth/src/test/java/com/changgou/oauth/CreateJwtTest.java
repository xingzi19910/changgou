package com.changgou.oauth;


import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;

import java.security.interfaces.RSAPrivateCrtKey;
import java.util.HashMap;
import java.util.Map;

public class CreateJwtTest {

    @Test
    public void creatJwtTest(){
        //文件路径
        String location = "changgou.jks";
        //私钥位置 参数 文件路径
        ClassPathResource resource = new ClassPathResource(location);
        //秘钥库密码
        String passwords = "changgou";
        //秘钥密码
        String password = "changgou";
        //秘钥别名
        String alias = "changgou";

        //创建一个秘钥工厂 参数1 私钥指定位置   参数2秘钥库的密钥
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, passwords.toCharArray());
        //读取秘钥对 公钥 私钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, password.toCharArray());

        //获取私钥 rsa私钥
        RSAPrivateCrtKey aPrivate = (RSAPrivateCrtKey)keyPair.getPrivate();
        Map<String,String> map = new HashMap<>();
        map.put("id","1");
        map.put("name","demo");
        map.put("age","18");
        //生成jwt令牌 参数1 jtw内容 content  singer rsa私钥作为当前签名
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), new RsaSigner(aPrivate));

        //取出令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);


    }



}
