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
package com.jeequan.jeepay.pay.channel.ysfpay;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.pay.channel.AbstractRefundService;
import com.jeequan.jeepay.pay.channel.ysfpay.utils.YsfHttpUtil;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * 退款接口： 云闪付官方
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2021/6/17 16:38
 */
@Slf4j
@Service
public class YsfpayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Autowired
    private YsfpayPaymentService ysfpayPaymentService;

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(payOrder.getWayCode());
        String logPrefix = "【云闪付("+orderType+")退款】";
        try {
            reqParams.put("origOrderNo", payOrder.getPayOrderId()); // 原交易订单号
            reqParams.put("origTxnAmt", payOrder.getAmount()); // 原交易金额
            reqParams.put("orderNo", refundOrder.getRefundOrderId()); // 退款订单号
            reqParams.put("txnAmt ", refundOrder.getRefundAmount()); // 退款金额
            reqParams.put("orderType ", orderType); // 订单类型

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/refund", reqParams, logPrefix, mchAppConfigContext);
            log.info("查询订单 payorderId:{}, 返回结果:{}", payOrder.getPayOrderId(), resJSON);
            if(resJSON == null){
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN); // 状态不明确
            }
            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            channelRetMsg.setChannelOrderId(refundOrder.getRefundOrderId());
            if("00".equals(respCode)){ // 交易成功
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                log.info("{} >>> 退款成功", logPrefix);
            }else{
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(respCode);
                channelRetMsg.setChannelErrMsg(respMsg);
                log.info("{} >>> 退款失败, {}", logPrefix, respMsg);
            }
        }catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(refundOrder.getWayCode());
        String logPrefix = "【云闪付("+orderType+")退款查询】";
        try {
            reqParams.put("orderNo", refundOrder.getRefundOrderId()); // 退款订单号
            reqParams.put("origOrderNo", refundOrder.getPayOrderId()); // 原交易订单号

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/refundQuery", reqParams, logPrefix, mchAppConfigContext);
            log.info("查询订单 refundOrderId:{}, 返回结果:{}", refundOrder.getRefundOrderId(), resJSON);
            if(resJSON == null){
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN); // 状态不明确
            }
            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            String origRespCode = resJSON.getString("origRespCode"); //原交易应答码
            String origRespMsg = resJSON.getString("origRespMsg"); //原交易应答信息
            channelRetMsg.setChannelOrderId(refundOrder.getRefundOrderId());
            if("00".equals(respCode)){ // 请求成功
                if("00".equals(origRespCode)){ //明确退款成功

                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    log.info("{} >>> 退款成功", logPrefix);

                } else if("01".equals(origRespCode)){ //明确退款失败

                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrCode(respCode);
                    channelRetMsg.setChannelErrMsg(respMsg);
                    log.info("{} >>> 退款失败, {}", logPrefix, origRespMsg);

                } else if("02".equals(origRespCode)){ //退款中
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    log.info("{} >>> 退款中", logPrefix);
                }
            }else{
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN);
                channelRetMsg.setChannelErrCode(respCode);
                channelRetMsg.setChannelErrMsg(respMsg);
                log.info("{} >>> 请求失败, {}", logPrefix, respMsg);
            }
        }catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        return channelRetMsg;
    }

}
