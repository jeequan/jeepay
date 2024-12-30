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
package com.jeequan.jeepay.components.mq.vender.rabbitmq;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;
import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.model.AbstractMQ;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
* RabbitMQ的配置项
* 1. 注册全部定义好的Queue Bean
* 2. 动态注册fanout交换机
* 3. 将Queue模式绑定到延时消息的交换机
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/23 16:33
*/
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.RABBIT_MQ)
public class RabbitMQConfig {

    /** 全局定义延迟交换机名称 **/
    public static final String DELAYED_EXCHANGE_NAME = "delayedExchange";

    /** 扇形交换机前缀（activeMQ中的topic模式）， 需根据queue动态拼接 **/
    public static final String FANOUT_EXCHANGE_NAME_PREFIX = "fanout_exchange_";

    /** 注入延迟交换机Bean **/
    @Autowired
    @Qualifier(DELAYED_EXCHANGE_NAME)
    private CustomExchange delayedExchange;

    /** 注入rabbitMQBeanProcessor **/
    @Autowired
    private RabbitMQBeanProcessor rabbitMQBeanProcessor;

    /** 在全部bean注册完成后再执行 **/
    @PostConstruct
    public void init(){

        // 获取到所有的MQ定义
        Set<Class<?>> set = ClassUtil.scanPackageBySuper(ClassUtil.getPackage(AbstractMQ.class), AbstractMQ.class);

        for (Class<?> aClass : set) {

            // 实例化
            AbstractMQ amq = (AbstractMQ) ReflectUtil.newInstance(aClass);

            // 注册Queue === new Queue(name)，  queue名称/bean名称 = mqName
            rabbitMQBeanProcessor.beanDefinitionRegistry.registerBeanDefinition(amq.getMQName(),
                    BeanDefinitionBuilder.rootBeanDefinition(Queue.class).addConstructorArgValue(amq.getMQName()).getBeanDefinition());

            // 广播模式
            if(amq.getMQType() == MQSendTypeEnum.BROADCAST){

                // 动态注册交换机， 交换机名称/bean名称 =  FANOUT_EXCHANGE_NAME_PREFIX + amq.getMQName()
                rabbitMQBeanProcessor.beanDefinitionRegistry.registerBeanDefinition(FANOUT_EXCHANGE_NAME_PREFIX +amq.getMQName(),
                        BeanDefinitionBuilder.genericBeanDefinition(FanoutExchange.class, () ->{

                            // 普通FanoutExchange 交换机
                             return new FanoutExchange(FANOUT_EXCHANGE_NAME_PREFIX +amq.getMQName(),true,false);

                            // 支持 延迟的 FanoutExchange 交换机， 配置无效果。
//                            Map<String, Object> args = new HashMap<>();
//                            args.put("x-delayed-type", ExchangeTypes.FANOUT);
//                            return new CustomExchange(RabbitMQConfig.DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
                        }

                        ).getBeanDefinition()
                );

            }else{

                // 延迟交换机与Queue进行绑定， 绑定Bean名称 = mqName_DelayedBind
                rabbitMQBeanProcessor.beanDefinitionRegistry.registerBeanDefinition(amq.getMQName() + "_DelayedBind",
                        BeanDefinitionBuilder.genericBeanDefinition(Binding.class, () ->
                                BindingBuilder.bind(SpringBeansUtil.getBean(amq.getMQName(), Queue.class)).to(delayedExchange).with(amq.getMQName()).noargs()

                        ).getBeanDefinition()
                );
            }
        }
    }
}
