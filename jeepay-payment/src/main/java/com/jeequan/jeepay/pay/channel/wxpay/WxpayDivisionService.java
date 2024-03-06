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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.profitsharing.request.*;
import com.github.binarywang.wxpay.bean.profitsharing.result.*;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchDivisionReceiver;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.pay.channel.IDivisionService;
import com.jeequan.jeepay.pay.channel.wxpay.kits.WxpayKit;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.model.WxServiceWrapper;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* 分账接口： 微信官方
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/22 09:05
*/
@Slf4j
@Service
public class WxpayDivisionService implements IDivisionService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public boolean isSupport() {
        return false;
    }

    @Override
    public ChannelRetMsg bind(MchDivisionReceiver mchDivisionReceiver, MchAppConfigContext mchAppConfigContext) {

        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                ProfitSharingReceiverRequest request = new ProfitSharingReceiverRequest();

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, request);

                JSONObject receiverJSON = new JSONObject();

                // 0-个人， 1-商户  (目前仅支持服务商appI获取个人openId, 即： PERSONAL_OPENID， 不支持 PERSONAL_SUB_OPENID )
                receiverJSON.put("type", mchDivisionReceiver.getAccType() == 0 ? "PERSONAL_OPENID" : "MERCHANT_ID");
                receiverJSON.put("account", mchDivisionReceiver.getAccNo());
                receiverJSON.put("name", mchDivisionReceiver.getAccName());
                receiverJSON.put("relation_type", mchDivisionReceiver.getRelationType());
                receiverJSON.put("custom_relation", mchDivisionReceiver.getRelationTypeName());
                request.setReceiver(receiverJSON.toJSONString());

                ProfitSharingReceiverResult profitSharingReceiverResult =
                        wxServiceWrapper.getWxPayService().getProfitSharingService().addReceiver(request);

                // 明确成功
                return ChannelRetMsg.confirmSuccess(null);
            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {

                ProfitSharingReceiverV3Request profitSharingReceiver = new ProfitSharingReceiverV3Request();
                profitSharingReceiver.setType(mchDivisionReceiver.getAccType() == 0 ? "PERSONAL_OPENID" : "MERCHANT_ID");
                profitSharingReceiver.setAccount(mchDivisionReceiver.getAccNo());
                profitSharingReceiver.setName(mchDivisionReceiver.getAccName());
                profitSharingReceiver.setRelationType(mchDivisionReceiver.getRelationType());
                profitSharingReceiver.setCustomRelation(mchDivisionReceiver.getRelationTypeName());

                profitSharingReceiver.setAppid(WxpayKit.getWxPayConfig(wxServiceWrapper).getAppId());
                // 特约商户
                if(mchAppConfigContext.isIsvsubMch()){
                    WxpayIsvsubMchParams isvsubMchParams =
                            (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);

                    profitSharingReceiver.setSubMchId(isvsubMchParams.getSubMchId());
                }

                ProfitSharingReceiverV3Result receiver = wxServiceWrapper.getWxPayService().getProfitSharingService().addReceiverV3(profitSharingReceiver);

                // 明确成功
                return ChannelRetMsg.confirmSuccess(null);
            } else {
                return ChannelRetMsg.sysError("请选择微信V2或V3模式");
            }

        } catch (WxPayException wxPayException) {
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            WxpayKit.commonSetErrInfo(channelRetMsg, wxPayException);
            return channelRetMsg;

        } catch (Exception e) {

            log.error("请求微信绑定分账接口异常！", e);
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            channelRetMsg.setChannelErrMsg("系统异常：" + e.getMessage());
            return channelRetMsg;
        }
    }

    @Override
    public ChannelRetMsg singleDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext) {

        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                ProfitSharingRequest request = new ProfitSharingRequest();
                request.setTransactionId(payOrder.getChannelOrderNo());

                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, request);

                if(recordList.isEmpty()){
                    request.setOutOrderNo(SeqKit.genDivisionBatchId()); // 随机生成一个订单号
                }else{
                    request.setOutOrderNo(recordList.get(0).getBatchOrderId()); //取到批次号
                }

                JSONArray receiverJSONArray = new JSONArray();

                for (int i = 0; i < recordList.size(); i++) {

                    PayOrderDivisionRecord record = recordList.get(i);
                    if(record.getCalDivisionAmount() <= 0){
                        continue;
                    }

                    JSONObject receiverJSON = new JSONObject();
                    // 0-个人， 1-商户  (目前仅支持服务商appI获取个人openId, 即： PERSONAL_OPENID， 不支持 PERSONAL_SUB_OPENID )
                    receiverJSON.put("type", record.getAccType() == 0 ? "PERSONAL_OPENID" : "MERCHANT_ID");
                    receiverJSON.put("account", record.getAccNo());
                    receiverJSON.put("amount", record.getCalDivisionAmount());
                    receiverJSON.put("description", record.getPayOrderId() + "分账");
                    receiverJSONArray.add(receiverJSON);
                }

                //不存在接收账号时，订单完结（解除冻结金额）
                if(receiverJSONArray.isEmpty()){
                    return ChannelRetMsg.confirmSuccess(this.divisionFinish(payOrder, mchAppConfigContext));
                }

                request.setReceivers(receiverJSONArray.toJSONString());

                ProfitSharingResult profitSharingResult = wxServiceWrapper.getWxPayService().getProfitSharingService().profitSharing(request);
                return ChannelRetMsg.waiting();

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {

                ProfitSharingV3Request request = new ProfitSharingV3Request();
                request.setTransactionId(payOrder.getChannelOrderNo());
                request.setUnfreezeUnsplit(true);

                request.setAppid(WxpayKit.getWxPayConfig(wxServiceWrapper).getAppId());
                // 特约商户
                if(mchAppConfigContext.isIsvsubMch()){
                    WxpayIsvsubMchParams isvsubMchParams =
                            (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);

                    request.setSubMchId(isvsubMchParams.getSubMchId());
                }

                if(recordList.isEmpty()){
                    request.setOutOrderNo(SeqKit.genDivisionBatchId()); // 随机生成一个订单号
                }else{
                    request.setOutOrderNo(recordList.get(0).getBatchOrderId()); //取到批次号
                }

                List<ProfitSharingV3Request.Receiver> receivers = new ArrayList<>();
                for (int i = 0; i < recordList.size(); i++) {

                    PayOrderDivisionRecord record = recordList.get(i);
                    if(record.getCalDivisionAmount() <= 0){
                        continue;
                    }

                    ProfitSharingV3Request.Receiver receiver = new ProfitSharingV3Request.Receiver();
                    // 0-个人， 1-商户  (目前仅支持服务商appI获取个人openId, 即： PERSONAL_OPENID， 不支持 PERSONAL_SUB_OPENID )
                    receiver.setType(record.getAccType() == 0 ? "PERSONAL_OPENID" : "MERCHANT_ID");
                    receiver.setAccount(record.getAccNo());
                    receiver.setAmount(record.getCalDivisionAmount().intValue());
                    receiver.setDescription(record.getPayOrderId() + "分账");
                    receivers.add(receiver);
                }
                //不存在接收账号时，订单完结（解除冻结金额）
                if(receivers.isEmpty()){
                    return ChannelRetMsg.confirmSuccess(this.divisionFinish(payOrder, mchAppConfigContext));
                }
                request.setReceivers(receivers);

                ProfitSharingV3Result profitSharingResult = wxServiceWrapper.getWxPayService().
                        getProfitSharingService().profitSharingV3(request);

                return ChannelRetMsg.waiting();

            } else {

                return ChannelRetMsg.sysError("请选择微信V2或V3模式");

            }

        } catch (WxPayException wxPayException) {

            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            WxpayKit.commonSetErrInfo(channelRetMsg, wxPayException);
            return channelRetMsg;

        } catch (Exception e) {
            log.error("微信分账失败", e);
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            channelRetMsg.setChannelErrMsg(e.getMessage());
            return channelRetMsg;
        }
    }

    @Override
    public HashMap<Long, ChannelRetMsg> queryDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext) {

        // 创建返回结果
        HashMap<Long, ChannelRetMsg> resultMap = new HashMap<>();
        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);


            // 得到所有的 accNo 与 recordId map
            Map<String, Long> accnoAndRecordIdSet = new HashMap<>();
            for (PayOrderDivisionRecord record : recordList) {
                accnoAndRecordIdSet.put(record.getAccNo(), record.getRecordId());
            }

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

                ProfitSharingQueryRequest request = new ProfitSharingQueryRequest();
                request.setTransactionId(payOrder.getChannelOrderNo());
                //放置isv信息
                WxpayKit.putApiIsvInfo(mchAppConfigContext, request);

                request.setOutOrderNo(recordList.get(0).getBatchOrderId()); //取到批次号

                ProfitSharingQueryResult profitSharingQueryResult = wxServiceWrapper.getWxPayService().getProfitSharingService().profitSharingQuery(request);
                List<ProfitSharingQueryResult.Receiver> receivers = profitSharingQueryResult.getReceivers();

                for (ProfitSharingQueryResult.Receiver receiver : receivers) {

                    // 我方系统的分账接收记录ID
                    Long recordId = accnoAndRecordIdSet.get(receiver.getAccount());

                    // 记录中包含账号
                    if (recordId != null) {

                        // 仅返回分账记录为最终态的结果 处理中的分账单不做返回处理
                        if ("SUCCESS".equals(receiver.getResult())) {

                            resultMap.put(recordId, ChannelRetMsg.confirmSuccess(null));

                        }else if ("CLOSED".equals(receiver.getResult())) {

                            resultMap.put(recordId, ChannelRetMsg.confirmFail(null, null, receiver.getFailReason()));
                        }
                    }

                }

            }else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {

                String url = String.format("%s/v3/profitsharing/orders/%s?transaction_id=%s",
                            wxServiceWrapper.getWxPayService().getPayBaseUrl(), recordList.get(0).getBatchOrderId(), payOrder.getChannelOrderNo());

                // 特约商户
                if(mchAppConfigContext.isIsvsubMch()){
                    WxpayIsvsubMchParams isvsubMchParams =
                            (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);
                    url += "&sub_mchid=" + isvsubMchParams.getSubMchId();
                }

                String result = wxServiceWrapper.getWxPayService().getV3(url);

                ProfitSharingV3Result profitSharingResult = JSON.parseObject(result, ProfitSharingV3Result.class);

                List<ProfitSharingV3Result.Receiver> receivers = profitSharingResult.getReceivers();

                for (ProfitSharingV3Result.Receiver receiver : receivers) {

                    // 我方系统的分账接收记录ID
                    Long recordId = accnoAndRecordIdSet.get(receiver.getAccount());

                    // 记录中包含账号
                    if (recordId != null) {

                        // 仅返回分账记录为最终态的结果 处理中的分账单不做返回处理
                        if ("SUCCESS".equals(receiver.getResult())) {

                            resultMap.put(recordId, ChannelRetMsg.confirmSuccess(null));

                        }else if ("CLOSED".equals(receiver.getResult())) {

                            resultMap.put(recordId, ChannelRetMsg.confirmFail(null, null, receiver.getFailReason()));
                        }
                    }

                }

            }

        } catch (WxPayException wxPayException) {
            log.error("微信查询分账结果失败, e = {}", wxPayException);
            throw new BizException(wxPayException.getCustomErrorMsg());

        } catch (Exception e) {
            log.error("微信分账失败", e);
            throw new BizException(e.getMessage());
        }

        return resultMap;
    }

    /** 调用订单的完结接口 (分账对象不存在时) */
    private String divisionFinish(PayOrder payOrder,MchAppConfigContext mchAppConfigContext) throws WxPayException {

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

            ProfitSharingUnfreezeRequest request = new ProfitSharingUnfreezeRequest();

            //放置isv信息
            WxpayKit.putApiIsvInfo(mchAppConfigContext, request);

            request.setSubAppId(null); // 传入subAppId 将导致签名失败

            request.setTransactionId(payOrder.getChannelOrderNo());
            request.setOutOrderNo(SeqKit.genDivisionBatchId());
            request.setDescription("完结分账");

            return wxServiceWrapper.getWxPayService().getProfitSharingService().profitSharingFinish(request).getOrderId();

        }else {

            ProfitSharingUnfreezeV3Request request = new ProfitSharingUnfreezeV3Request();
            // 特约商户
            if(mchAppConfigContext.isIsvsubMch()){
                WxpayIsvsubMchParams isvsubMchParams =
                        (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);

                request.setSubMchId(isvsubMchParams.getSubMchId());
            }

            request.setTransactionId(payOrder.getChannelOrderNo());
            request.setOutOrderNo(SeqKit.genDivisionBatchId());
            request.setSubMchId(null);
            request.setDescription("完结分账");
            ProfitSharingUnfreezeV3Result profitSharingUnfreezeResult = wxServiceWrapper.getWxPayService().getProfitSharingService().profitSharingUnfreeze(request);

            // 明确成功
            return profitSharingUnfreezeResult.getOrderId();
        }
    }

}
