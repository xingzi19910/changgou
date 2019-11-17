package com.changgou.pay.service.impl;

import com.changgou.pay.service.WxPayService;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class WxPayServiceImpl implements WxPayService {
    @Autowired
    private WXPay wxPay;

    @Value("${wxpay.notify_url}")
    private String notifyurl;

    /**
     * 生成微信支付二维码
     *
     * @param orderId
     * @param money
     * @return
     */
    @Override
    public Map nativePay(String orderId, Integer money) {
        //1.封装请求参数
        try {
            Map<String, String> map = new HashMap<>();
            map.put("body", "畅购商城");//商品描述
            map.put("out_trade_no", orderId);//订单号
            BigDecimal payMoney = new BigDecimal("0.01");
            BigDecimal fen = payMoney.multiply(new BigDecimal("100")); //1.00
            fen = fen.setScale(0, BigDecimal.ROUND_UP);//1
            map.put("total_fee", String.valueOf(fen));//标价币种
            map.put("spbill_create_ip", "127.0.0.1");//终端IP
            map.put("notify_url", notifyurl);//回调地址,先随便填一个
            map.put("trade_type", "NATIVE");//交易类型
            Map<String, String> map1 = wxPay.unifiedOrder(map);
            return map1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //基于微信查询订单信息
    @Override
    public Map queryOrder(String orderId) {
        Map map = new HashMap<>();
        map.put("out_trade_no",orderId); //商户订单号

        try {
            return wxPay.orderQuery(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //基于微信关闭订单
    @Override
    public Map closeOrder(String orderId) {
        Map map = new HashMap();
        map.put("out_trade_no",orderId);//设置商户订单号
        try {
          return   wxPay.closeOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}

