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
import com.github.binarywang.wxpay.bean.entpay.EntPayQueryResult;
import com.github.binarywang.wxpay.bean.entpay.EntPayRequest;
import com.github.binarywang.wxpay.bean.entpay.EntPayResult;
import com.github.binarywang.wxpay.bean.transfer.*;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.model.DBApplicationConfig;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.pay.channel.ITransferService;
import com.jeequan.jeepay.pay.channel.wxpay.kits.WxpayKit;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.model.WxServiceWrapper;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.transfer.TransferOrderRQ;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* 转账接口： 微信官方
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/11 14:05
*/
@Slf4j
@Service
public class WxpayTransferService implements ITransferService {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired protected SysConfigService sysConfigService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public boolean isSupport(String entryType) {

        // 微信仅支持 零钱 和 银行卡入账方式
        if(TransferOrder.ENTRY_WX_CASH.equals(entryType) || TransferOrder.ENTRY_BANK_CARD.equals(entryType)){
            return true;
        }

        return false;
    }

    @Override
    public String preCheck(TransferOrderRQ bizRQ, TransferOrder refundOrder) {

        /**
         * 微信企业付款到零钱 产品：不支持服务商模式，参考如下链接：
         * https://developers.weixin.qq.com/community/develop/doc/0004888f8603b042a45c632355a400?highLine=%25E4%25BB%2598%25E6%25AC%25BE%25E5%2588%25B0%25E9%259B%25B6%25E9%2592%25B1%2520%2520%25E6%259C%258D%25E5%258A%25A1%25E5%2595%2586
         * 微信官方解答： 目前企业付款到零钱，是不支持服务商模式的哈，如果特约商户需要使用该功能，请自行登录商户平台申请使用。
         **/
        if(refundOrder.getMchType() == CS.MCH_TYPE_ISVSUB){
            return "微信子商户暂不支持转账业务";
        }

        return null;
    }

    @Override
    public ChannelRetMsg transfer(TransferOrderRQ bizRQ, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext){
        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
            WxpayNormalMchParams normalMchParams = (WxpayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), transferOrder.getIfCode());
            // 转账版本信息
            String transferVersion = StringUtils.defaultIfEmpty(normalMchParams.getTransferVersion(), "old");

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2
                // 如果是新版 直接响应接口不支持
                if (transferVersion.equals("new202501")) {
                    return ChannelRetMsg.sysError("请选择微信V3模式");
                }
                EntPayRequest request = new EntPayRequest();

                request.setMchAppid(wxServiceWrapper.getWxPayService().getConfig().getAppId());  // 商户账号appid
                request.setMchId(wxServiceWrapper.getWxPayService().getConfig().getMchId());  //商户号

                request.setPartnerTradeNo(transferOrder.getTransferId()); //商户订单号
                request.setOpenid(transferOrder.getAccountNo()); //openid
                request.setAmount(transferOrder.getAmount().intValue()); //付款金额，单位为分
                request.setSpbillCreateIp(transferOrder.getClientIp());
                request.setDescription(transferOrder.getTransferDesc()); //付款备注
                if(StringUtils.isNotEmpty(transferOrder.getAccountName())){
                    request.setReUserName(transferOrder.getAccountName());
                    request.setCheckName("FORCE_CHECK");
                }else{
                    request.setCheckName("NO_CHECK");
                }

