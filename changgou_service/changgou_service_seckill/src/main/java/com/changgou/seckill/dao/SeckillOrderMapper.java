package com.changgou.seckill.dao;

import com.changgou.seckill.pojo.SeckillOrder;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;


public interface SeckillOrderMapper extends Mapper<SeckillOrder> {
    @Select("SELECT * FROM tb_seckill_order WHERE user_id=#{username} and seckill_id=#{id}")
  SeckillOrder getSecKillOrderByUserNameAndGoodsId(String username,Long id); //
}
