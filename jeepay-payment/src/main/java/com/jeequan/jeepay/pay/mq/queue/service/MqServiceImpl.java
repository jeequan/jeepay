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
package com.jeequan.jeepay.pay.mq.queue.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * mq消息推送
 *
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
@Slf4j
@Service
public class MqServiceImpl {

    @Autowired private MqChannelOrderQueryService mqChannelOrderQueryService;
    @Autowired private MqPayOrderMchNotifyService mqPayOrderMchNotifyService;

    /** 通道订单查询推送 **/
    public void sendChannelOrderQuery(String msg){
        mqChannelOrderQueryService.send(msg);
    }

    public void sendChannelOrderQuery(String msg, long delay){
        mqChannelOrderQueryService.send(msg, delay);
    }

    /** 商户订单回调 **/
    public void PayOrderMchNotify(String msg){
        mqPayOrderMchNotifyService.send(msg);
    }

    public void PayOrderMchNotify(String msg, long delay){
        mqPayOrderMchNotifyService.send(msg, delay);
    }

}
