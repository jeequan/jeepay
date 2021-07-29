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
package com.jeequan.jeepay.pay.rqrs.refund;

import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
* 退款订单 响应参数
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/16 15:41
*/
@Data
public class RefundOrderRS extends AbstractRS {

    /** 支付系统退款订单号 **/
    private String refundOrderId;

    /** 商户发起的退款订单号 **/
    private String mchRefundNo;

    /** 订单支付金额 **/
    private Long payAmount;

    /** 申请退款金额 **/
    private Long refundAmount;

    /** 退款状态 **/
    private Byte state;

    /** 渠道退款单号   **/
    private String channelOrderNo;

    /** 渠道返回错误代码 **/
    private String errCode;

    /** 渠道返回错误信息 **/
    private String errMsg;


    public static RefundOrderRS buildByRefundOrder(RefundOrder refundOrder){

        if(refundOrder == null){
            return null;
        }

        RefundOrderRS result = new RefundOrderRS();
        BeanUtils.copyProperties(refundOrder, result);

        return result;
    }


}
