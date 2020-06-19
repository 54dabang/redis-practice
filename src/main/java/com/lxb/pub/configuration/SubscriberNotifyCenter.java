package com.lxb.pub.configuration;

import com.lxb.pub.service.AbstractSubscribeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class SubscriberNotifyCenter {
    private List<AbstractSubscribeService> subscribeServiceList;

    private RedisMessageListenerContainer redisMessageListenerContainer;

    public SubscriberNotifyCenter(List<AbstractSubscribeService> subscribeServiceList, RedisMessageListenerContainer redisMessageListenerContainer) {
        this.subscribeServiceList = subscribeServiceList;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        if(!CollectionUtils.isEmpty(subscribeServiceList)){
            subscribeServiceList.stream().forEach(sub->redisMessageListenerContainer.addMessageListener(sub, new ChannelTopic(sub.getChannel())));
        }

    }

    public List<AbstractSubscribeService> getSubscribeServiceList() {
        return subscribeServiceList;
    }

    public void setSubscribeServiceList(List<AbstractSubscribeService> subscribeServiceList) {
        this.subscribeServiceList = subscribeServiceList;
    }

    public RedisMessageListenerContainer getRedisMessageListenerContainer() {
        return redisMessageListenerContainer;
    }

    public void setRedisMessageListenerContainer(RedisMessageListenerContainer redisMessageListenerContainer) {
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }
}
