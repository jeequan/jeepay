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
package com.jeequan.jeepay.pay.mq.receive;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.mq.MqCommonService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ChannelOrderReissueService;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 处理公共接收消息方法
 *
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
@Slf4j
@Service
public class MqReceiveCommon {

    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    private ConfigContextService configContextService;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;
    @Autowired
    private MchNotifyRecordService mchNotifyRecordService;
    @Autowired
    private MqCommonService mqCommonService;

    /** 接收 [商户配置信息] 的消息 **/
    public void modifyMchInfo(String mchNo) {
        log.info("成功接收 [商户配置信息] 的消息, msg={}", mchNo);
        configContextService.initMchInfoConfigContext(mchNo);
        log.info(" [商户配置信息] 已重置");
    }

    /** 接收 [商户应用支付参数配置信息] 的消息 **/
    public void modifyMchApp(String mchNoAndAppId) {
        log.info("成功接收 [商户应用支付参数配置信息] 的消息, msg={}", mchNoAndAppId);
        JSONObject jsonObject = (JSONObject) JSONObject.parse(mchNoAndAppId);
        configContextService.initMchAppConfigContext(jsonObject.getString("mchNo"), jsonObject.getString("appId"));
        log.info(" [商户应用支付参数配置信息] 已重置");
    }

    /** 重置ISV信息 **/
    public void modifyIsvInfo(String isvNo) {
        log.info("成功接收 [ISV信息] 重置, msg={}", isvNo);
        configContextService.initIsvConfigContext(isvNo);
        log.info("[ISV信息] 已重置");
    }

    /** 接收商户订单回调通知 **/
    public void payOrderMchNotify(String msg) {
        try {
            log.info("接收商户通知MQ, msg={}", msg);
            Long notifyId = Long.parseLong(msg);
            MchNotifyRecord record = mchNotifyRecordService.getById(notifyId);
            if(record == null || record.getState() != MchNotifyRecord.STATE_ING){
                log.info("查询通知记录不存在或状态不是通知中");
                return;
            }
            if( record.getNotifyCount() >= record.getNotifyCountLimit() ){
                log.info("已达到最大发送次数");
                return;
            }

            //1. (发送结果最多6次)
            Integer currentCount = record.getNotifyCount() + 1;

            String notifyUrl = record.getNotifyUrl();
            String res = "";
            try {
                res = HttpUtil.createPost(notifyUrl).timeout(20000).execute().body();
            } catch (HttpException e) {
                log.error("http error", e);
            }

            if(currentCount == 1){ //第一次通知: 更新为已通知
                payOrderService.updateNotifySent(record.getOrderId());
            }

            //通知成功
            if("SUCCESS".equalsIgnoreCase(res)){
                mchNotifyRecordService.updateNotifyResult(notifyId, MchNotifyRecord.STATE_SUCCESS, res);
                return;
            }

            //通知次数 >= 最大通知次数时， 更新响应结果为异常， 不在继续延迟发送消息
            if( currentCount >= record.getNotifyCountLimit() ){
                mchNotifyRecordService.updateNotifyResult(notifyId, MchNotifyRecord.STATE_FAIL, res);
                return;
            }

            // 继续发送MQ 延迟发送
            mchNotifyRecordService.updateNotifyResult(notifyId, MchNotifyRecord.STATE_ING, res);
            // 通知延时次数
    //        1   2  3  4   5   6
    //        0  30 60 90 120 150
            mqCommonService.send(msg, currentCount * 30 * 1000, CS.MQ.MQ_TYPE_PAY_ORDER_MCH_NOTIFY);
            return;
        }catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
    }

    /** 接收订单查单通知 **/
    public void channelOrderQuery(String msg) {
        try {
            String [] arr = msg.split(",");
            String payOrderId = arr[0];
            int currentCount = Integer.parseInt(arr[1]);
            log.info("接收轮询查单通知MQ, payOrderId={}, count={}", payOrderId, currentCount);
            currentCount++ ;

            PayOrder payOrder = payOrderService.getById(payOrderId);
            if(payOrder == null) {
                log.warn("查询支付订单为空,payOrderId={}", payOrderId);
                return;
            }

            if(payOrder.getState() != PayOrder.STATE_ING) {
                log.warn("订单状态不是支付中,不需查询渠道.payOrderId={}", payOrderId);
                return;
            }

            if (payOrder == null) return;
            ChannelRetMsg channelRetMsg = channelOrderReissueService.processPayOrder(payOrder);

            //返回null 可能为接口报错等， 需要再次轮询
            if(channelRetMsg == null || channelRetMsg.getChannelState() == null || channelRetMsg.getChannelState().equals(ChannelRetMsg.ChannelState.WAITING)){

                //最多查询6次
                if(currentCount <= 6){
                    mqCommonService.send(buildMsg(payOrderId, currentCount), 5 * 1000, CS.MQ.MQ_TYPE_CHANNEL_ORDER_QUERY); //延迟5s再次查询
                }else{

                    //TODO 调用【撤销订单】接口

                }

            }else{ //其他状态， 不需要再次轮询。
            }
        }catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
    }
    /** 接收系统配置修改通知 **/
    public void initDbConfig(String msg) {
        log.info("成功接收更新系统配置的订阅通知, msg={}", msg);
        sysConfigService.initDBConfig(msg);
        log.info("系统配置静态属性已重置");
    }

    public static final String buildMsg(String payOrderId, int count){
        return payOrderId + "," + count;
    }
}
