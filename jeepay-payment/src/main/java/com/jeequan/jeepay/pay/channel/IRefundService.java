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
package com.jeequan.jeepay.pay.channel;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRQ;

/*
* 调起上游渠道侧退款接口
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/17 9:35
*/
public interface IRefundService {

    /** 获取到接口code **/
    String getIfCode();

    /** 前置检查如参数等信息是否符合要求， 返回错误信息或直接抛出异常即可  */
    String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder);

    /** 调起退款接口，并响应数据；  内部处理普通商户和服务商模式  **/
    ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

    /** 退款查单接口  **/
    ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
