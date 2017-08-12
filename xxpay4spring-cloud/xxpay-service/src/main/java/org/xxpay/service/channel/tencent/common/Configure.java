package org.xxpay.service.channel.tencent.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.IPUtility;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.PropertiesFileUtil;

import java.util.Date;
import java.util.Map;

/**
 * User: rizenguo
 * Date: 2014/10/29
 * Time: 14:40
 * 这里放置各种配置数据
 */
@RefreshScope
@Service
public class Configure {

	private static final MyLog _log = MyLog.getLog(Configure.class);

	public Configure init(String configParam) {
		JSONObject paramObj = JSON.parseObject(configParam);
		this.setMchID(paramObj.getString("mchId"));
		this.setAppID(paramObj.getString("appId"));
		this.setCertLocalPath(Configure.class.getClassLoader().getResource(paramObj.getString("certLocalPath")).getPath());
		this.setCertPassword(paramObj.getString("certPassword"));
		this.setKey(paramObj.getString("key"));
		this.setIp(IPUtility.getLocalIP());
		return this;
	}


	// 这个就是自己要保管好的私有Key了（切记只能放在自己的后台代码里，不能放在任何可能被看到源代码的客户端程序中）
	// 每次自己Post数据给API的时候都要用这个key来对所有字段进行签名，生成的签名会放在Sign这个字段，API收到Post数据的时候也会用同样的签名算法对Post过来的数据进行签名和验证
	// 收到API的返回的时候也要用这个key来对返回的数据算下签名，跟API的Sign数据进行比较，如果值不一致，有可能数据被第三方给篡改

	private String key;

	//微信分配的公众号ID（开通公众号之后可以获取到）
	private String appID;

	private String mchID;

	//HTTPS证书的本地路径
//	private static String certLocalPath = "/Users/dingzhiwei/java/tmp/wx.crt.p12";
	private String certLocalPath;

	//HTTPS证书密码，默认密码等于商户号MCHID
	private String certPassword;

	//是否使用异步线程的方式来上报API测速，默认为异步模式
	private static boolean useThreadToDoReport = true;

	//配置描述
	private String desc;

	//机器IP
	private String ip;

	//以下是几个API的路径：
	//1）被扫支付API
	public static String PAY_API = "https://api.mch.weixin.qq.com/pay/micropay";

	//2）被扫支付查询API
	public static String ORDER_QUERY_API = "https://api.mch.weixin.qq.com/pay/orderquery";

	//3）退款API
	public static String REFUND_API = "https://api.mch.weixin.qq.com/secapi/pay/refund";

	//4）退款查询API
	public static String REFUND_QUERY_API = "https://api.mch.weixin.qq.com/pay/refundquery";

	//5）撤销API
	public static String REVERSE_API = "https://api.mch.weixin.qq.com/secapi/pay/reverse";

	//6）下载对账单API
	public static String DOWNLOAD_BILL_API = "https://api.mch.weixin.qq.com/pay/downloadbill";

	//7) 统计上报API
	public static String REPORT_API = "https://api.mch.weixin.qq.com/payitil/report";

	//8) 统一下单API
	public static String UNIFIED_ORDER_API = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	//9) 发送现金红包API
	public static String SEND_REDPACK_API = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";

	//10) 查询现金红包API
	public static String QUERY_REDPACK_API = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gethbinfo";

	//9) 企业付款API
	public static String TRANSFERS_API = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";

	//10) 查询企业付款API
	public static String GET_TRANSFERS_API = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo";

	@Value("${wx.notify_url}")
	private String notify_url;

	// 配置加载时间
	private Long loadTime = new Date().getTime();

	public static boolean isUseThreadToDoReport() {
		return useThreadToDoReport;
	}

	public static void setUseThreadToDoReport(boolean useThreadToDoReport) {
		Configure.useThreadToDoReport = useThreadToDoReport;
	}

	public static String HttpsRequestClassName = "org.xxpay.service.channel.tencent.common.HttpsRequest";

	public static void setHttpsRequestClassName(String name){
		HttpsRequestClassName = name;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

	public String getCertLocalPath() {
		return certLocalPath;
	}

	public void setCertLocalPath(String certLocalPath) {
		this.certLocalPath = certLocalPath;
	}

	public String getCertPassword() {
		return certPassword;
	}

	public void setCertPassword(String certPassword) {
		this.certPassword = certPassword;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getMchID() {
		return mchID;
	}

	public void setMchID(String mchID) {
		this.mchID = mchID;
	}

	public String getDesc() {
		return desc;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Long getLoadTime() {
		return loadTime;
	}

	public void setLoadTime(Long loadTime) {
		this.loadTime = loadTime;
	}

	@Override
	public String toString() {
		return "Configure{" +
				"key='" + key + '\'' +
				", appID='" + appID + '\'' +
				", certLocalPath='" + certLocalPath + '\'' +
				", certPassword='" + certPassword + '\'' +
				", desc='" + desc + '\'' +
				", ip='" + ip + '\'' +
				", loadTime=" + loadTime +
				'}';
	}

}
