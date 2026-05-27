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
package com.jeequan.jeepay.pay.channel.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.alipay.AlipayConfig;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/*
* 支付宝 回调接口实现类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:20
*/
@Service
@Slf4j
public class AlipayChannelNoticeService extends AbstractChannelNoticeService {


    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {

            JSONObject params = getReqParamJSON();
            String payOrderId = params.getString("out_trade_no");
            return MutablePair.of(payOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }



    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {

            //配置参数获取
            Byte useCert = null;
            String alipaySignType, alipayPublicCert, alipayPublicKey = null;
            String expectedAppId; // 业务字段交叉校验用
            if(mchAppConfigContext.isIsvsubMch()){

                // 获取支付参数
                AlipayIsvParams alipayParams = (AlipayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), getIfCode());
                useCert = alipayParams.getUseCert();
                alipaySignType = alipayParams.getSignType();
                alipayPublicCert = alipayParams.getAlipayPublicCert();
                alipayPublicKey = alipayParams.getAlipayPublicKey();
                expectedAppId = alipayParams.getAppId();

            }else{

                // 获取支付参数
                AlipayNormalMchParams alipayParams = (AlipayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

                useCert = alipayParams.getUseCert();
                alipaySignType = alipayParams.getSignType();
                alipayPublicCert = alipayParams.getAlipayPublicCert();
                alipayPublicKey = alipayParams.getAlipayPublicKey();
                expectedAppId = alipayParams.getAppId();
            }

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;

            boolean verifyResult;
            if(useCert != null && useCert == CS.YES){  //证书方式

                verifyResult = AlipaySignature.rsaCertCheckV1(jsonParams.toJavaObject(Map.class), getCertFilePath(alipayPublicCert),
                        AlipayConfig.CHARSET, alipaySignType);

            }else{
                verifyResult = AlipaySignature.rsaCheckV1(jsonParams.toJavaObject(Map.class), alipayPublicKey, AlipayConfig.CHARSET, alipaySignType);
            }

            //验签失败
            if(!verifyResult){
                throw ResponseException.buildText("ERROR");
            }

            // 业务字段交叉校验：验签通过仅证明请求来自支付宝并由本商户密钥签名，
            // 不保证回调内容就是这笔本地订单。多商户配置切换 / 跨订单签名穿越 / 沙箱与生产串扰
            // 等场景下，仍可能出现 out_trade_no、金额、app_id 与本地订单上下文不一致的回调。
            // 任一字段不一致 → 记 ERROR 日志 + 返回 SUCCESS 阻断支付宝重试 8 次 + 不更新订单状态。
            String notifyOutTradeNo = jsonParams.getString("out_trade_no");
            String notifyAppId = jsonParams.getString("app_id");
            String notifyTotalAmount = jsonParams.getString("total_amount");
            String expectedTotalAmount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            boolean amountMatch;
            try {
                amountMatch = new BigDecimal(notifyTotalAmount).compareTo(new BigDecimal(expectedTotalAmount)) == 0;
            } catch (NumberFormatException e) {
                amountMatch = false;
            }
            if (!StringUtils.equals(notifyOutTradeNo, payOrder.getPayOrderId())
                    || !StringUtils.equals(notifyAppId, expectedAppId)
                    || !amountMatch) {
                log.error("支付宝异步回调业务字段不匹配，已拒绝处理。out_trade_no=[{}] expected=[{}]; app_id=[{}] expected=[{}]; total_amount=[{}] expected=[{}]",
                        notifyOutTradeNo, payOrder.getPayOrderId(),
                        notifyAppId, expectedAppId,
                        notifyTotalAmount, expectedTotalAmount);
                ChannelRetMsg dropped = new ChannelRetMsg();
                dropped.setResponseEntity(textResp("SUCCESS"));
                dropped.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN);
                return dropped;
            }

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("SUCCESS");

            ChannelRetMsg result = new ChannelRetMsg();
            result.setChannelOrderId(jsonParams.getString("trade_no")); //渠道订单号
            result.setChannelUserId(jsonParams.getString("buyer_id")); //支付用户ID
            result.setResponseEntity(okResponse); //响应数据

            result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

            if("TRADE_SUCCESS".equals(jsonParams.getString("trade_status"))){
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);

            }else if("TRADE_CLOSED".equals(jsonParams.getString("trade_status"))){
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);

            }

            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

}
