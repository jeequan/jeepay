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
import com.github.binarywang.wxpay.bean.request.WxPayRefundQueryRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayIsvParams;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractRefundService;
import com.jeequan.jeepay.pay.channel.wxpay.kits.WxpayKit;
import com.jeequan.jeepay.pay.channel.wxpay.kits.WxpayV3Util;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * 退款接口： 微信官方
 *
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/17 16:38
 */
@Slf4j
@Service
public class WxpayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    /** 微信退款接口 **/
    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        try {
            ChannelRetMsg channelRetMsg = new ChannelRetMsg();
            if (CS.PAY_IF_VERSION.WX_V2.equals(mchAppConfigContext.getWxServiceWrapper().getApiVersion())) {  //V2

                WxPayRefundRequest req = new WxPayRefundRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutTradeNo(payOrder.getPayOrderId());    // 商户订单号
                req.setOutRefundNo(refundOrder.getRefundOrderId()); // 退款单号
                req.setTotalFee(payOrder.getAmount().intValue());   // 订单总金额
                req.setRefundFee(refundOrder.getRefundAmount().intValue()); // 退款金额
                WxPayService wxPayService = mchAppConfigContext.getWxServiceWrapper().getWxPayService();
                setCretPath(mchAppConfigContext, wxPayService); // 证书路径

                WxPayRefundResult result = wxPayService.refundV2(req);
                if("SUCCESS".equals(result.getResultCode())){ //支付成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    channelRetMsg.setChannelOrderId(result.getRefundId());
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrMsg(result.getReturnMsg());
                }
            }else if (CS.PAY_IF_VERSION.WX_V3.equals(mchAppConfigContext.getWxServiceWrapper().getApiVersion())) {   //V3
                // 微信统一下单请求对象
                JSONObject reqJSON = new JSONObject();
                reqJSON.put("out_trade_no", refundOrder.getPayOrderId());   // 订单号
                reqJSON.put("out_refund_no", refundOrder.getRefundOrderId()); // 退款订单号

                JSONObject amountJson = new JSONObject();
                amountJson.put("refund", refundOrder.getRefundAmount());// 退款金额
                amountJson.put("total", payOrder.getAmount());// 订单总金额
                amountJson.put("currency", "CNY");// 币种
                reqJSON.put("amount", amountJson);
                WxPayService wxPayService = mchAppConfigContext.getWxServiceWrapper().getWxPayService();
                setCretPath(mchAppConfigContext, wxPayService); // 证书路径

                if(mchAppConfigContext.isIsvsubMch()){ // 特约商户
                    WxpayIsvsubMchParams isvsubMchParams = mchAppConfigContext.getIsvsubMchParamsByIfCode(getIfCode(), WxpayIsvsubMchParams.class);
                    reqJSON.put("sub_mchid", isvsubMchParams.getSubMchId());
                }

                JSONObject resultJSON = WxpayV3Util.refundV3(reqJSON, mchAppConfigContext.getWxServiceWrapper().getWxPayService().getConfig());
                String status = resultJSON.getString("status");
                if("SUCCESS".equals(status)){ // 退款成功
                    String refundId = resultJSON.getString("refund_id");
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    channelRetMsg.setChannelOrderId(refundId);
                }else if ("PROCESSING".equals(status)){ // 退款处理中
                    String refundId = resultJSON.getString("refund_id");
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelOrderId(refundId);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrMsg(status);
                }

            }
            return channelRetMsg;
        } catch (WxPayException e) {
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

    /** 微信退款查单接口 **/
    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        try {
            ChannelRetMsg channelRetMsg = new ChannelRetMsg();
            if (CS.PAY_IF_VERSION.WX_V2.equals(mchAppConfigContext.getWxServiceWrapper().getApiVersion())) {  //V2

                WxPayRefundQueryRequest req = new WxPayRefundQueryRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, req);

                req.setOutRefundNo(refundOrder.getRefundOrderId()); // 退款单号
                WxPayService wxPayService = mchAppConfigContext.getWxServiceWrapper().getWxPayService();
                setCretPath(mchAppConfigContext, wxPayService); // 证书路径

                WxPayRefundQueryResult result = wxPayService.refundQueryV2(req);
                if("SUCCESS".equals(result.getResultCode())){ // 退款成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelErrMsg(result.getReturnMsg());
                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(mchAppConfigContext.getWxServiceWrapper().getApiVersion())) {   //V3
                WxPayService wxPayService = mchAppConfigContext.getWxServiceWrapper().getWxPayService();
                setCretPath(mchAppConfigContext, wxPayService); // 证书路径
                JSONObject resultJSON = null;
                if (mchAppConfigContext.isIsvsubMch()) {
                    WxpayIsvsubMchParams isvsubMchParams = mchAppConfigContext.getIsvsubMchParamsByIfCode(getIfCode(), WxpayIsvsubMchParams.class);
                    wxPayService.getConfig().setSubMchId(isvsubMchParams.getSubMchId());
                    resultJSON = WxpayV3Util.refundQueryV3Isv(refundOrder.getRefundOrderId(), wxPayService.getConfig());
                }else {
                    resultJSON = WxpayV3Util.refundQueryV3(refundOrder.getRefundOrderId(), wxPayService.getConfig());
                }
                String status = resultJSON.getString("status");
                if("SUCCESS".equals(status)){ // 退款成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else{
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    channelRetMsg.setChannelErrMsg(status);
                }
            }
            return channelRetMsg;
        } catch (WxPayException e) {
            return ChannelRetMsg.sysError(e.getReturnMsg());
        } catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage());
        }
    }

    private void setCretPath(MchAppConfigContext mchAppConfigContext, WxPayService wxPayService) {
        if(mchAppConfigContext.isIsvsubMch()){
            // 获取服务商配置信息
            WxpayIsvParams wxpayIsvParams = mchAppConfigContext.getIsvConfigContext().getIsvParamsByIfCode(CS.IF_CODE.WXPAY, WxpayIsvParams.class);
            wxPayService.getConfig().setKeyPath(channelCertConfigKitBean.getCertFilePath(wxpayIsvParams.getCert()));
        }else{
            // 获取商户配置信息
            WxpayNormalMchParams normalMchParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.WXPAY, WxpayNormalMchParams.class);
            wxPayService.getConfig().setKeyPath(channelCertConfigKitBean.getCertFilePath(normalMchParams.getCert()));
        }
    }

}
