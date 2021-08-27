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

import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.ITransferService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.transfer.TransferOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* 转账接口： 支付宝官方
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/11 14:05
*/
@Slf4j
@Service
public class AlipayTransferService implements ITransferService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public boolean isSupport(String entryType) {

        // 支付宝账户
        if(TransferOrder.ENTRY_ALIPAY_CASH.equals(entryType)){
            return true;
        }

        return false;
    }

    @Override
    public String preCheck(TransferOrderRQ bizRQ, TransferOrder transferOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg transfer(TransferOrderRQ bizRQ, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext){

        AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
        AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
        model.setAmount(AmountUtil.convertCent2Dollar(transferOrder.getAmount())); //转账金额，单位：元。
        model.setOutBizNo(transferOrder.getTransferId()); //商户转账唯一订单号
        model.setPayeeType("ALIPAY_LOGONID");  //ALIPAY_USERID： 支付宝用户ID      ALIPAY_LOGONID:支付宝登录账号
        model.setPayeeAccount(transferOrder.getAccountNo()); //收款方账户
        model.setPayeeRealName(StringUtils.defaultString(transferOrder.getAccountName(), null)); //收款方真实姓名
        model.setRemark(transferOrder.getTransferDesc()); //转账备注
        request.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

        // 调起支付宝接口
        AlipayFundTransToaccountTransferResponse response = mchAppConfigContext.getAlipayClientWrapper().execute(request);

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        channelRetMsg.setChannelAttach(response.getBody());

        // 调用成功
        if(response.isSuccess()){
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            channelRetMsg.setChannelOrderId(response.getOrderId());
        }else{

            //若 系统繁忙， 无法确认结果
            if("SYSTEM_ERROR".equalsIgnoreCase(response.getSubCode())){
                return ChannelRetMsg.waiting();
            }

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode(response.getSubCode());
            channelRetMsg.setChannelErrMsg(response.getSubMsg());
        }

        return channelRetMsg;
    }

}
