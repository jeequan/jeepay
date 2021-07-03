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
package com.jeequan.jeepay.mch.mq.service;

import com.alibaba.fastjson.JSONArray;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 处理公共接收消息方法
 *
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
@Slf4j
@Service
public class MqReceiveServiceImpl {

    @Autowired private SysConfigService sysConfigService;

    public void mchUserRemove(String userIdStr) {
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

    public void initDbConfig(String msg) {
        log.info("成功接收更新系统配置的订阅通知, msg={}", msg);
        sysConfigService.initDBConfig(msg);
        log.info("系统配置静态属性已重置");
    }
}
