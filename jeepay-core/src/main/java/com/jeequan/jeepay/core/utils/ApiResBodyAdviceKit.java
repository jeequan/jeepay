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

        // fastjson 2.x 兼容层说明：
        // 1) 树模式 toJSON() 可能产出 fastjson2 原生类型，且对泛型字段嵌套的 bean（如 ApiRes<T>.data 为实体）
        //    会序列化为空对象；字符串往返无此问题，统一走 parseObject(toJSONString(x)) 保证内容完整且为兼容类型
        Object json = JSON.parseObject(JSON.toJSONString(object));
        return processTreeNode(json);
    }

    /**
     * 递归处理 JSON 树节点：ext 扩展字段提升 + 节点归一化为 1.x 兼容类型。
     * 注意：不再对已是树节点的值重复调用 JSON.toJSON —— fastjson 1.x 下该操作幂等，
     * 但 2.x 兼容层对 JSONObject（Wrapper）会重新解包转换，导致内容丢失。
     */
    private static Object processTreeNode(Object node) {

        JSONObject jsonObject = JsonKit.wrap(node);
        if(jsonObject != null){  //对象类型

            //如果包含ext字段， 则赋值到外层然后删除该字段
            if(jsonObject.containsKey(API_EXTEND_FIELD_NAME)){
                JSONObject exFieldMap = JsonKit.wrap(jsonObject.get(API_EXTEND_FIELD_NAME));
                if(exFieldMap != null){ //包含字段
                    for (String s : exFieldMap.keySet()) {  //遍历赋值到外层
                        jsonObject.put(s, exFieldMap.get(s));
                    }
                }
                jsonObject.remove(API_EXTEND_FIELD_NAME);  //删除字段
            }

            //处理所有值
            for (String key : jsonObject.keySet()) {
                jsonObject.put(key, processTreeNode(jsonObject.get(key)));
            }
            return jsonObject;
        }

        if(node instanceof Collection){  //数组类型
            JSONArray result = new JSONArray();
            for (Object itemObj : (Collection) node) {
                result.add(processTreeNode(itemObj));
            }
            return result;
        }

        return node;
    }
}
