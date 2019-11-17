package com.changgou.search.service;

//将数据同步到ES中国
public interface SkuSearchService {
    //1.创建索引库结构
    void createIndexAndMapping();
    //2.导入全部数据进入es
    void importAll();

    //3.根据spuid查询skuList再导入索引库
    public void importDataToESBySpuId(String spuId);
    //商品下架
    void goodsDown(String spuId);
}
