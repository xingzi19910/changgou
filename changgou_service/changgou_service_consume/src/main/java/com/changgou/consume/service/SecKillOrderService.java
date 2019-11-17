package com.changgou.consume.service;

import com.changgou.seckill.pojo.SeckillOrder;

public interface SecKillOrderService {
    //同步mysql
    int handleCreateOrder(SeckillOrder order);
}
