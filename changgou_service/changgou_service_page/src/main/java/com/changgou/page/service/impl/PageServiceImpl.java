package com.changgou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private CategoryFeign categoryFeign;
    @Value("${pagepath}")
    private String pagepath;
    @Autowired
    private TemplateEngine templateEngine;
    //生成静态化页面
    @Override
    public void generateItemPage(String spuId) {
        //1.获取Context 对象用于储存商品的相关数据
        Context context = new Context();
        Map<String, Object> map = this.findItemData(spuId);
        context.setVariables(map);
        //2.获取商品详情页面的存储位置
        File dir = new File(pagepath);
        //3.判断当前存储位置的文件夹是否存在 如果不存在则新建
        if (!dir.exists()){
            dir.mkdir();
        }
        //4.定义输出流 完成文件的生成
        File file = new File(dir +"/"+spuId+".html");
        Writer out = null;

        try {
            out= new PrintWriter(file);
            //生成文件
            /**
             * 1.模板名称
             * 2.context对象,包含了模板需要的数据
             * 3.输出流 指定文件输出的位置
             */
            templateEngine.process("item",context,out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            //关闭流
            try {
                out.close();
            } catch (IOException e) {


            }
        }


    }
        //获取静态化页面数据
    private Map<String, Object> findItemData(String spuId) {
        Map<String,Object> resultMap = new HashMap<>();
        //获取spu信息
        Result<Spu> spuResult = spuFeign.findSpuById(spuId);
        Spu spu = spuResult.getData();
        resultMap.put("spu",spu);
        //获取图片信息
        if (spu!=null){
            if (!StringUtils.isEmpty(spu.getImages())){
                resultMap.put("imageList",spu.getImages().split(","));
            }
        }
        //获取分类信息
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        resultMap.put("category1",category1);
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        resultMap.put("category2",category2);
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
        resultMap.put("category3",category3);
        //获取sku集合
        List<Sku> skuList = skuFeign.findSkuBySpuId(spuId);
        resultMap.put("skuList",skuList);
        resultMap.put("specificationList", JSON.parseObject(spu.getSpecItems(),Map.class));

        return resultMap;
    }
}
