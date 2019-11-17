package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

public interface AuthService {
    //用户认证
  public   AuthToken login(String username,String password,String clientId,String clientSecret);
}
