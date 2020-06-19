## 一  避免存在与IP绑定的情况，选举master节点。

借鉴：mams-script业务

相关wiki:https://my.oschina.net/roccn/blog/909252

 LeaderSelect

~~~java

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;

/**
 * Created by zhanghongjun on 2017/8/21.
 *  leader选举节点 188.59
 */
@Component
public class LeaderSelect {
    public static final long startTime=System.currentTimeMillis();
    private final Logger logger = LoggerFactory.getLogger(LeaderSelect.class);
    private static boolean isLeader=false;
//    private String masterPath="/leader";

    private CuratorFramework client;
    private LeaderLatch leaderLatch;

    @Value("${zk.connect}")
    String zkConnectString ;
    @PostConstruct
    public void init(){
//        Properties properties = PropertiesUtils.loadProperties("classpath*:*.properties");
//        String zkConnectString = properties.getProperty("zookeeper.serverlist", "10.11.54.36:2181");

        int sessionTimeout=30000;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkConnectString)
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs( sessionTimeout)
                .connectionTimeoutMs(30000)
                .namespace("script")// 定义命名空间  最终节点为/wechat/leader  需要先手动逐级创建该两层持久节点
                .build();
        client.start();

        try {
            leaderLatch = new LeaderLatch(client, "/leader", "client#" + InetAddress.getLocalHost().getHostName());
            leaderLatch.addListener(new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    logger.info("leader selected");
                    isLeader=true;
                }

                @Override
                public void notLeader() {
                    logger.info("leader not selected");
                    isLeader=false;
                }
            });
            leaderLatch.start();
        } catch (Exception e) {
            logger.error("leader select error:{}",e);
        }

    }
    @PreDestroy
    public void PreDestroy(){
        closeZk();
    }
    public void closeZk(){
        if(client!=null){
            CloseableUtils.closeQuietly(client);
        }
        if(leaderLatch!=null){
            CloseableUtils.closeQuietly(leaderLatch);
        }
        isLeader=false;
        logger.info("close zk client");
    }

    public static  boolean isLeader(){
        return isLeader;
    }
}

~~~

## 二 通知更新缓存、更新mq的队列的机制变更

由使用mq订阅的机制改为redis订阅。

相关wiki:https://github.com/nailcankucuk/redis-pub-sub

https://github.com/strictnerd/redis-sub-pub

2.1具体配置：

RedisConfig:

~~~java
 @Bean
    public SubscriberNotifyCenter subscriberNotifyCenter(List<AbstractSubscribeService> subscribeServiceList,RedisMessageListenerContainer redisMessageListenerContainer){
        return new SubscriberNotifyCenter(subscribeServiceList,redisMessageListenerContainer);

    }
~~~

注册中心，将所有监听的服务进行注册：

~~~java
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
~~~

消息发布器：

~~~java
@Service
public class Publisher implements MessagePublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ChannelTopic topic;

    @Autowired
    public Publisher(StringRedisTemplate stringRedisTemplate,
                     ChannelTopic topic) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(String message) {
        stringRedisTemplate.convertAndSend(topic.getTopic(), message);
    }
}

~~~

抽象订阅服务（所有订阅的服务均需要继承自此抽象类）

~~~java
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
~~~





## 三 分布式闭锁解决数据入库的问题

主要使用分布式锁工具：redisson

相关wiki:

jianshu.com/p/cde0700f0128

具体的配置





