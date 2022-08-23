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

import com.jeequan.jeepay.Jeepay;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.jeepluspay.JeepluspayConfig;
import com.jeequan.jeepay.core.model.params.jeepluspay.JeepluspayNormalMchParams;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.pay.channel.jeepluspay.JeepluspayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliWapOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 计全付 支付宝 wap支付
 *
 * @author yr
 * @site https://www.jeequan.com
 * @date 2022/8/17 14:46
 */
@Service("jeepluspayPaymentByAliWapService") //Service Name需保持全局唯一性
public class AliWap extends JeepluspayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        JeepluspayNormalMchParams normalMchParams = (JeepluspayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.JEEPLUSPAY);
        // 构建请求数据
        PayOrderCreateRequest request = new PayOrderCreateRequest();
        PayOrderCreateReqModel model = new PayOrderCreateReqModel();
        model.setMchNo(normalMchParams.getMerchantNo());    // 商户号
        model.setAppId(normalMchParams.getAppId());         // 应用ID
        model.setMchOrderNo(payOrder.getPayOrderId());      // 商户订单号
        model.setWayCode(JeepluspayConfig.ALI_WAP);         // 支付方式
        model.setAmount(payOrder.getAmount());              // 金额，单位分
        model.setCurrency(payOrder.getCurrency());          // 币种，目前只支持cny
        model.setClientIp(payOrder.getClientIp());          // 发起支付请求客户端的IP地址
        model.setSubject(payOrder.getSubject());            // 商品标题
        model.setBody(payOrder.getBody());                  // 商品描述
        model.setNotifyUrl(getNotifyUrl());                 // 异步通知地址
        request.setBizModel(model);
        // 构造函数响应数据
        AliWapOrderRS res = ApiResBuilder.buildSuccess(AliWapOrderRS.class);
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
                    String payUrl = response.getData().getString("payData");
                    String payDataType = response.getData().getString("payDataType");
                    if (CS.PAY_DATA_TYPE.FORM.equals(payDataType)) { //表单方式
                        res.setFormContent(payUrl);
                    } else if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(payDataType)) { //二维码图片地址
                        res.setCodeImgUrl(payUrl);
                    } else { // 默认都为 payUrl方式
                        res.setPayUrl(payUrl);
                    }
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
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
