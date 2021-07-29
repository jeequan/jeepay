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
package com.jeequan.jeepay.pay.model;

import com.github.binarywang.wxpay.service.WxPayService;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpService;

/*
* wxService 包装类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:30
*/
@Data
@AllArgsConstructor
public class WxServiceWrapper {

    /** 缓存微信API版本 **/
    private String apiVersion;

    /** 缓存 wxPayService 对象 **/
    private WxPayService wxPayService;

    /** 缓存 wxJavaService 对象 **/
    private WxMpService wxMpService;

}
