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

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayIsvParams;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.util.ChannelCertConfigKitBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.apache.commons.lang3.StringUtils;

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


    public static WxServiceWrapper buildWxServiceWrapper(String mchId, String appId, String appSecret, String mchKey, String apiVersion, String apiV3Key,
                                                   String serialNo, String cert, String apiClientCert, String apiClientKey,
                                                         String wxpayPublicKeyId, String wxpayPublicKey){

        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setMchId(mchId);
        wxPayConfig.setAppId(appId);
        wxPayConfig.setMchKey(mchKey);

        if (CS.PAY_IF_VERSION.WX_V2.equals(apiVersion)) { // 微信API  V2
            wxPayConfig.setSignType(WxPayConstants.SignType.MD5);
        }

        ChannelCertConfigKitBean channelCertConfigKitBean = SpringBeansUtil.getBean(ChannelCertConfigKitBean.class);

        if(StringUtils.isNotBlank(apiV3Key)) {
            wxPayConfig.setApiV3Key(apiV3Key);
        }
        if(StringUtils.isNotBlank(serialNo)) {
            wxPayConfig.setCertSerialNo(serialNo);
        }
        if(StringUtils.isNotBlank(cert)){
            wxPayConfig.setKeyPath(channelCertConfigKitBean.getCertFilePath(cert));
        }
        if(StringUtils.isNotBlank(apiClientCert)){
            wxPayConfig.setPrivateCertPath(channelCertConfigKitBean.getCertFilePath(apiClientCert));
        }
        if(StringUtils.isNotBlank(apiClientKey)) {
            wxPayConfig.setPrivateKeyPath(channelCertConfigKitBean.getCertFilePath(apiClientKey));
        }

        if(StringUtils.isNotEmpty(wxpayPublicKey)){ // 微信公钥证书
            wxPayConfig.setPublicKeyPath(channelCertConfigKitBean.getCertFilePath(wxpayPublicKey));
        }
        if(StringUtils.isNotEmpty(wxpayPublicKeyId)){
            wxPayConfig.setPublicKeyId(wxpayPublicKeyId);  // 微信公钥ID
        }

        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(wxPayConfig); //微信配置信息

        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setAppId(appId);
        wxMpConfigStorage.setSecret(appSecret);

        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage); //微信配置信息

        return new WxServiceWrapper(apiVersion, wxPayService, wxMpService);
    }


    public static WxServiceWrapper buildWxServiceWrapper(WxpayIsvParams wxpayParams){
        //放置 wxJavaService
        return buildWxServiceWrapper(wxpayParams.getMchId(), wxpayParams.getAppId(),
                wxpayParams.getAppSecret(), wxpayParams.getKey(), wxpayParams.getApiVersion(), wxpayParams.getApiV3Key(),
                wxpayParams.getSerialNo(), wxpayParams.getCert(), wxpayParams.getApiClientCert(), wxpayParams.getApiClientKey(),
                wxpayParams.getWxpayPublicKeyId(), wxpayParams.getWxpayPublicKey());
    }

    public static WxServiceWrapper buildWxServiceWrapper(WxpayNormalMchParams wxpayParams){
        //放置 wxJavaService
        return buildWxServiceWrapper(wxpayParams.getMchId(), wxpayParams.getAppId(),
                wxpayParams.getAppSecret(), wxpayParams.getKey(), wxpayParams.getApiVersion(), wxpayParams.getApiV3Key(),
                wxpayParams.getSerialNo(), wxpayParams.getCert(), wxpayParams.getApiClientCert(), wxpayParams.getApiClientKey(),
                wxpayParams.getWxpayPublicKeyId(), wxpayParams.getWxpayPublicKey());
    }


}
