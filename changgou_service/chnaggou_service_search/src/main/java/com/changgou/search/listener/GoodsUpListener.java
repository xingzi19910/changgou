package com.changgou.search.listener;

import com.changgou.search.config.RabbitMQConfig;
import com.changgou.search.service.SkuSearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//监听类 监听rabbitmq消息队列
@Component
public class GoodsUpListener {
    @Autowired
    private SkuSearchService skuSearchService;
    @RabbitListener(queues = RabbitMQConfig.SEARCH_ADD_QUEUE)
    public void receiveMessage(String spuId){
        System.out.println("接收到的消息为: "+spuId);
            skuSearchService.goodsDown(spuId);
    }
}
