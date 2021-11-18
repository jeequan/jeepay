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

import com.alibaba.fastjson.JSON;
import com.alipay.api.domain.*;
import com.alipay.api.request.AlipayTradeOrderSettleRequest;
import com.alipay.api.request.AlipayTradeRoyaltyRelationBindRequest;
import com.alipay.api.response.AlipayTradeOrderSettleResponse;
import com.alipay.api.response.AlipayTradeRoyaltyRelationBindResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchDivisionReceiver;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.RegKit;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.pay.channel.IDivisionService;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* 分账接口： 支付宝官方
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/22 09:05
*/
@Slf4j
@Service
public class AlipayDivisionService implements IDivisionService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public boolean isSupport() {
        return false;
    }

    @Override
    public ChannelRetMsg bind(MchDivisionReceiver mchDivisionReceiver, MchAppConfigContext mchAppConfigContext) {

        try {
            AlipayTradeRoyaltyRelationBindRequest request = new AlipayTradeRoyaltyRelationBindRequest();
            AlipayTradeRoyaltyRelationBindModel model = new AlipayTradeRoyaltyRelationBindModel();
            request.setBizModel(model);
            model.setOutRequestNo(SeqKit.genDivisionBatchId());

            //统一放置 isv接口必传信息
            AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

            RoyaltyEntity royaltyEntity = new RoyaltyEntity();

            royaltyEntity.setType("loginName");
            if(RegKit.isAlipayUserId(mchDivisionReceiver.getAccNo())){
                royaltyEntity.setType("userId");
            }
            royaltyEntity.setAccount(mchDivisionReceiver.getAccNo());
            royaltyEntity.setName(mchDivisionReceiver.getAccName());
            royaltyEntity.setMemo(mchDivisionReceiver.getRelationTypeName()); //分账关系描述
            model.setReceiverList(Arrays.asList(royaltyEntity));

            AlipayTradeRoyaltyRelationBindResponse alipayResp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);

            if(alipayResp.isSuccess()){
                return ChannelRetMsg.confirmSuccess(null);
            }

            //异常：
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            channelRetMsg.setChannelErrCode(AlipayKit.appendErrCode(alipayResp.getCode(), alipayResp.getSubCode()));
            channelRetMsg.setChannelErrMsg(AlipayKit.appendErrMsg(alipayResp.getMsg(), alipayResp.getSubMsg()));
            return channelRetMsg;

        } catch (ChannelException e) {

            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            channelRetMsg.setChannelErrCode(e.getChannelRetMsg().getChannelErrCode());
            channelRetMsg.setChannelErrMsg(e.getChannelRetMsg().getChannelErrMsg());
            return channelRetMsg;

        } catch (Exception e) {
            log.error("绑定支付宝账号异常", e);
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            channelRetMsg.setChannelErrMsg(e.getMessage());
            return channelRetMsg;
        }
    }

    @Override
    public ChannelRetMsg singleDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext) {


        try {

            if(recordList.isEmpty()){ // 当无分账用户时， 支付宝不允许发起分账请求， 支付宝没有完结接口，直接响应成功即可。
                return ChannelRetMsg.confirmSuccess(null);
            }

            AlipayTradeOrderSettleRequest request = new AlipayTradeOrderSettleRequest();
            AlipayTradeOrderSettleModel model = new AlipayTradeOrderSettleModel();
            request.setBizModel(model);

            model.setOutRequestNo(recordList.get(0).getBatchOrderId()); //结算请求流水号，由商家自定义。32个字符以内，仅可包含字母、数字、下划线。需保证在商户端不重复。
            model.setTradeNo(recordList.get(0).getPayOrderChannelOrderNo()); //支付宝订单号

            //统一放置 isv接口必传信息
            AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

            List<OpenApiRoyaltyDetailInfoPojo> reqReceiverList = new ArrayList<>();

            for (int i = 0; i < recordList.size(); i++) {

                PayOrderDivisionRecord record = recordList.get(i);

                if(record.getCalDivisionAmount() <= 0){ //金额为 0 不参与分账处理
                    continue;
                }

                OpenApiRoyaltyDetailInfoPojo reqReceiver = new OpenApiRoyaltyDetailInfoPojo();
                reqReceiver.setRoyaltyType("transfer"); //分账类型： 普通分账

                // 出款信息
                // reqReceiver.setTransOutType("loginName"); reqReceiver.setTransOut("xqxemt4735@sandbox.com");

                // 入款信息
                reqReceiver.setTransIn(record.getAccNo()); //收入方账号
                reqReceiver.setTransInType("loginName");
                if(RegKit.isAlipayUserId(record.getAccNo())){
                    reqReceiver.setTransInType("userId");
                }
                // 分账金额
                reqReceiver.setAmount(AmountUtil.convertCent2Dollar(record.getCalDivisionAmount()));
                reqReceiver.setDesc("[" + payOrder.getPayOrderId() + "]订单分账");
                reqReceiverList.add(reqReceiver);

            }

            if(reqReceiverList.isEmpty()){ // 当无分账用户时， 支付宝不允许发起分账请求， 支付宝没有完结接口，直接响应成功即可。
                return ChannelRetMsg.confirmSuccess(null);
            }

            model.setRoyaltyParameters(reqReceiverList); // 分账明细信息

            // 完结
            SettleExtendParams settleExtendParams = new SettleExtendParams();
            settleExtendParams.setRoyaltyFinish("true");
            model.setExtendParams(settleExtendParams);

            //调起支付宝分账接口
            if(log.isInfoEnabled()){
                log.info("订单：[{}], 支付宝分账请求：{}", payOrder.getPayOrderId(), JSON.toJSONString(model));
            }
            AlipayTradeOrderSettleResponse alipayResp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);
            log.info("订单：[{}], 支付宝分账响应：{}", payOrder.getPayOrderId(), alipayResp.getBody());
            if(alipayResp.isSuccess()){
                return ChannelRetMsg.confirmSuccess(alipayResp.getTradeNo());
            }

            //异常：
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            channelRetMsg.setChannelErrCode(AlipayKit.appendErrCode(alipayResp.getCode(), alipayResp.getSubCode()));
            channelRetMsg.setChannelErrMsg(AlipayKit.appendErrMsg(alipayResp.getMsg(), alipayResp.getSubMsg()));
            return channelRetMsg;

        } catch (ChannelException e) {

            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            channelRetMsg.setChannelErrCode(e.getChannelRetMsg().getChannelErrCode());
            channelRetMsg.setChannelErrMsg(e.getChannelRetMsg().getChannelErrMsg());
            return channelRetMsg;

        } catch (Exception e) {
            log.error("绑定支付宝账号异常", e);
            ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmFail();
            channelRetMsg.setChannelErrMsg(e.getMessage());
            return channelRetMsg;
        }
    }

}
