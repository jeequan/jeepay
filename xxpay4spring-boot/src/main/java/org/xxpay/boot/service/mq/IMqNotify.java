package org.xxpay.boot.service.mq;

/**
 * MQ通知接口
 * @author https://github.com/cbwleft
 * @date 2018年5月3日
 */
public interface IMqNotify {

	/**
	 * 发送mq消息
	 * @param queueName
	 * @param msg
	 */
	void send(String queueName, String msg);

	/**
	 * 发送mq延迟消息
	 * @param queueName
	 * @param msg
	 * @param delay
	 */
	void send(String queueName, String msg, int delay);

}
