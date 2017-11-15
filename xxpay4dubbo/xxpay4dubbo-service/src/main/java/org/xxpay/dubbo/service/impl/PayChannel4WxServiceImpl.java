package org.xxpay.dubbo.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.*;
import com.github.binarywang.wxpay.bean.result.*;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.github.binarywang.wxpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.domain.BaseParam;
import org.xxpay.common.enumm.RetEnum;
import org.xxpay.common.util.*;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dal.dao.model.PayOrder;
import org.xxpay.dal.dao.model.RefundOrder;
import org.xxpay.dal.dao.model.TransOrder;
import org.xxpay.dubbo.api.service.IPayChannel4WxService;
import org.xxpay.dubbo.service.BaseService;
import org.xxpay.dubbo.service.BaseService4PayOrder;
import org.xxpay.dubbo.service.channel.wechat.WxPayProperties;
import org.xxpay.dubbo.service.channel.wechat.WxPayUtil;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 支付渠道接口:微信
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-09-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Service(version = "1.0.0")
public class PayChannel4WxServiceImpl extends BaseService implements IPayChannel4WxService {

    private final MyLog _log = MyLog.getLog(PayChannel4WxServiceImpl.class);

    @Resource
    private WxPayProperties wxPayProperties;

    @Autowired
    private BaseService4PayOrder baseService4PayOrder;

