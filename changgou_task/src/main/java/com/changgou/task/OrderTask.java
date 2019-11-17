package com.changgou.task;

import com.changgou.confing.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class OrderTask {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Scheduled(cron = "0 0 0 * * ?") //每隔一天执行一次
    public void autoTask(){
        System.out.println(new Date());
        rabbitTemplate.convertAndSend("", RabbitMQConfig.ORDER_TACK,"向消息队列发送的消息");
    }
}
