package com.changgou.test;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class TestBcrypt {
    public static void main(String[] args) {

     for (int i = 0; i < 10; i++) {
         //获取盐
         String gensalt = BCrypt.gensalt();
         System.out.println(gensalt);
         //基于当前的盐对密码进行加密
         String hashpw = BCrypt.hashpw("123456", gensalt);
         System.out.println(hashpw);
         //密码校验
         boolean checkpw = BCrypt.checkpw("123456", hashpw);
         System.out.println(checkpw);
        }



    }
}
