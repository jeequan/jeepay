package com.jeequan.jeepay.pay.mq.queue.service;

/**
 * RabbitMq
 * 通道订单查询
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
public abstract class MqChannelOrderQueryService {

    public abstract void send(String msg);

    public abstract void send(String msg, long delay);

}
