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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingReceiverRequest;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingReceiverResult;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingRequest;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchDivisionReceiver;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.pay.channel.IDivisionService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* 分账接口： 微信官方
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/8/22 09:05
*/
@Slf4j
@Service
public class WxpayDivisionService implements IDivisionService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public boolean isSupport() {
        return false;
    }

    @Override
    public boolean bind(MchDivisionReceiver mchDivisionReceiver, MchAppConfigContext mchAppConfigContext) {

        try {

            ProfitSharingReceiverRequest request = new ProfitSharingReceiverRequest();

            JSONObject receiverJSON = new JSONObject();

            // 0-个人， 1-商户  (目前仅支持服务商appI获取个人openId, 即： PERSONAL_OPENID， 不支持 PERSONAL_SUB_OPENID )
            receiverJSON.put("type", mchDivisionReceiver.getAccType() == 0 ? "PERSONAL_OPENID" : "MERCHANT_ID");
            receiverJSON.put("account", mchDivisionReceiver.getAccNo());
            receiverJSON.put("name", mchDivisionReceiver.getAccName());
            receiverJSON.put("relation_type", mchDivisionReceiver.getRelationType());
            receiverJSON.put("custom_relation", mchDivisionReceiver.getRelationTypeName());
            request.setReceiver(receiverJSON.toJSONString());

            ProfitSharingReceiverResult profitSharingReceiverResult =
                    mchAppConfigContext.getWxServiceWrapper().getWxPayService().getProfitSharingService().addReceiver(request);

        } catch (WxPayException wxPayException) {
            wxPayException.printStackTrace();
        }


        return false;
    }

    @Override
    public boolean singleDivision(List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext) {

        try {

            if(true || recordList.isEmpty()){
                return true;
            }

            ProfitSharingRequest request = new ProfitSharingRequest();
            request.setTransactionId(recordList.get(0).getPayOrderChannelOrderNo());
            request.setOutOrderNo(recordList.get(0).getBatchOrderId());

            JSONArray receiverJSONArray = new JSONArray();

            for (PayOrderDivisionRecord record : recordList) {
                JSONObject receiverJSON = new JSONObject();
                // 0-个人， 1-商户  (目前仅支持服务商appI获取个人openId, 即： PERSONAL_OPENID， 不支持 PERSONAL_SUB_OPENID )
                receiverJSON.put("type", record.getAccType() == 0 ? "PERSONAL_OPENID" : "MERCHANT_ID");
                receiverJSON.put("account", record.getAccNo());
                receiverJSON.put("amount", record.getCalDivisionAmount());
                receiverJSON.put("description", record.getPayOrderId() + "分账");
                receiverJSONArray.add(receiverJSON);
            }

            request.setReceivers(receiverJSONArray.toJSONString());

            ProfitSharingResult profitSharingResult = mchAppConfigContext.getWxServiceWrapper().getWxPayService().getProfitSharingService().profitSharing(request);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }
}
