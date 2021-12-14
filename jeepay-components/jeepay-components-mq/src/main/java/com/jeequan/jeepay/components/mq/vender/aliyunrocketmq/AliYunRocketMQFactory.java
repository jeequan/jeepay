package com.jeequan.jeepay.components.mq.vender.aliyunrocketmq;

import com.aliyun.openservices.ons.api.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class AliYunRocketMQFactory {

    public static final String defaultTag = "Default";

    @Value("${aliyun-rocketmq.namesrvAddr:}")
    public String namesrvAddr;
    @Value("${aliyun-rocketmq.accessKey}")
    private String accessKey;
    @Value("${aliyun-rocketmq.secretKey}")
    private String secretKey;
    @Value("${aliyun-rocketmq.consumerId}")
    private String consumerId;
    @Value("${aliyun-rocketmq.producerId}")
    private String producerId;

    @Bean(name = "producerClient")
    public Producer producerClient() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ProducerId, producerId);
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        // 判断是否为空（生产环境走k8s集群公共配置，不获取本地配置文件的值）
        if (StringUtils.isNotEmpty(namesrvAddr)) {
            properties.put(PropertyKeyConst.NAMESRV_ADDR, namesrvAddr);
        }
        return ONSFactory.createProducer(properties);
    }

    @Bean(name = "consumerClient")
    public Consumer consumerClient() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ConsumerId, consumerId);
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        // 判断是否为空（生产环境走k8s集群公共配置，不获取本地配置文件的值）
        if (StringUtils.isNotEmpty(namesrvAddr)) {
            properties.put(PropertyKeyConst.NAMESRV_ADDR, namesrvAddr);
        }
        return ONSFactory.createConsumer(properties);
    }

    @Bean(name = "broadcastConsumerClient")
    public Consumer broadcastConsumerClient() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ConsumerId, consumerId);
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        // 广播订阅方式设置
        properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);
        // 判断是否为空（生产环境走k8s集群环境变量自动注入，不获取本地配置文件的值）
        if (StringUtils.isNotEmpty(namesrvAddr)) {
            properties.put(PropertyKeyConst.NAMESRV_ADDR, namesrvAddr);
        }
        return ONSFactory.createConsumer(properties);
    }

}
