package com.changgou.business.listener;

import okhttp3.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdListener {
    //指定目标方法来作为消费消息的方法 通过注解参数指定所监听的队列或binding
    //接收消息
    @RabbitListener(queues = "ad_update_queue")
    public void receiveMessage(String message){
      //1.发起远程调用  调用nginx
        OkHttpClient  okHttpClient = new OkHttpClient();
        String url ="http://192.168.200.128/ad_update?position="+message;
        //3.创建request
        Request request = new Request.Builder().url(url).build();
        //2.发送请求 设置新的访问
        Call call = okHttpClient.newCall(request);
        //4.回调
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //请求成功
                System.out.println("请求成功:"+response.message());
            }
        });
    }
}
