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
package com.jeequan.jeepay.mch.mq.queue;

import com.alibaba.fastjson.JSONArray;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 商户用户登录信息清除
 *
 * @author pangxiaoyu
 * @site https://www.jeepay.vip
 * @date 2021-04-27 15:50
 */
@Slf4j
@Component
@Profile(CS.MQTYPE.RABBIT_MQ)
public class RabbitMqQueue4ModifyMchUserRemove {

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:17
     * @describe: 接收 商户用户登录信息清除消息
     */
    @RabbitListener(queues = CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE)
    public void receive(String userIdStr) {
        log.info("成功接收删除商户用户登录的订阅通知, msg={}", userIdStr);
        // 字符串转List<Long>
        List<Long> userIdList = JSONArray.parseArray(userIdStr, Long.class);
        // 删除redis用户缓存
        if(userIdList == null || userIdList.isEmpty()){
            log.info("用户ID为空");
            return ;
        }
        for (Long sysUserId : userIdList) {
            Collection<String> cacheKeyList = RedisUtil.keys(CS.getCacheKeyToken(sysUserId, "*"));
            if(cacheKeyList == null || cacheKeyList.isEmpty()){
                continue;
            }
            for (String cacheKey : cacheKeyList) {
                // 删除用户Redis信息
                RedisUtil.del(cacheKey);
                continue;
            }
        }
        log.info("无权限登录用户信息已清除");
    }

}
