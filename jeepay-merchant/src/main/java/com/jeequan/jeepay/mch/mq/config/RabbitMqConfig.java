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
package com.jeequan.jeepay.mch.mq.config;

import com.jeequan.jeepay.core.constants.CS;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * RabbitMq
 * 队列交换机注册
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
@Profile(CS.MQTYPE.RABBIT_MQ)
@Configuration
@EnableRabbit
public class RabbitMqConfig {


    @Bean("modifyMchApp")
    public Queue modifyMchApp() {
        return new Queue(CS.MQ.TOPIC_MODIFY_MCH_APP,true);
    }

    //创建 direct 交换机
    @Bean("directExchange")
    DirectExchange directExchange() {
        return new DirectExchange(CS.DIRECT_EXCHANGE,true,false);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：TOPIC_MODIFY_MCH_APP
    @Bean
    Binding bindingMchApp(@Qualifier("modifyMchApp") Queue modifyMchApp, @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(modifyMchApp).to(directExchange).with(CS.MQ.TOPIC_MODIFY_MCH_APP);
    }

}
