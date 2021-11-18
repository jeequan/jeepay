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
package com.jeequan.jeepay.pay.ctrl.qr;

import com.alipay.api.AlipayApiException;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.channel.IChannelUserService;
import com.jeequan.jeepay.pay.ctrl.payorder.AbstractPayOrderController;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliJsapiOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.WxJsapiOrderRQ;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 聚合码支付二维码收银台controller
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:27
*/
@RestController
@RequestMapping("/api/cashier")
public class QrCashierController extends AbstractPayOrderController {

    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private PayMchNotifyService payMchNotifyService;

    /**
     * 返回 oauth2【获取uerId跳转地址】
     * **/
    @PostMapping("/redirectUrl")
    public ApiRes redirectUrl(){

        //查询订单
        PayOrder payOrder = getPayOrder();

        //回调地址
        String redirectUrlEncode = sysConfigService.getDBApplicationConfig().genOauth2RedirectUrlEncode(payOrder.getPayOrderId());

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());

        //获取接口并返回数据
        IChannelUserService channelUserService = getServiceByWayCode(getWayCode(), "ChannelUserService", IChannelUserService.class);
        return ApiRes.ok(channelUserService.buildUserRedirectUrl(redirectUrlEncode, mchAppConfigContext));

    }

    /**
     * 获取userId
     * **/
    @PostMapping("/channelUserId")
    public ApiRes channelUserId() throws Exception {

        //查询订单
        PayOrder payOrder = getPayOrder();

        String wayCode = getWayCode();

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
        IChannelUserService channelUserService = getServiceByWayCode(wayCode, "ChannelUserService", IChannelUserService.class);
        return ApiRes.ok(channelUserService.getChannelUserId(getReqParamJSON(), mchAppConfigContext));

    }


    /**
     * 获取订单支付信息
     * **/
    @PostMapping("/payOrderInfo")
    public ApiRes payOrderInfo() throws Exception {

        //查询订单
        PayOrder payOrder = getPayOrder();

        PayOrder resOrder = new PayOrder();
        resOrder.setPayOrderId(payOrder.getPayOrderId());
        resOrder.setMchOrderNo(payOrder.getMchOrderNo());
        resOrder.setMchName(payOrder.getMchName());
        resOrder.setAmount(payOrder.getAmount());
        resOrder.setReturnUrl(payMchNotifyService.createReturnUrl(payOrder, configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId()).getMchApp().getAppSecret()));
        return ApiRes.ok(resOrder);
    }


    /** 调起下单接口, 返回支付数据包  **/
    @PostMapping("/pay")
    public ApiRes pay() throws Exception {

        //查询订单
        PayOrder payOrder = getPayOrder();

        String wayCode = getWayCode();

        ApiRes apiRes = null;

        if(wayCode.equals(CS.PAY_WAY_CODE.ALI_JSAPI)){
            apiRes = packageAlipayPayPackage(payOrder);
        }else if(wayCode.equals(CS.PAY_WAY_CODE.WX_JSAPI)){
            apiRes = packageWxpayPayPackage(payOrder);
        }

        return ApiRes.ok(apiRes);
    }


    /** 获取支付宝的 支付参数 **/
    private ApiRes packageAlipayPayPackage(PayOrder payOrder) throws AlipayApiException {

        String channelUserId = getValStringRequired("channelUserId");
        AliJsapiOrderRQ rq = new AliJsapiOrderRQ();
        rq.setBuyerUserId(channelUserId);
        return this.unifiedOrder(getWayCode(), rq, payOrder);
    }


    /** 获取微信的 支付参数 **/
    private ApiRes packageWxpayPayPackage(PayOrder payOrder) throws AlipayApiException {

        String openId = getValStringRequired("channelUserId");
        WxJsapiOrderRQ rq = new WxJsapiOrderRQ();
        rq.setOpenid(openId);
        return this.unifiedOrder(getWayCode(), rq, payOrder);
    }


    private String getToken(){
        return getValStringRequired("token");
    }

    private String getWayCode(){
        return getValStringRequired("wayCode");
    }

    private PayOrder getPayOrder(){

        String payOrderId = JeepayKit.aesDecode(getToken()); //解析token

        PayOrder payOrder = payOrderService.getById(payOrderId);
        if(payOrder == null || payOrder.getState() != PayOrder.STATE_INIT){
            throw new BizException("订单不存在或状态不正确");
        }

        return payOrderService.getById(payOrderId);
    }


    private <T> T getServiceByWayCode(String wayCode, String serviceSuffix, Class<T> cls){

        if(CS.PAY_WAY_CODE.ALI_JSAPI.equals(wayCode)){
            return SpringBeansUtil.getBean(CS.IF_CODE.ALIPAY + serviceSuffix, cls);
        }else if(CS.PAY_WAY_CODE.WX_JSAPI.equals(wayCode)){
            return SpringBeansUtil.getBean(CS.IF_CODE.WXPAY + serviceSuffix, cls);
        }

        return null;
    }






}
