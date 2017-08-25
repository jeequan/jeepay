package org.xxpay.service.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dal.dao.model.PayOrder;
import org.xxpay.service.channel.alipay.AlipayConfig;
import org.xxpay.service.service.PayChannelService;
import org.xxpay.service.service.PayOrderService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
	public void aliPayNotifyRes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String result =  doAliPayRes(request, response);
		if(result != null) {
			_log.info("alipay notify response: {}", result);
			response.setContentType("text/html");
			PrintWriter pw;
			try {
				pw = response.getWriter();
				pw.print(result);

			} catch (IOException e) {
				_log.error("Pay response write exception.", e);
			}
		}

	}

	public String doAliPayRes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String logPrefix = "【支付宝支付回调通知】";
		_log.info("====== 开始接收支付宝支付回调通知 ======");
		//获取支付宝POST过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
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
			return PayConstant.RETURN_ALIPAY_VALUE_FAIL;
		}
		Map<String, Object> payContext = new HashMap();
		PayOrder payOrder;
		payContext.put("parameters", params);

		if(!verifyAliPayParams(payContext)) {
			return PayConstant.RETURN_ALIPAY_VALUE_FAIL;
		}
		_log.info("{}验证请求数据及签名通过", logPrefix);
		String trade_status = params.get("trade_status");		// 交易状态
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
		doNotify(payOrder);
		_log.info("====== 完成接收支付宝支付回调通知 ======");
		return PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
	}
	
	/**
	 * 验证支付宝支付通知参数
	 * @return
	 */
	public boolean verifyAliPayParams(Map<String, Object> payContext) {
		Map<String,String> params = (Map<String,String>)payContext.get("parameters");
		String out_trade_no = params.get("out_trade_no");		// 商户订单号
		String total_amount = params.get("total_amount"); 		// 支付金额
		if (StringUtils.isEmpty(out_trade_no)) {
			_log.error("AliPay Notify parameter out_trade_no is empty. out_trade_no={}", out_trade_no);
			payContext.put("retMsg", "out_trade_no is empty");
			return false;
		}
		if (StringUtils.isEmpty(total_amount)) {
			_log.error("AliPay Notify parameter total_amount is empty. total_fee={}", total_amount);
			payContext.put("retMsg", "total_amount is empty");
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
		boolean verify_result = false;
		try {
			verify_result = AlipaySignature.rsaCheckV1(params, alipayConfig.init(payChannel.getParam()).getAlipay_public_key(), AlipayConfig.CHARSET, "RSA2");
		} catch (AlipayApiException e) {
			_log.error(e, "AlipaySignature.rsaCheckV1 error");
		}

		// 验证签名
		if (!verify_result) {
			errorMessage = "rsaCheckV1 failed.";
			_log.error("AliPay Notify parameter {}", errorMessage);
			payContext.put("retMsg", errorMessage);
			return false;
		}

		// 核对金额
		long aliPayAmt = new BigDecimal(total_amount).movePointRight(2).longValue();
		long dbPayAmt = payOrder.getAmount().longValue();
		if (dbPayAmt != aliPayAmt) {
			_log.error("db payOrder record payPrice not equals total_amount. total_amount={},payOrderId={}", total_amount, payOrderId);
			payContext.put("retMsg", "");
			return false;
		}
		payContext.put("payOrder", payOrder);
		return true;
	}

}
