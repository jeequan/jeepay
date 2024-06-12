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
package com.jeequan.jeepay.pay.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.alipay.AlipayKit;
import com.jeequan.jeepay.pay.channel.alipay.AlipayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliOcOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliOcOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import org.springframework.stereotype.Service;

/**
 * 支付宝 订单码支付
 *
 * @author zhangheming
 * @site https://www.jeequan.com
 * @date 2024/6/12 17:20
 */
@Service("alipayPaymentByAliOcService")
public class AliOc extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        AliOcOrderRQ ocOrderRQ = (AliOcOrderRQ) rq;
        // 构造请求参数以调用接口
        AlipayTradePrecreateRequest req = new AlipayTradePrecreateRequest();
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        //设置商户订单号
        model.setOutTradeNo(payOrder.getPayOrderId());
        //设置订单总金额
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        // 设置订单标题
        model.setSubject(payOrder.getSubject());
        // 设置产品码
        model.setProductCode("QR_CODE_OFFLINE");
        // 设置订单附加信息
        model.setBody(payOrder.getBody());
        //设置订单超时时间
        model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));
        //设置异步通知地址
        req.setNotifyUrl(getNotifyUrl());
        req.setBizModel(model);
        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);
        //调起支付宝 （如果异常， 将直接跑出   ChannelException ）
        AlipayTradePrecreateResponse alipayResp = configContextQueryService.
                getAlipayClientWrapper(mchAppConfigContext).execute(req);
        AliOcOrderRS res = ApiResBuilder.buildSuccess(AliOcOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        channelRetMsg.setChannelAttach(alipayResp.getBody());
        if (alipayResp.isSuccess()) { //处理成功
            if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(ocOrderRQ.getPayDataType())) { //二维码地址
                res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(alipayResp.getQrCode()));
            } else { //默认都为跳转地址方式
                res.setCodeUrl(alipayResp.getQrCode());
            }
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        } else {  //其他状态, 表示下单失败
            res.setOrderState(PayOrder.STATE_FAIL);  //支付失败
            channelRetMsg.setChannelErrCode(AlipayKit.appendErrCode(alipayResp.getCode(), alipayResp.getSubCode()));
            channelRetMsg.setChannelErrMsg(AlipayKit.appendErrMsg(alipayResp.getMsg(), alipayResp.getSubMsg()));
        }
        return res;
    }

}
