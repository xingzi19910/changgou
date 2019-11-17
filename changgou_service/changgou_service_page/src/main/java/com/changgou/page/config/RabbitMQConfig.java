package com.changgou.page.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    //定义队列名称
    public static final String AD_UPDATE_QUEUE = "ad_update_queue";
    //交换机名称
    public  static final String GOODS_UP_EXCHANGE = "goods_up_exchange";
    public  static final String GOODS_DOWN_EXCHANGE = "goods_down_exchange";
    //定义队列名称
    public  static final String SEARCH_ADD_QUEUE = "search_add_queue";
    public  static final String SEARCH_DELETE_QUEUE = "search_delete_queue";
    public static final String PAGE_CREATE_QUEUE="page_create_queue";

    @Bean
    //声明队列
    public Queue queue() {
        return new Queue(AD_UPDATE_QUEUE);
    }
    //声明队列
    @Bean(SEARCH_ADD_QUEUE)
    public Queue search_add_queue() {

        return new Queue(SEARCH_ADD_QUEUE);
    }
    @Bean(PAGE_CREATE_QUEUE)
    public Queue page_create_queue() {

        return new Queue(PAGE_CREATE_QUEUE);
    }

    @Bean(SEARCH_DELETE_QUEUE)
    public Queue search_delete_queue(){
        return new Queue(SEARCH_DELETE_QUEUE);
    }

    //声明交换机
    @Bean(GOODS_DOWN_EXCHANGE)
    public Exchange goods_down_exchange(){
        return ExchangeBuilder.fanoutExchange(GOODS_DOWN_EXCHANGE).durable(true).build();
    }

    //声明交换机
    @Bean(GOODS_UP_EXCHANGE)
    public Exchange goods_up_exchange(){
        return ExchangeBuilder.fanoutExchange(GOODS_UP_EXCHANGE).durable(true).build();
    }
    //队列绑定交换机    @Qualifier   结合@Autowired一起使用用于根据名称进行依赖注入   noargs不指定其他参数
    @Bean
    public Binding AD_UPDATE_QUEUE_BINDING(@Qualifier (SEARCH_ADD_QUEUE) Queue queue,@Qualifier (GOODS_UP_EXCHANGE)Exchange exchange){
        //noargs不指定其他参数
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }
    @Bean
    public Binding PAGE_CREATE_QUEUE_BINDING(@Qualifier (PAGE_CREATE_QUEUE) Queue queue,@Qualifier (GOODS_UP_EXCHANGE)Exchange exchange){
        //noargs不指定其他参数
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

    //队列绑定交换机    @Qualifier   结合@Autowired一起使用用于根据名称进行依赖注入   noargs不指定其他参数
    @Bean
    public Binding DEL_UPDATE_QUEUE_BINDING(@Qualifier (SEARCH_DELETE_QUEUE) Queue queue,@Qualifier (GOODS_DOWN_EXCHANGE)Exchange exchange){
        //noargs不指定其他参数
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }
}
