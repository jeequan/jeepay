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

import lombok.Data;

/*
* 支付宝， 通用配置信息
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 16:32
*/
@Data
public class AlipayConfig{

    public static final String SIGN_TYPE_RSA = "RSA";
    public static final String SIGN_TYPE_RSA2 = "RSA2";


    /** 网关地址 */
    public static String PROD_SERVER_URL = "https://openapi.alipay.com/gateway.do";
    public static String SANDBOX_SERVER_URL = "https://openapi.alipaydev.com/gateway.do";

    public static String PROD_OAUTH_URL = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=auth_base&state=&redirect_uri=%s";
    public static String SANDBOX_OAUTH_URL = "https://openauth.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=auth_base&state=&redirect_uri=%s";

    /** isv获取授权商户URL地址 **/
    public static String PROD_APP_TO_APP_AUTH_URL = "https://openauth.alipay.com/oauth2/appToAppAuth.htm?app_id=%s&redirect_uri=%s&state=%s";
    public static String SANDBOX_APP_TO_APP_AUTH_URL = "https://openauth.alipaydev.com/oauth2/appToAppAuth.htm?app_id=%s&redirect_uri=%s&state=%s";


    public static String FORMAT = "json";

    public static String CHARSET = "UTF-8";

}
