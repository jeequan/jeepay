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

import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;

/**
* 定义MQ消息格式
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/22 15:33
*/
public abstract class AbstractMQ {

    /** MQ名称 **/
    public abstract String getMQName();

    /** MQ 类型 **/
    public abstract MQSendTypeEnum getMQType();

    /** 构造MQ消息体 String类型 **/
    public abstract String toMessage();

}
