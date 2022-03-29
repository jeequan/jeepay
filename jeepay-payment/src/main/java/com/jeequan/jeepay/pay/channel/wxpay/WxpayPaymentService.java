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
package com.jeequan.jeepay.pay.channel.wxpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.service.WxPayService;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayIsvsubMchParams;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.WxServiceWrapper;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.util.PaywayUtil;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
* 支付接口： 微信官方
* 支付方式： 自适应
*
* @author zhuxiao
* @site https://www.jeequan.com
* @date 2021/6/8 18:10
*/
@Service
public class WxpayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).preCheck(rq, payOrder);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        // 微信API版本

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        String apiVersion = wxServiceWrapper.getApiVersion();
        if (CS.PAY_IF_VERSION.WX_V2.equals(apiVersion)) {
            return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
        } else if (CS.PAY_IF_VERSION.WX_V3.equals(apiVersion)) {
            return PaywayUtil.getRealPaywayV3Service(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
        } else {
            throw new BizException("不支持的微信支付API版本");
        }

    }

    /**
     * 构建微信统一下单请求数据
     * @param payOrder
     * @return
     */
    public WxPayUnifiedOrderRequest buildUnifiedOrderRequest(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        String payOrderId = payOrder.getPayOrderId();

        // 微信统一下单请求对象
        WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
        request.setOutTradeNo(payOrderId);
        request.setBody(payOrder.getSubject());
        request.setDetail(payOrder.getBody());
        request.setFeeType("CNY");
        request.setTotalFee(payOrder.getAmount().intValue());
        request.setSpbillCreateIp(payOrder.getClientIp());
        request.setNotifyUrl(getNotifyUrl());
        request.setProductId(System.currentTimeMillis()+"");
        request.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.PURE_DATETIME_PATTERN));

        //订单分账， 将冻结商户资金。
        if(isDivisionOrder(payOrder)){
            request.setProfitSharing("Y");
        }

        // 特约商户
        if(mchAppConfigContext.isIsvsubMch()){
            WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
            request.setSubMchId(isvsubMchParams.getSubMchId());
            if (StringUtils.isNotBlank(isvsubMchParams.getSubMchAppId())) {
                request.setSubAppId(isvsubMchParams.getSubMchAppId());
            }
        }

        return request;
    }

    /**
     * 构建微信APIV3接口  统一下单请求数据
     * @param payOrder
     * @return
     */
    public JSONObject buildV3OrderRequest(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
        String payOrderId = payOrder.getPayOrderId();

        // 微信统一下单请求对象
        JSONObject reqJSON = new JSONObject();
        reqJSON.put("out_trade_no", payOrderId);
        reqJSON.put("description", payOrder.getSubject());
        // 订单失效时间，遵循rfc3339标准格式，格式为yyyy-MM-DDTHH:mm:ss+TIMEZONE,示例值：2018-06-08T10:34:56+08:00
        reqJSON.put("time_expire", String.format("%sT%s+08:00", DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATE_FORMAT), DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_TIME_FORMAT)));

        reqJSON.put("notify_url", getNotifyUrl(payOrderId));

        JSONObject amount = new JSONObject();
        amount.put("total", payOrder.getAmount().intValue());
        amount.put("currency", "CNY");
        reqJSON.put("amount", amount);

        JSONObject sceneInfo = new JSONObject();
        sceneInfo.put("payer_client_ip", payOrder.getClientIp());
        reqJSON.put("scene_info", sceneInfo);

        //订单分账， 将冻结商户资金。
        if(isDivisionOrder(payOrder)){
           JSONObject settleInfo = new JSONObject();
           settleInfo.put("profit_sharing", true);
           reqJSON.put("settle_info", settleInfo);
        }

        WxPayService wxPayService = configContextQueryService.getWxServiceWrapper(mchAppConfigContext).getWxPayService();
        if(mchAppConfigContext.isIsvsubMch()){ // 特约商户

            WxpayIsvsubMchParams isvsubMchParams = (WxpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
            reqJSON.put("sp_appid", wxPayService.getConfig().getAppId());
            reqJSON.put("sp_mchid", wxPayService.getConfig().getMchId());
            reqJSON.put("sub_mchid", isvsubMchParams.getSubMchId());
            if (StringUtils.isNotBlank(isvsubMchParams.getSubMchAppId())) {
                reqJSON.put("sub_appid", isvsubMchParams.getSubMchAppId());
            }
        }else { // 普通商户
            reqJSON.put("appid", wxPayService.getConfig().getAppId());
            reqJSON.put("mchid", wxPayService.getConfig().getMchId());
        }

        return reqJSON;
    }

}
