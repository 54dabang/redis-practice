package com.lxb.pub.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lxb.pub.configuration.RedisConfig;
import com.lxb.pub.service.AbstractSubscribeService;
import org.springframework.stereotype.Component;

@Component
public class SubscriberB extends AbstractSubscribeService {
    @Override
    public String getChannel() {
        return RedisConfig.CHANNEL_NAME;
    }

    @Override
    public boolean doAction(JSONObject messageContent) {
        System.out.println("我收到了yeeye："+messageContent);
        return true;
    }
}
