package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.pay.config.RabbitMQConfig;
import com.changgou.pay.service.WxPayService;
import com.changgou.util.ConvertUtils;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wxpay")
public class WxPayController {
    @Autowired
    private WxPayService wxPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //跳转支付方式页面 微信下单
    @GetMapping("/nativePay")
    public Result nativePay(@RequestParam("orderId") String orderId, @RequestParam("money") Integer money) {
        Map map = wxPayService.nativePay(orderId, money);
        return new Result(true, StatusCode.OK, "微信下单成功", map);
    }

    /**
     * 回调
     */
    @RequestMapping("/notify")
    public void notifyLogic(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("支付成功回调....");
        try {
            //输入流转换为字符串
            String xml = ConvertUtils.convertToString(request.getInputStream());
            System.out.println(xml);
            //解析xml
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            //如果返回的结果是成功
            if ("SUCCESS".equals(map.get("result_code"))) {
                //查询订单
                Map queryOrder = wxPayService.queryOrder(map.get("out_trade_no"));
                System.out.println("查询订单返回结果"+queryOrder);
                //如果查询结果是成功的发送到mq中
                if ("SUCCESS".equals(queryOrder.get("result_code"))) {
                    Map map1 = new HashMap();
                    map1.put("orderId", queryOrder.get("out_trade_no"));
                    map1.put("transactionId", queryOrder.get("transaction_id"));
                    rabbitTemplate.convertAndSend("", RabbitMQConfig.ORDER_PAY, JSON.toJSONString(map1));
                   //完成双向通信
                    rabbitTemplate.convertAndSend("paynotify","",map.get("out_trade_no"));

                    //给微信一个结果通知
                    response.setContentType("text/xml");
                    String data = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
                    response.getWriter().write(data);

                }else {
                    System.out.println(map.get("err_code_des"));//错误信息描述
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //基于微信查询订单
    @GetMapping("/query/{orderId}")
    public Result queryOrder(@PathVariable String orderId){
        Map map = wxPayService.queryOrder(orderId);
        return new Result(true,StatusCode.OK,"",map);
    }
    //关闭微信订单
    @PutMapping("/close/{orderId}")
    public Result closeOrder(@PathVariable String orderId){
        Map map = wxPayService.closeOrder(orderId);
        return new Result(true,StatusCode.OK,"",map);
    }
}
