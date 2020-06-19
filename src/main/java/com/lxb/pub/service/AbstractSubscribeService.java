package com.lxb.pub.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
@Slf4j
public abstract class AbstractSubscribeService implements MessageListener {
    public abstract String getChannel();//获取订阅的频道

    public abstract boolean doAction(JSONObject messageContent);//执行对应的任务

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = getChannel();
        try {
            log.info("[AbstractSubscribeService][action:onMessage][step=start][class={}][channel:{}][message={}]",getClass().getSimpleName(),channel,message);
            boolean result = doAction(JSONObject.parseObject(message.toString()));
            log.info("[AbstractSubscribeService][action:onMessage][step=end][class={}][channel:{}][message={}][result={}]",getClass().getSimpleName(),channel,message,result);
        } catch (Exception e) {
            log.error("[AbstractSubscribeService][action:onMessage][step=error][class={}][channel:{}][message={}]",getClass().getSimpleName(),channel,message,e);
        }
    }
}
