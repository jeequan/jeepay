package org.xxpay.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dal.dao.model.PayOrder;
import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.common.Signature;
import org.xxpay.service.channel.tencent.common.Util;
import org.xxpay.service.channel.tencent.protocol.notify_protocol.NotifyUnifiedOrderReqData;
import org.xxpay.service.channel.tencent.protocol.notify_protocol.NotifyUnifiedOrderResData;
import org.xxpay.service.service.PayChannelService;
import org.xxpay.service.service.PayOrderService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 接收处理微信通知
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class Notify4WxPayController extends Notify4BasePay {

	private static final MyLog _log = MyLog.getLog(Notify4WxPayController.class);

	@Autowired
	private PayOrderService payOrderService;

	@Autowired
	private PayChannelService payChannelService;

	@Autowired
	private Configure configure;

	/**
	 * 微信支付(统一下单接口)后台通知响应
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
     */
	@RequestMapping("/pay/wxPayNotifyRes.htm")
	@ResponseBody
	public String wxPayNotifyRes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return doWxPayRes(request, response);
	}

	public String doWxPayRes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String logPrefix = "【微信支付回调通知】";
		_log.info("====== 开始接收微信支付回调通知 ======");
		String reqStr = Util.inputStreamToString(request.getInputStream());
		_log.info("{}通知请求数据:reqStr={}", logPrefix, reqStr);
		if(reqStr == null || "".equals(reqStr.trim())) {
			_log.error("{}请求参数为空", logPrefix);
			return makeRetData(PayConstant.RETURN_VALUE_FAIL, "请求参数解析错误");
		}
		NotifyUnifiedOrderReqData reqData = (NotifyUnifiedOrderReqData) Util.getObjectFromXML(reqStr, NotifyUnifiedOrderReqData.class);
		if (reqData == null || reqData.getReturn_code() == null) {
			_log.error("{}请求数据解析数据异常或return_code为空", logPrefix);
			return makeRetData(PayConstant.RETURN_VALUE_FAIL, "请求参数解析错误");
		}
		Map<String, Object> payContext = new HashMap();
		PayOrder payOrder = null;
		payContext.put("parameters", reqData);
		payContext.put("reqStr", reqStr);
		if (PayConstant.RETURN_VALUE_FAIL.equals(reqData.getReturn_code())) {
			_log.error("{}请求数据return_code=FAIL", logPrefix);
			return makeRetData(PayConstant.RETURN_VALUE_SUCCESS);
		} else {
			_log.info("{}请求数据return_code=SUCCESS,开始验证请求数据", logPrefix);
			//--------------------------------------------------------------------
			//收到通知数据的时候得先验证一下数据有没有被第三方篡改，确保安全
			//--------------------------------------------------------------------
			if(!verifyWxPayParams(payContext)) {
				return makeRetData(PayConstant.RETURN_VALUE_FAIL, (String) payContext.get("retMsg"));
			}
			_log.info("{}验证请求数据及签名通过", logPrefix);
			//获取错误码
			String errorCode = reqData.getErr_code();
			//获取错误描述
			String errorCodeDes = reqData.getErr_code_des();
			if (reqData.getResult_code().equals(PayConstant.RETURN_VALUE_SUCCESS)) {
				int updatePayOrderRows;
				payOrder = (PayOrder) payContext.get("payOrder");
				byte payStatus = payOrder.getStatus(); // 0：订单生成，1：支付中，-1：支付失败，2：支付成功，3：业务处理完成，-2：订单过期
				if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
					updatePayOrderRows = payOrderService.updateStatus4Success(payOrder.getPayOrderId());
					if (updatePayOrderRows != 1) {
						_log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
						return makeRetData(PayConstant.RETURN_VALUE_FAIL, "更新订单失败");
					}
					_log.error("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
					payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
				}
			}else{
				//出现业务错误
				_log.info("{}请求数据result_code=FAIL", logPrefix);
				_log.info("err_code:", errorCode);
				_log.info("err_code_des:", errorCodeDes);
				return makeRetData(PayConstant.RETURN_VALUE_SUCCESS);
			}
		}

		doNotify(payOrder, response, makeRetData(PayConstant.RETURN_VALUE_SUCCESS));

		return null;
	}
	
	/**
	 * 验证微信支付通知参数
	 * @return
	 */
	public boolean verifyWxPayParams(Map<String, Object> payContext) {
		NotifyUnifiedOrderReqData params = (NotifyUnifiedOrderReqData)payContext.get("parameters");

		String return_code = params.getReturn_code();     	// 返回码
		String appid = params.getAppid(); 					// 公众账号ID
		String mch_id = params.getMch_id(); 				// 商户号
		String sign = params.getSign();   					// 签名
		String result_code = params.getResult_code();  		// 业务结果
		String openid = params.getOpenid();  				// 用户标识
		String trade_type = params.getTrade_type();			// 交易类型
		String bank_type = params.getBank_type(); 			// 付款银行
		String total_fee = params.getTotal_fee();   		// 总金额
		String cash_fee = params.getCash_fee();				// 现金支付金额
		String transaction_id = params.getTransaction_id(); // 微信支付订单号
		String out_trade_no = params.getOut_trade_no();		// 商户系统订单号
		String time_end = params.getTime_end();				// 支付完成时间

		if (StringUtils.isEmpty(return_code)) {
			_log.error("WXPay parameter return_code is empty. return_code={}", result_code);
			payContext.put("retMsg", "return_code is empty");
			return false;
		}
		if (PayConstant.RETURN_VALUE_FAIL.equals(return_code)) {
			_log.error("WXPay return_code is FAIL. return_code={}", result_code);
			payContext.put("retMsg", "return_code is FAIL");
			return false;
		}
		if (StringUtils.isEmpty(appid)) {
			_log.error("WXPay parameter appid is empty. appid={}", appid);
			payContext.put("retMsg", "appid is empty");
			return false;
		}
		if (StringUtils.isEmpty(mch_id)) {
			_log.error("XWPay parameter mch_id is empty. mch_id={}", mch_id);
			payContext.put("retMsg", "mch_id is empty");
			return false;
		}
		if (StringUtils.isEmpty(sign)) {
			_log.error("XWPay parameter sign is empty. sign={}", sign);
			payContext.put("retMsg", "sign is empty");
			return false;
		}
		if (StringUtils.isEmpty(result_code)) {
			_log.error("XWPay parameter result_code is empty. result_code={}", result_code);
			payContext.put("retMsg", "result_code is empty");
			return false;
		}
		if (StringUtils.isEmpty(openid)) {
			_log.error("XWPay parameter openid is empty. openid={}", openid);
			payContext.put("retMsg", "openid is empty");
			return false;
		}
		if (StringUtils.isEmpty(trade_type)) {
			_log.error("XWPay parameter trade_type is empty. trade_type={}", trade_type);
			payContext.put("retMsg", "trade_type is empty");
			return false;
		}
		if (StringUtils.isEmpty(bank_type)) {
			_log.error("XWPay parameter bank_type is empty. bank_type={}", bank_type);
			payContext.put("retMsg", "bank_type is empty");
			return false;
		}
		if (StringUtils.isEmpty(total_fee)) {
			_log.error("XWPay parameter total_fee is empty. total_fee={}", total_fee);
			payContext.put("retMsg", "total_fee is empty");
			return false;
		}
		if (StringUtils.isEmpty(cash_fee)) {
			_log.error("XWPay parameter cash_fee is empty. cash_fee={}", cash_fee);
			payContext.put("retMsg", "cash_fee is empty");
			return false;
		}
		if (StringUtils.isEmpty(transaction_id)) {
			_log.error("XWPay parameter transaction_id is empty. transaction_id={}", transaction_id);
			payContext.put("retMsg", "transaction_id is empty");
			return false;
		}
		if (StringUtils.isEmpty(out_trade_no)) {
			_log.error("XWPay parameter out_trade_no is empty. out_trade_no={}", out_trade_no);
			payContext.put("retMsg", "out_trade_no is empty");
			return false;
		}
		if (StringUtils.isEmpty(time_end)) {
			_log.error("XWPay parameter time_end is empty. time_end={}", time_end);
			payContext.put("retMsg", "time_end is empty");
			return false;
		}

		// 查询payOrder记录
		String payOrderId = out_trade_no;
		PayOrder payOrder = payOrderService.selectPayOrder(payOrderId);
		if (payOrder==null) {
			_log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
			payContext.put("retMsg", "Can't found payOrder");
			return false;
		}

		// 查询payChannel记录
		String mchId = payOrder.getMchId();
		String channelId = payOrder.getChannelId();
		PayChannel payChannel = payChannelService.selectPayChannel(channelId, mchId);
		if(payChannel == null) {
			_log.error("Can't found payChannel form db. mchId={} channelId={}, ", payOrderId, mch_id, channelId);
			payContext.put("retMsg", "Can't found payChannel");
			return false;
		}

		// 验证微信支付结果签名
		try {
			if(!Signature.checkIsSignValidFromResponseString((String)payContext.get("reqStr"), configure.init(payChannel.getParam()).getKey())) {
				_log.error("WXPay parameter sign verified failed. sign={}", sign);
				payContext.put("retMsg", "sign is verified failed");
				return false;
            }
		} catch (ParserConfigurationException e) {
			_log.error(e, "");
		} catch (IOException e) {
			_log.error(e, "");
		} catch (SAXException e) {
			_log.error(e, "");
		}

		// 核对金额
		long wxPayAmt = new BigDecimal(total_fee).longValue();
		long dbPayAmt = payOrder.getAmount().longValue();
		if (dbPayAmt != wxPayAmt) {
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
