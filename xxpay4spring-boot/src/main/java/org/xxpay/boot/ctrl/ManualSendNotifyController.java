package org.xxpay.boot.ctrl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.boot.service.impl.MchInfoServiceImpl;
import org.xxpay.boot.service.mq.Mq4PayNotify;
import org.xxpay.boot.service.mq.Mq4RefundNotify;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.XXPayUtil;
import org.xxpay.dal.dao.model.MchNotify;

import com.alibaba.fastjson.JSON;

/**
 * @Description: 手动发送通知
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class ManualSendNotifyController {

	private static final MyLog _log = MyLog.getLog(ManualSendNotifyController.class);

	@Autowired
	private Mq4PayNotify mq4PayNotify;
	
	@Autowired
	private Mq4RefundNotify mq4RefundNotify;
	
	@Autowired
	private MchInfoServiceImpl mchInfoServiceImpl;

	/**
	 * 统一手动发送通知
	 * @param request
	 * @param orderId
	 * @return
	 */
	@RequestMapping("/manual/send/notify")
	@ResponseBody
	public String manualSendNotify(String orderId, Long count){
		_log.info("====== 开始手动发送通知 ======");
		MchNotify  mchNotify  = mchInfoServiceImpl.baseSelectMchNotify(orderId);
		if (mchNotify == null) {
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "商户通知不存在", null, null));
		}
		if(StringUtils.isBlank(mchNotify.getNotifyUrl())){
			 return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "商户通知NotiryUrl不能为空", null, null));
		}
		Map  notifyUrlMap = JSON.parseObject(mchNotify.getNotifyUrl());
		notifyUrlMap.put("count", (count-2) < 0 ? 0 : (count-2));
		if(PayConstant.MCH_NOTIFY_TYPE_PAY.equals(mchNotify.getOrderType())) {
			mq4PayNotify.send(notifyUrlMap.toString());
		}
		if(PayConstant.MCH_NOTIFY_TYPE_REFUND.equals(mchNotify.getOrderType())) {
			mq4RefundNotify.send(notifyUrlMap.toString());
		}
		_log.info("====== 完成手动发送通知 ======");
		return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "手动通知发送成功", null, null));
	}

}
