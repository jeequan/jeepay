package org.xxpay.boot.service.mq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.boot.service.BaseService;
import org.xxpay.boot.service.IPayChannel4AliService;
import org.xxpay.boot.service.IPayChannel4WxService;
import org.xxpay.boot.service.Notify4BaseRefund;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.RpcUtil;
import org.xxpay.common.util.StrUtil;
import org.xxpay.dal.dao.model.RefundOrder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description: MQ通知 调用第三方退款业务处理  
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Component
public class Mq4RefundNotify extends BaseService {
	
	@Autowired
	private IMqNotify mqNotify;

	@Autowired
	private IPayChannel4WxService payChannel4WxService;
    
    @Autowired
    private IPayChannel4AliService payChannel4AliService;
    
    @Autowired
    private Notify4BaseRefund notify4BaseRefund;

    protected static final MyLog _log = MyLog.getLog(Mq4RefundNotify.class);

    public void send(String msg) {
    	mqNotify.send(MqConfig.REFUND_NOTIFY_QUEUE_NAME, msg);
    }

    /**
     * 发送延迟消息
     * @param msg
     * @param delay
     */
    public void send(String msg, int delay) {
    	mqNotify.send(MqConfig.REFUND_NOTIFY_QUEUE_NAME, msg, delay);
    }

    public void receive(String msg) {
        _log.info("处理退款任务.msg={}", msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String refundOrderId = msgObj.getString("refundOrderId");
        String channelName = msgObj.getString("channelName");
        RefundOrder refundOrder = baseSelectRefundOrder(refundOrderId);
        if(refundOrder == null) {
            _log.warn("查询退款订单为空,不能退款.refundOrderId={}", refundOrderId);
            return;
        }
        if(refundOrder.getStatus() != PayConstant.REFUND_STATUS_INIT) {
            _log.warn("退款状态不是初始({})或失败({}),不能退款.refundOrderId={}", PayConstant.REFUND_STATUS_INIT, PayConstant.REFUND_STATUS_FAIL, refundOrderId);
            return;
        }
        int result = this.baseUpdateStatus4IngByRefund(refundOrderId, "");
        if(result != 1) {
            _log.warn("更改退款为退款中({})失败,不能退款.refundOrderId={}", PayConstant.REFUND_STATUS_REFUNDING, refundOrderId);
            return;
        }
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map resultMap;
        if(PayConstant.CHANNEL_NAME_WX.equalsIgnoreCase(channelName)) {
            resultMap = payChannel4WxService.doWxRefundReq(jsonParam);
        }else if(PayConstant.CHANNEL_NAME_ALIPAY.equalsIgnoreCase(channelName)) {
            resultMap = payChannel4AliService.doAliRefundReq(jsonParam);
        }else {
            _log.warn("不支持的退款渠道,停止退款处理.refundOrderId={},channelName={}", refundOrderId, channelName);
            return;
        }
        if(!RpcUtil.isSuccess(resultMap)) {
            _log.warn("发起退款返回异常,停止退款处理.refundOrderId={}", refundOrderId);
            return;
        }
        Map bizResult = (Map) resultMap.get("bizResult");
        Boolean isSuccess = false;
        if(bizResult.get("isSuccess") != null) isSuccess = Boolean.parseBoolean(bizResult.get("isSuccess").toString());
        if(isSuccess) {
            // 更新退款状态为成功
            String channelOrderNo = StrUtil.toString(bizResult.get("channelOrderNo"));
            result = baseUpdateStatus4SuccessByRefund(refundOrderId, channelOrderNo);
            _log.info("更新退款订单状态为成功({}),refundOrderId={},返回结果:{}", PayConstant.REFUND_STATUS_SUCCESS, refundOrderId, result);
            // 发送商户通知
            notify4BaseRefund.doNotify(refundOrder, true);
        }else {
            // 更新退款状态为失败
            String channelErrCode = StrUtil.toString(bizResult.get("channelErrCode"));
            String channelErrMsg = StrUtil.toString(bizResult.get("channelErrMsg"));
            result = baseUpdateStatus4FailByRefund(refundOrderId, channelErrCode, channelErrMsg);
            _log.info("更新退款订单状态为失败({}),refundOrderId={},返回结果:{}", PayConstant.REFUND_STATUS_FAIL, refundOrderId, result);
            // 发送商户通知
            notify4BaseRefund.doNotify(refundOrder, true);
        }

    }
}
