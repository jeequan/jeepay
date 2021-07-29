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
package com.jeequan.jeepay.core.cache;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;

/*
* token service
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/5/24 09:06
*/
public class ITokenService {

    /** 处理token信息
     * 1. 如果不允许多用户则踢掉之前的所有用户信息
     * 2. 更新token 缓存时间信息
     * 3. 更新用户token列表
     * **/
    public static void processTokenCache(JeeUserDetails userDetail, String cacheKey){

        userDetail.setCacheKey(cacheKey);  //设置cacheKey

        //当前用户的所有登录token 集合
//        if(!PropKit.isAllowMultiUser()){ //不允许多用户登录
//
//            List<String> allTokenList = new ArrayList<>();
//            for (String token : allTokenList) {
//                if(!cacheKey.equalsIgnoreCase(token)){
//                    RedisUtil.del(token);
//                }
//            }
//        }

        //保存token
        RedisUtil.set(cacheKey, userDetail, CS.TOKEN_TIME);  //缓存时间2小时, 保存具体信息而只是uid, 因为很多场景需要得到信息， 例如验证接口权限， 每次请求都需要获取。 将信息封装在一起减少磁盘请求次数， 如果放置多个key会增加非顺序读取。
    }


    /** 退出时，清除token信息 */
    public static void removeIToken(String iToken, Long currentUID){

        //1. 清除token的信息
        RedisUtil.del(iToken);
    }

    /**
     * 刷新数据
     * **/
    public static void refData(JeeUserDetails currentUserInfo){

        //保存token 和 tokenList信息
        RedisUtil.set(currentUserInfo.getCacheKey(), currentUserInfo, CS.TOKEN_TIME);  //缓存时间2小时, 保存具体信息而只是uid, 因为很多场景需要得到信息， 例如验证接口权限， 每次请求都需要获取。 将信息封装在一起减少磁盘请求次数， 如果放置多个key会增加非顺序读取。

    }

}
