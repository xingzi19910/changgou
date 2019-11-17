package com.changgou.consume.service.impl;

import com.changgou.consume.dao.SeckillGoodsMapper;
import com.changgou.consume.dao.SeckillOrderMapper;
import com.changgou.consume.service.SecKillOrderService;
import com.changgou.seckill.pojo.SeckillOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    //同步mysql数据
    @Override
    @Transactional
    public int handleCreateOrder(SeckillOrder seckillOrder) {
        //1.扣减秒杀商品的库存
        int count = seckillGoodsMapper.updateStockCount(seckillOrder.getSeckillId());
        if (count<=0){
            return 0;
        }
        //新增秒杀订单
        int i = seckillOrderMapper.insertSelective(seckillOrder);
        if (i<=0){
            return i;
        }
        return 1;
    }
}
