package org.xxpay.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dal.dao.model.PayOrder;
import org.xxpay.service.channel.alipay.config.AlipayConfig;
import org.xxpay.service.channel.alipay.util.AlipayNotify;
import org.xxpay.service.channel.tencent.common.Util;
import org.xxpay.service.channel.tencent.protocol.notify_protocol.NotifyUnifiedOrderResData;
import org.xxpay.service.service.PayChannelService;
import org.xxpay.service.service.PayOrderService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description: 接收处理支付宝通知
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class Notify4AliPayController extends Notify4BasePay {

	private static final MyLog _log = MyLog.getLog(Notify4AliPayController.class);

	@Autowired
	private PayOrderService payOrderService;

	@Autowired
	private PayChannelService payChannelService;

	@Autowired
	private AlipayConfig alipayConfig;

	/**
	 * 支付宝移动支付后台通知响应
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
     */
	@RequestMapping(value = "/pay/aliPayNotifyRes.htm")
	@ResponseBody
	public String aliPayNotifyRes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return doAliPayRes(request, response);
	}

	public String doAliPayRes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String logPrefix = "【支付宝支付回调通知】";
		_log.info("====== 开始接收支付宝支付回调通知 ======");
		//获取支付宝POST过来反馈信息
		Map<String,String> params = new HashMap<>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
		}
		_log.info("{}通知请求数据:reqStr={}", logPrefix, params);
		if(params.isEmpty()) {
			_log.error("{}请求参数为空", logPrefix);
			return makeRetData(PayConstant.RETURN_VALUE_FAIL, "请求参数为空");
		}

		Map<String, Object> payContext = new HashMap();
		PayOrder payOrder;
		payContext.put("parameters", params);

		if(!verifyAliPayParams(payContext)) {
			return makeRetData(PayConstant.RETURN_VALUE_FAIL, (String) payContext.get("retMsg"));
		}
		_log.info("{}验证请求数据及签名通过", logPrefix);

		String out_trade_no = params.get("out_trade_no");		// 商户订单号
		String trade_no = params.get("trade_no");				// 支付宝订单号
		String trade_status = params.get("trade_status");		// 交易状态
		String buyer_email = params.get("buyer_email");			// 买家支付宝账号

		// 支付状态成功或者完成
		if (trade_status.equals(PayConstant.AlipayConstant.TRADE_STATUS_SUCCESS) ||
				trade_status.equals(PayConstant.AlipayConstant.TRADE_STATUS_FINISHED)) {
			int updatePayOrderRows;
			payOrder = (PayOrder)payContext.get("payOrder");
			byte payStatus = payOrder.getStatus(); // 0：订单生成，1：支付中，-1：支付失败，2：支付成功，3：业务处理完成，-2：订单过期
			if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
				updatePayOrderRows = payOrderService.updateStatus4Success(payOrder.getPayOrderId());
				if (updatePayOrderRows != 1) {
					_log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
					_log.info("{}响应给支付宝结果：{}", logPrefix, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
					return PayConstant.RETURN_ALIPAY_VALUE_FAIL;
				}
				_log.info("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
				payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
			}
		}else{
			// 其他状态
			_log.info("{}支付状态trade_status={},不做业务处理", logPrefix, trade_status);
			_log.info("{}响应给支付宝结果：{}", logPrefix, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
			return PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
		}
		doNotify(payOrder, response, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
		_log.info("====== 完成接收支付宝支付回调通知 ======");
		return null;
	}
	
	/**
	 * 验证支付宝支付通知参数
	 * @return
	 */
	public boolean verifyAliPayParams(Map<String, Object> payContext) {
		Map<String,String> params = (Map<String,String>)payContext.get("parameters");
		String out_trade_no = params.get("out_trade_no");		// 商户订单号
		String seller_email = params.get("seller_email"); 		// 卖家支付宝账号
		String total_fee = params.get("total_fee"); 			// 支付金额
		String buyer_email = params.get("buyer_email");			// 买家支付宝账号
		String sign = params.get("sign"); 						// 签名
		if (StringUtils.isEmpty(out_trade_no)) {
			_log.error("AliPay Notify parameter out_trade_no is empty. out_trade_no={}", out_trade_no);
			payContext.put("retMsg", "out_trade_no is empty");
			return false;
		}
		if (StringUtils.isEmpty(seller_email)) {
			_log.error("AliPay Notify parameter seller_email is empty. seller_email={}", seller_email);
			payContext.put("retMsg", "seller_email is empty");
			return false;
		}
		if (StringUtils.isEmpty(total_fee)) {
			_log.error("AliPay Notify parameter total_fee is empty. total_fee={}", total_fee);
			payContext.put("retMsg", "total_fee is empty");
			return false;
		}
		if (StringUtils.isEmpty(buyer_email)) {
			_log.error("AliPay Notify parameter buyer_email is empty. buyer_email={}", buyer_email);
			payContext.put("retMsg", "buyer_email is empty");
			return false;
		}
		if (StringUtils.isEmpty(sign)) {
			_log.error("AliPay Notify parameter sign is empty. sign={}", sign);
			payContext.put("retMsg", "sign is empty");
			return false;
		}

		String errorMessage;

		// 查询payOrder记录
		String payOrderId = out_trade_no;
		PayOrder payOrder = payOrderService.selectPayOrder(payOrderId);
		if (payOrder == null) {
			_log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
			payContext.put("retMsg", "Can't found payOrder");
			return false;
		}

		// 查询payChannel记录
		String mchId = payOrder.getMchId();
		String channelId = payOrder.getChannelId();
		PayChannel payChannel = payChannelService.selectPayChannel(channelId, mchId);
		if(payChannel == null) {
			_log.error("Can't found payChannel form db. mchId={} channelId={}, ", payOrderId, mchId, channelId);
			payContext.put("retMsg", "Can't found payChannel");
			return false;
		}

		// 验证签名
		if (!AlipayNotify.verify(alipayConfig.init(payChannel.getParam()), params)) {
			errorMessage = "Verify VV pay sign failed.";
			_log.error("AliPay Notify parameter {}", errorMessage);
			payContext.put("retMsg", errorMessage);
			return false;
		}

		// 核对金额
		long aliPayAmt = new BigDecimal(total_fee).movePointRight(2).longValue();
		long dbPayAmt = payOrder.getAmount().longValue();
		if (dbPayAmt != aliPayAmt) {
			_log.error("db payOrder record payPrice not equals total_fee. total_fee={},payOrderId={}", total_fee, payOrderId);
			payContext.put("retMsg", "");
			return false;
		}
		payContext.put("payOrder", payOrder);
		return true;
	}

	String makeRetData(String retCode, String retMsg) {
		NotifyUnifiedOrderResData data = new NotifyUnifiedOrderResData();
		data.setReturn_code(retCode);
		data.setReturn_msg(retMsg);
		return Util.objectToXML(data);
	}

	String makeRetData(String retCode) {
		return makeRetData(retCode, "");
	}

}
