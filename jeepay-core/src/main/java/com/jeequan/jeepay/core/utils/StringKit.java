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
package com.jeequan.jeepay.core.utils;

import cn.hutool.core.net.url.UrlBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.UUID;

/*
* String 工具类
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 16:58
*/
public class StringKit {

	public static String getUUID(){
		return UUID.randomUUID().toString().replace("-", "") + Thread.currentThread().getId();
	}

	public static String getUUID(int endAt){
		return getUUID().substring(0, endAt);
	}

	/** 拼接url参数 **/
	public static String appendUrlQuery(String url, Map<String, Object> map){

		if(StringUtils.isEmpty(url) || map == null || map.isEmpty()){
			return url;
		}
		UrlBuilder result = UrlBuilder.create().of(url);
		map.forEach((k, v) -> {
			if(k != null && v != null){
				result.addQuery(k, v.toString());
			}
		});

		return result.build();
	}


	/** 是否 http 或 https连接 **/
	public static boolean isAvailableUrl(String url){

		if(StringUtils.isEmpty(url)){
			return false;
		}

		return url.startsWith("http://") ||url.startsWith("https://");
	}

}
