package com.changgou.oauth.controller_20190929_105615;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import com.changgou.oauth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

@Controller

@RequestMapping("/oauth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;
    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;
    //授权登录
    @PostMapping("/login")
    @ResponseBody
    public Result login(String username, String password) {
        //1.校验参数
        if (StringUtils.isEmpty(username)){
            throw  new RuntimeException("用户名不存在");

        }
        if (StringUtils.isEmpty(password)){
            throw  new RuntimeException("密码不存在");
        }
        //申请令牌
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);

        this.saveJtiToCookie(authToken.getJti());



        return new Result(true, StatusCode.OK,"登录成功",authToken);
    }

    private void saveJtiToCookie(String jti) {
        HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
        //将jti的值保存在cookie中
      //  CookieUtil cookieUtil =new CookieUtil();
        //cookieDomain 域名
        CookieUtil.addCookie(response,cookieDomain,"/","uid",jti,cookieMaxAge,false);
    }

    @RequestMapping("/toLogin")
    public String logingo(@RequestParam(value = "FROM",required = false,defaultValue = "") String from, Model model){

        model.addAttribute("from",from);
        return "login";
    }
}
