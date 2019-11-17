package com.changgou.consume.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.consume.config.RabbitMQConfig;
import com.changgou.consume.service.SecKillOrderService;
import com.changgou.seckill.pojo.SeckillOrder;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConsumeListener {
    @Autowired
    private SecKillOrderService secKillOrderService;
    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_KEY)
    public void receiveSecKillOrderMessage(Message message, Channel channel){
        //设置预抓取总数
        try {
            channel.basicQos(300);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //1.转换消息格式
        SeckillOrder seckillOrder = JSON.parseObject(message.getBody(), SeckillOrder.class);
        //2.基于业务层完成同步mysql的操作
        int i = secKillOrderService.handleCreateOrder(seckillOrder);
        if (i>0){
            //同步数据成功 向服务器返回成功通知
            try {//参数1 消息在消息队列中的唯一标识  参数2 是否开启批处理
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            //同步mysql失败 向消息服务器返回失败通知
            try {
                //参数1 消息在消息队列中的唯一标识 参数2 boolean true所有消费者都会拒绝这个消息，false代表只有当前消费者拒绝
                //参数3 boolean true当前消息会进入到死信队列，false重新回到原有队列中，默认回到头部
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException e) {


            }
        }

    }
}
