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
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
*
* 定义MQ消息格式
* 业务场景： [ 更新服务商/商户/商户应用配置信息 ]
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/22 15:25
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetIsvMchAppInfoConfigMQ extends AbstractMQ {

    /** 【！重要配置项！】 定义MQ名称 **/
    public static final String MQ_NAME = "BROADCAST_RESET_ISV_MCH_APP_INFO_CONFIG";

    /** 内置msg 消息体定义 **/
    private MsgPayload payload;

    // 重置类型 （枚举类型，无法json反序列化）
    public static final byte RESET_TYPE_ISV_INFO = 1;
    public static final byte RESET_TYPE_MCH_INFO = 2;
    public static final byte RESET_TYPE_MCH_APP = 3;

    /**  【！重要配置项！】 定义Msg消息载体 **/
    @Data
    @AllArgsConstructor
    public static class MsgPayload {

        /** 重置类型 **/
        private Byte resetType;

        /** isvNo **/
        private String isvNo;

        /** mchNo **/
        private String mchNo;

        /** appId **/
        private String appId;

    }


    @Override
    public String getMQName() {
        return MQ_NAME;
    }

    /**  【！重要配置项！】 **/
    @Override
    public MQSendTypeEnum getMQType(){
        return MQSendTypeEnum.BROADCAST;  // QUEUE - 点对点 、 BROADCAST - 广播模式
    }

    @Override
    public String toMessage() {
        return JSONObject.toJSONString(payload);
    }

    /**  【！重要配置项！】 构造MQModel , 一般用于发送MQ时 **/
    public static ResetIsvMchAppInfoConfigMQ build(Byte resetType, String isvNo, String mchNo, String appId){
        return new ResetIsvMchAppInfoConfigMQ(new MsgPayload(resetType, isvNo, mchNo, appId));
    }

    /** 解析MQ消息， 一般用于接收MQ消息时 **/
    public static MsgPayload parse(String msg){
        return JSON.parseObject(msg, MsgPayload.class);
    }

    /** 定义 IMQReceiver 接口： 项目实现该接口则可接收到对应的业务消息  **/
    public interface IMQReceiver{
        void receive(MsgPayload payload);
    }

}
