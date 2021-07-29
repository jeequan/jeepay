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
package com.jeequan.jeepay.core.model.params.ysf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.IsvParams;
import com.jeequan.jeepay.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
 * 云闪付 配置信息
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:02
 */
@Data
public class YsfpayIsvParams extends IsvParams {

    /** 是否沙箱环境 */
    private Byte sandbox;

    /** serProvId **/
    private String serProvId;

    /** isvPrivateCertFile 证书 **/
    private String isvPrivateCertFile;

    /** isvPrivateCertPwd **/
    private String isvPrivateCertPwd;

    /** ysfpayPublicKey **/
    private String ysfpayPublicKey;

    /** acqOrgCodeList 支付机构号 **/
    private String acqOrgCode;

    @Override
    public String deSenData() {

        YsfpayIsvParams isvParams = this;
        if (StringUtils.isNotBlank(this.isvPrivateCertPwd)) {
            isvParams.setIsvPrivateCertPwd(StringKit.str2Star(this.isvPrivateCertPwd, 0, 3, 6));
        }
        if (StringUtils.isNotBlank(this.ysfpayPublicKey)) {
            isvParams.setYsfpayPublicKey(StringKit.str2Star(this.ysfpayPublicKey, 6, 6, 6));
        }
        return ((JSONObject) JSON.toJSON(isvParams)).toJSONString();
    }

}
