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
package com.jeequan.jeepay.core.model;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

/*
* BaseModel 封装公共处理函数
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 16:49
*/
public class BaseModel<T> implements Serializable{

	private static final long serialVersionUID = 1L;

	/** ext参数, 用作扩展参数， 会在转换为api数据时自动将ext全部属性放置在对象的主属性上, 并且不包含ext属性   **/

	/** api接口扩展字段， 当包含该字段时 将自动填充到实体对象属性中如{id:1, ext:{abc:222}}  则自动转换为： {id:1, abc:222}，
	 *  需配合ResponseBodyAdvice使用
	 *  **/
	@TableField(exist = false)
	private JSONObject ext;

	//获取的时候设置默认值
	public JSONObject getExt() {
		return ext;
	}

	//设置扩展字段
	public BaseModel addExt(String key, Object val) {

		if(ext == null) {
            ext = new JSONObject();
        }
		ext.put(key,val);
		return this;
	}

	/** get ext value  可直接使用JSONObject对象的函数 **/
	public JSONObject extv() {
		return ext == null ? new JSONObject() : ext;
	}


}
