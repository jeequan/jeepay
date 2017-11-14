package org.xxpay.boot.service.channel.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.config.WxPayConfig;

import java.io.File;

/**
 * @author: dingzhiwei
 * @date: 17/8/25
 * @description:
 */
public class WxPayUtil {

    /**
     * 获取微信支付配置
     * @param configParam
     * @param tradeType
     * @param certRootPath
     * @param notifyUrl
     * @return
     */
    public static WxPayConfig getWxPayConfig(String configParam, String tradeType, String certRootPath, String notifyUrl) {
        WxPayConfig wxPayConfig = new WxPayConfig();
        JSONObject paramObj = JSON.parseObject(configParam);
        wxPayConfig.setMchId(paramObj.getString("mchId"));
        wxPayConfig.setAppId(paramObj.getString("appId"));
        wxPayConfig.setKeyPath(certRootPath + File.separator + paramObj.getString("certLocalPath"));
        wxPayConfig.setMchKey(paramObj.getString("key"));
        wxPayConfig.setNotifyUrl(notifyUrl);
        wxPayConfig.setTradeType(tradeType);
        return wxPayConfig;
    }

    /**
     * 获取微信支付配置
     * @param configParam
     * @return
     */
    public static WxPayConfig getWxPayConfig(String configParam) {
        WxPayConfig wxPayConfig = new WxPayConfig();
        JSONObject paramObj = JSON.parseObject(configParam);
        wxPayConfig.setMchId(paramObj.getString("mchId"));
        wxPayConfig.setAppId(paramObj.getString("appId"));
        wxPayConfig.setMchKey(paramObj.getString("key"));
        return wxPayConfig;
    }

}
