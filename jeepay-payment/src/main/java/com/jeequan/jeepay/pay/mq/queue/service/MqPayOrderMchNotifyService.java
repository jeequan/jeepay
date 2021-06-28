package com.jeequan.jeepay.pay.mq.queue.service;

/**
 * RabbitMq
 * 商户订单回调
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
public abstract class MqPayOrderMchNotifyService {

    public abstract void send(String msg);

    public abstract void send(String msg, long delay);

}
