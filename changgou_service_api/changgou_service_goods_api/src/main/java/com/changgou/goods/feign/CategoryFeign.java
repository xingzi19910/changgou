package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "goods")
public interface CategoryFeign {
       //根据id查找分类数据
    @GetMapping("/category/{id}")
    public Result<Category> findById(@PathVariable Integer id);
}
