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
package com.jeequan.jeepay.pay.rqrs.division;

import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import lombok.Data;

/**
* 发起订单分账 响应参数
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/26 17:20
*/
@Data
public class PayOrderDivisionExecRS extends AbstractRS {

    /**
     * 分账状态 1-分账成功, 2-分账失败
     */
    private Byte state;

    /**
     * 上游分账批次号
     */
    private String channelBatchOrderId;

    /**
     * 支付渠道错误码
     */
    private String errCode;

    /**
     * 支付渠道错误信息
     */
    private String errMsg;


}
