package com.itheima.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.itheima.canal.config.RabbitMQConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//数据监控服务
@CanalEventListener //监听类
public class SpuListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;
   @ListenPoint(schema = "changgou_goods",table = "tb_spu")
    public void goods(CanalEntry.EntryType entryType,CanalEntry.RowData rowData){
        //获取改变之前的数据并将这部分数据转换成map
       Map<String,String> oldData = new HashMap<>();
       List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
       for (CanalEntry.Column column : beforeColumnsList) {
           //将获取到的数据添加到 oldData中
          oldData.put(column.getName(),column.getValue());
       }

       //获取改变之后的数据并将数据转换成map
       Map<String,String> newData = new HashMap<>();
       List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
       for (CanalEntry.Column column : afterColumnsList) {
           newData.put(column.getName(),column.getValue());
       }

    //获取最新上架的商品  之前ismaketable 状态为0 之后状态为1
       if ("0".equals(oldData.get("is_marketable"))&&"1".equals(newData.get("is_marketable"))){
           //将商品的spuid发送到mq
           rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_UP_EXCHANGE,"",newData.get("id"));
       }
       //获取最新下架的商品 之前的 ismaketable状态为1 之后为0
       if ("1".equals(oldData.get("is_marketable"))&&"0".equals(newData.get("is_marketable"))){
           //将消息的spuid发送到mq
           rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_DOWN_EXCHANGE,"",newData.get("id"));
       }
       //获取最新审核商品
        if ("0".equals(oldData.get("status"))&&"1".equals(newData.get("status"))){
           //发送商品spuId
           rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_UP_EXCHANGE,"",newData.get("id"));
       }
   }
}
