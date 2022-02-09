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
package com.jeequan.jeepay.pay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jeequan.jeepay.components.mq.model.PayOrderDivisionMQ;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchDivisionReceiver;
import com.jeequan.jeepay.core.entity.MchDivisionReceiverGroup;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IDivisionService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.MchDivisionReceiverGroupService;
import com.jeequan.jeepay.service.impl.MchDivisionReceiverService;
import com.jeequan.jeepay.service.impl.PayOrderDivisionRecordService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 业务： 支付订单分账处理逻辑
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/8/27 9:43
 */
@Slf4j
@Component
public class PayOrderDivisionProcessService {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private MchDivisionReceiverService mchDivisionReceiverService;
    @Autowired
    private MchDivisionReceiverGroupService mchDivisionReceiverGroupService;
    @Autowired
    private PayOrderDivisionRecordService payOrderDivisionRecordService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;

    /***
    * 处理分账，
     * 1. 向外抛异常： 系统检查没有通过 / 系统级别异常
     * 2 若正常调起接口将返回渠道侧响应结果
    *
    * @author terrfly
    * @site https://www.jeequan.com
    * @date 2021/8/27 9:44
    */
    public ChannelRetMsg processPayOrderDivision(String payOrderId, Byte useSysAutoDivisionReceivers, List<PayOrderDivisionMQ.CustomerDivisionReceiver> receiverList, Boolean isResend) {

        // 是否重发分账接口（ 当分账失败， 列表允许再次发送请求 ）
        if(isResend == null){
            isResend = false;
        }


        String logPrefix = "订单["+payOrderId+"]执行分账";

        //查询订单信息
        PayOrder payOrder = payOrderService.getById(payOrderId);

        if(payOrder == null){
            log.error("{}，订单不存在", logPrefix);
            throw new BizException("订单不存在");
        }

        // 分账状态不正确
        if(payOrder.getDivisionState() != PayOrder.DIVISION_STATE_WAIT_TASK && payOrder.getDivisionState() != PayOrder.DIVISION_STATE_UNHAPPEN){
            log.error("{}, 分账状态不正确", logPrefix);
            throw new BizException("分账状态不正确");
        }

        //更新订单为： 分账任务处理中
        boolean updPayOrder = payOrderService.update(new LambdaUpdateWrapper<PayOrder>()
                .set(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_ING)
                .eq(PayOrder::getPayOrderId, payOrderId)
                .eq(PayOrder::getDivisionState, payOrder.getDivisionState()));
        if(!updPayOrder){
            log.error("{}, 更新支付订单为分账处理中异常！", logPrefix);
            throw new BizException("更新支付订单为分账处理中异常");
        }


        // 所有的分账列表
        List<PayOrderDivisionRecord> recordList = null;

        // 重发通知，可直接查库
        if(isResend){
            recordList = payOrderDivisionRecordService.list(PayOrderDivisionRecord.gw().eq(PayOrderDivisionRecord::getPayOrderId, payOrderId));
        }else{

            // 查询&过滤 所有的分账接收对象
            List<MchDivisionReceiver> allReceiver = this.queryReceiver(useSysAutoDivisionReceivers, payOrder, receiverList);

            //得到全部分账比例 (所有待分账账号的分账比例总和)
            BigDecimal allDivisionProfit = BigDecimal.ZERO;
            for (MchDivisionReceiver receiver : allReceiver) {
                allDivisionProfit = allDivisionProfit.add(receiver.getDivisionProfit());
            }

            //计算分账金额 = 商家实际入账金额
            Long payOrderDivisionAmount = payOrderService.calMchIncomeAmount(payOrder);

            //剩余待分账金额 (用作最后一个分账账号的 计算， 避免出现分账金额超出最大) [结果向下取整 ， 避免出现金额溢出的情况。 ]
            Long subDivisionAmount = AmountUtil.calPercentageFee(payOrderDivisionAmount, allDivisionProfit, BigDecimal.ROUND_FLOOR);

            recordList = new ArrayList<>();

            //计算订单分账金额, 并插入到记录表

            String batchOrderId = SeqKit.genDivisionBatchId();

            for (MchDivisionReceiver receiver : allReceiver) {

                PayOrderDivisionRecord record = genRecord(batchOrderId, payOrder, receiver, payOrderDivisionAmount, subDivisionAmount);

                //剩余金额
                subDivisionAmount = subDivisionAmount - record.getCalDivisionAmount();

                //入库保存
                payOrderDivisionRecordService.save(record);
                recordList.add(record);
            }
        }


        ChannelRetMsg channelRetMsg = null;

        try{

            //调用渠道侧分账接口
            IDivisionService divisionService = SpringBeansUtil.getBean(payOrder.getIfCode() + "DivisionService", IDivisionService.class);
            if(divisionService == null){
                throw new BizException("通道无此分账接口");
            }

            channelRetMsg = divisionService.singleDivision(payOrder, recordList, configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId()));

            // 确认分账成功
            if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {

                //分账成功
                payOrderDivisionRecordService.updateRecordSuccessOrFail(recordList, PayOrderDivisionRecord.STATE_SUCCESS,
                        channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelOriginResponse());

            }else{
                //分账失败
                payOrderDivisionRecordService.updateRecordSuccessOrFail(recordList, PayOrderDivisionRecord.STATE_FAIL,
                        channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrMsg());
            }

        } catch (Exception e) {
            log.error("{}, 调用分账接口异常", logPrefix, e);
            payOrderDivisionRecordService.updateRecordSuccessOrFail(recordList, PayOrderDivisionRecord.STATE_FAIL,
                    null, "系统异常：" + e.getMessage());

            channelRetMsg = ChannelRetMsg.confirmFail(null, null, e.getMessage());
        }

