package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    @GetMapping("/spu/{spuId}")
    public List<Sku> findSkuBySpuId(@PathVariable String spuId);

    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable String id);

    @PostMapping(value = "/decr/count")
    public Result decrCount(@RequestParam("username") String username);

    @RequestMapping(value = "/resumeStockNum")
    public Result resumeStockNum(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num);
}
