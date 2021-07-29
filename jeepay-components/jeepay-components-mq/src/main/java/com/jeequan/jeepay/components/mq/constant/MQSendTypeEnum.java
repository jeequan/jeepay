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
package com.jeequan.jeepay.components.mq.constant;

/**
* 定义MQ消息类型
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/23 16:49
*/
public enum MQSendTypeEnum {
    /** QUEUE - 点对点 （只有1个消费者可消费。 ActiveMQ的queue模式 ） **/
    QUEUE,
    /** BROADCAST - 订阅模式 (所有接收者都可接收到。 ActiveMQ的topic模式, RabbitMQ的fanout类型的交换机, RocketMQ的广播模式  ) **/
    BROADCAST
}
