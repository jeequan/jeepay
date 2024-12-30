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
package com.jeequan.jeepay.pay.rqrs;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/*
* 商户获取渠道用户ID 请求参数对象
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:40
*/
@Data
public class ChannelUserIdRQ extends AbstractMchAppRQ{

    /** 接口代码,  AUTO表示：自动获取 **/
    @NotBlank(message="接口代码不能为空")
    private String ifCode;

    /** 商户扩展参数，将原样返回 **/
    private String extParam;

    /** 回调地址 **/
    @NotBlank(message="回调地址不能为空")
    private String redirectUrl;

}