                EntPayResult entPayResult = wxServiceWrapper.getWxPayService().getEntPayService().entPay(request);
                return ChannelRetMsg.waiting();

            } else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {
                if (transferVersion.equals("new202501")) {
                    return version202501Transfer(wxServiceWrapper, bizRQ, transferOrder);
                }
                TransferBatchesRequest request = new TransferBatchesRequest();
                request.setAppid(wxServiceWrapper.getWxPayService().getConfig().getAppId());
                request.setOutBatchNo(transferOrder.getTransferId());
                if(StringUtils.isNotBlank(transferOrder.getAccountName())){
                    request.setBatchName(transferOrder.getAccountName());
                }else{
                    request.setBatchName(transferOrder.getTransferDesc());
                }
                request.setBatchRemark(transferOrder.getTransferDesc());
                request.setTotalAmount(transferOrder.getAmount().intValue());
                request.setTotalNum(1);

                List<TransferBatchesRequest.TransferDetail> list = new ArrayList<>();
                TransferBatchesRequest.TransferDetail transferDetail = new TransferBatchesRequest.TransferDetail();
                transferDetail.setOutDetailNo(transferOrder.getTransferId());
                transferDetail.setOpenid(transferOrder.getAccountNo());
                transferDetail.setTransferAmount(transferOrder.getAmount().intValue()); //付款金额，单位为分
                transferDetail.setUserName(transferOrder.getAccountName());
                transferDetail.setTransferRemark(transferOrder.getTransferDesc());
                list.add(transferDetail);
                request.setTransferDetailList(list);

                TransferBatchesResult transferBatchesResult = wxServiceWrapper.getWxPayService().getTransferService().transferBatches(request);
                return ChannelRetMsg.waiting();
            } else {
                return ChannelRetMsg.sysError("请选择微信V2或V3模式");
            }

        } catch (WxPayException e) {

            //出现未明确的错误码时（SYSTEMERROR等），请务必用原商户订单号重试，或通过查询接口确认此次付款的结果。
            if("SYSTEMERROR".equalsIgnoreCase(e.getErrCode())){
                return ChannelRetMsg.waiting();
            }

            return ChannelRetMsg.confirmFail(null,
                    WxpayKit.appendErrCode(e.getReturnMsg(), e.getErrCode()),
                    WxpayKit.appendErrMsg(e.getReturnMsg(), StringUtils.defaultIfEmpty(e.getErrCodeDes(), e.getCustomErrorMsg())));

        } catch (Exception e) {
            log.error("转账异常：", e);
            return ChannelRetMsg.waiting();
        }
    }

    /** 适用于2025年01月15日更新后的版本 **/
    private ChannelRetMsg version202501Transfer(WxServiceWrapper wxServiceWrapper, TransferOrderRQ bizRQ, TransferOrder transferOrder) {
        if (StringUtils.isBlank(bizRQ.getChannelExtra())) {
            return ChannelRetMsg.sysError("channelExtra不能为空");
        }
        // 场景类型ID
        String transferSceneId;
        List<TransferBillsRequest.TransferSceneReportInfo> transferSceneReportInfos;
        try {
            JSONObject channelExtra = JSONObject.parseObject(bizRQ.getChannelExtra());
            transferSceneId = StringUtils.defaultIfEmpty(channelExtra.getString("transferSceneId"), "1000");
            // 转账场景报备信息
            transferSceneReportInfos = channelExtra.getJSONArray("transferSceneReportInfos").toJavaList(TransferBillsRequest.TransferSceneReportInfo.class);
            if (transferSceneReportInfos.isEmpty()) {
                return ChannelRetMsg.sysError("channelExtra.transferSceneReportInfos不能为空");
            }
        } catch (Exception e){
            log.error("转账数据转换异常：", e);
            return ChannelRetMsg.sysError("channelExtra格式不正确");
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        TransferBillsRequest transferBillsRequest = new TransferBillsRequest();
        try {
            transferBillsRequest.setAppid(wxServiceWrapper.getWxPayService().getConfig().getAppId());// 商户AppId
            transferBillsRequest.setOutBillNo(transferOrder.getTransferId()); // 商户单号
            transferBillsRequest.setTransferSceneId(transferSceneId); // 1000-现金营销
            transferBillsRequest.setOpenid(transferOrder.getAccountNo()); // 收款用户OpenID
            transferBillsRequest.setUserName(transferOrder.getAccountName()); // 收款方真实姓名
            transferBillsRequest.setTransferAmount(transferOrder.getAmount().intValue()); // 转账金额单位为“分”
            transferBillsRequest.setTransferRemark(transferOrder.getTransferDesc()); // 转账备注
            // 回调地址
            String notifyUrl = sysConfigService.getDBApplicationConfig().getPaySiteUrl() +
                    "/api/transfer/notify/" + transferOrder.getIfCode() + "/" + transferOrder.getTransferId();

            transferBillsRequest.setNotifyUrl(notifyUrl);
            // 转账场景报备信息
            transferBillsRequest.setTransferSceneReportInfos(transferSceneReportInfos);
            TransferBillsResult transferBillsResult = wxServiceWrapper.getWxPayService().getTransferService().transferBills(transferBillsRequest);

            String state = transferBillsResult.getState();
            // 明确转账失败
            if ("FAIL".equals(state)) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(transferBillsResult.getState());
                channelRetMsg.setChannelErrMsg(transferBillsResult.getFailReason());
                return channelRetMsg;
            }

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            if ("SUCCESS".equals(state)) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }
            // 组装转账数据包
            JSONObject transferData = new JSONObject();
            transferData.put("appId", wxServiceWrapper.getWxPayService().getConfig().getAppId());
            transferData.put("mchId", wxServiceWrapper.getWxPayService().getConfig().getMchId());
            transferData.put("package", transferBillsResult.getPackageInfo());
            // 转账确认链接
            DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();
            String userH5ConfirmUrl = dbApplicationConfig.genTransferUserConfirm(transferOrder.getIfCode(), transferOrder.getTransferId());
            transferData.put("userH5ConfirmUrl", userH5ConfirmUrl);
            // 图片格式链接地址
            transferData.put("userH5ConfirmQrImgUrl", dbApplicationConfig.genScanImgUrl(userH5ConfirmUrl));

            channelRetMsg.setChannelAttach(transferData.toJSONString());

        } catch (WxPayException e) {

            //出现未明确的错误码时（SYSTEMERROR等），请务必用原商户订单号重试，或通过查询接口确认此次付款的结果。
            if("SYSTEMERROR".equalsIgnoreCase(e.getErrCode())){
                return ChannelRetMsg.waiting();
            }

            return ChannelRetMsg.confirmFail(null,
                    WxpayKit.appendErrCode(e.getReturnMsg(), e.getErrCode()),
                    WxpayKit.appendErrMsg(e.getReturnMsg(), StringUtils.defaultIfEmpty(e.getErrCodeDes(), e.getCustomErrorMsg())));

        } catch (Exception e) {
            log.error("转账异常：", e);
            return ChannelRetMsg.waiting();
        }
        return channelRetMsg;
    }


    @Override
    public ChannelRetMsg query(TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext) {

        try {
            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
            WxpayNormalMchParams normalMchParams = (WxpayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), transferOrder.getIfCode());
            // 转账版本信息
            String transferVersion = StringUtils.defaultIfEmpty(normalMchParams.getTransferVersion(), "old");

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2
                if (transferVersion.equals("new202501")) {
                    return ChannelRetMsg.sysError("请选择微信V3模式");
                }

                EntPayQueryResult entPayQueryResult = wxServiceWrapper.getWxPayService().getEntPayService().queryEntPay(transferOrder.getTransferId());

                // SUCCESS，明确成功
                if("SUCCESS".equalsIgnoreCase(entPayQueryResult.getStatus())){
                    return ChannelRetMsg.confirmSuccess(entPayQueryResult.getDetailId());
                } else if ("FAILED".equalsIgnoreCase(entPayQueryResult.getStatus())){ // FAILED，明确失败
                    return ChannelRetMsg.confirmFail(entPayQueryResult.getStatus(), entPayQueryResult.getReason());
                } else{
                    return ChannelRetMsg.waiting();
                }
            } else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {
                if (transferVersion.equals("new202501")) {
                    return version202501TransferQuery(wxServiceWrapper, transferOrder);
                }
                TransferBatchDetailResult transferBatchDetailResult =
                        wxServiceWrapper.getWxPayService().getTransferService().transferBatchesOutBatchNoDetail(transferOrder.getTransferId(), transferOrder.getTransferId());

                // SUCCESS，明确成功
                if("SUCCESS".equalsIgnoreCase(transferBatchDetailResult.getDetailStatus())){
                    return ChannelRetMsg.confirmSuccess(transferBatchDetailResult.getDetailId());
                } else if ("FAIL".equalsIgnoreCase(transferBatchDetailResult.getDetailStatus())){ // FAIL，明确失败
                    return ChannelRetMsg.confirmFail(transferBatchDetailResult.getDetailStatus(), transferBatchDetailResult.getFailReason());
                } else{
                    return ChannelRetMsg.waiting();
                }
            } else {
                return ChannelRetMsg.sysError("请选择微信V2或V3模式");
            }

        } catch (WxPayException e) {


            // NOT_FOUND:那么数据不存在的原因可能是：（1）付款还在处理中；（2）付款处理失败导致付款订单没有落地，务必再次查询确认此次付款的结果。
            // INVALID_REQUEST:请等待批次处理完成后再查询明细单据
            // SYSTEM_ERROR: 系统错误
            // 当出现以上情况时，继续查询，不能直接返回错误信息
            if("NOT_FOUND".equalsIgnoreCase(e.getErrCode()) || "INVALID_REQUEST".equalsIgnoreCase(e.getErrCode()) || "SYSTEM_ERROR".equalsIgnoreCase(e.getErrCode())){
                return ChannelRetMsg.waiting();
            }

            return ChannelRetMsg.confirmFail(null,
                    WxpayKit.appendErrCode(e.getReturnMsg(), e.getErrCode()),
                    WxpayKit.appendErrMsg(e.getReturnMsg(), StringUtils.defaultIfEmpty(e.getErrCodeDes(), e.getCustomErrorMsg())));

        } catch (Exception e) {
            log.error("转账状态查询异常：", e);
            return ChannelRetMsg.waiting();
        }
    }

    /** 适用于2015年01月15日更新后的版本 **/
    private ChannelRetMsg version202501TransferQuery(WxServiceWrapper wxServiceWrapper, TransferOrder transferOrder) {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        try {
            TransferBillsGetResult transferBillsGetResult = wxServiceWrapper.getWxPayService().getTransferService().getBillsByOutBillNo(transferOrder.getTransferId());

            String state = transferBillsGetResult.getState();
            // 明确转账失败
            if ("FAIL".equals(state)) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(transferBillsGetResult.getState());
                channelRetMsg.setChannelErrMsg(transferBillsGetResult.getFailReason());
                return channelRetMsg;
            }

            // 转账撤销 CANCELING: 商户撤销请求受理成功，该笔转账正在撤销中 CANCELLED: 转账撤销完成
            if ("CANCELLING".equals(state) || "CANCELED".equals(state)) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(transferBillsGetResult.getState());
                channelRetMsg.setChannelErrMsg("转账失败，该笔转账记录已执行撤销操作");
                return channelRetMsg;
            }

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            if ("SUCCESS".equals(state)) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                channelRetMsg.setChannelOrderId(transferBillsGetResult.getTransferBillNo());
                return channelRetMsg;
            }
            return ChannelRetMsg.waiting();
        } catch (WxPayException e) {
            //出现未明确的错误码时（SYSTEMERROR等），请务必用原商户订单号重试，或通过查询接口确认此次付款的结果。
            if("SYSTEMERROR".equalsIgnoreCase(e.getErrCode())){
                return ChannelRetMsg.waiting();
            }

            return ChannelRetMsg.confirmFail(null,
                    WxpayKit.appendErrCode(e.getReturnMsg(), e.getErrCode()),
                    WxpayKit.appendErrMsg(e.getReturnMsg(), StringUtils.defaultIfEmpty(e.getErrCodeDes(), e.getCustomErrorMsg())));

        } catch (Exception e) {
            log.error("转账查询异常：", e);
            return ChannelRetMsg.waiting();
        }
    }
}
