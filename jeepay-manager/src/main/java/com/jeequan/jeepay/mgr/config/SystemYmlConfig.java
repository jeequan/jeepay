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
package com.jeequan.jeepay.mgr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 系统Yml配置参数定义Bean
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Data
@Component
@ConfigurationProperties(prefix="isys")
public class SystemYmlConfig {

	/** 是否允许跨域请求 [生产环境建议关闭， 若api与前端项目没有在同一个域名下时，应开启此配置或在nginx统一配置允许跨域]  **/
	private Boolean allowCors;

	/** 生成jwt的秘钥。 要求每个系统有单独的秘钥管理机制。 **/
	private String jwtSecret;

	/** 是否内存缓存配置信息: true表示开启如支付网关地址/商户应用配置/服务商配置等， 开启后需检查MQ的广播模式是否正常； false表示直接查询DB.  **/
	private Boolean cacheConfig;

}



