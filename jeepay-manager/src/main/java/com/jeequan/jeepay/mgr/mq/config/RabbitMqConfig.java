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
package com.jeequan.jeepay.mgr.mq.config;

import com.jeequan.jeepay.core.constants.CS;
import org.springframework.amqp.core.*;
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
@Configuration
@EnableRabbit
@Profile(CS.MQTYPE.RABBIT_MQ)
public class RabbitMqConfig {

    @Bean("modifyIsvInfo")
    public Queue modifyIsvInfo() { return new Queue(CS.MQ.TOPIC_MODIFY_ISV_INFO,true); }

    @Bean("modifyMchApp")
    public Queue modifyMchApp() {
        return new Queue(CS.MQ.TOPIC_MODIFY_MCH_APP,true);
    }

    @Bean("modifyMchInfo")
    public Queue modifyMchInfo() {
        return new Queue(CS.MQ.TOPIC_MODIFY_MCH_INFO,true);
    }

    @Bean("modifySysConfig")
    public Queue modifySysConfig() {
        return new Queue(CS.MQ.FANOUT_MODIFY_SYS_CONFIG,true);
    }

    @Bean("payOrderMchNotify")
    public Queue payOrderMchNotify() {
        return new Queue(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY,true);
    }

    @Bean("mchUserRemove")
    public Queue mchUserRemove() {
        return new Queue(CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE,true);
    }

    //Fanout交换机 起名：fanoutExchange
    @Bean("fanoutExchange")
    FanoutExchange fanoutExchange() {
        return new FanoutExchange(CS.FANOUT_EXCHANGE_SYS_CONFIG,true,false);
    }

    //交换机 起名：directExchange
    @Bean("directExchange")
    DirectExchange directExchange() {
        return new DirectExchange(CS.DIRECT_EXCHANGE,true,false);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：TOPIC_MODIFY_ISV_INFO
    @Bean
    Binding bindingIsvInfo(@Qualifier("modifyIsvInfo") Queue modifyIsvInfo, @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(modifyIsvInfo).to(directExchange).with(CS.MQ.TOPIC_MODIFY_ISV_INFO);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：TOPIC_MODIFY_MCH_APP
    @Bean
    Binding bindingMchApp(@Qualifier("modifyMchApp") Queue modifyMchApp, @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(modifyMchApp).to(directExchange).with(CS.MQ.TOPIC_MODIFY_MCH_APP);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：TOPIC_MODIFY_MCH_INFO
    @Bean
    Binding bindingMchInfo(@Qualifier("modifyMchInfo") Queue modifyMchInfo, @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(modifyMchInfo).to(directExchange).with(CS.MQ.TOPIC_MODIFY_MCH_INFO);
    }

    //绑定  将队列和交换机绑定
    @Bean
    Binding bindingSysConfig(Queue modifySysConfig, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(modifySysConfig).to(fanoutExchange);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：QUEUE_PAYORDER_MCH_NOTIFY
    @Bean
    Binding bindingPayOrderMchNotify(@Qualifier("payOrderMchNotify") Queue payOrderMchNotify, @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(payOrderMchNotify).to(directExchange).with(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：QUEUE_MODIFY_MCH_USER_REMOVE
    @Bean
    Binding bindingMchUserRemove(@Qualifier("mchUserRemove") Queue mchUserRemove, @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(mchUserRemove).to(directExchange).with(CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE);
    }

}
