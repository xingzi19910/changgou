package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.ESManagerMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
//把数据同步到ES中
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private ESManagerMapper esManagerMapper;

    //创建索引库结构
    @Override
    public void createIndexAndMapping() {
        //创建索引
        elasticsearchTemplate.createIndex(SkuInfo.class);
        //创建映射
        elasticsearchTemplate.putMapping(SkuInfo.class);

    }

    //导入全部数据到ES中
    @Override
    public void importAll() {
        //查询sku 返回集合 基于feign远程调用为服务
        List<Sku> skuList = skuFeign.findSkuBySpuId("all");
        if (skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("没有数据可导入ES中...");
        }
        //将skuList转换为json
        String jsonString = JSON.toJSONString(skuList);
        //再将json转换为skuInfo
        List<SkuInfo> array = JSON.parseArray(jsonString, SkuInfo.class);
        for (SkuInfo skuInfo : array) {
            //将skuinfo中的规格信息转换为map
            Map parse = (Map) JSON.parse(skuInfo.getSpec());
            skuInfo.setSpecMap(parse);
            //导入索引库
            esManagerMapper.save(skuInfo);
        }


    }

    //根据spuid查询skulist再导入索引库
    @Override
    public void importDataToESBySpuId(String spuId) {
        //查询sku集合
        List<Sku> skuList = skuFeign.findSkuBySpuId(spuId);
        if (skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("没有数据可导入ES中...");
        }
        //将集合转换成json
        String jsonString = JSON.toJSONString(skuList);
        //将json转换成skuinfo
        List<SkuInfo> jsonArray = JSON.parseArray(jsonString,SkuInfo.class);
        for (SkuInfo skuInfo : jsonArray) {
            //将规格信息转换为map
            Map spemap = JSON.parseObject(skuInfo.getSpec());
            skuInfo.setSpecMap(spemap);
            esManagerMapper.save(skuInfo);
        }
    }

    //商品下架
    @Override
    public void goodsDown(String spuId) {
            //查询sku集合
        List<Sku> skuList = skuFeign.findSkuBySpuId(spuId);
        if (skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("没有数据可删除...");
        }
        for (Sku sku : skuList) {
            esManagerMapper.deleteById(Long.parseLong(sku.getId()));
        }

    }

}
