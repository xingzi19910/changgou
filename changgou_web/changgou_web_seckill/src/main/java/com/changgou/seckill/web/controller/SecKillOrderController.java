package com.changgou.seckill.web.controller;

import com.changgou.entity.Result;


import com.changgou.seckill.feign.SecKillOrderFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckillorder")
public class SecKillOrderController {
    @Autowired
    private SecKillOrderFeign secKillOrderFeign;

    @RequestMapping("/add")
    public Result add(@RequestParam("time") String time, @RequestParam("id") Long id){
        Result result = secKillOrderFeign.add(time, id);
        return  result;
    }
}
