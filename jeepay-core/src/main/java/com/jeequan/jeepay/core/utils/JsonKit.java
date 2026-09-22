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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/*
* json工具类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 16:51
*/
public class JsonKit {

	public static JSONObject newJson(String key, Object val){

		JSONObject result = new JSONObject();
		result.put(key, val);
		return result;
	}

	/**
	 * fastjson 2.x 兼容层安全转换：JSON.toJSON() 在部分运行条件下会产出 fastjson2 原生
	 * JSONObject/JSONArray，静态强转 (JSONObject) 会抛 ClassCastException（实测登录后菜单树
	 * TreeDataBuilder 必现）。原生对象具备同名常用方法，唯一断点是强转，因此统一在此完成
	 * "产出 + 包装"，调用方拿到的恒为 1.x 兼容类型。替代 (JSONObject) JSON.toJSON(x) 写法。
	 */
	public static JSONObject toJSONObject(Object bean) {
		Object o = JSON.toJSON(bean);
		return wrap(o);
	}

	/** 替代 (JSONArray) JSON.toJSON(list) 写法；数组内元素一并归一化为兼容类型 */
	public static JSONArray toJSONArray(Object list) {
		Object o = JSON.toJSON(list);
		if (o == null) {
			return new JSONArray();
		}
		if (o instanceof JSONArray) {
			JSONArray array = (JSONArray) o;
			for (int i = 0; i < array.size(); i++) {
				Object item = array.get(i);
				if (!(item instanceof JSONObject) && item instanceof Map) {
					array.set(i, new JSONObject((Map<String, Object>) item));
				}
			}
			return array;
		}
		if (o instanceof List) {
			JSONArray array = new JSONArray();
			for (Object item : (List<?>) o) {
				array.add(wrap(item));
			}
			return array;
		}
		JSONArray single = new JSONArray();
		single.add(wrap(o));
		return single;
	}

	/** 任意 JSON 树节点 -> 兼容 JSONObject；无法包装返回 null */
	@SuppressWarnings("unchecked")
	public static JSONObject wrap(Object node) {
		if (node instanceof JSONObject) {
			return (JSONObject) node;
		}
		if (node instanceof Map) {
			return new JSONObject((Map<String, Object>) node);
		}
		return null;
	}

	/** 集合整体归一化：元素为原生 Map 的包装为兼容 JSONObject */
	public static List<JSONObject> wrapList(Collection<?> nodes) {
		List<JSONObject> result = new ArrayList<>();
		if (nodes == null) {
			return result;
		}
		for (Object node : nodes) {
			JSONObject wrapped = wrap(node);
			if (wrapped != null) {
				result.add(wrapped);
			}
		}
		return result;
	}
}
