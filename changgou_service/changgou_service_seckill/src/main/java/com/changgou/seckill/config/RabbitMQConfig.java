package com.changgou.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static  final  String SECKILL_ORDER_KEY="seckill_order_key";

    @Bean
    public Queue secKill_order_queue(){
        return new Queue(SECKILL_ORDER_KEY);
    }
}
