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
package com.jeequan.jeepay.pay.channel.plspay;

import com.jeequan.jeepay.Jeepay;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.model.params.plspay.PlspayConfig;
import com.jeequan.jeepay.core.model.params.plspay.PlspayNormalMchParams;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.model.RefundOrderQueryReqModel;
import com.jeequan.jeepay.pay.channel.AbstractRefundService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRQ;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.request.RefundOrderQueryRequest;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.jeequan.jeepay.response.RefundOrderQueryResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 退款接口： 计全退款plus
 *
 * @author yurong
 * @site https://www.jeequan.com
 * @date 2022/8/16 15:28
 */
@Service
public class PlspayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.PLSPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        PlspayNormalMchParams normalMchParams = (PlspayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.PLSPAY);
        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
        model.setMchNo(normalMchParams.getMerchantNo());            // 商户号
        model.setAppId(normalMchParams.getAppId());                 // 应用ID
        model.setPayOrderId(payOrder.getChannelOrderNo());          // 支付订单号
        model.setMchRefundNo(refundOrder.getRefundOrderId());       // 商户退款单号
        model.setRefundAmount(refundOrder.getRefundAmount());       // 金额，单位分
        model.setCurrency(refundOrder.getCurrency());               // 币种，目前只支持cny
        model.setRefundReason(refundOrder.getRefundReason());       // 退款原因
        model.setClientIp(refundOrder.getClientIp());               // 发起退款请求客户端的IP地址
        model.setNotifyUrl(getNotifyUrl());                         // 异步通知地址
        request.setBizModel(model);
        // 构造函数响应数据
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        try {
            // 发起退款
            RefundOrderCreateResponse response = new RefundOrderCreateResponse();
            if (normalMchParams.getSignType().equals(PlspayConfig.DEFAULT_SIGN_TYPE) || StringUtils.isEmpty(normalMchParams.getSignType())) {
                JeepayClient jeepayClient = JeepayClient.getInstance(normalMchParams.getAppId(), normalMchParams.getAppSecret(), Jeepay.getApiBase());
                response = jeepayClient.execute(request);

            } else if (normalMchParams.getSignType().equals(PlspayConfig.SIGN_TYPE_RSA2)) {
                JeepayClient jeepayClient = JeepayClient.getInstance(normalMchParams.getAppId(), normalMchParams.getRsa2AppPrivateKey(), Jeepay.getApiBase());
                response = jeepayClient.executeByRSA2(request);
            }

            // 下单返回状态
            Boolean isSuccess = PlspayKit.checkPayResp(response, mchAppConfigContext);

            // 退款发送成功
            if (isSuccess) {
                if (PlspayConfig.REFUND_STATE_SUCCESS.equals(response.get().getState().toString())) {
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }
                channelRetMsg.setChannelOrderId(response.get().getRefundOrderId());
            } else {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(response.getCode().toString());
                channelRetMsg.setChannelErrMsg(response.getMsg());
            }
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        } catch (JeepayException e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        RefundOrderQueryRequest request = new RefundOrderQueryRequest();
        RefundOrderQueryReqModel model = new RefundOrderQueryReqModel();
        try {
            PlspayNormalMchParams normalMchParams = (PlspayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.PLSPAY);
            model.setMchNo(normalMchParams.getMerchantNo());            // 商户号
            model.setAppId(normalMchParams.getAppId());                 // 应用ID
            model.setRefundOrderId(refundOrder.getRefundOrderId());     // 退款订单号
            request.setBizModel(model);
            // 发起请求
            RefundOrderQueryResponse response = new RefundOrderQueryResponse();
            if (normalMchParams.getSignType().equals(PlspayConfig.DEFAULT_SIGN_TYPE) || StringUtils.isEmpty(normalMchParams.getSignType())) {
                JeepayClient jeepayClient = JeepayClient.getInstance(normalMchParams.getAppId(), normalMchParams.getAppSecret(), Jeepay.getApiBase());
                response = jeepayClient.execute(request);
            } else if (normalMchParams.getSignType().equals(PlspayConfig.SIGN_TYPE_RSA2)) {
                JeepayClient jeepayClient = JeepayClient.getInstance(normalMchParams.getAppId(), normalMchParams.getRsa2AppPrivateKey(), Jeepay.getApiBase());
                response = jeepayClient.executeByRSA2(request);
            }

            // 下单返回状态
            Boolean isSuccess = PlspayKit.checkPayResp(response, mchAppConfigContext);

            // 请求响应状态
            if (isSuccess) {
                // 如果查询请求成功
                if (PlspayConfig.PAY_STATE_SUCCESS.equals(response.get().getState().toString())) {
                    return ChannelRetMsg.confirmSuccess(response.get().getRefundOrderId());
                } else if (PlspayConfig.PAY_STATE_FAIL.equals(response.get().getState().toString())) {
                    // 失败
                    return ChannelRetMsg.confirmFail();
                }
            }
            // 退款中
            return ChannelRetMsg.waiting();
        } catch (Exception e) {
            // 退款中
            return ChannelRetMsg.waiting();
        }
    }
}
