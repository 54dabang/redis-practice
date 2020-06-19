package com.lxb.pub.controller;

import com.alibaba.fastjson.JSONObject;
import com.lxb.pub.service.impl.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
@Slf4j
@RestController
public class MessageController {
    private static final String COUNT_DOWN_LATCH = "count_down_latch";
    @Autowired
    private   Publisher redisMessagePublisher;
    @Autowired
    private RedissonClient redissonClient;
    @RequestMapping("/hello")
    public String hello(String message){
        JSONObject content = new JSONObject();
        content.put("content",message+ UUID.randomUUID());
        redisMessagePublisher.publish(content.toJSONString());
        return "成功";
    }
    @RequestMapping("/lock")
    public  String setCountDownLock() throws InterruptedException {
        RCountDownLatch rCountDownLatch= redissonClient.getCountDownLatch(COUNT_DOWN_LATCH);
        rCountDownLatch.trySetCount(1);
        log.info("上锁了....."+COUNT_DOWN_LATCH);
        Thread.sleep(1000*20);
        rCountDownLatch.countDown();
        log.info("解锁了....."+COUNT_DOWN_LATCH);
        return "成功!";

    }

    /**
     * 当锁不被占有时，不会被阻塞，直接执行 
     * @return
     * @throws InterruptedException
     */
    @RequestMapping("/wait1")
    public String wait1() throws InterruptedException {
        RCountDownLatch rCountDownLatch= redissonClient.getCountDownLatch(COUNT_DOWN_LATCH);
        rCountDownLatch.await();
        log.info("执行了。。。。。wait1");
        return "成功!";
    }
    @RequestMapping("/wait2")
    public String wait2() throws InterruptedException {
        RCountDownLatch rCountDownLatch= redissonClient.getCountDownLatch(COUNT_DOWN_LATCH);
        rCountDownLatch.await();
        log.info("执行了。。。。。wait2");
        return "成功!";
    }
}
