/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.pay.channel.wxpay.kits;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.v3.util.PemUtils;
import com.github.binarywang.wxpay.v3.util.SignUtils;
import com.jeequan.jeepay.pay.channel.wxpay.model.WxpayV3OrderRequestModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ZhuXiao
 * @Description:
 * @Date: 15:22 2021/5/26
*/
@Slf4j
public class WxpayV3Util {

    private static final String PAY_BASE_URL = "https://api.mch.weixin.qq.com";
    public static final Map<String, String> NORMALMCH_URL_MAP = new HashMap<>();
    static {
        NORMALMCH_URL_MAP.put(WxPayConstants.TradeType.APP, "/v3/pay/transactions/app");
        NORMALMCH_URL_MAP.put(WxPayConstants.TradeType.JSAPI, "/v3/pay/transactions/jsapi");
        NORMALMCH_URL_MAP.put(WxPayConstants.TradeType.NATIVE, "/v3/pay/transactions/native");
        NORMALMCH_URL_MAP.put(WxPayConstants.TradeType.MWEB, "/v3/pay/transactions/h5");
    }

    public static final Map<String, String> ISV_URL_MAP = new HashMap<>();
    static {
        ISV_URL_MAP.put(WxPayConstants.TradeType.APP, "/v3/pay/partner/transactions/app");
        ISV_URL_MAP.put(WxPayConstants.TradeType.JSAPI, "/v3/pay/partner/transactions/jsapi");
        ISV_URL_MAP.put(WxPayConstants.TradeType.NATIVE, "/v3/pay/partner/transactions/native");
        ISV_URL_MAP.put(WxPayConstants.TradeType.MWEB, "/v3/pay/partner/transactions/h5");
    }

    public static JSONObject queryOrderV3(String url, WxPayService wxPayService) throws WxPayException {
        String response = wxPayService.getV3(PAY_BASE_URL + url);
        return JSON.parseObject(response);
    }

    public static JSONObject closeOrderV3(String url, JSONObject reqJSON, WxPayService wxPayService) throws WxPayException {
        String response = wxPayService.postV3(PAY_BASE_URL + url, reqJSON.toJSONString());
        return JSON.parseObject(response);
    }

    public static JSONObject refundV3(JSONObject reqJSON, WxPayService wxPayService) throws WxPayException {
        String url = String.format("%s/v3/refund/domestic/refunds", PAY_BASE_URL);
        String response = wxPayService.postV3(url, reqJSON.toJSONString());
        return JSON.parseObject(response);
    }

    public static JSONObject refundQueryV3(String refundOrderId, WxPayService wxPayService) throws WxPayException {
        String url = String.format("%s/v3/refund/domestic/refunds/%s", PAY_BASE_URL, refundOrderId);
        String response = wxPayService.getV3(url);
        return JSON.parseObject(response);
    }

    public static JSONObject refundQueryV3Isv(String refundOrderId, WxPayService wxPayService) throws WxPayException {
        String url = String.format("%s/v3/refund/domestic/refunds/%s?sub_mchid=%s", PAY_BASE_URL, refundOrderId, wxPayService.getConfig().getSubMchId());
        String response = wxPayService.getV3(url);
        return JSON.parseObject(response);
    }


    /**
     * 功能描述:
     * 统一调起微信支付请求
     * @param model
     * @param wxPayService
     * @param isIsvsubMch
     * @param tradeType
     * @param wxCallBack
     * @Return: java.lang.String
     * @Author: terrfly
     * @Date: 2022/4/25 12:38
     */
    public static String commonReqWx(WxpayV3OrderRequestModel model, WxPayService wxPayService, boolean isIsvsubMch, String tradeType, WxCallBack wxCallBack) throws WxPayException {

        // 请求地址
        String reqUrl = PAY_BASE_URL + NORMALMCH_URL_MAP.get(tradeType);
        if(isIsvsubMch){ // 特约商户
            reqUrl = PAY_BASE_URL + ISV_URL_MAP.get(tradeType);
        }

        // 调起http请求
        String response = wxPayService.postV3(reqUrl, JSON.toJSONString(model));

        JSONObject wxRes = JSON.parseObject(response);

        if(wxCallBack != null){
            return wxCallBack.genPayInfo(wxRes);
        }

        return response;
    }

    public interface WxCallBack{
        /** 生成返回数据 */
        String genPayInfo(JSONObject wxRes);
    }

}
