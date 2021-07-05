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
package com.jeequan.jeepay.pay.mq.config;

import com.jeequan.jeepay.core.constants.CS;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMq
 * 延迟消息队列绑定交换机
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
@Configuration
@Profile(CS.MQTYPE.RABBIT_MQ)
public class RabbitMqConfig {

    @Bean("channelOrderQuery")
    public Queue channelOrderQuery() {
        return new Queue(CS.MQ.QUEUE_CHANNEL_ORDER_QUERY,true);
    }

    @Bean("payOrderMchNotify")
    public Queue payOrderMchNotify() {
        return new Queue(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY,true);
    }

    //创建 custom 交换机
    @Bean
    CustomExchange customExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(CS.DELAYED_EXCHANGE, "x-delayed-message", true, false, args);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：QUEUE_CHANNEL_ORDER_QUERY
    @Bean
    Binding bindingChannelOrderQuery(@Qualifier("channelOrderQuery") Queue channelOrderQuery) {
        return BindingBuilder.bind(channelOrderQuery).to(customExchange()).with(CS.MQ.QUEUE_CHANNEL_ORDER_QUERY).noargs();
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：QUEUE_PAYORDER_MCH_NOTIFY
    @Bean
    Binding bindingPayOrderNotify(@Qualifier("payOrderMchNotify") Queue payOrderMchNotify) {
        return BindingBuilder.bind(payOrderMchNotify).to(customExchange()).with(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY).noargs();
    }

}
