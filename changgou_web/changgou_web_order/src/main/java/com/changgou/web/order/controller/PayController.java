package com.changgou.web.order.controller;

import com.changgou.entity.Result;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.feign.WxPayFeign;
import jdk.nashorn.internal.ir.IfNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/wxpay")
public class PayController {
    @Autowired
    private OrderFeign orderFeign;
    @Autowired
    private WxPayFeign wxPayFeign;

    //跳转到微信支付的二维码页面
    @GetMapping
    public String wxPay(String orderId, Model model) {
        //1.根据orderId 查询订单 如果订单不存在跳转到错误页面
        Result<Order> result = orderFeign.findById(orderId);
        if (result.getData() == null) {
            return "fail";//出错页
        }
        Order order = result.getData();
        //2.根据订单的支付状态进行判断,如果不是未支付的订单 跳转到错误页面
        if (!"0".equals(order.getPayStatus())) {
            return "fail";
        }

        //3.基于payFeign调用统计下单接口 并获取返回结果
        Result pay = wxPayFeign.nativePay(orderId, order.getPayMoney());
        if (pay.getData() == null) {
            return "fail";
        }

        //4.封装结果数据
        Map map = (Map)pay.getData();
        map.put("payMoney",order.getPayMoney());
        map.put("orderId",orderId);
        model.addAllAttributes(map);
        return "wxpay";
    }

    //支付成功页面的跳转
    @RequestMapping("/toPaySuccess")
        public String toPaySuccess(Integer payMoney,Model model){
        model.addAttribute("payMoney",payMoney);
        return "paysuccess";
        }
}
