package org.xxpay.dubbo.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.PayDigestUtil;
import org.xxpay.common.util.XXPayUtil;
import org.xxpay.dal.dao.model.MchInfo;
import org.xxpay.dal.dao.model.MchNotify;
import org.xxpay.dal.dao.model.TransOrder;
import org.xxpay.dubbo.service.mq.Mq4MchTransNotify;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 商户转账通知处理基类
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-11-01
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Component
public class BaseNotify4MchTrans extends BaseService4TransOrder {

	private static final MyLog _log = MyLog.getLog(BaseNotify4MchTrans.class);

	@Autowired
	private Mq4MchTransNotify mq4MchTransNotify;

	/**
	 * 创建响应URL
	 * @param transOrder
	 * @param backType 1：前台页面；2：后台接口
	 * @return
	 */
	public String createNotifyUrl(TransOrder transOrder, String backType) {
		String mchId = transOrder.getMchId();
		MchInfo mchInfo = super.baseSelectMchInfo(mchId);
		String resKey = mchInfo.getResKey();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("transOrderId", ObjectUtils.defaultIfNull(transOrder.getTransOrderId(), ""));           	// 转账订单号
		paramMap.put("mchId", ObjectUtils.defaultIfNull(transOrder.getMchId(), ""));                      	 	// 商户ID
		paramMap.put("mchOrderNo", ObjectUtils.defaultIfNull(transOrder.getMchTransNo(), ""));       		 	// 商户订单号
		paramMap.put("channelId", ObjectUtils.defaultIfNull(transOrder.getChannelId(), ""));              		// 渠道ID
		paramMap.put("amount", ObjectUtils.defaultIfNull(transOrder.getAmount(), ""));                      	// 支付金额
		paramMap.put("currency", ObjectUtils.defaultIfNull(transOrder.getCurrency(), ""));                 		// 货币类型
		paramMap.put("status", ObjectUtils.defaultIfNull(transOrder.getStatus(), ""));               			// 转账状态
		paramMap.put("result", ObjectUtils.defaultIfNull(transOrder.getResult(), ""));               			// 转账结果
		paramMap.put("clientIp", ObjectUtils.defaultIfNull(transOrder.getClientIp(), ""));   					// 客户端IP
		paramMap.put("device", ObjectUtils.defaultIfNull(transOrder.getDevice(), ""));               			// 设备
		paramMap.put("channelOrderNo", ObjectUtils.defaultIfNull(transOrder.getChannelOrderNo(), "")); 			// 渠道订单号
		paramMap.put("param1", ObjectUtils.defaultIfNull(transOrder.getParam1(), ""));               		   	// 扩展参数1
		paramMap.put("param2", ObjectUtils.defaultIfNull(transOrder.getParam2(), ""));               		   	// 扩展参数2
		paramMap.put("transSuccTime", ObjectUtils.defaultIfNull(transOrder.getTransSuccTime(), ""));			// 转账成功时间
		paramMap.put("backType", backType==null ? "" : backType);
		// 先对原文签名
		String reqSign = PayDigestUtil.getSign(paramMap, resKey);
		paramMap.put("sign", reqSign);   // 签名
		// 签名后再对有中文参数编码
		try {
			paramMap.put("device", URLEncoder.encode(ObjectUtils.defaultIfNull(transOrder.getDevice(), ""), PayConstant.RESP_UTF8));
			paramMap.put("param1", URLEncoder.encode(ObjectUtils.defaultIfNull(transOrder.getParam1(), ""), PayConstant.RESP_UTF8));
			paramMap.put("param2", URLEncoder.encode(ObjectUtils.defaultIfNull(transOrder.getParam2(), ""), PayConstant.RESP_UTF8));
		}catch (UnsupportedEncodingException e) {
			_log.error("URL Encode exception.", e);
			return null;
		}
		String param = XXPayUtil.genUrlParams(paramMap);
		StringBuffer sb = new StringBuffer();
		sb.append(transOrder.getNotifyUrl()).append("?").append(param);
		return sb.toString();
	}

	/**
	 * 处理商户转账后台服务器通知
	 */
	public void doNotify(TransOrder transOrder, boolean isFirst) {
		_log.info(">>>>>> TRANS开始回调通知业务系统 <<<<<<");
		// 发起后台通知业务系统
		JSONObject object = createNotifyInfo(transOrder, isFirst);
		try {
			mq4MchTransNotify.send(object.toJSONString());
		} catch (Exception e) {
			_log.error(e, "transOrderId=%s,sendMessage error.", ObjectUtils.defaultIfNull(transOrder.getTransOrderId(), ""));
		}
		_log.info(">>>>>> TRANS回调通知业务系统完成 <<<<<<");
	}

	public JSONObject createNotifyInfo(TransOrder transOrder, boolean isFirst) {
		String url = createNotifyUrl(transOrder, "2");
		if(isFirst) {
			int result = baseInsertMchNotify(transOrder.getTransOrderId(), transOrder.getMchId(), transOrder.getMchTransNo(), PayConstant.MCH_NOTIFY_TYPE_TRANS, url);
			_log.info("增加商户通知记录,orderId={},result:{}", transOrder.getTransOrderId(), result);
		}
		int count = 0;
		if(!isFirst) {
			MchNotify mchNotify = baseSelectMchNotify(transOrder.getTransOrderId());
			if(mchNotify != null) count = mchNotify.getNotifyCount();
		}
		JSONObject object = new JSONObject();
		object.put("method", "GET");
		object.put("url", url);
		object.put("orderId", transOrder.getTransOrderId());
		object.put("count", count);
		object.put("createTime", System.currentTimeMillis());
		return object;
	}

}
