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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * 系统Yml配置参数定义Bean
 *
 * @author terrfly
 * @site https://www.jeepay.vip
 * @date 2021-04-27 15:50
 */
@Component
@ConfigurationProperties(prefix="isys")
@Data
public class SystemYmlConfig {

	/** 是否允许跨域请求 [生产环境建议关闭， 若api与前端项目没有在同一个域名下时，应开启此配置或在nginx统一配置允许跨域]  **/
	private Boolean allowCors;

	/** 生成jwt的秘钥。 要求每个系统有单独的秘钥管理机制。 **/
	private String jwtSecret;

	@NestedConfigurationProperty //指定该属性为嵌套值, 否则默认为简单值导致对象为空（外部类不存在该问题， 内部static需明确指定）
	private OssFile ossFile;

	/** 系统oss配置信息 **/
	@Data
	public static class OssFile{

		/** 存储根路径 **/
		private String rootPath;

		/** 公共读取块 **/
		private String publicPath;

		/** 私有读取块 **/
		private String privatePath;

	}
}