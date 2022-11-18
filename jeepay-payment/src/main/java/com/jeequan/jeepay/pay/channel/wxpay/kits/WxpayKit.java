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
package com.jeequan.jeepay.pay.channel.wxpay.kits;

import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.model.WxServiceWrapper;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import org.apache.commons.lang3.StringUtils;

/*
* 【微信支付】支付通道工具包
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:21
*/
public class WxpayKit {

    /** 放置 isv特殊信息 **/
    public static void putApiIsvInfo(MchAppConfigContext mchAppConfigContext, BaseWxPayRequest req){

        //不是特约商户， 无需放置此值
        if(!mchAppConfigContext.isIsvsubMch()){
            return ;
        }

        ConfigContextQueryService configContextQueryService = SpringBeansUtil.getBean(ConfigContextQueryService.class);

        WxpayIsvsubMchParams isvsubMchParams =
                (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);

        req.setSubMchId(isvsubMchParams.getSubMchId());
        req.setSubAppId(isvsubMchParams.getSubMchAppId());
    }

    /** 构造服务商 + 商户配置  wxPayConfig **/
    public static WxPayConfig getWxPayConfig(WxServiceWrapper wxServiceWrapper){
        return wxServiceWrapper.getWxPayService().getConfig();
    }

    public static String appendErrCode(String code, String subCode){
        return StringUtils.defaultIfEmpty(subCode, code); //优先： subCode
    }

    public static String appendErrMsg(String msg, String subMsg){

        if(StringUtils.isNotEmpty(msg) && StringUtils.isNotEmpty(subMsg) ){
            return msg + "【" + subMsg + "】";
        }
        return StringUtils.defaultIfEmpty(subMsg, msg);
    }

    public static void commonSetErrInfo(ChannelRetMsg channelRetMsg, WxPayException wxPayException){

        channelRetMsg.setChannelErrCode(appendErrCode( wxPayException.getReturnCode(), wxPayException.getErrCode() ));
        channelRetMsg.setChannelErrMsg(appendErrMsg( "OK".equalsIgnoreCase(wxPayException.getReturnMsg()) ? null : wxPayException.getReturnMsg(), wxPayException.getErrCodeDes() ));

        // 如果仍然为空
        if(StringUtils.isEmpty(channelRetMsg.getChannelErrMsg())){
            channelRetMsg.setChannelErrMsg(StringUtils.defaultIfEmpty(wxPayException.getCustomErrorMsg(), wxPayException.getMessage()));
        }

    }

}
