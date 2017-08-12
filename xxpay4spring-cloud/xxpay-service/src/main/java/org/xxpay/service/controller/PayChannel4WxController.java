package org.xxpay.service.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.constant.PayEnum;
import org.xxpay.common.util.MyBase64;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.PayDigestUtil;
import org.xxpay.common.util.XXPayUtil;
import org.xxpay.dal.dao.model.MchInfo;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dal.dao.model.PayOrder;
import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.common.Signature;
import org.xxpay.service.channel.tencent.common.Util;
import org.xxpay.service.channel.tencent.protocol.order_protocol.UnifiedOrderReqData;
import org.xxpay.service.channel.tencent.protocol.order_protocol.UnifiedOrderResData;
import org.xxpay.service.channel.tencent.service.UnifiedOrderService;
import org.xxpay.service.service.MchInfoService;
import org.xxpay.service.service.PayChannelService;
import org.xxpay.service.service.PayOrderService;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 支付渠道接口:微信
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class PayChannel4WxController {

    private final MyLog _log = MyLog.getLog(PayChannel4WxController.class);

    @Autowired
    private DiscoveryClient client;

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PayChannelService payChannelService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private UnifiedOrderService unifiedOrderService;

    @Autowired
    private Configure configure;

    /**
     * 发起微信支付(统一下单)
     * @param
     * @return
     */
    @RequestMapping(value = "/pay/channel/wx")
    public String doWxPayReq(@RequestParam String jsonParam) {
        // TODO 参数校验
        JSONObject paramObj = JSON.parseObject(new String(MyBase64.decode(jsonParam)));
        PayOrder payOrder = paramObj.getObject("payOrder", PayOrder.class);
        String tradeType = paramObj.getString("tradeType");

        String logPrefix = "【微信支付统一下单】";
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        MchInfo mchInfo = mchInfoService.selectMchInfo(mchId);
        String resKey = mchInfo == null ? "" : mchInfo.getResKey();
        if("".equals(resKey)) return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "", PayConstant.RETURN_VALUE_FAIL, PayEnum.ERR_0001));
        PayChannel payChannel = payChannelService.selectPayChannel(channelId, mchId);
        configure.init(payChannel.getParam());
        String payOrderId = payOrder.getPayOrderId();
        UnifiedOrderReqData unifiedOrderReqData = buildUnifiedOrderReqData(tradeType, payOrder, configure);
        try {
            _log.info("{}发起支付请求,请求数据:{}", logPrefix, unifiedOrderReqData.toMap());
            // 设置CRT
            unifiedOrderService.init(configure);
            String responseStr = unifiedOrderService.request(unifiedOrderReqData);
            // 打印回包数据
            _log.info("{}发起支付请求,返回数据:{}", logPrefix, responseStr);
            // 转换为返回数据类型
            UnifiedOrderResData unifiedOrderResData = (UnifiedOrderResData) Util.getObjectFromXML(responseStr, UnifiedOrderResData.class);
            if (unifiedOrderResData == null || unifiedOrderResData.getReturn_code() == null) {
                _log.error("{}【支付失败】支付请求逻辑错误,请仔细检测传过去的每一个参数是否合法,或是看API能否被正常访问", logPrefix);
                return XXPayUtil.makeRetData(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, PayEnum.ERR_0111), resKey);
            }
            if (unifiedOrderResData.getReturn_code().equals("FAIL")) {
                //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
                _log.error("{}【支付失败】支付API系统返回失败,请检测Post给API的数据是否规范合法", logPrefix);
                return XXPayUtil.makeRetData(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, PayEnum.ERR_0111), resKey);
            } else {
                _log.info("{}支付API系统成功返回数据", logPrefix);
                //--------------------------------------------------------------------
                //收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
                //--------------------------------------------------------------------
                if (!Signature.checkIsSignValidFromResponseString(responseStr, configure.getKey())) {
                    _log.error("{}【支付失败】支付请求API返回的数据签名验证失败,有可能数据被篡改了", logPrefix);
                    return XXPayUtil.makeRetData(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, PayEnum.ERR_0111), resKey);
                }
                _log.info("{}支付请求返回数据验签通过", logPrefix);
                //获取错误码
                String errorCode = unifiedOrderResData.getErr_code();
                //获取错误描述
                String errorCodeDes = unifiedOrderResData.getErr_code_des();
                if (unifiedOrderResData.getResult_code().equals("SUCCESS")) {
                    _log.info("{} >>> 下单成功", logPrefix);
                    Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
                    map.put("payOrderId", payOrderId);
                    map.put("prepayId", unifiedOrderResData.getPrepay_id());

                    int result = payOrderService.updateStatus4Ing(payOrderId, unifiedOrderResData.getPrepay_id());
                    _log.info("更新第三方支付订单号:payOrderId={},prepayId={},result={}", payOrderId, unifiedOrderResData.getPrepay_id(), result);

                    //map.put("rechargeId", String.valueOf(rechargeId));
                    if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_NATIVE)) {
                        map.put("codeUrl", unifiedOrderResData.getCode_url());
                    }else if (tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_APP)) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("appid", unifiedOrderReqData.getAppid());
                        m.put("partnerid", unifiedOrderReqData.getMch_id());
                        m.put("prepayid", unifiedOrderResData.getPrepay_id());
                        m.put("package", "Sign=WXPay");
                        m.put("noncestr", unifiedOrderReqData.getNonce_str());
                        m.put("timestamp", System.currentTimeMillis()/1000);
                        String wxSign = Signature.getSign(m, configure.getKey());
                        m.put("sign", wxSign);
                        map.put("payParams", m);
                        map.put("payPrice", payOrder.getAmount());
                    }else if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_JSPAI)) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("appId", unifiedOrderReqData.getAppid());
                        m.put("timeStamp", System.currentTimeMillis()/1000);
                        m.put("nonceStr", unifiedOrderReqData.getNonce_str());
                        m.put("package", "prepay_id=" + unifiedOrderResData.getPrepay_id());
                        m.put("signType", "MD5");
                        String wxSign = Signature.getSign(m, configure.getKey());
                        m.put("paySign", wxSign);
                        map.put("payParams", m);
                        map.put("payPrice", payOrder.getAmount());
                    }
                    return XXPayUtil.makeRetData(map, resKey);
                }else{
                    //出现业务错误
                    _log.info("{}下单返回失败", logPrefix);
                    _log.info("err_code:{}", errorCode);
                    _log.info("err_code_des:{}", errorCodeDes);
                    return XXPayUtil.makeRetData(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, "0111", "调用微信支付失败," + errorCode + ":" + errorCodeDes), resKey);
                }
            }

        } catch (Exception e) {
            _log.error(e, "请求异常,%s", e.getMessage());
        }
        _log.info("###### 商户统一下单处理完成 ######");
        return XXPayUtil.makeRetData(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null), resKey);
    }

    /**
     * 构建统一下单请求数据
     * @param tradeType
     * @param payOrder
     * @param configure
     * @return
     */
    UnifiedOrderReqData buildUnifiedOrderReqData(String tradeType, PayOrder payOrder, Configure configure) {
        String payOrderId = payOrder.getPayOrderId();
        String payPrice = payOrder.getAmount()+"";
        String deviceInfo = payOrder.getDevice();
        String body = payOrder.getBody();
        String detail = null;
        String attach = null;
        String outTradeNo = payOrderId;
        String feeType = "CNY";
        String totalFee = payPrice;		// 支付金额,单位分
        String spBillCreateIP = payOrder.getClientIp();
        String timeStart = null;
        String timeExpire = null;
        String goodsTag = null;
        String notifyUrl = configure.getNotify_url();
        String productId = null;
        if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_NATIVE)) productId = JSON.parseObject(payOrder.getExtra()).getString("productId");
        String limitPay = null;
        String openId = null;
        if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_JSPAI)) openId = JSON.parseObject(payOrder.getExtra()).getString("openId");
        UnifiedOrderReqData unifiedOrderReqData = new UnifiedOrderReqData(configure, deviceInfo, body, detail, attach,
                outTradeNo, feeType, totalFee, spBillCreateIP, timeStart, timeExpire, goodsTag, notifyUrl,
                tradeType, productId, limitPay, openId);
        return unifiedOrderReqData;
    }

}