    public Map doWxPayReq(String jsonParam) {
        String logPrefix = "【微信支付统一下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
            String tradeType = baseParam.isNullValue("tradeType") ? null : bizParamMap.get("tradeType").toString();
            PayOrder payOrder = JSON.toJavaObject(payOrderObj, PayOrder.class);
            if (ObjectValidUtil.isInvalid(payOrder, tradeType)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            String mchId = payOrder.getMchId();
            String channelId = payOrder.getChannelId();
            PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel.getParam(), tradeType, wxPayProperties.getCertRootPath(), wxPayProperties.getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest = buildUnifiedOrderRequest(payOrder, wxPayConfig);
            String payOrderId = payOrder.getPayOrderId();
            WxPayUnifiedOrderResult wxPayUnifiedOrderResult;
            try {
                wxPayUnifiedOrderResult = wxPayService.unifiedOrder(wxPayUnifiedOrderRequest);
                _log.info("{} >>> 下单成功", logPrefix);
                Map<String, Object> map = new HashMap<>();
                map.put("payOrderId", payOrderId);
                map.put("prepayId", wxPayUnifiedOrderResult.getPrepayId());
                int result = baseService4PayOrder.baseUpdateStatus4Ing(payOrderId, null);
                _log.info("更新第三方支付订单号:payOrderId={},prepayId={},result={}", payOrderId, wxPayUnifiedOrderResult.getPrepayId(), result);
                switch (tradeType) {
                    case PayConstant.WxConstant.TRADE_TYPE_NATIVE : {
                        map.put("codeUrl", wxPayUnifiedOrderResult.getCodeURL());   // 二维码支付链接
                        break;
                    }
                    case PayConstant.WxConstant.TRADE_TYPE_APP : {
                        Map<String, String> payInfo = new HashMap<>();
                        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                        String nonceStr = String.valueOf(System.currentTimeMillis());
                        // APP支付绑定的是微信开放平台上的账号，APPID为开放平台上绑定APP后发放的参数
                        String appId = wxPayConfig.getAppId();
                        Map<String, String> configMap = new HashMap<>();
                        // 此map用于参与调起sdk支付的二次签名,格式全小写，timestamp只能是10位,格式固定，切勿修改
                        String partnerId = wxPayConfig.getMchId();
                        configMap.put("prepayid", wxPayUnifiedOrderResult.getPrepayId());
                        configMap.put("partnerid", partnerId);
                        String packageValue = "Sign=WXPay";
                        configMap.put("package", packageValue);
                        configMap.put("timestamp", timestamp);
                        configMap.put("noncestr", nonceStr);
                        configMap.put("appid", appId);
                        // 此map用于客户端与微信服务器交互
                        payInfo.put("sign", SignUtils.createSign(configMap, wxPayConfig.getMchKey(), null));
                        payInfo.put("prepayId", wxPayUnifiedOrderResult.getPrepayId());
                        payInfo.put("partnerId", partnerId);
                        payInfo.put("appId", appId);
                        payInfo.put("packageValue", packageValue);
                        payInfo.put("timeStamp", timestamp);
                        payInfo.put("nonceStr", nonceStr);
                        map.put("payParams", payInfo);
                        break;
                    }
                    case PayConstant.WxConstant.TRADE_TYPE_JSPAI : {
                        Map<String, String> payInfo = new HashMap<>();
                        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                        String nonceStr = String.valueOf(System.currentTimeMillis());
                        payInfo.put("appId", wxPayUnifiedOrderResult.getAppid());
                        // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符
                        payInfo.put("timeStamp", timestamp);
                        payInfo.put("nonceStr", nonceStr);
                        payInfo.put("package", "prepay_id=" + wxPayUnifiedOrderResult.getPrepayId());
                        payInfo.put("signType", WxPayConstants.SignType.MD5);
                        payInfo.put("paySign", SignUtils.createSign(payInfo, wxPayConfig.getMchKey(), null));
                        map.put("payParams", payInfo);
                        break;
                    }
                    case PayConstant.WxConstant.TRADE_TYPE_MWEB : {
                        map.put("payUrl", wxPayUnifiedOrderResult.getMwebUrl());    // h5支付链接地址
                        break;
                    }
                }
                return RpcUtil.createBizResult(baseParam, map);
            } catch (WxPayException e) {
                _log.error(e, "下单失败");
                //出现业务错误
                _log.info("{}下单返回失败", logPrefix);
                _log.info("err_code:{}", e.getErrCode());
                _log.info("err_code_des:{}", e.getErrCodeDes());
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
            }
        }catch (Exception e) {
            _log.error(e, "微信支付统一下单异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    @Override
    public Map doWxTransReq(String jsonParam) {
        String logPrefix = "【微信企业付款】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
            TransOrder transOrder = JSON.toJavaObject(transOrderObj, TransOrder.class);
            if (ObjectValidUtil.isInvalid(transOrder)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            String mchId = transOrder.getMchId();
            String channelId = transOrder.getChannelId();
            PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel.getParam(), "", wxPayProperties.getCertRootPath(), wxPayProperties.getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            WxEntPayRequest wxEntPayRequest = buildWxEntPayRequest(transOrder, wxPayConfig);
            String transOrderId = transOrder.getTransOrderId();
            Map<String, Object> map = new HashMap<>();
            WxEntPayResult result;
            try {
                result = wxPayService.entPay(wxEntPayRequest);
                _log.info("{} >>> 转账成功", logPrefix);
                map.put("transOrderId", transOrderId);
                map.put("isSuccess", true);
                map.put("channelOrderNo", result.getPaymentNo());
            } catch (WxPayException e) {
                _log.error(e, "转账失败");
                //出现业务错误
                _log.info("{}转账返回失败", logPrefix);
                _log.info("err_code:{}", e.getErrCode());
                _log.info("err_code_des:{}", e.getErrCodeDes());
                map.put("transOrderId", transOrderId);
                map.put("isSuccess", false);
                map.put("channelErrCode", e.getErrCode());
                map.put("channelErrMsg", e.getErrCodeDes());
            }
            return RpcUtil.createBizResult(baseParam, map);
        }catch (Exception e) {
            _log.error(e, "微信转账异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    @Override
    public Map getWxTransReq(String jsonParam) {
        String logPrefix = "【微信企业付款查询】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
            TransOrder transOrder = JSON.toJavaObject(transOrderObj, TransOrder.class);
            if (ObjectValidUtil.isInvalid(transOrder)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            String mchId = transOrder.getMchId();
            String channelId = transOrder.getChannelId();
            PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel.getParam(), "", wxPayProperties.getCertRootPath(), wxPayProperties.getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            String transOrderId = transOrder.getTransOrderId();
            Map<String, Object> map = new HashMap<>();
            WxEntPayQueryResult result;
            try {
                result = wxPayService.queryEntPay(transOrderId);
                _log.info("{} >>> 成功", logPrefix);
                map.putAll((Map) JSON.toJSON(result));
                map.put("isSuccess", true);
                map.put("transOrderId", transOrderId);
            } catch (WxPayException e) {
                _log.error(e, "失败");
                //出现业务错误
                _log.info("{}返回失败", logPrefix);
                _log.info("err_code:{}", e.getErrCode());
                _log.info("err_code_des:{}", e.getErrCodeDes());
                map.put("channelErrCode", e.getErrCode());
                map.put("channelErrMsg", e.getErrCodeDes());
                map.put("isSuccess", false);
            }
            return RpcUtil.createBizResult(baseParam, map);
        }catch (Exception e) {
            _log.error(e, "微信企业付款查询异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    @Override
    public Map doWxRefundReq(String jsonParam) {
        String logPrefix = "【微信退款】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
            RefundOrder refundOrder = JSON.toJavaObject(refundOrderObj, RefundOrder.class);
            if (ObjectValidUtil.isInvalid(refundOrder)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            String mchId = refundOrder.getMchId();
            String channelId = refundOrder.getChannelId();
            PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel.getParam(), "", wxPayProperties.getCertRootPath(), wxPayProperties.getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            WxPayRefundRequest wxPayRefundRequest = buildWxPayRefundRequest(refundOrder, wxPayConfig);
            String refundOrderId = refundOrder.getRefundOrderId();
            Map<String, Object> map = new HashMap<>();
            WxPayRefundResult result;
            try {
                result = wxPayService.refund(wxPayRefundRequest);
                _log.info("{} >>> 下单成功", logPrefix);
                map.put("isSuccess", true);
                map.put("refundOrderId", refundOrderId);
                map.put("channelOrderNo", result.getRefundId());
            } catch (WxPayException e) {
                _log.error(e, "下单失败");
                //出现业务错误
                _log.info("{}下单返回失败", logPrefix);
                _log.info("err_code:{}", e.getErrCode());
                _log.info("err_code_des:{}", e.getErrCodeDes());
                map.put("isSuccess", false);
                map.put("channelErrCode", e.getErrCode());
                map.put("channelErrMsg", e.getErrCodeDes());
            }
            return RpcUtil.createBizResult(baseParam, map);
        }catch (Exception e) {
            _log.error(e, "微信退款异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    @Override
    public Map getWxRefundReq(String jsonParam) {
        String logPrefix = "【微信退款查询】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
            RefundOrder refundOrder = JSON.toJavaObject(refundOrderObj, RefundOrder.class);
            if (ObjectValidUtil.isInvalid(refundOrder)) {
                _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            String mchId = refundOrder.getMchId();
            String channelId = refundOrder.getChannelId();
            PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel.getParam(), "", wxPayProperties.getCertRootPath(), wxPayProperties.getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            String refundOrderId = refundOrder.getRefundOrderId();
            Map<String, Object> map = new HashMap<>();
            WxPayRefundQueryResult result;
            try {
                result = wxPayService.refundQuery(refundOrder.getChannelPayOrderNo(), refundOrder.getPayOrderId(), refundOrder.getRefundOrderId(), refundOrder.getChannelOrderNo());
                _log.info("{} >>> 成功", logPrefix);
                map.putAll((Map) JSON.toJSON(result));
                map.put("isSuccess", true);
                map.put("refundOrderId", refundOrderId);
            } catch (WxPayException e) {
                _log.error(e, "失败");
                //出现业务错误
                _log.info("{}返回失败", logPrefix);
                _log.info("err_code:{}", e.getErrCode());
                _log.info("err_code_des:{}", e.getErrCodeDes());
                map.put("channelErrCode", e.getErrCode());
                map.put("channelErrMsg", e.getErrCodeDes());
                map.put("isSuccess", false);
            }
            return RpcUtil.createBizResult(baseParam, map);
        }catch (Exception e) {
            _log.error(e, "微信退款查询异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    /**
     * 构建微信统一下单请求数据
     * @param payOrder
     * @param wxPayConfig
     * @return
     */
    WxPayUnifiedOrderRequest buildUnifiedOrderRequest(PayOrder payOrder, WxPayConfig wxPayConfig) {
        String tradeType = wxPayConfig.getTradeType();
        String payOrderId = payOrder.getPayOrderId();
        Integer totalFee = payOrder.getAmount().intValue();// 支付金额,单位分
        String deviceInfo = payOrder.getDevice();
        String body = payOrder.getBody();
        String detail = null;
        String attach = null;
        String outTradeNo = payOrderId;
        String feeType = "CNY";
        String spBillCreateIP = payOrder.getClientIp();
        String timeStart = null;
        String timeExpire = null;
        String goodsTag = null;
        String notifyUrl = wxPayConfig.getNotifyUrl();
        String productId = null;
        if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_NATIVE)) productId = JSON.parseObject(payOrder.getExtra()).getString("productId");
        String limitPay = null;
        String openId = null;
        if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_JSPAI)) openId = JSON.parseObject(payOrder.getExtra()).getString("openId");
        String sceneInfo = null;
        if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_MWEB)) sceneInfo = JSON.parseObject(payOrder.getExtra()).getString("sceneInfo");
        // 微信统一下单请求对象
        WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
        request.setDeviceInfo(deviceInfo);
        request.setBody(body);
        request.setDetail(detail);
        request.setAttach(attach);
        request.setOutTradeNo(outTradeNo);
        request.setFeeType(feeType);
        request.setTotalFee(totalFee);
        request.setSpbillCreateIp(spBillCreateIP);
        request.setTimeStart(timeStart);
        request.setTimeExpire(timeExpire);
        request.setGoodsTag(goodsTag);
        request.setNotifyURL(notifyUrl);
        request.setTradeType(tradeType);
        request.setProductId(productId);
        request.setLimitPay(limitPay);
        request.setOpenid(openId);
        request.setSceneInfo(sceneInfo);
        return request;
    }

    /**
     * 构建微信企业付款请求数据
     * @param transOrder
     * @param wxPayConfig
     * @return
     */
    WxEntPayRequest buildWxEntPayRequest(TransOrder transOrder, WxPayConfig wxPayConfig) {
        // 微信企业付款请求对象
        WxEntPayRequest request = new WxEntPayRequest();
        request.setAmount(transOrder.getAmount().intValue()); // 金额,单位分
        String checkName = "NO_CHECK";
        if(transOrder.getExtra() != null) checkName = JSON.parseObject(transOrder.getExtra()).getString("checkName");
        request.setCheckName(checkName);
        request.setDescription(transOrder.getRemarkInfo());
        request.setReUserName(transOrder.getUserName());
        request.setPartnerTradeNo(transOrder.getTransOrderId());
        request.setDeviceInfo(transOrder.getDevice());
        request.setSpbillCreateIp(transOrder.getClientIp());
        request.setOpenid(transOrder.getChannelUser());
        return request;
    }

    /**
     * 构建微信退款请求数据
     * @param refundOrder
     * @param wxPayConfig
     * @return
     */
    WxPayRefundRequest buildWxPayRefundRequest(RefundOrder refundOrder, WxPayConfig wxPayConfig) {
        // 微信退款请求对象
        WxPayRefundRequest request = new WxPayRefundRequest();
        request.setTransactionId(refundOrder.getChannelPayOrderNo());
        request.setOutTradeNo(refundOrder.getPayOrderId());
        request.setDeviceInfo(refundOrder.getDevice());
        request.setOutRefundNo(refundOrder.getRefundOrderId());
        request.setRefundDesc(refundOrder.getRemarkInfo());
        request.setRefundFee(refundOrder.getRefundAmount().intValue());
        request.setRefundFeeType("CNY");
        request.setTotalFee(refundOrder.getPayAmount().intValue());
        return request;
    }

}
