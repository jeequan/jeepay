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
package com.jeequan.jeepay.core.jwt;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import lombok.Data;

import java.util.Map;

/*
* JWT payload 载体
* 格式：
    {
        "sysUserId": "10001",
        "created": "1568250147846",
        "cacheKey": "KEYKEYKEYKEY",
    }
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 18:01
*/
@Data
public class JWTPayload {

    private Long sysUserId;       //登录用户ID
    private Long created;         //创建时间, 格式：13位时间戳
    private String cacheKey;      //redis保存的key

    protected JWTPayload(){}

    public JWTPayload(JeeUserDetails jeeUserDetails){

        this.setSysUserId(jeeUserDetails.getSysUser().getSysUserId());
        this.setCreated(System.currentTimeMillis());
        this.setCacheKey(jeeUserDetails.getCacheKey());
    }


    /** toMap **/
    public Map<String, Object> toMap(){
        JSONObject json = (JSONObject)JSONObject.toJSON(this);
        return json.toJavaObject(Map.class);
    }

}
