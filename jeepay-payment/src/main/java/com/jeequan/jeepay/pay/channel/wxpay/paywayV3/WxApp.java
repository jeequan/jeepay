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
package com.jeequan.jeepay.pay.channel.wxpay.paywayV3;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.v3.util.PemUtils;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.pay.channel.wxpay.WxpayPaymentService;
import com.jeequan.jeepay.pay.channel.wxpay.kits.WxpayKit;
import com.jeequan.jeepay.pay.channel.wxpay.kits.WxpayV3Util;
import com.jeequan.jeepay.pay.channel.wxpay.model.WxpayV3OrderRequestModel;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.model.WxServiceWrapper;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.WxAppOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * 微信 app支付
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:08
 */
@Service("wxpayPaymentByAppV3Service") //Service Name需保持全局唯一性
public class WxApp extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();

        // 构造请求数据
        WxpayV3OrderRequestModel wxpayV3OrderRequestModel = buildV3OrderRequestModel(payOrder, mchAppConfigContext);

        // 构造函数响应数据
        WxAppOrderRS res = ApiResBuilder.buildSuccess(WxAppOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        try {
            String payInfo = WxpayV3Util.commonReqWx(wxpayV3OrderRequestModel, wxPayService, mchAppConfigContext.isIsvsubMch(), WxPayConstants.TradeType.APP,
                    (JSONObject wxRes) -> {

                        // 普通商户，App支付与公众号支付  同一个应用只能配置其中一个
                        String resultAppId = wxpayV3OrderRequestModel.getNormalAppid();
                        String resultMchId = wxpayV3OrderRequestModel.getNormalMchid();

                        // 特约商户，App支付与公众号支付  同一个应用只能配置其中一个
                        if(mchAppConfigContext.isIsvsubMch()){
                            resultAppId = wxpayV3OrderRequestModel.getSubAppid();
                            resultMchId = wxpayV3OrderRequestModel.getSubMchid();
                        }

                        WxPayUnifiedOrderV3Result wxPayUnifiedOrderV3Result = new WxPayUnifiedOrderV3Result();
                        wxPayUnifiedOrderV3Result.setPrepayId(wxRes.getString("prepay_id"));

                        try {

                            FileInputStream fis = new FileInputStream(wxPayService.getConfig().getPrivateKeyPath());

                            WxPayUnifiedOrderV3Result.AppResult appResult =
                                    wxPayUnifiedOrderV3Result.getPayInfo(TradeTypeEnum.APP, resultAppId, resultMchId,
                                            PemUtils.loadPrivateKey(fis));

                            JSONObject jsonRes = (JSONObject) JSON.toJSON(appResult);
                            jsonRes.put("package", jsonRes.getString("packageValue"));
                            jsonRes.remove("packageValue");

                            try {
                                fis.close();
                            } catch (IOException e) {
                            }

                            return JSON.toJSONString(jsonRes);

                        } catch (FileNotFoundException e) {

                            return null;

                        }
                    }
            );

            res.setPayInfo(payInfo);

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            //明确失败
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;
    }

}
