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
package com.jeequan.jeepay.pay.channel.ysfpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.ysf.YsfpayConfig;
import com.jeequan.jeepay.core.model.params.ysf.YsfpayIsvParams;
import com.jeequan.jeepay.core.model.params.ysf.YsfpayIsvsubMchParams;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.channel.ysfpay.utils.YsfHttpUtil;
import com.jeequan.jeepay.pay.channel.ysfpay.utils.YsfSignUtils;
import com.jeequan.jeepay.pay.model.IsvConfigContext;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.util.PaywayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 云闪付下单
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Service
@Slf4j
public class YsfpayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
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
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode()).pay(rq, payOrder, mchAppConfigContext);
    }


    /** 封装参数 & 统一请求 **/
    public JSONObject packageParamAndReq(String apiUri, JSONObject reqParams, String logPrefix, MchAppConfigContext mchAppConfigContext) throws Exception {

        YsfpayIsvParams isvParams = (YsfpayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), getIfCode());

        if (isvParams.getSerProvId() == null) {
            log.error("服务商配置为空：isvParams：{}", isvParams);
            throw new BizException("服务商配置为空。");
        }

        reqParams.put("serProvId", isvParams.getSerProvId()); //云闪付服务商标识
        YsfpayIsvsubMchParams isvsubMchParams = (YsfpayIsvsubMchParams) configContextQueryService.queryIsvsubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
        reqParams.put("merId", isvsubMchParams.getMerId()); // 商户号

        //签名
        String isvPrivateCertFile = channelCertConfigKitBean.getCertFilePath(isvParams.getIsvPrivateCertFile());
        String isvPrivateCertPwd = isvParams.getIsvPrivateCertPwd();
        reqParams.put("signature", YsfSignUtils.signBy256(reqParams, isvPrivateCertFile, isvPrivateCertPwd)); //RSA 签名串

        // 调起上游接口
        log.info("{} reqJSON={}", logPrefix, reqParams);
        String resText = YsfHttpUtil.doPostJson(getYsfpayHost4env(isvParams) + apiUri, null, reqParams);
        log.info("{} resJSON={}", logPrefix, resText);

        if(StringUtils.isEmpty(resText)){
            return null;
        }
        return JSONObject.parseObject(resText);
    }

    /** 获取云闪付正式环境/沙箱HOST地址   **/
    public static String getYsfpayHost4env(YsfpayIsvParams isvParams){
        return CS.YES == isvParams.getSandbox() ? YsfpayConfig.SANDBOX_SERVER_URL : YsfpayConfig.PROD_SERVER_URL;
    }

    /** 云闪付 jsapi下单请求统一发送参数 **/
    public static void jsapiParamsSet(JSONObject reqParams, PayOrder payOrder, String notifyUrl, String returnUrl) {
        String orderType = YsfHttpUtil.getOrderTypeByJSapi(payOrder.getWayCode());
        reqParams.put("orderType", orderType); //订单类型： alipayJs-支付宝， wechatJs-微信支付， upJs-银联二维码
        ysfPublicParams(reqParams, payOrder);
        reqParams.put("backUrl", notifyUrl); //交易通知地址
        reqParams.put("frontUrl", returnUrl); //前台通知地址
    }

    /** 云闪付 bar下单请求统一发送参数 **/
    public static void barParamsSet(JSONObject reqParams, PayOrder payOrder) {
        String orderType = YsfHttpUtil.getOrderTypeByBar(payOrder.getWayCode());
        reqParams.put("orderType", orderType); //订单类型： alipay-支付宝， wechat-微信支付， -unionpay银联二维码
        ysfPublicParams(reqParams, payOrder);
        // TODO 终端编号暂时写死
        reqParams.put("termId", "01727367"); // 终端编号
    }

    /** 云闪付公共参数赋值 **/
    public static void ysfPublicParams(JSONObject reqParams, PayOrder payOrder) {
        //获取订单类型
        reqParams.put("orderNo", payOrder.getPayOrderId()); //订单号
        reqParams.put("orderTime", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN)); //订单时间 如：20180702142900
        reqParams.put("txnAmt", payOrder.getAmount()); //交易金额 单位：分，不带小数点
        reqParams.put("currencyCode", "156"); //交易币种 不出现则默认为人民币-156
        reqParams.put("orderInfo", payOrder.getSubject()); //订单信息 订单描述信息，如：京东生鲜食品
    }
}
