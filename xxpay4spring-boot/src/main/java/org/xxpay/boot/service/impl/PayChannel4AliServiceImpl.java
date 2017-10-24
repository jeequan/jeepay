package org.xxpay.boot.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.boot.service.BaseService;
import org.xxpay.boot.service.IPayChannel4AliService;
import org.xxpay.boot.service.channel.alipay.AlipayConfig;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.domain.BaseParam;
import org.xxpay.common.enumm.RetEnum;
import org.xxpay.common.util.*;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dal.dao.model.PayOrder;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/10
 * @description:
 */
@Service
public class PayChannel4AliServiceImpl extends BaseService implements IPayChannel4AliService {

    private static final MyLog _log = MyLog.getLog(PayChannel4AliServiceImpl.class);

    @Autowired
    private AlipayConfig alipayConfig;

    @Override
    public Map doAliPayWapReq(String jsonParam) {
        String logPrefix = "【支付宝WAP支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();
        // 封装请求支付信息
        AlipayTradeWapPayModel model=new AlipayTradeWapPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("QUICK_WAP_PAY");
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if(StringUtils.isNotBlank(objParamsJson.getString("quit_url"))) {
                    model.setQuitUrl(objParamsJson.getString("quit_url"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payUrl = null;
        try {
            payUrl = client.pageExecute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        super.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliPayPcReq(String jsonParam) {
        String logPrefix = "【支付宝PC支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradePagePayRequest alipay_request = new AlipayTradePagePayRequest();
        // 封装请求支付信息
        AlipayTradePagePayModel model=new AlipayTradePagePayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        String qr_pay_mode = "2";
        String qrcode_width = "200";
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                qr_pay_mode = ObjectUtils.toString(objParamsJson.getString("qr_pay_mode"), "2");
                qrcode_width = ObjectUtils.toString(objParamsJson.getString("qrcode_width"), "200");
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        model.setQrPayMode(qr_pay_mode);
        model.setQrcodeWidth(Long.parseLong(qrcode_width));
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payUrl = null;
        try {
            payUrl = client.pageExecute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        super.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliPayMobileReq(String jsonParam) {
        String logPrefix = "【支付宝APP支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeAppPayRequest alipay_request = new AlipayTradeAppPayRequest();
        // 封装请求支付信息
        AlipayTradeAppPayModel model=new AlipayTradeAppPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("QUICK_MSECURITY_PAY");
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payParams = null;
        try {
            payParams = client.sdkExecute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        super.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,payParams={}", logPrefix, payParams);
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payParams", payParams);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliPayQrReq(String jsonParam) {
        String logPrefix = "【支付宝当面付之扫码支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        PayOrder payOrder = BeanConvertUtils.map2Bean(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        String mchId = payOrder.getMchId();
        String channelId = payOrder.getChannelId();
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradePrecreateRequest alipay_request = new AlipayTradePrecreateRequest();
        // 封装请求支付信息
        AlipayTradePrecreateModel model=new AlipayTradePrecreateModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if(StringUtils.isNotBlank(objParamsJson.getString("discountable_amount"))) {
                    //可打折金额
                    model.setDiscountableAmount(objParamsJson.getString("discountable_amount"));
                }
                if(StringUtils.isNotBlank(objParamsJson.getString("undiscountable_amount"))) {
                    //不可打折金额
                    model.setUndiscountableAmount(objParamsJson.getString("undiscountable_amount"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payUrl = null;
        try {
            payUrl = client.execute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        super.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return RpcUtil.createBizResult(baseParam, map);
    }

}
