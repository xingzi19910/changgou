package com.changgou.pay.service;

import java.util.Map;

public interface WxPayService {
    /**
     * 生成微信二维码
     */
    Map nativePay(String orderId, Integer money);
    //基于微信查询订单
    Map queryOrder(String orderId);
    //微信关闭订单
    Map closeOrder(String orderId);
}
