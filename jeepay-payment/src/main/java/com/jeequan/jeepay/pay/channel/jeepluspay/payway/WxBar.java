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
package com.jeequan.jeepay.pay.channel.jeepluspay.payway;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.Jeepay;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.jeepluspay.JeepluspayConfig;
import com.jeequan.jeepay.core.model.params.jeepluspay.JeepluspayNormalMchParams;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.pay.channel.jeepluspay.JeepluspayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliBarOrderRS;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.WxBarOrderRQ;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 计全付 微信 bar
 *
 * @author yr
 * @site https://www.jeequan.com
 * @date 2022/8/16 18:37
 */
@Service("jeepluspayPaymentByWxBarService") //Service Name需保持全局唯一性
public class WxBar extends JeepluspayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;
        if (StringUtils.isEmpty(bizRQ.getAuthCode())) {
            throw new BizException("用户支付条码[authCode]不可为空");
        }
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;
        JeepluspayNormalMchParams normalMchParams = (JeepluspayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.JEEPLUSPAY);
        // 构建请求数据
        PayOrderCreateRequest request = new PayOrderCreateRequest();
        PayOrderCreateReqModel model = new PayOrderCreateReqModel();
        model.setMchNo(normalMchParams.getMerchantNo());    // 商户号
        model.setAppId(normalMchParams.getAppId());         // 应用ID
        model.setMchOrderNo(payOrder.getPayOrderId());      // 商户订单号
        model.setWayCode(JeepluspayConfig.WX_BAR);          // 支付方式
        model.setAmount(payOrder.getAmount());              // 金额，单位分
        model.setCurrency(payOrder.getCurrency());          // 币种，目前只支持cny
        model.setClientIp(payOrder.getClientIp());          // 发起支付请求客户端的IP地址
        model.setSubject(payOrder.getSubject());            // 商品标题
        model.setBody(payOrder.getBody());                  // 商品描述
        model.setNotifyUrl(getNotifyUrl());                 // 异步通知地址
        JSONObject channelExtra = new JSONObject();
        channelExtra.put("authCode", bizRQ.getAuthCode());
        model.setChannelExtra(channelExtra.toString());         // 用户付款码值
        request.setBizModel(model);
        // 构造函数响应数据
        AliBarOrderRS res = ApiResBuilder.buildSuccess(AliBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        try {
            // 发起统一下单
            PayOrderCreateResponse response = new PayOrderCreateResponse();
            boolean checkSign = false;
            boolean isSuccess = false;
            if (normalMchParams.getSignType().equals(JeepluspayConfig.DEFAULT_SIGN_TYPE) || StringUtils.isEmpty(normalMchParams.getSignType())) {
                JeepayClient jeepayClient = JeepayClient.getInstance(normalMchParams.getAppId(), normalMchParams.getAppSecret(), Jeepay.getApiBase());
                response = jeepayClient.execute(request);
                checkSign = response.checkSign(normalMchParams.getAppSecret());
                isSuccess = response.isSuccess(normalMchParams.getAppSecret());

            } else if (normalMchParams.getSignType().equals(JeepluspayConfig.SIGN_TYPE_RSA2)) {
                JeepayClient jeepayClient = JeepayClient.getInstance(normalMchParams.getAppId(), normalMchParams.getRsa2AppPrivateKey(), Jeepay.getApiBase());
                response = jeepayClient.executeByRSA2(request);
                checkSign = response.checkSignByRsa2(normalMchParams.getRsa2PayPublicKey());
                isSuccess = response.isSuccessByRsa2(normalMchParams.getRsa2PayPublicKey());
            }

            if (checkSign) {
                channelRetMsg.setChannelOrderId(response.get().getPayOrderId());
                if (isSuccess) {
                    // 下单成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                } else {
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrCode(response.get().getErrCode());
                    channelRetMsg.setChannelErrMsg(response.get().getErrMsg());
                }
            }
        } catch (JeepayException e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
        }
        return res;
    }
}
