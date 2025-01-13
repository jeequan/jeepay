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
package com.jeequan.jeepay.mch.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis管理类
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private Integer port;

    @Value("${spring.data.redis.timeout}")
    private Integer timeout;

    @Value("${spring.data.redis.database}")
    private Integer defaultDatabase;

    @Value("${spring.data.redis.password}")
    private String password;

    /** 当前系统的redis缓存操作对象 (主对象) **/
    @Primary
    @Bean(name = "defaultStringRedisTemplate")
    public StringRedisTemplate sysStringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();

        LettuceConnectionFactory jedisConnectionFactory = new LettuceConnectionFactory();
        jedisConnectionFactory.setHostName(host);
        jedisConnectionFactory.setPort(port);
        jedisConnectionFactory.setTimeout(timeout);

        if (!StringUtils.isEmpty(password)) {
            jedisConnectionFactory.setPassword(password);
        }

        if (defaultDatabase != 0) {
            jedisConnectionFactory.setDatabase(defaultDatabase);
        }

        jedisConnectionFactory.afterPropertiesSet();

        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }

}
