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
import com.github.binarywang.wxpay.bean.ecommerce.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.v3.auth.AutoUpdateCertificatesVerifier;
import com.github.binarywang.wxpay.v3.auth.PrivateKeySigner;
import com.github.binarywang.wxpay.v3.auth.WxPayCredentials;
import com.github.binarywang.wxpay.v3.util.AesUtils;
import com.github.binarywang.wxpay.v3.util.PemUtils;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/*
* 微信回调
*
* @author zhuxiao
* @site https://www.jeepay.vip
* @date 2021/6/8 18:10
*/
@Service
@Slf4j
public class WxpayChannelNoticeService extends AbstractChannelNoticeService {

    @Autowired private ConfigContextService configContextService;

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
                MchAppConfigContext mchAppConfigContext = configContextService.getMchAppConfigContext(payOrder.getMchNo(), payOrder.getAppId());
                if(mchAppConfigContext == null){
                    throw new BizException("获取商户信息失败");
                }

                // 验签
                if (!verifyNotifySign(request, mchAppConfigContext)) {
                    throw new BizException("验签失败");
                }

                // 获取加密信息
                JSONObject params = getReqParamJSON();
                JSONObject resource = params.getJSONObject("resource");
                String cipherText = resource.getString("cipherText");
                String associatedData = resource.getString("associatedData");
                String nonce = resource.getString("nonce");

                // 解密
                String result = AesUtils.decryptToString(associatedData, nonce, cipherText, mchAppConfigContext.getWxServiceWrapper().getWxPayService().getConfig().getApiV3Key());
                JSONObject decryptJSON = JSONObject.parseObject(result);
                return MutablePair.of(decryptJSON.getString("out_trade_no"), decryptJSON);

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

            if (CS.PAY_IF_VERSION.WX_V2.equals(mchAppConfigContext.getWxServiceWrapper().getApiVersion())) { // V2
                // 获取回调参数
                WxPayOrderNotifyResult result = (WxPayOrderNotifyResult) params;

                WxPayService wxPayService = mchAppConfigContext.getWxServiceWrapper().getWxPayService();

                // 验证参数
                verifyWxPayParams(wxPayService, result, payOrder);

                channelResult.setChannelOrderId(result.getTransactionId()); //渠道订单号
                channelResult.setChannelUserId(result.getOpenid()); //支付用户ID
                channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(mchAppConfigContext.getWxServiceWrapper().getApiVersion())) { // V3
                // 获取回调参数
                JSONObject resultJSON = (JSONObject) params;

                // 验证参数
                verifyWxPayParams(resultJSON, payOrder);

                String channelState = resultJSON.getString("trade_state");
                if ("SUCCESS".equals(channelState)) {
                    channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else if("CLOSED".equals(channelState)
                        || "REVOKED".equals(channelState)
                        || "PAYERROR".equals(channelState)){  //CLOSED—已关闭， REVOKED—已撤销, PAYERROR--支付失败
                    channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL); //支付失败
                }

                channelResult.setChannelOrderId(resultJSON.getString("transaction_id")); //渠道订单号
                JSONObject payer = resultJSON.getJSONObject("payer");
                if (payer != null) {
                    channelResult.setChannelUserId(StringUtils.isNotBlank(payer.getString("openid")) ? payer.getString("openid") : payer.getString("sp_openid")); //支付用户ID
                }

            }else {
                throw ResponseException.buildText("API_VERSION ERROR");
            }

            ResponseEntity okResponse = textResp("SUCCESS");
            channelResult.setResponseEntity(okResponse); //响应数据

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
    private boolean verifyNotifySign(HttpServletRequest request, MchAppConfigContext mchAppConfigContext) throws Exception {
        SignatureHeader header = new SignatureHeader();
        header.setTimeStamp(request.getHeader("Wechatpay-Timestamp"));
        header.setNonce(request.getHeader("Wechatpay-Nonce"));
        header.setSerialNo(request.getHeader("Wechatpay-Serial"));
        header.setSigned(request.getHeader("Wechatpay-Signature"));

        String beforeSign = String.format("%s\n%s\n%s\n",
                header.getTimeStamp(),
                header.getNonce(),
                getReqParamJSON().toJSONString());

        WxPayConfig wxPayConfig = mchAppConfigContext.getWxServiceWrapper().getWxPayService().getConfig();
        // 自动获取微信平台证书
        PrivateKey privateKey = PemUtils.loadPrivateKey(new FileInputStream(wxPayConfig.getPrivateKeyPath()));
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                new WxPayCredentials(wxPayConfig.getMchId(), new PrivateKeySigner(wxPayConfig.getCertSerialNo(), privateKey)),
                wxPayConfig.getApiV3Key().getBytes("utf-8"));

        return verifier.verify(header.getSerialNo(),
                beforeSign.getBytes(StandardCharsets.UTF_8), header.getSigned());
    }

    /**
     * V3接口验证微信支付通知参数
     * @return
     */
    public void verifyWxPayParams(JSONObject result, PayOrder payOrder) {

        try {
            // 核对金额
            Integer total_fee = result.getInteger("total");   			// 总金额
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
