package com.changgou.consume.dao;

import com.changgou.seckill.pojo.SeckillGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;


public interface SeckillGoodsMapper extends Mapper<SeckillGoods> {
    @Update("UPDATE tb_seckill_goods  SET stock_count = stock_count-1 WHERE id = #{id} and stock_count>=1")
  int updateStockCount(@Param("id") Long id);
}
