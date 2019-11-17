package com.changgou.order.feign;

import com.changgou.entity.Result;
import com.changgou.order.pojo.Order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "order")
@RequestMapping("/order")
public interface OrderFeign {
    /**
     * 提交订单数据
     */
    @PostMapping
    public Result add(@RequestBody Order order);

    @GetMapping("/{id}")
    public Result<Order> findById(@PathVariable String id);


}
