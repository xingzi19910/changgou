package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.util.DateUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时任务
 */
@Component
public class SecKillGoodsPushTask {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    private static  final String SECKILL_GOODS_KEY="seckill_goods_";
    private static  final String SECKILL_GOODS_STOCK_COUNT_KEY="seckill_goods_stock_count_";
    @Scheduled(cron = "0/15 * * * * ?")
    public void loadSecKillGoodsToRedis(){
        //1.获取时间段集合循环遍历
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                //格式转换
            String date = DateUtil.date2Str(dateMenu);
            //条件查询
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();

            criteria.andEqualTo("status","1");  //审核通过
            criteria.andGreaterThan("stockCount","0");//库存大于0
            criteria.andGreaterThanOrEqualTo("startTime",simpleDateFormat.format(dateMenu));// 开始时间大于等于当前时间段
            criteria.andLessThan("endTime",simpleDateFormat.format(DateUtil.addDateHour(dateMenu,2)));//秒杀商品结束时间段<当前时间段+2
            //排除之前已经加载到redis缓存中的商品数据        获取到所有filed
            Set keys = redisTemplate.boundHashOps(SECKILL_GOODS_KEY + date).keys(); //key filed value
            if (keys!=null&&keys.size()>0){
                //拼接条件 查询id不在keys中的
                criteria.andNotIn("id",keys);
            }


            List<SeckillGoods> secKillGoodsList = seckillGoodsMapper.selectByExample(example);

            //添加到缓存中
           /* for (SeckillGoods seckillGoods : secKillGoodsList) {
                redisTemplate.boundHashOps(SECKILL_GOODS_KEY+ date).put(seckillGoods.getId(),seckillGoods);
            }*/
            for (SeckillGoods seckillGoods : secKillGoodsList) {
                redisTemplate.opsForHash().put(SECKILL_GOODS_KEY + date,seckillGoods.getId(),seckillGoods);
                //加载秒杀商品的库存
                redisTemplate.opsForValue().set(SECKILL_GOODS_STOCK_COUNT_KEY+seckillGoods.getId(),seckillGoods.getStockCount());
            }
        }
    }

}
