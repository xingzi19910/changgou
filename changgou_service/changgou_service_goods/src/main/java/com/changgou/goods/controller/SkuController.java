package com.changgou.goods.controller;

import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.service.SkuService;
import com.changgou.goods.pojo.Sku;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/sku")
public class SkuController {


    @Autowired
    private SkuService skuService;

    /**
     * 查询全部数据
     *
     * @return
     */
    @GetMapping
    public Result findAll() {
        List<Sku> skuList = skuService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", skuList);
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id) {
        Sku sku = skuService.findById(id);
        return new Result(true, StatusCode.OK, "查询成功", sku);
    }


    /***
     * 新增数据
     * @param sku
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Sku sku) {
        skuService.add(sku);
        return new Result(true, StatusCode.OK, "添加成功");
    }


    /***
     * 修改数据
     * @param sku
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Sku sku, @PathVariable String id) {
        sku.setId(id);
        skuService.update(sku);
        return new Result(true, StatusCode.OK, "修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable String id) {
        skuService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search")
    public Result findList(@RequestParam Map searchMap) {
        List<Sku> list = skuService.findList(searchMap);
        return new Result(true, StatusCode.OK, "查询成功", list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result findPage(@RequestParam Map searchMap, @PathVariable int page, @PathVariable int size) {
        Page<Sku> pageList = skuService.findPage(searchMap, page, size);
        PageResult pageResult = new PageResult(pageList.getTotal(), pageList.getResult());
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }

    //商品上架 之 消息消费者根据SpuId查询 sku列表 基于feign向外暴露
    @GetMapping(value = "/spu/{spuId}")
    public List<Sku> findSkuBySpuId(@PathVariable String spuId) {

        Map<String, Object> searchMap = new HashMap<>();
        //前端传入特定自定义字符 判断
        if (!"all".equals(spuId)) {
            //如果前端传入的数据和spuid不符 将的id 添加到当前map中 组成新的查询条件进行查询
            searchMap.put("spuId", spuId);
        }
        //前端传入特定自定义字符 如果和spuid一样 查询所有审核通过的列表
        searchMap.put("status", "1");
        List<Sku> list = skuService.findList(searchMap);
        return list;
    }

    @PostMapping(value = "/decr/count")
    public Result decrCount(@RequestParam("username") String username){
        //库存递减
        skuService.decrCount(username);

        return new Result(true,StatusCode.OK,"出库成功！");
    }
    //回滚库存
    @RequestMapping(value = "/resumeStockNum")
    public Result resumeStockNum(@Param("skuId") String skuId, @Param("num") Integer num){
        skuService.resumeStockNum(skuId,num);
        return new Result(true,StatusCode.OK,"商品回滚库存");
    }
}
