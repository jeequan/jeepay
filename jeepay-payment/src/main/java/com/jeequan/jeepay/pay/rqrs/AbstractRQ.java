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
import java.io.Serializable;

/*
* 基础请求参数
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:39
*/
@Data
public abstract class AbstractRQ implements Serializable {

    /** 版本号 **/
    @NotBlank(message="版本号不能为空")
    protected String version;

    /** 签名类型 **/
    @NotBlank(message="签名类型不能为空")
    protected String signType;

    /** 签名值 **/
    @NotBlank(message="签名值不能为空")
    protected String sign;

    /** 接口请求时间 **/
    @NotBlank(message="时间戳不能为空")
    protected String reqTime;

}
