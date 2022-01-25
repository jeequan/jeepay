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

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jeequan.jeepay.components.mq.model.PayOrderDivisionMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
* 订单处理通用逻辑
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/22 16:50
*/
@Service
@Slf4j
public class PayOrderProcessService {


    @Autowired private PayOrderService payOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private IMQSender mqSender;

    /** 明确成功的处理逻辑（除更新订单其他业务） **/
    public void confirmSuccess(PayOrder payOrder){

        //设置订单状态
        payOrder.setState(PayOrder.STATE_SUCCESS);

        //自动分账 处理逻辑， 不影响主订单任务
        this.updatePayOrderAutoDivision(payOrder);

        //发送商户通知
        payMchNotifyService.payOrderNotify(payOrder);

    }



    /** 更新订单自动分账业务 **/
    private void updatePayOrderAutoDivision(PayOrder payOrder){

        try {

            //默认不分账  || 其他非【自动分账】逻辑时， 不处理
            if(payOrder == null || payOrder.getDivisionMode() == null || payOrder.getDivisionMode() != PayOrder.DIVISION_MODE_AUTO){
                return ;
            }

            //更新订单表分账状态为： 等待分账任务处理
            boolean updDivisionState = payOrderService.update(new LambdaUpdateWrapper<PayOrder>()
                    .set(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_WAIT_TASK)
                    .eq(PayOrder::getPayOrderId, payOrder.getPayOrderId())
                    .eq(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_UNHAPPEN)
            );

            if(updDivisionState){
                //推送到分账MQ
                mqSender.send(PayOrderDivisionMQ.build(payOrder.getPayOrderId(), CS.YES,null), 80); //80s 后执行
            }

        } catch (Exception e) {
            log.error("订单[{}]自动分账逻辑异常：", payOrder.getPayOrderId(), e);
        }
    }


}
