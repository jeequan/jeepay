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
package com.jeequan.jeepay.pay.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.v3.auth.AutoUpdateCertificatesVerifier;
import com.github.binarywang.wxpay.v3.auth.PrivateKeySigner;
import com.github.binarywang.wxpay.v3.auth.WxPayCredentials;
import com.github.binarywang.wxpay.v3.util.PemUtils;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.model.WxServiceWrapper;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.PrivateKey;

/*
* 微信回调
*
* @author zhuxiao
* @site https://www.jeequan.com
* @date 2021/6/8 18:10
*/
@Service
@Slf4j
public class WxpayChannelNoticeService extends AbstractChannelNoticeService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Autowired private PayOrderService payOrderService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {
            if(StringUtils.isNotBlank(urlOrderId)){     // V3接口回调

                // 获取订单信息
                PayOrder payOrder = payOrderService.getById(urlOrderId);
                if(payOrder == null){
                    throw new BizException("订单不存在");
                }

                //获取支付参数 (缓存数据) 和 商户信息
                MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
                if(mchAppConfigContext == null){
                    throw new BizException("获取商户信息失败");
                }

                // 验签 && 获取订单回调数据
                WxPayNotifyV3Result.DecryptNotifyResult result = parseOrderNotifyV3Result(request, mchAppConfigContext);

                return MutablePair.of(result.getOutTradeNo(), result);

            } else {     // V2接口回调
                String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
                if(StringUtils.isEmpty(xmlResult)) {
                    return null;
                }

                WxPayOrderNotifyResult result = WxPayOrderNotifyResult.fromXML(xmlResult);
                String payOrderId = result.getOutTradeNo();
                return MutablePair.of(payOrderId, result);
            }
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {
            ChannelRetMsg channelResult = new ChannelRetMsg();
            channelResult.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) { // V2
                // 获取回调参数
                WxPayOrderNotifyResult result = (WxPayOrderNotifyResult) params;

                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                // 验证参数
                verifyWxPayParams(wxPayService, result, payOrder);

                channelResult.setChannelOrderId(result.getTransactionId()); //渠道订单号
                channelResult.setChannelUserId(result.getOpenid()); //支付用户ID
                channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                channelResult.setResponseEntity(textResp(WxPayNotifyResponse.successResp("OK")));

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) { // V3
                // 获取回调参数
                WxPayNotifyV3Result.DecryptNotifyResult result = (WxPayNotifyV3Result.DecryptNotifyResult) params;

                // 验证参数
                verifyWxPayParams(result, payOrder);

                String channelState = result.getTradeState();
                if ("SUCCESS".equals(channelState)) {
                    channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else if("CLOSED".equals(channelState)
                        || "REVOKED".equals(channelState)
                        || "PAYERROR".equals(channelState)){  //CLOSED—已关闭， REVOKED—已撤销, PAYERROR--支付失败
                    channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL); //支付失败
                }

                channelResult.setChannelOrderId(result.getTransactionId()); //渠道订单号
                WxPayNotifyV3Result.Payer payer = result.getPayer();
                if (payer != null) {
                    channelResult.setChannelUserId(payer.getOpenid()); //支付用户ID
                }

                JSONObject resJSON = new JSONObject();
                resJSON.put("code", "SUCCESS");
                resJSON.put("message", "成功");

                ResponseEntity okResponse = jsonResp(resJSON);
                channelResult.setResponseEntity(okResponse); //响应数据

            }else {
                throw ResponseException.buildText("API_VERSION ERROR");
            }

            return channelResult;

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * V2接口验证微信支付通知参数
     * @return
     */
    public void verifyWxPayParams(WxPayService wxPayService, WxPayOrderNotifyResult result, PayOrder payOrder) {

        try {
            result.checkResult(wxPayService, WxPayConstants.SignType.MD5, true);

            // 核对金额
            Integer total_fee = result.getTotalFee();   			// 总金额
            long wxPayAmt = new BigDecimal(total_fee).longValue();
            long dbPayAmt = payOrder.getAmount().longValue();
            if (dbPayAmt != wxPayAmt) {
                throw ResponseException.buildText("AMOUNT ERROR");
            }
        } catch (Exception e) {
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * V3校验通知签名
     * @param request 请求信息
     * @param mchAppConfigContext 商户配置
     * @return true:校验通过 false:校验不通过
     */
    private WxPayNotifyV3Result.DecryptNotifyResult parseOrderNotifyV3Result(HttpServletRequest request, MchAppConfigContext mchAppConfigContext) throws Exception {
        SignatureHeader header = new SignatureHeader();
        header.setTimeStamp(request.getHeader("Wechatpay-Timestamp"));
        header.setNonce(request.getHeader("Wechatpay-Nonce"));
        header.setSerial(request.getHeader("Wechatpay-Serial"));
        header.setSignature(request.getHeader("Wechatpay-Signature"));

        // 获取加密信息
        String params = getReqParamFromBody();

        log.info("\n【请求头信息】：{}\n【加密数据】：{}", header.toString(), params);

        WxPayService wxPayService = configContextQueryService.getWxServiceWrapper(mchAppConfigContext).getWxPayService();
        WxPayConfig wxPayConfig = wxPayService.getConfig();

        if(StringUtils.isEmpty(wxPayConfig.getPublicKeyId())){ // 如果存在wxPublicKeyId, 那么无需自动换取平台证书
            // 自动获取微信平台证书
            FileInputStream fis = new FileInputStream(wxPayConfig.getPrivateKeyPath());
            PrivateKey privateKey = PemUtils.loadPrivateKey(fis);
            fis.close();
            AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                    new WxPayCredentials(wxPayConfig.getMchId(), new PrivateKeySigner(wxPayConfig.getCertSerialNo(), privateKey)),
                    wxPayConfig.getApiV3Key().getBytes("utf-8"), "https://api.mch.weixin.qq.com");
            wxPayConfig.setVerifier(verifier);
        }

        wxPayService.setConfig(wxPayConfig);

        WxPayNotifyV3Result result = wxPayService.parseOrderNotifyV3Result(params, header);

        return result.getResult();
    }

    /**
     * V3接口验证微信支付通知参数
     * @return
     */
    public void verifyWxPayParams(WxPayNotifyV3Result.DecryptNotifyResult result, PayOrder payOrder) {

        try {
            // 核对金额
            Integer total_fee = result.getAmount().getTotal();   			// 总金额
            long wxPayAmt = new BigDecimal(total_fee).longValue();
            long dbPayAmt = payOrder.getAmount().longValue();
            if (dbPayAmt != wxPayAmt) {
                throw ResponseException.buildText("AMOUNT ERROR");
            }
        } catch (Exception e) {
            throw ResponseException.buildText("ERROR");
        }
    }

}
