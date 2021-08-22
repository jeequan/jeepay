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
package com.jeequan.jeepay.pay.channel;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;

/*
* @Description: 301方式获取渠道侧用户ID， 如微信openId 支付宝的userId等
* @author terrfly
* @date 2021/5/2 15:10
*/
public interface IChannelUserService {

    /** 获取到接口code **/
    String getIfCode();

    /** 获取重定向地址 **/
    String buildUserRedirectUrl(String callbackUrlEncode, MchAppConfigContext mchAppConfigContext);

    /** 获取渠道用户ID **/
    String getChannelUserId(JSONObject reqParams, MchAppConfigContext mchAppConfigContext);

}
