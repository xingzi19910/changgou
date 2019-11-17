package com.itheima.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.itheima.canal.config.RabbitMQConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//声明当前类是canal的监听类
@CanalEventListener
public class BusinessListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     *
     * @param entryType  当前操作数据库的类型
     * @param rowData  当前操作数据库的数据
     */
    @ListenPoint(schema = "changgou_business",table = "tb_ad")
    public void adUpdate(CanalEntry.EntryType entryType,CanalEntry.RowData rowData){

        System.out.println("广告数据发生变化");
        //获取改变之前的数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        System.out.println(beforeColumnsList);
        for (CanalEntry.Column column : beforeColumnsList) {
            //System.out.println("改变前的数据:"+column.getName()+": :"+column.getValue());
            //发生改变 发送消息到MQ
            if ("position".equals(column.getName())){
                System.out.println("发送最新的数据到MQ: " +column.getValue());
                //发送消息  参数1 交换机 参数2 routing key 路由  参数3发送的值
                rabbitTemplate.convertAndSend("", RabbitMQConfig.AD_UPDATE_QUEUE,column.getValue());
            }
        }

      /*  //获取改变后的数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            System.out.println("改变后的数据:"+column.getName()+": :"+column.getValue());
        }*/

    }
}
