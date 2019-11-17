package com.changgou.pay.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "pay")
@RequestMapping("/wxpay")
public interface WxPayFeign {
    @GetMapping("/nativePay")
    public Result nativePay(@RequestParam("orderId")String orderId, @RequestParam("money") Integer money);

    @GetMapping("/query/{orderId}")
    public Result queryOrder(@PathVariable("orderId") String orderId);

    @PutMapping("/close/{orderId}")
    public Result closeOrder(@PathVariable("orderId") String orderId);
}
