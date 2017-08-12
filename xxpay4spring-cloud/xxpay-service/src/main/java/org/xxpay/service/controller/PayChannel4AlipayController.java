package org.xxpay.service.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.constant.PayEnum;
import org.xxpay.common.util.*;
import org.xxpay.dal.dao.model.MchInfo;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dal.dao.model.PayOrder;
import org.xxpay.service.channel.alipay.config.AlipayConfig;
import org.xxpay.service.channel.alipay.sign.RSA;
import org.xxpay.service.channel.alipay.util.AlipayCore;
import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.common.Signature;
import org.xxpay.service.channel.tencent.common.Util;
import org.xxpay.service.channel.tencent.protocol.order_protocol.UnifiedOrderReqData;
import org.xxpay.service.channel.tencent.protocol.order_protocol.UnifiedOrderResData;
import org.xxpay.service.channel.tencent.service.UnifiedOrderService;
import org.xxpay.service.service.MchInfoService;
import org.xxpay.service.service.PayChannelService;
import org.xxpay.service.service.PayOrderService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 支付渠道接口:支付宝
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class PayChannel4AlipayController {

    private final MyLog _log = MyLog.getLog(PayChannel4AlipayController.class);

    @Autowired
    private DiscoveryClient client;

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PayChannelService payChannelService;

    @Autowired
    UnifiedOrderService unifiedOrderService;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private MchInfoService mchInfoService;

    /**
     * 支付宝WAP支付
     * 文档：https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.ZGobeF&treeId=60&articleId=104790&docType=1
     * @param jsonParam
     * @return
     */
    @RequestMapping(value = "/pay/channel/ali_wap")
    public String doAliPayWapReq(@RequestParam String jsonParam) {
        String logPrefix = "【支付宝WAP支付下单】";
        JSONObject paramObj = JSON.parseObject(new String(MyBase64.decode(jsonParam)));
        PayOrder payOrder = paramObj.getObject("payOrder", PayOrder.class);
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        MchInfo mchInfo = mchInfoService.selectMchInfo(mchId);
        String resKey = mchInfo == null ? "" : mchInfo.getResKey();
        if("".equals(resKey)) return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "", PayConstant.RETURN_VALUE_FAIL, PayEnum.ERR_0001));
        PayChannel payChannel = payChannelService.selectPayChannel(channelId, mchId);
        alipayConfig.init(payChannel.getParam());
        Map<String, String> payMap = new HashMap<>();
        payMap.put("service", "alipay.wap.create.direct.pay.by.user");      // 接口名称
        payMap.put("partner", alipayConfig.getPartner());                   // 签约的支付宝账号对应的支付宝唯一用户号
        payMap.put("_input_charset", "utf-8");                              // 商户网站使用的编码格式，仅支持UTF-8
        payMap.put("notify_url", alipayConfig.getNotify_url());             // 支付宝服务器主动通知商户网站里指定的页面http路径
        //if (!StringUtils.isEmpty(params.get("respUrl"))) {
        //	payMap.put("return_url", params.get("respUrl"));  // 支付宝处理完请求后，当前页面自动跳转到商户网站里指定页面的http路径(该字段可以不参与签名，但值不能为空！)
        //}
        payMap.put("out_trade_no", payOrderId);                             // 支付宝合作商户网站唯一订单号
        payMap.put("subject", payOrder.getSubject());                       // 商品的标题/交易标题/订单标题/订单关键字等
        payMap.put("total_fee", AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));       // 该笔订单的资金总额，单位为RMB-Yuan。取值范围为[0.01，100000000.00]，精确到小数点后两位
        payMap.put("seller_id", alipayConfig.getPartner());                 // 卖家支付宝账号对应的支付宝唯一用户号
        payMap.put("payment_type", "1");                                    // 支付类型。仅支持：1（商品购买）
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        String show_url = "http://www.xxpay.org";
        if (!org.springframework.util.StringUtils.isEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSONObject.parseObject(objParams);
                show_url = ObjectUtils.toString(objParamsJson.getString("ali_show_url"), "http://www.xxpay.org");
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        payMap.put("show_url", show_url);                      				// 用户付款中途退出返回商户网站的地址
        payMap.put("body", payOrder.getBody());                     	    // 对一笔交易的具体描述信息
        payMap.put("it_b_pay", "60m");                                    	// 设置未付款交易的超时时间，一旦超时，该笔交易就会自动被关闭
        payMap.put("app_pay", "Y");                                         // app_pay=Y：尝试唤起支付宝客户端进行支付，若用户未安装支付宝，则继续使用wap收银台进行支付

        String signContent = AlipayCore.createLinkString(payMap);
        String paySign = RSA.sign(signContent, alipayConfig.getPrivate_key(), AlipayConfig.input_charset);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            paySign = URLEncoder.encode(paySign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.error(e, "URLEncoder AliPay Sign Exception");
        }
        _log.info("{}生成请求支付宝签名,sign={}", logPrefix, paySign);
        payMap.put("sign_type", "RSA");							// 签名类型
        payMap.put("sign", paySign);							// 签名

        // 对有中文的参数转码
        try {
            payMap.put("subject", URLEncoder.encode(payOrder.getSubject(), "UTF-8"));
            payMap.put("body", URLEncoder.encode(payOrder.getBody(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String payUrl = alipayConfig.getGateway().concat("?").concat(AlipayCore.createLinkString(payMap));
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);

        payOrderService.updateStatus4Ing(payOrderId, null);

        _log.info("{}生成请求支付宝数据,req={}", logPrefix, payMap);
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return XXPayUtil.makeRetData(map, resKey);
    }

    /**
     * 支付宝PC支付(即时到帐接口)
     * 文档：https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.M7NEo1&treeId=62&articleId=104743&docType=1
     * @param jsonParam
     * @return
     */
    @RequestMapping(value = "/pay/channel/ali_pc")
    public String doAliPayPcReq(@RequestParam String jsonParam) {
        String logPrefix = "【支付宝PC支付下单】";
        JSONObject paramObj = JSON.parseObject(new String(MyBase64.decode(jsonParam)));
        PayOrder payOrder = paramObj.getObject("payOrder", PayOrder.class);
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        MchInfo mchInfo = mchInfoService.selectMchInfo(mchId);
        String resKey = mchInfo == null ? "" : mchInfo.getResKey();
        if("".equals(resKey)) return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "", PayConstant.RETURN_VALUE_FAIL, PayEnum.ERR_0001));
        PayChannel payChannel = payChannelService.selectPayChannel(channelId, mchId);
        alipayConfig.init(payChannel.getParam());
        Map<String, String> payMap = new HashMap<>();
        payMap.put("service", "create_direct_pay_by_user");		// 接口名称
        payMap.put("partner", alipayConfig.getPartner());		// 签约的支付宝账号对应的支付宝唯一用户号
        payMap.put("_input_charset", "utf-8");					// 编码字符集
        payMap.put("notify_url", alipayConfig.getNotify_url());	// 支付宝服务器主动通知商户网站里指定的页面http路径
        //if (!StringUtils.isEmpty(params.get("respUrl"))) {
        //	payMap.put("return_url", params.get("respUrl"));    // 支付宝处理完请求后，当前页面自动跳转到商户网站里指定页面的http路径(该字段可以不参与签名，但值不能为空！)
        //}
        payMap.put("out_trade_no", payOrderId);                 // 商户网站唯一订单号
        payMap.put("subject", payOrder.getSubject());           // 订单标题
        payMap.put("payment_type", "1");                        // 支付类型,只支持取值为1（商品购买）
        payMap.put("total_fee", AmountUtil.convertCent2Dollar(payOrder.getAmount().toString())); // 订单总金额
        payMap.put("seller_id", alipayConfig.getPartner());		// 卖家支付宝用户号
        payMap.put("body", payOrder.getBody());                 // 对交易或商品的描述

        // 附加参数
        payMap.put("qr_pay_mode", "4");
        String objParams = payOrder.getExtra();
        if (!org.springframework.util.StringUtils.isEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSONObject.parseObject(objParams);
                payMap.put("qr_pay_mode", ObjectUtils.toString(objParamsJson.getString("qr_pay_mode"), "4"));      // 扫码支付的方式，支持前置模式和跳转模式。(4:直接显示二维码)
                payMap.put("qrcode_width", ObjectUtils.toString(objParamsJson.getString("qrcode_width"), "200"));    // 扫码支付的方式，支持前置模式和跳转模式。(定义二维码大小)
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }

        String signContent = AlipayCore.createLinkString(payMap);
        String paySign = RSA.sign(signContent, alipayConfig.getPrivate_key(), AlipayConfig.input_charset);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            paySign = URLEncoder.encode(paySign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.error(e, "URLEncoder AliPay Sign Exception");
        }
        _log.info("{}生成请求支付宝签名,sign={}", logPrefix, paySign);
        payMap.put("sign_type", "RSA");							// 签名类型
        payMap.put("sign", paySign);							// 签名


        // 对有中文的参数转码
        try {
            payMap.put("subject", URLEncoder.encode(payOrder.getSubject(), "UTF-8"));
            payMap.put("body", URLEncoder.encode(payOrder.getBody(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String payUrl = alipayConfig.getGateway().concat("?").concat(AlipayCore.createLinkString(payMap));
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        payOrderService.updateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, payMap);
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return XXPayUtil.makeRetData(map, resKey);
    }

    /**
     * 支付宝支付,生产签名及请求支付宝的参数(注:不会向支付宝发请求)
     * @param jsonParam
     * @return
     */
    @RequestMapping(value = "/pay/channel/ali_mobile")
    public String doAliPayMobileReq(@RequestParam String jsonParam) {
        String logPrefix = "【支付宝支付下单】";
        JSONObject paramObj = JSON.parseObject(new String(MyBase64.decode(jsonParam)));
        PayOrder payOrder = paramObj.getObject("payOrder", PayOrder.class);
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        MchInfo mchInfo = mchInfoService.selectMchInfo(mchId);
        String resKey = mchInfo == null ? "" : mchInfo.getResKey();
        if("".equals(resKey)) return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "", PayConstant.RETURN_VALUE_FAIL, PayEnum.ERR_0001));
        PayChannel payChannel = payChannelService.selectPayChannel(channelId, mchId);
        alipayConfig.init(payChannel.getParam());
        Map<String, String> payMap = new HashMap<>();
        payMap.put("service", "mobile.securitypay.pay");		// 接口名称
        payMap.put("partner", alipayConfig.getPartner());		// 合作者身份ID
        payMap.put("_input_charset", "utf-8");					// 参数编码字符集
        payMap.put("notify_url", alipayConfig.getNotify_url());	// 服务器异步通知页面路径
        //payMap.put("app_id", "");								// 客户端号
        //payMap.put("appenv", "");								// 客户端来源
        payMap.put("out_trade_no", payOrderId);					// 商户网站唯一订单号
        payMap.put("subject", payOrder.getSubject());		    // 商品名称
        payMap.put("payment_type", "1");						// 支付类型
        payMap.put("seller_id", alipayConfig.getAli_account());	// 卖家支付宝账号
        payMap.put("total_fee", AmountUtil.convertCent2Dollar(payOrder.getAmount().toString())); // 总金额,需要分转元.
        payMap.put("body", payOrder.getBody());			        // 商品详情
        //payMap.put("goods_type", "");							// 商品类型
        //payMap.put("hb_fq_param", "");						// 花呗分期参数
        //payMap.put("rn_check", "");							// 是否发起实名校验
        //payMap.put("it_b_pay", "30m");						// 未付款交易的超时时间
        //payMap.put("extern_token", "");						// 授权令牌
        String signContent = AlipayCore.createLinkString(payMap);
        String paySign = RSA.sign(signContent, alipayConfig.getPrivate_key(), AlipayConfig.input_charset);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            paySign = URLEncoder.encode(paySign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.error(e, "URLEncoder AliPay Sign Exception");
        }
        _log.info("{}生成请求支付宝签名,sign={}", logPrefix, paySign);
        payMap.put("sign_type", "RSA");							// 签名类型
        payMap.put("sign", paySign);							// 签名
        payOrderService.updateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, payMap);
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payParams", payMap);
        return XXPayUtil.makeRetData(map, resKey);
    }
}
