package com.changgou.search.service;


import java.util.Map;

//商品搜索
public interface SearchService {
    //根据搜索关键字查询
    Map<String, Object> findByKeyWord(Map<String, String> searchMap) ;
}
