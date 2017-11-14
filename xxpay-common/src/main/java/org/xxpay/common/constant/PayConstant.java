package org.xxpay.common.constant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 支付常量类
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
public class PayConstant {

	public final static String PAY_CHANNEL_WX_JSAPI = "WX_JSAPI"; 				// 微信公众号支付
	public final static String PAY_CHANNEL_WX_NATIVE = "WX_NATIVE";				// 微信原生扫码支付
	public final static String PAY_CHANNEL_WX_APP = "WX_APP";					// 微信APP支付
	public final static String PAY_CHANNEL_WX_MWEB = "WX_MWEB";					// 微信H5支付
	public final static String PAY_CHANNEL_IAP = "IAP";							// 苹果应用内支付
	public final static String PAY_CHANNEL_ALIPAY_MOBILE = "ALIPAY_MOBILE";		// 支付宝移动支付
	public final static String PAY_CHANNEL_ALIPAY_PC = "ALIPAY_PC";	    		// 支付宝PC支付
	public final static String PAY_CHANNEL_ALIPAY_WAP = "ALIPAY_WAP";	    	// 支付宝WAP支付
	public final static String PAY_CHANNEL_ALIPAY_QR = "ALIPAY_QR";	    	// 支付宝当面付之扫码支付

	public final static String TRANS_CHANNEL_WX_APP = "TRANS_WX_APP"; 			// 微信APP转账
	public final static String TRANS_CHANNEL_WX_JSAPI = "TRANS_WX_JSAPI"; 		// 微信公众号转账


	
	public final static byte PAY_STATUS_EXPIRED = -2; 	// 订单过期
	public final static byte PAY_STATUS_FAILED = -1; 	// 支付失败
	public final static byte PAY_STATUS_INIT = 0; 		// 初始态
	public final static byte PAY_STATUS_PAYING = 1; 	// 支付中
	public final static byte PAY_STATUS_SUCCESS = 2; 	// 支付成功
	public final static byte PAY_STATUS_COMPLETE = 3; 	// 业务完成

	public final static byte TRANS_STATUS_INIT = 0; 		// 初始态
	public final static byte TRANS_STATUS_TRANING = 1; 		// 转账中
	public final static byte TRANS_STATUS_SUCCESS = 2; 		// 成功
	public final static byte TRANS_STATUS_FAIL = 3; 		// 失败
	public final static byte TRANS_STATUS_COMPLETE = 4; 	// 业务完成


	public final static String RESP_UTF8 = "UTF-8";			// 通知业务系统使用的编码

	public static final String RETURN_PARAM_RETCODE = "retCode";
	public static final String RETURN_PARAM_RETMSG = "retMsg";
	public static final String RESULT_PARAM_RESCODE = "resCode";
	public static final String RESULT_PARAM_ERRCODE = "errCode";
	public static final String RESULT_PARAM_ERRCODEDES = "errCodeDes";
	public static final String RESULT_PARAM_SIGN = "sign";

	public static final String RETURN_VALUE_SUCCESS = "SUCCESS";
	public static final String RETURN_VALUE_FAIL = "FAIL";

	public static final String RETURN_ALIPAY_VALUE_SUCCESS = "success";
	public static final String RETURN_ALIPAY_VALUE_FAIL = "fail";

	public static class JdConstant {
		public final static String CONFIG_PATH = "jd" + File.separator + "jd";	// 京东支付配置文件路径
	}

	public static class WxConstant {
		public final static String TRADE_TYPE_APP = "APP";									// APP支付
		public final static String TRADE_TYPE_JSPAI = "JSAPI";								// 公众号支付或小程序支付
		public final static String TRADE_TYPE_NATIVE = "NATIVE";							// 原生扫码支付
		public final static String TRADE_TYPE_MWEB = "MWEB";								// H5支付

	}

	public static class IapConstant {
		public final static String CONFIG_PATH = "iap" + File.separator + "iap";		// 苹果应用内支付
	}

	public static class AlipayConstant {
		public final static String CONFIG_PATH = "alipay" + File.separator + "alipay";	// 支付宝移动支付
		public final static String TRADE_STATUS_WAIT = "WAIT_BUYER_PAY";		// 交易创建,等待买家付款
		public final static String TRADE_STATUS_CLOSED = "TRADE_CLOSED";		// 交易关闭
		public final static String TRADE_STATUS_SUCCESS = "TRADE_SUCCESS";		// 交易成功
		public final static String TRADE_STATUS_FINISHED = "TRADE_FINISHED";	// 交易成功且结束
	}

	public static final String NOTIFY_BUSI_PAY = "NOTIFY_VV_PAY_RES";
	public static final String NOTIFY_BUSI_TRANS = "NOTIFY_VV_TRANS_RES";
	
}
