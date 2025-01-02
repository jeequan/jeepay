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
package com.jeequan.jeepay.mgr.bootstrap;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import io.swagger.v3.oas.models.OpenAPI;

import java.lang.reflect.Type;

// 实现FastJson序列号接口
public class SwaggerJsonSerializer implements ObjectSerializer, ObjectDeserializer {

    public final static SwaggerJsonSerializer instance = new SwaggerJsonSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) {
        SerializeWriter out = serializer.getWriter();
        byte[] byteArr = (byte[]) object;

        try {
            String result = new String(byteArr);
            // OpenAPI openAPI = JSON.parseObject(result, OpenAPI.class);
            // if (openAPI != null && new OpenAPI().getOpenapi().equals(openAPI.getOpenapi())) {
            //     out.write(result);
            // }

            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject != null && new OpenAPI().getOpenapi().equals(jsonObject.getString("openapi"))) {
                out.write(result);
            }

        }catch (Exception e) {
            out.writeByteArray(byteArr);
        }
    }

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
