package com.changgou.user.listener;

import com.alibaba.fastjson.JSON;

import com.changgou.order.pojo.Task;
import com.changgou.user.config.RabbitMQConfig;
import com.changgou.user.service.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AddPointListener {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    //监听类
    @RabbitListener(queues = RabbitMQConfig.CG_BUYING_ADDPOINT)
    public void receiveMessage(String message) {
        System.out.println("用户服务接收到了任务消息");
        //1.转换消息
        Task task = JSON.parseObject(message, Task.class);
        if (task == null || StringUtils.isEmpty(task.getRequestBody())) {
            return;
        }
        //2.判断redis中当前的任务是否存在
        Object value = redisTemplate.boundValueOps(task.getId()).get();
        if (value != null) {
            return;
        }
        //3.更新用户积分
        int result = userService.updateUserPoints(task);
        if (result < 0) {
            return;
        }
        //4.向订单服务返回通知积分
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_BUYING_ADDPOINTUSER,RabbitMQConfig.CG_BUYING_FINISHADDPOINT_KEY,JSON.toJSONString(task));
        System.out.println("用户服务向完成添加积分队列发送了一条消息");

    }

}
