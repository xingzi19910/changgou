package com.changgou.order.service.impl;

import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 10173
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;

    private static final String CART = "Cart";

    @Override
    public void addCart(String skuId, Integer num, String username) {

        //1.查询redis中相对应的商品信息

        OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART + username).get(skuId);

        //如果当前商品在redis中存在 则更新商品的数量与价钱
        if (orderItem != null) {

            orderItem.setNum(orderItem.getNum() + num);
            //如果商品数量小于1 删除该商品
            if(orderItem.getNum()<=0){
                //删除该商品
                redisTemplate.boundHashOps(CART+username).delete(skuId);
                return;
            }

            orderItem.setMoney(orderItem.getNum() * orderItem.getPrice());
            orderItem.setPayMoney(orderItem.getNum() + orderItem.getPrice());
        } else {
            //如果商品在redis中不存在 将商品添加到redis中

            Sku sku = skuFeign.findById(skuId).getData();
            Result<Spu> spuResult = spuFeign.findSpuById(sku.getSpuId());
            Spu spu = spuResult.getData();
            //将sku转换成orderItem
            orderItem = this.sku2OrderItem(sku, spu, num);
        }
        redisTemplate.boundHashOps(CART + username).put(skuId, orderItem);

    }

    //查询购物车列表
    @Override
    public Map list(String username) {
        Map map = new HashMap();
        List<OrderItem> orderItemList = redisTemplate.boundHashOps(CART + username).values();
        map.put("orderItemList", orderItemList);

        //商品数量与总价格
        Integer totalNum=0;
        Integer totalPrice=0;
        for (OrderItem orderItem : orderItemList) {
            totalNum+=orderItem.getNum();
            totalPrice+=orderItem.getMoney();
        }
        map.put("totalNum",totalNum);
        map.put("totalPrice",totalPrice);

        return map;
    }

    //将sku转换成orderItem
    private OrderItem sku2OrderItem(Sku sku, Spu spu, Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        orderItem.setPayMoney(num * orderItem.getPrice());
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight() * num);
        //分类ID设置
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        return orderItem;
    }
}
