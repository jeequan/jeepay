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
package com.jeequan.jeepay.core.model.params.plspay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 计全支付plus， 普通商户参数定义
 *
 * @author yurong
 * @site https://www.jeequan.com
 * @date 2022/8/11 14:32
 */
@Data
public class PlspayNormalMchParams extends NormalMchParams {

    /** 商户号 */
    private String merchantNo;

    /** 应用ID */
    private String appId;

    /** 签名方式 **/
    private String signType;

    /** md5秘钥 */
    private String appSecret;

    /** RSA2: 应用私钥 */
    private String rsa2AppPrivateKey;

    /** RSA2: 支付网关公钥 */
    public String rsa2PayPublicKey;


    @Override
    public String deSenData() {

        PlspayNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.appSecret)) {
            mchParams.setAppSecret(StringKit.str2Star(this.appSecret, 4, 4, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }

}
