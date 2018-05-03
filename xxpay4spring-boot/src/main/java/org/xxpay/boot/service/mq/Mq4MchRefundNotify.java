package org.xxpay.boot.service.mq;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.xxpay.boot.service.BaseService;
import org.xxpay.common.util.MyLog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 管理后台手动发起退款通知
 * @author https://github.com/cbwleft
 * @date 2018年5月3日
 */
@Component
public class Mq4MchRefundNotify extends BaseService {
	
	@Autowired
	private IMqNotify mqNotify;

	@Autowired
    private RestTemplate restTemplate;

    protected static final MyLog _log = MyLog.getLog(Mq4MchRefundNotify.class);

    public void send(String msg) {
    	mqNotify.send(MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME, msg);
    }
    /**
     * 发送延迟消息
     * @param msg
     * @param delay
     */
    public void send(String msg, int delay) {
    	mqNotify.send(MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME, msg, delay);
    	
    }

    public void receive(String msg) {
        String logPrefix = "【商户退款通知】";
        _log.info("{}接收消息:msg={}", logPrefix, msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String respUrl = msgObj.getString("url");
        String orderId = msgObj.getString("orderId");
        int count = msgObj.getInteger("count");
        if(StringUtils.isEmpty(respUrl)) {
            _log.warn("{}商户通知URL为空,respUrl={}", logPrefix, respUrl);
            return;
        }
        String notifyResult = "";
        int cnt = count + 1;
        _log.info("{}notifyCount={}", logPrefix, cnt);
        try {
        	 URI uri = new URI(respUrl);
        	 notifyResult = restTemplate.postForObject(uri, null, String.class);
        }catch (Exception e) {
        	_log.error(e, "通知商户系统异常");
		}
        if("success".equalsIgnoreCase(notifyResult)){
            // 修改退款订单表
            try {
                int result = baseUpdateStatus4CompleteByRefund(orderId);
                _log.info("{}修改payOrderId={},退款订单状态为处理完成->{}", logPrefix, orderId, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error(e, "修改订单状态为处理完成异常");
            }
            // 修改通知
            try {
                int result = super.baseUpdateMchNotifySuccess(orderId, notifyResult, (byte) cnt);
                _log.info("{}订单退款修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, notifyResult, cnt, result == 1 ? "成功" : "失败");
            }catch (Exception e) {
                _log.error(e, "订单退款修改商户支付通知异常");
            }
            return ; // 通知成功结束
        }else {
            // 修改通知次数
            try {
                int result = super.baseUpdateMchNotifyFail(orderId, notifyResult, (byte) cnt);
                _log.info("{}订单退款修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, notifyResult, cnt, result == 1 ? "成功" : "失败");
            }catch (Exception e) {
                _log.error(e, "订单退款修改商户支付通知异常");
            }
            if (cnt > 5) {
                _log.info("{}订单退款通知次数notifyCount()>5,停止通知", respUrl, cnt);
                return ;
            }
            // 通知失败，延时再通知
            msgObj.put("count", cnt);
            this.send(msgObj.toJSONString(), cnt * 60 * 1000);
            _log.info("{}发送延时通知完成,通知次数:{},{}秒后执行通知", respUrl, cnt, cnt * 60);
        }
    }
    
}
