package org.xxpay.boot.ctrl;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.boot.service.INotifyPayService;
import org.xxpay.common.util.MyLog;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Description: 接收处理微信通知
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class Notify4WxPayController {

	private static final MyLog _log = MyLog.getLog(Notify4WxPayController.class);

	@Autowired
	private INotifyPayService notifyPayService;

	/**
	 * 微信支付(统一下单接口)后台通知响应
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
     */
	@RequestMapping("/notify/pay/wxPayNotifyRes.htm")
	@ResponseBody
	public String wxPayNotifyRes(HttpServletRequest request) throws ServletException, IOException {
		_log.info("====== 开始接收微信支付回调通知 ======");
		String notifyRes = doWxPayRes(request);
		_log.info("响应给微信:{}", notifyRes);
		_log.info("====== 完成接收微信支付回调通知 ======");
		return notifyRes;
	}

	public String doWxPayRes(HttpServletRequest request) throws ServletException, IOException {
		String logPrefix = "【微信支付回调通知】";
		String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
		_log.info("{}通知请求数据:reqStr={}", logPrefix, xmlResult);
		return notifyPayService.handleWxPayNotify(xmlResult);
	}

}
