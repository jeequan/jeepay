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
package com.jeequan.jeepay.pay.mq;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jeequan.jeepay.components.mq.model.PayOrderDivisionMQ;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchDivisionReceiver;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IDivisionService;
import com.jeequan.jeepay.pay.channel.ITransferService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.service.impl.MchDivisionReceiverService;
import com.jeequan.jeepay.service.impl.PayOrderDivisionRecordService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 接收MQ消息
 * 业务： 支付订单分账处理逻辑
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/8/22 8:23
 */
@Slf4j
@Component
public class PayOrderDivisionMQReceiver implements PayOrderDivisionMQ.IMQReceiver {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private MchDivisionReceiverService mchDivisionReceiverService;
    @Autowired
    private PayOrderDivisionRecordService payOrderDivisionRecordService;
    @Autowired
    private ConfigContextService configContextService;

    @Override
    public void receive(PayOrderDivisionMQ.MsgPayload payload) {

        try {

            log.info("接收订单分账通知MQ, msg={}", payload.toString());

            String logPrefix = "订单["+payload.getPayOrderId()+"]执行分账";

            //查询订单信息
            PayOrder payOrder = payOrderService.getById(payload.getPayOrderId());

            if(payOrder == null){
                log.error("{}，订单不存在", logPrefix);
                return ;
            }

            if(payOrder.getState() != PayOrder.STATE_SUCCESS || payOrder.getDivisionState() != PayOrder.DIVISION_STATE_WAIT_TASK){
                log.error("{}, 订单状态或分账状态不正确", logPrefix);
                return ;
            }

            //更新订单为： 分账任务处理中
            boolean updPayOrder = payOrderService.update(new LambdaUpdateWrapper<PayOrder>()
                    .set(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_ING)
                    .eq(PayOrder::getPayOrderId, payload.getPayOrderId())
                    .eq(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_WAIT_TASK));
            if(!updPayOrder){
                log.error("{}, 更新支付订单为分账处理中异常！", logPrefix);
                return ;
            }

            // 查询所有的分账接收对象
            List<MchDivisionReceiver> allReceiver = queryReceiver(payOrder, payload.getReceiverList());

            //得到全部分账比例 (所有待分账账号的分账比例总和)
            BigDecimal allDivisionProfit = BigDecimal.ZERO;
            for (MchDivisionReceiver receiver : allReceiver) {
                allDivisionProfit = allDivisionProfit.add(receiver.getDivisionProfit());
            }

            //剩余待分账金额 (用作最后一个分账账号的 计算， 避免出现分账金额超出最大)
            Long subDivisionAmount = AmountUtil.calPercentageFee(payOrder.getMchIncomeAmount(), allDivisionProfit);

            List<PayOrderDivisionRecord> recordList = new ArrayList<>();

            //计算订单分账金额, 并插入到记录表

            String batchOrderId = SeqKit.genDivisionBatchId();

            for (MchDivisionReceiver receiver : allReceiver) {

                PayOrderDivisionRecord record = genRecord(batchOrderId, payOrder, receiver, subDivisionAmount);

                //剩余金额
                subDivisionAmount = subDivisionAmount - record.getCalDivisionAmount();

                //入库保存
                payOrderDivisionRecordService.save(record);
                recordList.add(record);
            }

            try{

                //调用渠道侧分账接口

                IDivisionService divisionService = SpringBeansUtil.getBean(payOrder.getIfCode() + "DivisionService", IDivisionService.class);
                if(divisionService == null){
                    throw new BizException("通道无此分账接口");
                }

                divisionService.singleDivision(recordList, configContextService.getMchAppConfigContext(payOrder.getMchNo(), payOrder.getAppId()));

                if(true) {

                    //分账成功

                }else{
                    //分账失败
                }

            } catch (BizException e) {
                log.error("{}, 调用分账接口异常, {}", logPrefix, e.getMessage());

            } catch (Exception e) {
                log.error("{}, 调用分账接口异常", logPrefix, e);

                //分账失败

            }

            //更新 支付订单主表状态  分账任务已结束。
            payOrderService.update(new LambdaUpdateWrapper<PayOrder>()
                    .set(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_FINISH)
                    .eq(PayOrder::getPayOrderId, payload.getPayOrderId())
                    .eq(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_ING)
            );

        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /** 生成对象信息 **/
    private PayOrderDivisionRecord genRecord(String batchOrderId, PayOrder payOrder, MchDivisionReceiver receiver, Long subDivisionAmount){

        PayOrderDivisionRecord record = new PayOrderDivisionRecord();
        record.setMchNo(payOrder.getMchNo());
        record.setIsvNo(payOrder.getIsvNo());
        record.setAppId(payOrder.getAppId());
        record.setMchName(payOrder.getMchName());
        record.setMchType(payOrder.getMchType());
        record.setIfCode(payOrder.getIfCode());
        record.setPayOrderId(payOrder.getPayOrderId());
        record.setPayOrderChannelOrderNo(payOrder.getChannelOrderNo()); //支付订单渠道订单号
        record.setPayOrderAmount(payOrder.getAmount()); //订单金额
        record.setPayOrderDivisionAmount(payOrder.getMchIncomeAmount()); // 订单实际分账金额, 单位：分（订单金额 - 商户手续费 - 已退款金额）  //TODO 实际计算金额
        record.setBatchOrderId(batchOrderId); //系统分账批次号
        record.setState(MchDivisionReceiver.STATE_WAIT); //状态: 待分账
        record.setReceiverId(receiver.getReceiverId());
        record.setReceiverGroupId(receiver.getReceiverGroupId());
        record.setAccType(receiver.getAccType());
        record.setAccNo(receiver.getAccNo());
        record.setAccName(receiver.getAccName());
        record.setRelationType(receiver.getRelationType());
        record.setRelationTypeName(receiver.getRelationTypeName());
        record.setDivisionProfit(receiver.getDivisionProfit());

        if( subDivisionAmount <= 0 ) {
            record.setCalDivisionAmount(0L);
        }else{
            record.setCalDivisionAmount(AmountUtil.calPercentageFee(record.getPayOrderDivisionAmount(), record.getDivisionProfit()));
        }

        return record;
    }


    private List<MchDivisionReceiver> queryReceiver(PayOrder payOrder, List<PayOrderDivisionMQ.CustomerDivisionReceiver> customerDivisionReceiverList){

        // 查询全部分账列表
        LambdaQueryWrapper<MchDivisionReceiver> queryWrapper = MchDivisionReceiver.gw();

        queryWrapper.eq(MchDivisionReceiver::getMchNo, payOrder.getMchNo()); //mchNo
        queryWrapper.eq(MchDivisionReceiver::getAppId, payOrder.getAppId()); //appId
        queryWrapper.eq(MchDivisionReceiver::getIfCode, payOrder.getIfCode()); //ifCode
        queryWrapper.eq(MchDivisionReceiver::getState, CS.PUB_USABLE); // 可用状态的账号

        //全部分账账号
        List<MchDivisionReceiver> allMchReceiver = mchDivisionReceiverService.list(queryWrapper);
        if(allMchReceiver.isEmpty()){
            return allMchReceiver;
        }

        // 自定义列表未定义
        if(customerDivisionReceiverList == null){
            return allMchReceiver;
        }

        //参数有定义，但是没有任何值
        if(customerDivisionReceiverList.isEmpty()){
            return new ArrayList<>();
        }


        // 过滤账号
        List<MchDivisionReceiver> filterMchReceiver = new ArrayList<>();

        for (MchDivisionReceiver mchDivisionReceiver : allMchReceiver) {
            for (PayOrderDivisionMQ.CustomerDivisionReceiver customerDivisionReceiver : customerDivisionReceiverList) {

                // 查询匹配相同的项目
                if( mchDivisionReceiver.getReceiverId().equals(customerDivisionReceiver.getReceiverId()) ||
                    mchDivisionReceiver.getReceiverGroupId().equals(customerDivisionReceiver.getReceiverGroupId())
                ){

                    // 重新对分账比例赋值
                    if(customerDivisionReceiver.getDivisionProfit() != null){
                        mchDivisionReceiver.setDivisionProfit(customerDivisionReceiver.getDivisionProfit());
                    }
                    filterMchReceiver.add(mchDivisionReceiver);
                }
            }
        }

        return filterMchReceiver;
    }

}
