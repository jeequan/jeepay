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

import cn.hutool.core.text.CharSequenceUtil;
import com.alipay.api.AlipayObject;
import com.alipay.api.AlipayRequest;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvsubMchParams;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/*
* 【支付宝】支付通道工具包
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:19
*/
public class AlipayKit {


    /** 放置 isv特殊信息 **/
    public static void putApiIsvInfo(MchAppConfigContext mchAppConfigContext, AlipayRequest req, AlipayObject model){

        //不是特约商户， 无需放置此值
        if(!mchAppConfigContext.isIsvsubMch()){
            return ;
        }

        ConfigContextQueryService configContextQueryService = SpringBeansUtil.getBean(ConfigContextQueryService.class);

        // 获取支付参数
        AlipayIsvParams isvParams = (AlipayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.ALIPAY);
        AlipayIsvsubMchParams isvsubMchParams = (AlipayIsvsubMchParams)configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.ALIPAY);

        // 子商户信息
        if(req instanceof AlipayTradePayRequest) {
            ((AlipayTradePayRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayTradeAppPayRequest) {
            ((AlipayTradeAppPayRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayTradeCreateRequest) {
            ((AlipayTradeCreateRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayTradePagePayRequest) {
            ((AlipayTradePagePayRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayTradePrecreateRequest) {
            ((AlipayTradePrecreateRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayTradeWapPayRequest) {
            ((AlipayTradeWapPayRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayTradeQueryRequest) {
            ((AlipayTradeQueryRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayTradeRefundRequest) {
            ((AlipayTradeRefundRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayTradeFastpayRefundQueryRequest) {
            ((AlipayTradeFastpayRefundQueryRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof AlipayFundTransToaccountTransferRequest) {
            ((AlipayFundTransToaccountTransferRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof  AlipayTradeRoyaltyRelationBindRequest) {
            ((AlipayTradeRoyaltyRelationBindRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof  AlipayTradeOrderSettleRequest) {
            ((AlipayTradeOrderSettleRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof  AlipayTradeCloseRequest) {
            ((AlipayTradeCloseRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        } else if(req instanceof  AlipayTradeOrderSettleQueryRequest) {
            ((AlipayTradeOrderSettleQueryRequest)req).putOtherTextParam("app_auth_token", isvsubMchParams.getAppAuthToken());
        }


        // 服务商信息
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(isvParams.getPid());

        if(model instanceof AlipayTradePayModel) {
            ((AlipayTradePayModel)model).setExtendParams(extendParams);
        } else if(model instanceof AlipayTradeAppPayModel) {
            ((AlipayTradeAppPayModel)model).setExtendParams(extendParams);
        } else if(model instanceof AlipayTradeCreateModel) {
            ((AlipayTradeCreateModel)model).setExtendParams(extendParams);
        } else if(model instanceof AlipayTradePagePayModel) {
            ((AlipayTradePagePayModel)model).setExtendParams(extendParams);
        } else if(model instanceof AlipayTradePrecreateModel) {
            ((AlipayTradePrecreateModel)model).setExtendParams(extendParams);
        } else if(model instanceof AlipayTradeWapPayModel) {
            ((AlipayTradeWapPayModel)model).setExtendParams(extendParams);
        }
    }


    public static String appendErrCode(String code, String subCode){
        return StringUtils.defaultIfEmpty(subCode, code); //优先： subCode
    }

    /**
     * 解析支付宝授权回调中拼接的 state 参数（格式：ISVNO_MCHAPPID）。
     *
     * 历史上 controller 直接写 split("_")[0]/[1]，没做边界校验：
     * 空串、不含 "_"、或者 "_" 在首尾的输入都会触发 ArrayIndexOutOfBoundsException
     * 或得到空字符串，进而被恶意构造的请求触发回调链路 500。
     *
     * 使用 indexOf 取第一个 "_" 切分，而不是 split，避免 mchAppId 内部含 "_" 时被切碎。
     *
     * @return ImmutablePair(isvNo, mchAppId)；解析失败返回 null，由调用方决定如何提示
     */
    public static Pair<String, String> parseIsvAndMchAppIdState(String state) {
        if (StringUtils.isEmpty(state)) {
            return null;
        }
        int idx = state.indexOf('_');
        // idx <= 0：没有 "_" 或 "_" 在最前面（isvNo 为空）
        // idx >= state.length() - 1：" _" 在最后（mchAppId 为空）
        if (idx <= 0 || idx >= state.length() - 1) {
            return null;
        }
        return ImmutablePair.of(state.substring(0, idx), state.substring(idx + 1));
    }

    public static String appendErrMsg(String msg, String subMsg){

        String result = null;
        if(StringUtils.isNotEmpty(msg) && StringUtils.isNotEmpty(subMsg) ){
            result = msg + "【" + subMsg + "】";
        }else{
            result = StringUtils.defaultIfEmpty(subMsg, msg);
        }
        return CharSequenceUtil.maxLength(result, 253);
    }

}
