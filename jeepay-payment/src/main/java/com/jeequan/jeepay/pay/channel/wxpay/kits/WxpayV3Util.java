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
import com.github.binarywang.wxpay.v3.WxPayV3HttpClientBuilder;
import com.github.binarywang.wxpay.v3.auth.AutoUpdateCertificatesVerifier;
import com.github.binarywang.wxpay.v3.auth.PrivateKeySigner;
import com.github.binarywang.wxpay.v3.auth.WxPayCredentials;
import com.github.binarywang.wxpay.v3.auth.WxPayValidator;
import com.github.binarywang.wxpay.v3.util.PemUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
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

    public static JSONObject unifiedOrderV3(String url, JSONObject reqJSON, WxPayConfig wxPayConfig) throws WxPayException {
        String response = postV3(PAY_BASE_URL + url, reqJSON.toJSONString(), wxPayConfig);
        return JSON.parseObject(response);
    }

    public static JSONObject queryOrderV3(String payOrderId, WxPayConfig wxPayConfig) throws WxPayException {
        String url = String.format("%s/v3/pay/transactions/out-trade-no/%s", PAY_BASE_URL, payOrderId);
        String response = getV3(url, wxPayConfig);
        return JSON.parseObject(response);
    }

    public static JSONObject refundV3(JSONObject reqJSON, WxPayConfig wxPayConfig) throws WxPayException {
        String url = String.format("%s/v3/refund/domestic/refunds", PAY_BASE_URL);
        String response = postV3(url, reqJSON.toJSONString(), wxPayConfig);
        return JSON.parseObject(response);
    }

    public static JSONObject refundQueryV3(String refundOrderId, WxPayConfig wxPayConfig) throws WxPayException {
        String url = String.format("%s/v3/refund/domestic/refunds/%s", PAY_BASE_URL, refundOrderId);
        String response = getV3(url, wxPayConfig);
        return JSON.parseObject(response);
    }

    public static JSONObject refundQueryV3Isv(String refundOrderId, WxPayConfig wxPayConfig) throws WxPayException {
        String url = String.format("%s/v3/refund/domestic/refunds/%s?sub_mchid=%s", PAY_BASE_URL, refundOrderId, wxPayConfig.getSubMchId());
        String response = getV3(url, wxPayConfig);
        return JSON.parseObject(response);
    }

    public static String postV3(String url, String requestStr, WxPayConfig wxPayConfig) throws WxPayException {
        CloseableHttpClient httpClient = createApiV3HttpClient(wxPayConfig);
        HttpPost httpPost = createHttpPost(url, requestStr);
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            //v3已经改为通过状态码判断200 204 成功
            int statusCode = response.getStatusLine().getStatusCode();
            //post方法有可能会没有返回值的情况
            String responseString;
            if (response.getEntity() == null) {
                responseString = null;
            } else {
                responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
            log.info("\n【请求地址】：{}\n【请求数据】：{}\n【响应数据】：{}", url, requestStr, responseString);

            if (HttpStatus.SC_OK == statusCode || HttpStatus.SC_NO_CONTENT == statusCode) {
                return responseString;
            } else {
                //有错误提示信息返回
                JSONObject jsonObject = JSON.parseObject(responseString);
                WxPayException wxPayException = new WxPayException(jsonObject.getString("message"));
                wxPayException.setErrCode(jsonObject.getString("code"));
                wxPayException.setErrCodeDes(jsonObject.getString("message"));
                throw wxPayException;
            }
        } catch (Exception e) {
            log.error("\n【异常信息】：{}", e.getMessage());
            throw (e instanceof WxPayException) ? (WxPayException) e : new WxPayException(e.getMessage(), e);
        } finally {
            httpPost.releaseConnection();
        }

    }

    public static String getV3(String url, WxPayConfig wxPayConfig) throws WxPayException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");
        httpGet.addHeader("Content-Type", "application/json");

        httpGet.setConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(10000)
                .build());

        CloseableHttpClient httpClient = createApiV3HttpClient(wxPayConfig);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            //v3已经改为通过状态码判断200 204 成功
            int statusCode = response.getStatusLine().getStatusCode();
            //post方法有可能会没有返回值的情况
            String responseString;
            if (response.getEntity() == null) {
                responseString = null;
            } else {
                responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
            if (HttpStatus.SC_OK == statusCode || HttpStatus.SC_NO_CONTENT == statusCode) {
                log.info("\n【请求地址】：{}\n【响应数据】：{}", url, responseString);
                return responseString;
            } else {
                //有错误提示信息返回
                JSONObject jsonObject = JSON.parseObject(responseString);
                WxPayException wxPayException = new WxPayException(jsonObject.getString("message"));
                wxPayException.setErrCode(jsonObject.getString("code"));
                wxPayException.setErrCodeDes(jsonObject.getString("message"));
                throw wxPayException;
            }
        } catch (Exception e) {
            log.error("\n【异常信息】：{}", url, e.getMessage());
            throw (e instanceof WxPayException) ? (WxPayException) e : new WxPayException(e.getMessage(), e);
        } finally {
            httpGet.releaseConnection();
        }
    }

    private static CloseableHttpClient createApiV3HttpClient(WxPayConfig wxPayConfig) throws WxPayException {

        try {
            // 自动获取微信平台证书
            PrivateKey privateKey = PemUtils.loadPrivateKey(new FileInputStream(wxPayConfig.getPrivateKeyPath()));
            AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                    new WxPayCredentials(wxPayConfig.getMchId(), new PrivateKeySigner(wxPayConfig.getCertSerialNo(), privateKey)),
                    wxPayConfig.getApiV3Key().getBytes("utf-8"));

            WxPayV3HttpClientBuilder builder = WxPayV3HttpClientBuilder.create()
                    .withMerchant(wxPayConfig.getMchId(), wxPayConfig.getCertSerialNo(), privateKey)
                    .withValidator(new WxPayValidator(verifier));

            CloseableHttpClient apiV3HttpClient = builder.build();
            return apiV3HttpClient;
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            log.error("", e);
        }

        CloseableHttpClient apiV3HttpClient = wxPayConfig.getApiV3HttpClient();
        if (null == apiV3HttpClient) {
            return wxPayConfig.initApiV3HttpClient();
        }
        return null;
    }

    private static HttpPost createHttpPost(String url, String requestStr) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(requestStr, ContentType.create("application/json", "utf-8")));

        httpPost.setConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(10000)
                .build());

        return httpPost;
    }

}
