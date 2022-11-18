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

import com.github.binarywang.wxpay.bean.entpay.EntPayRequest;
import com.github.binarywang.wxpay.bean.entpay.EntPayResult;
import com.github.binarywang.wxpay.bean.transfer.TransferBatchesRequest;
import com.github.binarywang.wxpay.bean.transfer.TransferBatchesResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.pay.channel.ITransferService;
import com.jeequan.jeepay.pay.channel.wxpay.kits.WxpayKit;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.model.WxServiceWrapper;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.transfer.TransferOrderRQ;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
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

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {  //V2

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

                // SUCCESS/FAIL，注意：当状态为FAIL时，存在业务结果未明确的情况。如果状态为FAIL，请务必关注错误代码（err_code字段），通过查询接口确认此次付款的结果。
                if("SUCCESS".equalsIgnoreCase(entPayResult.getResultCode())){
                    return ChannelRetMsg.confirmSuccess(entPayResult.getPaymentNo());
                }else{
                    return ChannelRetMsg.waiting();
                }
            } else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {
                TransferBatchesRequest request = new TransferBatchesRequest();
                request.setAppid(wxServiceWrapper.getWxPayService().getConfig().getAppId());
                request.setTotalAmount(transferOrder.getAmount().intValue());
                request.setTotalNum(1);
                request.setOutBatchNo(transferOrder.getTransferId());
                request.setBatchName(transferOrder.getAccountName());
                request.setBatchRemark(transferOrder.getTransferDesc());

                List<TransferBatchesRequest.TransferDetail> list = new ArrayList<>();
                TransferBatchesRequest.TransferDetail transferDetail = new TransferBatchesRequest.TransferDetail();
                transferDetail.setOutDetailNo(transferOrder.getTransferId());
                transferDetail.setOpenid(transferOrder.getAccountNo());
                transferDetail.setTransferAmount(transferOrder.getAmount().intValue()); //付款金额，单位为分
                transferDetail.setUserName(transferOrder.getAccountName());
                transferDetail.setTransferRemark(transferOrder.getTransferDesc());
                request.setTransferDetailList(list);

                TransferBatchesResult transferBatchesResult = wxServiceWrapper.getWxPayService().getTransferService().transferBatches(request);
                return ChannelRetMsg.confirmSuccess(transferBatchesResult.getBatchId());
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

}
