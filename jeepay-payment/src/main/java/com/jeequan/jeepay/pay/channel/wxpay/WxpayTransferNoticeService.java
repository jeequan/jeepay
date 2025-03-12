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
import com.github.binarywang.wxpay.bean.transfer.TransferBillsNotifyResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.v3.auth.AutoUpdateCertificatesVerifier;
import com.github.binarywang.wxpay.v3.auth.PrivateKeySigner;
import com.github.binarywang.wxpay.v3.auth.WxPayCredentials;
import com.github.binarywang.wxpay.v3.util.PemUtils;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.pay.channel.AbstractTransferNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.TransferOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.PrivateKey;

/*
* 微信 转账回调接口实现类
*
* @author yr
* @site https://www.jeequan.com
* @date 2025/03/11 17:16
*/
@Service
@Slf4j
public class WxpayTransferNoticeService extends AbstractTransferNoticeService {

    @Autowired private TransferOrderService transferOrderService;
    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId) {
        try {
            // 获取转账单信息
            TransferOrder transferOrder = transferOrderService.getById(urlOrderId);
            if(transferOrder == null){
                throw new BizException("转账记录不存在");
            }

            //获取支付参数 (缓存数据) 和 商户信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(transferOrder.getMchNo(), transferOrder.getAppId());
            if(mchAppConfigContext == null){
                throw new BizException("获取商户信息失败");
            }

            // 验签 && 获取订单回调数据
            TransferBillsNotifyResult.DecryptNotifyResult result = parseTransferBillsNotifyV3Result(request, mchAppConfigContext);
            return MutablePair.of(result.getOutBillNo(), result);
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }


    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext) {

        String logPrefix = "【微信转账通知】";

        try {
            ChannelRetMsg channelResult = new ChannelRetMsg();
            channelResult.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认转账中

            // 获取回调参数
            TransferBillsNotifyResult.DecryptNotifyResult result = (TransferBillsNotifyResult.DecryptNotifyResult) params;

            // 验证参数
            verifyWxTransferParams(result, transferOrder);

            // 成功－SUCCESS
            String status = result.getState();
            if("SUCCESS".equals(status)){
                channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            } else if("FAIL".equals(status)
                    || "CANCELING".equals(status)
                    || "CANCELLED".equals(status)){  //FAIL—失败， CANCELING—撤销中, CANCELLED--已撤销
                channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL); //支付失败
            }

            channelResult.setChannelOrderId(result.getTransferBillNo()); //渠道订单号
            channelResult.setChannelUserId(result.getOpenid()); //支付用户ID

            JSONObject resJSON = new JSONObject();
            resJSON.put("code", "SUCCESS");
            resJSON.put("message", "成功");

            ResponseEntity okResponse = jsonResp(resJSON);
            channelResult.setResponseEntity(okResponse); //响应数据

            return channelResult;

        } catch (Exception e) {
            log.error("{}error", logPrefix, e);
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * V3校验通知签名
     * @param request 请求信息
     * @param mchAppConfigContext 商户配置
     * @return true:校验通过 false:校验不通过
     */
    private TransferBillsNotifyResult.DecryptNotifyResult parseTransferBillsNotifyV3Result(HttpServletRequest request, MchAppConfigContext mchAppConfigContext) throws Exception {
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

        TransferBillsNotifyResult result = wxPayService.parseTransferBillsNotifyV3Result(params, header);
        return result.getResult();
    }


    /**
     * V3接口验证微信转账通知参数
     * @return
     */
    public void verifyWxTransferParams(TransferBillsNotifyResult.DecryptNotifyResult result, TransferOrder transferOrder) {
        try {
            // 核对金额
            Integer totalAmount = result.getTransferAmount();   			// 总金额
            long wxTransferAmt = new BigDecimal(totalAmount).longValue();
            long dbTransferAmt = transferOrder.getAmount().longValue();
            if (dbTransferAmt != wxTransferAmt) {
                throw ResponseException.buildText("AMOUNT ERROR");
            }
        } catch (Exception e) {
            throw ResponseException.buildText("ERROR");
        }
    }
}
