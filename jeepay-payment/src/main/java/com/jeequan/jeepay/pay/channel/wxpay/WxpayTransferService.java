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
import com.github.binarywang.wxpay.exception.WxPayException;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.pay.channel.ITransferService;
import com.jeequan.jeepay.pay.channel.wxpay.kits.WxpayKit;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.transfer.TransferOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* 转账接口： 微信官方
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/8/11 14:05
*/
@Slf4j
@Service
public class WxpayTransferService implements ITransferService {

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
        return null;
    }

    @Override
    public ChannelRetMsg transfer(TransferOrderRQ bizRQ, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext){

        try {

            WxpayNormalMchParams params = mchAppConfigContext.getNormalMchParamsByIfCode(getIfCode(), WxpayNormalMchParams.class);

            EntPayRequest request = new EntPayRequest();
            request.setMchAppid(params.getAppId());  // 商户账号appid
            request.setMchId(params.getMchId());  //商户号

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

            EntPayResult entPayResult = mchAppConfigContext.getWxServiceWrapper().getWxPayService().getEntPayService().entPay(request);

            // SUCCESS/FAIL，注意：当状态为FAIL时，存在业务结果未明确的情况。如果状态为FAIL，请务必关注错误代码（err_code字段），通过查询接口确认此次付款的结果。
            if("SUCCESS".equalsIgnoreCase(entPayResult.getResultCode())){
                return ChannelRetMsg.confirmSuccess(entPayResult.getPaymentNo());
            }else{
                return ChannelRetMsg.waiting();
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
