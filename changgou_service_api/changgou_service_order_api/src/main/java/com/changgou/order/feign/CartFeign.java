package com.changgou.order.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "order")
@RequestMapping("/cart")
public interface CartFeign {
    //添加购物车
    @GetMapping("/addCart")
    public Result add(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num);
    //查询购物车列表
    @GetMapping(value = "/list")
    public Map list();

}
