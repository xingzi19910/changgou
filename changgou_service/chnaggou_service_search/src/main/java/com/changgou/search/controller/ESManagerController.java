package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.SkuSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/es")
public class ESManagerController {
    @Autowired
    private SkuSearchService skuSearchService;


    @GetMapping("/creat")
    public Result createIndexAndMapping(){
        skuSearchService.createIndexAndMapping();
        return new Result(true, StatusCode.OK,"索引创建成功");
    }

    @GetMapping("/search")
    public Result importAll(){
        skuSearchService.importAll();
        return new Result(true,StatusCode.OK,"导入数据成功");
    }

}
