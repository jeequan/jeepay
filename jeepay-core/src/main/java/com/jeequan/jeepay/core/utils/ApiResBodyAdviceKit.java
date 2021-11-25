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
import com.jeequan.jeepay.core.model.OriginalRes;
import com.jeequan.jeepay.core.model.ApiRes;
import org.springframework.core.io.InputStreamResource;

import java.util.Collection;

/*
* 自定义springMVC的controller的返回值
 * 功能：
 *      1. 自动添加ApiRes.ok();
 *      2. 处理model的扩展字段 (只需要在model中设置[ext]参数， 可以实现json自动转换为外层字段。 )
 *         比如 model为 {id:1, ext:{abc:222}}  则自动转换为： {id:1, abc:222}
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 16:49
*/
public class ApiResBodyAdviceKit {

    /** 扩展字段的key名称 **/
    private static final String API_EXTEND_FIELD_NAME = "ext";

    public static Object beforeBodyWrite(Object body) {

        //空的情况 不处理
        if(body == null ) {
            return null;
        }

        if(body instanceof OriginalRes){
            return ((OriginalRes) body).getData();
        }

        // 返回String 避免 StringHttpMessageConverter
        if(body instanceof String){
            return body;
        }

        //返回文件流不处理
        if(body instanceof InputStreamResource){
            return body;
        }

        //返回二进制文件不处理
        if(body instanceof byte[]){
            return body;
        }

        //如果为ApiRes类型则仅处理扩展字段
        if(body instanceof ApiRes) {
            return procAndConvertJSON(body);
        }else{

            //ctrl返回其他非[ApiRes]认为处理成功， 先转换为成功状态， 在处理字段
            return procAndConvertJSON(ApiRes.ok(body));
        }
    }

    /** 处理扩展字段 and 转换为json格式 **/
    private static Object procAndConvertJSON(Object object){

        Object json = JSON.toJSON(object); //转换为JSON格式

        if(json instanceof JSONObject){  //对象类型
            processExtFieldByJSONObject((JSONObject) json);
            return json;
        }

        if(json instanceof Collection){  //数组类型

            JSONArray result = new JSONArray();
            for (Object itemObj : (Collection) json) {
                result.add(procAndConvertJSON(itemObj));
            }
            return result;
        }

        return json;
    }


    /** 处理jsonObject格式 **/
    private static void processExtFieldByJSONObject(JSONObject jsonObject){

        //如果包含字段， 则赋值到外层然后删除该字段
        if(jsonObject.containsKey(API_EXTEND_FIELD_NAME)){
            JSONObject exFieldMap = jsonObject.getJSONObject(API_EXTEND_FIELD_NAME);
            if(exFieldMap != null){ //包含字段
                for (String s : exFieldMap.keySet()) {  //遍历赋值到外层
                    jsonObject.put(s, exFieldMap.get(s));
                }
            }
            jsonObject.remove(API_EXTEND_FIELD_NAME);  //删除字段
        }

        //处理所有值
        for (String key : jsonObject.keySet()) {
            jsonObject.put(key, procAndConvertJSON(jsonObject.get(key)));
        }
    }
}
