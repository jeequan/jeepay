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
package com.jeequan.jeepay.components.mq.vender.aliyunrocketmq;

import com.aliyun.openservices.ons.api.*;
import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.core.service.ICodeSysTypeManager;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ALIYUN_ROCKET_MQ)
@Data
public class AliYunRocketMQFactory implements InitializingBean {

    public static final String defaultTag = "Default";

    @Autowired private ICodeSysTypeManager codeSysTypeManager;

    //消费者， 每个机器创建一个消费者示例 ，可以通过 多个订阅函数（subscribe）来增加。
    private Consumer aliyunRocketMQConsumer;

    //广播模式消费者
    private Consumer aliyunRocketMQClientBroadcastConsumer;


    @Value("${aliyun-rocketmq.namesrvAddr}")
    public String namesrvAddr;
    @Value("${aliyun-rocketmq.accessKey}")
    private String accessKey;
    @Value("${aliyun-rocketmq.secretKey}")
    private String secretKey;
    @Value("${aliyun-rocketmq.groupIdPrefix}")
    private String groupIdPrefix;

    @Bean(name = "producerClient")
    public Producer producerClient() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.GROUP_ID, genGroupId(false)); // 生产者的groupID 意义不大。
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        // 判断是否为空（生产环境走k8s集群环境变量自动注入，不获取本地配置文件的值）
        if (StringUtils.isNotEmpty(namesrvAddr)) {
            properties.put(PropertyKeyConst.NAMESRV_ADDR, namesrvAddr);
        }
        return ONSFactory.createProducer(properties);
    }

    private Consumer genAliyunRocketMQConsumer(boolean isBROADCASTING) {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.GROUP_ID, genGroupId(isBROADCASTING));
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);

        // 广播订阅方式设置
        if(isBROADCASTING){
            properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);
        }

        // 判断是否为空（生产环境走k8s集群环境变量自动注入，不获取本地配置文件的值）
        if (StringUtils.isNotEmpty(namesrvAddr)) {
            properties.put(PropertyKeyConst.NAMESRV_ADDR, namesrvAddr);
        }

        return ONSFactory.createConsumer(properties);
    }


    /**
     *
     *
     * 阿里云ROCKETMQ，  在 消费者端的gid下的机器要求所有的监听topID是一样的， 如果不一样可能出现丢失消息的情况。
     * 消息模式也应该是一样的（文档没有写，但是在 【消息队列 RocketMQ 版/实例列表/Group 管理/Group 详情】）
     * 可以清晰的看到 消费模式， 是放置在上面的， 也就是需要是一样的。 ！
     *
     *
     * 【消息队列 RocketMQ 版/实例列表/Group 管理/Group 详情】  如果有异常， 这里可以清晰的看到。
     *
     * 阿里云文档 ：https://help.aliyun.com/document_detail/43523.html?spm=5176.rocketmq.help.dexternal.4370176fcn7PRB
     *
     * GroupID命名规则： 前缀 + 系统名称 + 模式
     * 示例：
     * GID_JEEPAY_MANAGER_BROADCAST  : 运营平台_广播分组
     * GID_JEEPAY_PAYMENT_QUEUE  : 支付网关_队列分组
     * GID_JEEPAY_PAYMENT_BROADCAST  : 支付网关_广播分组
     *
     *
     * **/
    private String genGroupId(boolean isBROADCASTING){
        return groupIdPrefix + codeSysTypeManager.getCodeSysName() + (isBROADCASTING ? "_BROADCAST" : "_QUEUE");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        this.aliyunRocketMQConsumer = genAliyunRocketMQConsumer(false);

        this.aliyunRocketMQClientBroadcastConsumer = genAliyunRocketMQConsumer(true);
    }

}
