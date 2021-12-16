package com.jeequan.jeepay.components.mq.vender.aliyunrocketmq;

import com.aliyun.openservices.ons.api.*;
import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;
import com.jeequan.jeepay.components.mq.vender.IMQMsgReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractAliYunRocketMQReceiver implements IMQMsgReceiver, InitializingBean {

    @Autowired
    private AliYunRocketMQFactory aliYunRocketMQFactory;

    /**
     * 获取topic名称
     *
     * @return
     */
    public abstract String getMQName();

    /**
     * 获取业务名称
     *
     * @return
     */
    public abstract String getConsumerName();

    /**
     * 发送类型
     *
     * @return
     */
    public MQSendTypeEnum getMQType() {
        // QUEUE - 点对点 （只有1个消费者可消费。 ActiveMQ的queue模式 ）
        return MQSendTypeEnum.QUEUE;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Consumer consumerClient = MQSendTypeEnum.BROADCAST.equals(getMQType()) ?
                // 广播订阅模式
                aliYunRocketMQFactory.broadcastConsumerClient() :
                aliYunRocketMQFactory.consumerClient();
        consumerClient.subscribe(this.getMQName(), AliYunRocketMQFactory.defaultTag, new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                try {
                    receiveMsg(new String(message.getBody()));
                    log.debug("【{}】MQ消息消费成功topic:{}, messageId:{}", getConsumerName(), message.getTopic(), message.getMsgID());
                    return Action.CommitMessage;
                } catch (Exception e) {
                    log.error("【{}】MQ消息消费失败topic:{}, messageId:{}", getConsumerName(), message.getTopic(), message.getMsgID(), e);
                }
                return Action.ReconsumeLater;
            }
        });
        consumerClient.start();
        log.info("初始化[{}]消费者topic: {},tag: {}成功", getConsumerName(), this.getMQName(), AliYunRocketMQFactory.defaultTag);
    }

}
