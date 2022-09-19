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
package com.jeequan.jeepay.pay.channel.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.alipay.AlipayConfig;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.pay.channel.IChannelUserService;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
* 支付宝： 获取用户ID实现类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:21
*/
@Service
@Slf4j
public class AlipayChannelUserService implements IChannelUserService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public String buildUserRedirectUrl(String callbackUrlEncode, MchAppConfigContext mchAppConfigContext) {

        String oauthUrl = AlipayConfig.PROD_OAUTH_URL;
        String appId = null;

        if(mchAppConfigContext.isIsvsubMch()){
            AlipayIsvParams isvParams = (AlipayIsvParams) configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), getIfCode());
            if(isvParams == null) {
                throw new BizException("服务商支付宝接口没有配置！");
            }
            appId = isvParams.getAppId();
            if(isvParams.getSandbox() != null && isvParams.getSandbox() == CS.YES){
                oauthUrl = AlipayConfig.SANDBOX_OAUTH_URL;
            }
        }else{
            //获取商户配置信息
            AlipayNormalMchParams normalMchParams = (AlipayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
            if(normalMchParams == null) {
                throw new BizException("商户支付宝接口没有配置！");
            }
            appId = normalMchParams.getAppId();
            if(normalMchParams.getSandbox() != null && normalMchParams.getSandbox() == CS.YES){
                oauthUrl = AlipayConfig.SANDBOX_OAUTH_URL;
            }
        }
        String alipayUserRedirectUrl = String.format(oauthUrl, appId, callbackUrlEncode);
        log.info("alipayUserRedirectUrl={}", alipayUserRedirectUrl);
        return alipayUserRedirectUrl;
    }

    @Override
    public String getChannelUserId(JSONObject reqParams, MchAppConfigContext mchAppConfigContext) {

        String authCode = reqParams.getString("auth_code");

        //通过code 换取openId
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setCode(authCode); request.setGrantType("authorization_code");
        try {
            return configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request).getUserId();
        } catch (ChannelException e) {
            e.printStackTrace();
            return null;
        }
    }

}
