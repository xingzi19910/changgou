package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.config.CustomMessageSender;
import com.changgou.seckill.config.RabbitMQConfig;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SecKillOrderService;
import com.changgou.util.IdWorker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CustomMessageSender customMessageSender;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;


    @Override
    //异步下单预扣减库存实现
    public boolean add(Long id, String time, String username) {
        //防止用户恶意刷单 防止重复提交
        String result = this.preventRepeatCommit(username, id);
        if ("fail".equals(result)) {
            return false;
        }
        //防止相同商品重复秒杀
        SeckillOrder order = seckillOrderMapper.getSecKillOrderByUserNameAndGoodsId(username, id);
        if (order!=null){
            return false;
        }


        //1.获取redis中的商品信息与库存信息,并进行判断
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("seckill_goods_" + time).get(id);
        String count = (String) redisTemplate.opsForValue().get("seckill_goods_stock_count_" + id);
        if (StringUtils.isEmpty(count)) {
            return false;
        }

        int value = Integer.parseInt(count);
        if (seckillGoods==null || value<=0){
            return false;
        }
        //2.执行redis的预扣减库存操作,并获取扣减之后的库存值   decrement保证原子性
        Long decrement = redisTemplate.opsForValue().decrement("seckill_goods_stock_count_" + id);
        //没有库存
        if (decrement <= 0) {
            //删除redis中的商品信息与库存信息
            redisTemplate.boundHashOps("seckill_goods_" + time).delete(id);
            redisTemplate.delete("seckill_goods_stock_count_" + seckillGoods.getId());
        }
        //有库存   如果有库存，则创建秒杀商品订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId()); //商品订单id
        seckillOrder.setSeckillId(id);  //
        seckillOrder.setMoney(seckillGoods.getCostPrice()); //秒杀价格
        seckillOrder.setUserId(username); //用户
        seckillOrder.setSellerId(seckillGoods.getSellerId()); //商家id
        seckillOrder.setCreateTime(new Date()); //创建时间
        seckillOrder.setStatus("0"); //状态
        //发送消息
        customMessageSender.sendMessage("", RabbitMQConfig.SECKILL_ORDER_KEY, JSON.toJSONString(seckillOrder));


        return true;
    }

    /**
     * 防止重复提交
     *
     * @param username
     * @param id
     * @return
     */
    private String preventRepeatCommit(String username, Long id) {
        //自定义redis_key
        String redisKey = "seckill_user_" + username + "_id_";
        //使用 increment自动增长  自动增长1
        Long count = redisTemplate.opsForValue().increment(redisKey, 1);
        if (count == 1) {
            //代表当前的用户是第一次访问 放行
            //对当前的key设置一个五分钟的有效期
            redisTemplate.expire(redisKey, 5, TimeUnit.MINUTES);
            return "success";

        }
        if (count > 1) {
            return "fail";
        }
        return "fail";
    }
}
