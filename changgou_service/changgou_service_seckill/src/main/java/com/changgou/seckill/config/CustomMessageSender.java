package com.changgou.seckill.config;

import com.alibaba.fastjson.JSON;
import org.aspectj.weaver.ast.Or;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CustomMessageSender implements RabbitTemplate.ConfirmCallback {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private static final  String MESSAGE_CONFIRM="message_confirm_";

    /**
     * Confirmation callback.
     *
     * @param correlationData correlation data for the callback.回调的相关数据
     * @param ack             true for ack, false for nack
     * @param cause           An optional cause, for nack, when available, otherwise null.
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        //接收通知并进行判断
        if (ack){
            //成功通知删除redis中的消息数据  包括信息和原数据
            redisTemplate.delete(correlationData.getId());
            redisTemplate.delete(MESSAGE_CONFIRM+correlationData.getId());

        }else {
            //失败的通知 从redis中获取消息内容 重新发送  entries获取所有数据
            Map <String,String> map = redisTemplate.opsForHash().entries(MESSAGE_CONFIRM + correlationData.getId());
            String exchange = map.get("exchange");
            String routingKey = map.get("routingKey");
            String message = map.get("message");
            //重新发送
            rabbitTemplate.convertAndSend(exchange,routingKey, JSON.toJSONString(message));
        }
    }

    //定义构造方法

    public CustomMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate =rabbitTemplate;
        //设置当前的回调函数
        rabbitTemplate.setConfirmCallback(this);

    }

    //自定义发送方法
    public void sendMessage(String exchange,String routingKey,String message){
        //设置消息的唯一标识并存入redis中
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //存入redis中
        redisTemplate.opsForValue().set(correlationData.getId(),message);
        //将本次发送的消息的相关的原数据保存到redis中
        Map<String,String> map = new HashMap<>();
        map.put("exchange",exchange);
        map.put("routingKey",routingKey);
        map.put("message",message);
        redisTemplate.opsForHash().putAll(MESSAGE_CONFIRM+correlationData.getId(),map);
        //携带着本次消息的唯一标识,进行数据发送
        rabbitTemplate.convertAndSend(exchange,routingKey,message,correlationData);


    }
}
