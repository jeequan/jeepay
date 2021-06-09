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
package com.jeequan.jeepay.core.model.params.alipay;

import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

/*
 * 支付宝 普通商户参数定义
 *
 * @author terrfly
 * @site https://www.jeepay.vip
 * @date 2021/6/8 16:33
 */
@Data
public class AlipayNormalMchParams extends NormalMchParams {

    /** 是否沙箱环境 */
    private Byte sandbox;

    /** appId */
    private String appId;

    /** privateKey */
    private String privateKey;

    /** alipayPublicKey */
    private String alipayPublicKey;

    /** 签名方式 **/
    private String signType;

    /** 是否使用证书方式 **/
    private Byte useCert;

    /** app 证书 **/
    private String appPublicCert;

    /** 支付宝公钥证书（.crt格式） **/
    private String alipayPublicCert;

    /** 支付宝根证书 **/
    private String alipayRootCert;


}
