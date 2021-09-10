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
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.UUID;

/*
* String 工具类
*
* @author terrfly
* @site https://www.jeequan.com
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

		StringBuilder sb = new StringBuilder(url);
		if(url.indexOf("?") < 0){
			sb.append("?");
		}

		//是否包含query条件
		boolean isHasCondition = url.indexOf("=") >= 0;

		for (String k : map.keySet()) {
			if(k != null && map.get(k) != null){
				if(isHasCondition){
					sb.append("&"); //包含了查询条件， 那么应当拼接&符号
				}else{
					isHasCondition = true; //变更为： 已存在query条件
				}
				sb.append(k).append("=").append(URLUtil.encodeQuery(map.get(k).toString()));
			}
		}
		return sb.toString();
	}


	/** 拼接url参数: 旧版采用Hutool方式（当回调地址是 http://abc.com/#/abc 时存在位置问题） **/
	@Deprecated
	public static String appendUrlQueryWithHutool(String url, Map<String, Object> map){

		if(StringUtils.isEmpty(url) || map == null || map.isEmpty()){
			return url;
		}
		UrlBuilder result = UrlBuilder.of(url);
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

	/**
	 * 对字符加星号处理：除前面几位和后面几位外，其他的字符以星号代替
	 *
	 * @param content 传入的字符串
	 * @param frontNum 保留前面字符的位数
	 * @param endNum 保留后面字符的位数
	 * @return 带星号的字符串
	 */
	public static String str2Star2(String content, int frontNum, int endNum) {
		if (frontNum >= content.length() || frontNum < 0) {
			return content;
		}
		if (endNum >= content.length() || endNum < 0) {
			return content;
		}
		if (frontNum + endNum >= content.length()) {
			return content;
		}
		String starStr = "";
		for (int i = 0; i < (content.length() - frontNum - endNum); i++) {
			starStr = starStr + "*";
		}
		return content.substring(0, frontNum) + starStr
				+ content.substring(content.length() - endNum, content.length());
	}

	/**
	 * 对字符加星号处理：除前面几位和后面几位外，其他的字符以星号代替
	 *
	 * @param content 传入的字符串
	 * @param frontNum 保留前面字符的位数
	 * @param endNum 保留后面字符的位数
	 * @param starNum 指定star的数量
	 * @return 带星号的字符串
	 */
	public static String str2Star(String content, int frontNum, int endNum, int starNum) {
		if (frontNum >= content.length() || frontNum < 0) {
			return content;
		}
		if (endNum >= content.length() || endNum < 0) {
			return content;
		}
		if (frontNum + endNum >= content.length()) {
			return content;
		}
		String starStr = "";
		for (int i = 0; i < starNum; i++) {
			starStr = starStr + "*";
		}
		return content.substring(0, frontNum) + starStr
				+ content.substring(content.length() - endNum, content.length());
	}


	/**
	 * 合并两个json字符串
	 * key相同，则后者覆盖前者的值
	 * key不同，则合并至前者
	 * @param originStr
	 * @param mergeStr
	 * @return 合并后的json字符串
	 */
	public static String marge(String originStr, String mergeStr) {

		if (StringUtils.isAnyBlank(originStr, mergeStr)) {
			return null;
		}

		JSONObject originJSON = JSONObject.parseObject(originStr);
		JSONObject mergeJSON = JSONObject.parseObject(mergeStr);

		if (originJSON == null || mergeJSON == null) {
			return null;
		}

		originJSON.putAll(mergeJSON);
		return originJSON.toJSONString();
	}

}
