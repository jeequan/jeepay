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
package com.jeequan.jeepay.pay.channel.xxpay.payway;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.xxpay.XxpayNormalMchParams;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.pay.channel.xxpay.XxpayKit;
import com.jeequan.jeepay.pay.channel.xxpay.XxpayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliBarOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliBarOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

/*
 * 小新支付 支付宝条码支付
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/9/20 10:09
 */
@Service("xxpayPaymentByAliBarService") //Service Name需保持全局唯一性
@Slf4j
public class AliBar extends XxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        AliBarOrderRQ bizRQ = (AliBarOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            throw new BizException("用户支付条码[authCode]不可为空");
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext){

        AliBarOrderRQ bizRQ = (AliBarOrderRQ) rq;
        XxpayNormalMchParams params = mchAppConfigContext.getNormalMchParamsByIfCode(getIfCode(), XxpayNormalMchParams.class);
        Map<String,Object> paramMap = new TreeMap();
        // 接口类型
        paramMap.put("mchId", params.getMchId());
        paramMap.put("productId", "8021"); // 支付宝条码
        paramMap.put("mchOrderNo", payOrder.getPayOrderId());
        paramMap.put("amount", payOrder.getAmount() + "");
        paramMap.put("currency", "cny");
        paramMap.put("clientIp", payOrder.getClientIp());
        paramMap.put("device", "web");
        paramMap.put("returnUrl", getReturnUrl());
        paramMap.put("notifyUrl", getNotifyUrl(payOrder.getPayOrderId()));
        paramMap.put("subject", payOrder.getSubject());
        paramMap.put("body", payOrder.getBody());
        paramMap.put("extra", bizRQ.getAuthCode());

        String sign = XxpayKit.getSign(paramMap, params.getKey());
        paramMap.put("sign", sign);

        // 构造函数响应数据
        AliBarOrderRS res = ApiResBuilder.buildSuccess(AliBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 设置支付下单地址
        String payUrl = XxpayKit.getPaymentUrl(params.getPayUrl());
        String resStr = "";
        try {
            resStr = HttpUtil.createPost(payUrl + "?" + JeepayKit.genUrlParams(paramMap)).timeout(60 * 1000).execute().body();
        } catch (Exception e) {
            log.error("http error", e);
        }

        if(StringUtils.isEmpty(resStr)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("");
            channelRetMsg.setChannelErrMsg("请求"+getIfCode()+"接口异常");
            return res;
        }

        JSONObject resObj = JSONObject.parseObject(resStr);

        if(!"0".equals(resObj.getString("retCode"))){
            String retMsg = resObj.getString("retMsg");
            log.error("请求"+getIfCode()+"返回结果异常， resObj={}", resObj.toJSONString());
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("");
            channelRetMsg.setChannelErrMsg(retMsg);
            return res;
        }

        // 验证响应数据签名
        String checkSign = resObj.getString("sign");
        resObj.remove("sign");
        if(!checkSign.equals(XxpayKit.getSign(resObj, params.getKey()))) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            return res;
        }

        // 订单状态-2:订单已关闭,0-订单生成,1-支付中,2-支付成功,3-业务处理完成,4-已退款
        String orderStatus = resObj.getString("orderStatus");
        if("2".equals(orderStatus) || "3".equals(orderStatus)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        }else {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        }
        return res;

    }

}