        //更新 支付订单主表状态  分账任务已结束。
        payOrderService.update(new LambdaUpdateWrapper<PayOrder>()
                .set(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_FINISH)
                .set(PayOrder::getDivisionLastTime, new Date())
                .eq(PayOrder::getPayOrderId, payOrderId)
                .eq(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_ING)
        );

        return channelRetMsg;
    }


    /** 生成对象信息 **/
    private PayOrderDivisionRecord genRecord(String batchOrderId, PayOrder payOrder, MchDivisionReceiver receiver,
                                             Long payOrderDivisionAmount, Long subDivisionAmount){

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
        record.setPayOrderDivisionAmount(payOrderDivisionAmount); // 订单计算分账金额
        record.setBatchOrderId(batchOrderId); //系统分账批次号
        record.setState(PayOrderDivisionRecord.STATE_WAIT); //状态: 待分账
        record.setReceiverId(receiver.getReceiverId());
        record.setReceiverGroupId(receiver.getReceiverGroupId());
        record.setReceiverAlias(receiver.getReceiverAlias());
        record.setAccType(receiver.getAccType());
        record.setAccNo(receiver.getAccNo());
        record.setAccName(receiver.getAccName());
        record.setRelationType(receiver.getRelationType());
        record.setRelationTypeName(receiver.getRelationTypeName());
        record.setDivisionProfit(receiver.getDivisionProfit());

        if( subDivisionAmount <= 0 ) {
            record.setCalDivisionAmount(0L);
        }else{

            //计算的分账金额
            record.setCalDivisionAmount(AmountUtil.calPercentageFee(record.getPayOrderDivisionAmount(), record.getDivisionProfit()));
            if(record.getCalDivisionAmount() > subDivisionAmount){ // 分账金额超过剩余总金额时： 将按照剩余金额进行分账。
                record.setCalDivisionAmount(subDivisionAmount);
            }
        }

        return record;
    }


    private List<MchDivisionReceiver> queryReceiver(Byte useSysAutoDivisionReceivers, PayOrder payOrder, List<PayOrderDivisionMQ.CustomerDivisionReceiver> customerDivisionReceiverList){

        // 查询全部分账列表
        LambdaQueryWrapper<MchDivisionReceiver> queryWrapper = MchDivisionReceiver.gw();

        queryWrapper.eq(MchDivisionReceiver::getMchNo, payOrder.getMchNo()); //mchNo
        queryWrapper.eq(MchDivisionReceiver::getAppId, payOrder.getAppId()); //appId
        queryWrapper.eq(MchDivisionReceiver::getIfCode, payOrder.getIfCode()); //ifCode
        queryWrapper.eq(MchDivisionReceiver::getState, CS.PUB_USABLE); // 可用状态的账号

        // 自动分账组的账号
        if(useSysAutoDivisionReceivers == CS.YES) {

            List<MchDivisionReceiverGroup> groups = mchDivisionReceiverGroupService.list(
                    MchDivisionReceiverGroup.gw().eq(MchDivisionReceiverGroup::getMchNo, payOrder.getMchNo())
                            .eq(MchDivisionReceiverGroup::getAutoDivisionFlag, CS.YES));

            if(groups.isEmpty()){
                return new ArrayList<>();
            }

            queryWrapper.eq(MchDivisionReceiver::getReceiverGroupId, groups.get(0).getReceiverGroupId());
        }

        //全部分账账号
        List<MchDivisionReceiver> allMchReceiver = mchDivisionReceiverService.list(queryWrapper);
        if(allMchReceiver.isEmpty()){
            return allMchReceiver;
        }

        //自动分账组
        if(useSysAutoDivisionReceivers == CS.YES){
            return allMchReceiver;
        }

        //以下为 自定义列表

        // 自定义列表未定义
        if(customerDivisionReceiverList == null || customerDivisionReceiverList.isEmpty()){
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
