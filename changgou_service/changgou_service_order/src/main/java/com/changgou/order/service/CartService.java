package com.changgou.order.service;

import java.util.Map;

public interface CartService {
    //添加购物车
    void addCart(String skuId,Integer unm,String username);
    //查询购物车列表
    Map list (String username);
}
