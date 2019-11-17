package com.changgou.web.order.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.user.feign.AddressFeign;
import com.changgou.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/worder")
public class OrderController {
    @Autowired
    private CartFeign cartFeign;
    @Autowired
    private AddressFeign addressFeign;
    @RequestMapping("/ready/order")
    public String readyOrder(Model model) {
        //携带收件人信息
        List<Address> addressList = addressFeign.list().getData();
        model.addAttribute("addressList",addressList);
        //购物车信息
        Map map = cartFeign.list();
        List<OrderItem> orderItemList =(List<OrderItem> ) map.get("orderItemList");
        Integer totalMoney = (Integer)map.get("totalPrice");
        Integer totalNum = (Integer)map.get("totalNum");

        model.addAttribute("orders",orderItemList);
        model.addAttribute("totalMoney",totalMoney);
        model.addAttribute("totalNum",totalNum);

        //默认收件信息
        for (Address address : addressList) {
            if ("1".equals(address.getIsDefault())){
                //默认收件人
                model.addAttribute("defaultAddress",address);
                break;
            }
        }


        return "order";
    }
        @Autowired
        private OrderFeign orderFeign;

     @PostMapping("/add")
     @ResponseBody
    public Result add(@RequestBody Order order){
         Result result = orderFeign.add(order);
         return result;
     }
    @GetMapping("/toPayPage")
     public String toPayPage(String orderId,Model model){
        Order resultData =(Order) orderFeign.findById(orderId).getData();
       // Order resultData = result.getData();
        model.addAttribute("orderId",orderId);
        model.addAttribute("payMoney", resultData.getPayMoney());
        return "pay";
     }
}
