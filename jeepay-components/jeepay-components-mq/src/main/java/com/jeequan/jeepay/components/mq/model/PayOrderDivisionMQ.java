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
package com.jeequan.jeepay.components.mq.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
*
* 定义MQ消息格式
* 业务场景： [ 支付订单的订单分账消息 ]
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/22 11:25
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderDivisionMQ extends AbstractMQ {

    /** 【！重要配置项！】 定义MQ名称 **/
    public static final String MQ_NAME = "QUEUE_PAY_ORDER_DIVISION";

    /** 内置msg 消息体定义 **/
    private MsgPayload payload;

    /**  【！重要配置项！】 定义Msg消息载体 **/
    @Data
    @AllArgsConstructor
    public static class MsgPayload {

        /** 支付订单号 **/
        private String payOrderId;

        /** 是否使用默认分组 **/
        private Byte useSysAutoDivisionReceivers;

        /**
         * 分账接受者列表， 字段值为空表示系统默认配置项。
         * 格式：{receiverId: '1001', receiverGroupId: '1001', divisionProfit: '0.1'}
         * divisionProfit: 空表示使用系统默认比例。
         * **/
        private List<CustomerDivisionReceiver> receiverList;

        /** 是否重新发送 ( 如分账失败，重新请求分账接口 ) ， 空表示false **/
        private Boolean isResend;

    }

    @Override
    public String getMQName() {
        return MQ_NAME;
    }

    /**  【！重要配置项！】 **/
    @Override
    public MQSendTypeEnum getMQType(){
        return MQSendTypeEnum.QUEUE;  // QUEUE - 点对点 、 BROADCAST - 广播模式
    }

    @Override
    public String toMessage() {
        return JSONObject.toJSONString(payload);
    }

    /**  【！重要配置项！】 构造MQModel , 一般用于发送MQ时 **/
    public static PayOrderDivisionMQ build(String payOrderId, Byte useSysAutoDivisionReceivers, List<CustomerDivisionReceiver> receiverList){
        return new PayOrderDivisionMQ(new MsgPayload(payOrderId, useSysAutoDivisionReceivers, receiverList, false));
    }

    /**  【！重要配置项！】 构造MQModel , 一般用于发送MQ时 **/
    public static PayOrderDivisionMQ build(String payOrderId, Byte useSysAutoDivisionReceivers, List<CustomerDivisionReceiver> receiverList, Boolean isResend){
        return new PayOrderDivisionMQ(new MsgPayload(payOrderId, useSysAutoDivisionReceivers, receiverList, isResend));
    }

    /** 解析MQ消息， 一般用于接收MQ消息时 **/
    public static MsgPayload parse(String msg){
        return JSON.parseObject(msg, MsgPayload.class);
    }

    /** 定义 IMQReceiver 接口： 项目实现该接口则可接收到对应的业务消息  **/
    public interface IMQReceiver{
        void receive(MsgPayload payload);
    }




    /**  自定义定义接收账号定义信息 **/
    @Data
    @AllArgsConstructor
    public static class CustomerDivisionReceiver {

        /**
         * 分账接收者ID (与receiverGroupId 二选一)
         */
        private Long receiverId;

        /**
         * 组ID（便于商户接口使用） (与 receiverId 二选一)
         */
        private Long receiverGroupId;

        /**
         * 分账比例 （可以为空， 为空表示使用系统默认值）
         */
        private BigDecimal divisionProfit;

    }

}
