package com.changgou.oauth;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {

    @Test
    public void testParseToken(){
        //变量声明jwt 和公钥
        String jwt ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU3MzM0MjgxOSwiYXV0aG9yaXRpZXMiOlsic2Vja2lsbF9saXN0IiwiZ29vZHNfbGlzdCIsInVzZXIiLCJhY2NvdXRhbnQiXSwianRpIjoiZWJhZDIzZjEtNmVjMC00MjY3LWE2YWMtZjViMTBhZGZkZWY5IiwiY2xpZW50X2lkIjoiY2hhbmdnb3UiLCJ1c2VybmFtZSI6ImhlaW1hIn0.AzaYGUw_envwHoEqSUpycs2cpKwvNsVc6QJP83Z-B1WxduLgRJgnQggU9kg4fMObYuafprDS6YuyOAhVpVIR7DSbuiyrP5QKg0je3TvmPkZb7is3Pqcn2uXQeWfEB7kxwMXpZKXStpXh5zRMA3yaP8-D5leUOzN5igAqAZwo5dy7VtoFrmXGjfttHSg6UfNpBEpMisE5zu_ZwMpCA31UvRs2g8aNZTSNGKklQCSp4ajlCHTahWSfr1LaXjHPkdURmD-pfhaZIj-j-9PToQADdy5aCxt8kCqYz4DWEXsmrg767MN4B3IJTRbmmOOKLcxhR4dM5gmmlTgyeEJkXVKAkA";
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFsEiaLvij9C1Mz+oyAmt47whAaRkRu/8kePM+X8760UGU0RMwGti6Z9y3LQ0RvK6I0brXmbGB/RsN38PVnhcP8ZfxGUH26kX0RK+tlrxcrG+HkPYOH4XPAL8Q1lu1n9x3tLcIPxq8ZZtuIyKYEmoLKyMsvTviG5flTpDprT25unWgE4md1kthRWXOnfWHATVY7Y/r4obiOL1mS5bEa/iNKotQNnvIAKtjBM4RlIDWMa6dmz+lHtLtqDD2LF1qwoiSIHI75LQZ/CNYaHCfZSxtOydpNKq8eb1/PGiLNolD4La2zf0/1dlcr5mkesV570NxRmU1tFm8Zd3MZlZmyv9QIDAQAB-----END PUBLIC KEY-----";
        //解析 校验 Jwt
        Jwt jwt1 = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publickey));
        //获取jwt原始内容
        String claims = jwt1.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt1.getEncoded();
        System.out.println(encoded);


    }
}
