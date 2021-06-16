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
package com.jeequan.jeepay.pay.ctrl.refund;

import cn.hutool.core.date.DateUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.pay.channel.IPaymentService;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.ctrl.payorder.AbstractPayOrderController;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.IsvConfigContext;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.mq.queue.MqQueue4ChannelOrderQuery;
import com.jeequan.jeepay.pay.rqrs.QueryPayOrderRQ;
import com.jeequan.jeepay.pay.rqrs.QueryPayOrderRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.QrCashierOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.QrCashierOrderRS;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRQ;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/*
* 商户发起退款 controller
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/16 15:54
*/
@Slf4j
@RestController
public class RefundOrderController extends ApiController {

    @Autowired private PayOrderService payOrderService;
    @Autowired private RefundOrderService refundOrderService;
    @Autowired private ConfigContextService configContextService;

    /** 申请退款 **/
    @PostMapping("/api/refund/refundOrder")
    public ApiRes refundOrder(){

        try {

            //获取参数 & 验签
            RefundOrderRQ rq = getRQByWithMchSign(RefundOrderRQ.class);

            if(StringUtils.isAllEmpty(rq.getMchOrderNo(), rq.getPayOrderId())){
                throw new BizException("mchOrderNo 和 payOrderId不能同时为空");
            }

            PayOrder payOrder = payOrderService.queryMchOrder(rq.getMchNo(), rq.getPayOrderId(), rq.getMchOrderNo());
            if(payOrder == null){
                throw new BizException("订单不存在");
            }

            String mchNo = rq.getMchNo();
            String appId = rq.getAppId();

            // 只有新订单模式，进行校验
            if(refundOrderService.count(RefundOrder.gw().eq(RefundOrder::getMchNo, mchNo).eq(RefundOrder::getMchRefundNo, rq.getMchRefundNo())) > 0){
                throw new BizException("商户退款订单号["+rq.getMchRefundNo()+"]已存在");
            }

            if(StringUtils.isNotEmpty(rq.getNotifyUrl()) && !StringKit.isAvailableUrl(rq.getNotifyUrl())){
                throw new BizException("异步通知地址协议仅支持http:// 或 https:// !");
            }

            //获取支付参数 (缓存数据) 和 商户信息
            MchAppConfigContext mchAppConfigContext = configContextService.getMchAppConfigContext(mchNo, appId);
            if(mchAppConfigContext == null){
                throw new BizException("获取商户应用信息失败");
            }

            MchInfo mchInfo = mchAppConfigContext.getMchInfo();
            MchApp mchApp = mchAppConfigContext.getMchApp();


            //获取支付接口
            // 接口代码 TODO
            IPaymentService paymentService = SpringBeansUtil.getBean(payOrder.getIfCode() + "PaymentService", IPaymentService.class);
            if(paymentService == null){
                throw new BizException("当前通道不支持退款！");
            }


            RefundOrder refundOrder = genPayOrder(rq, payOrder, mchInfo, mchApp);

            //订单入库 订单状态： 生成状态  此时没有和任何上游渠道产生交互。
            refundOrderService.save(refundOrder);

            //调起上游支付接口
//            bizRS = (UnifiedOrderRS) paymentService.pay(bizRQ, payOrder, mchAppConfigContext);

            // 调起退款接口
            ChannelRetMsg channelRetMsg = null;

            RefundOrderRS bizRes = RefundOrderRS.buildByRefundOrder(refundOrder);
            return ApiRes.okWithSign(bizRes, configContextService.getMchAppConfigContext(rq.getMchNo(), rq.getAppId()).getMchApp().getAppSecret());



        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());

        } catch (Exception e) {
            log.error("系统异常：{}", e);
            return ApiRes.customFail("系统异常");
        }

    }

    private RefundOrder genPayOrder(RefundOrderRQ rq, PayOrder payOrder, MchInfo mchInfo, MchApp mchApp){

        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(SeqKit.genPayOrderId()); //退款订单号
        refundOrder.setPayOrderId(payOrder.getPayOrderId()); //支付订单号
        refundOrder.setChannelPayOrderNo(payOrder.getChannelOrderNo()); //渠道支付单号
        refundOrder.setMchNo(mchInfo.getMchNo()); //商户号
        refundOrder.setMchType(mchInfo.getType()); //商户类型
        refundOrder.setMchRefundNo(rq.getMchRefundNo()); //商户退款单号
        refundOrder.setIsvNo(mchInfo.getIsvNo()); //服务商号
        refundOrder.setWayCode(payOrder.getWayCode()); //支付方式代码
        refundOrder.setIfCode(payOrder.getIfCode()); //支付接口代码
        refundOrder.setPayAmount(payOrder.getAmount()); //支付金额,单位分
        refundOrder.setRefundAmount(rq.getAmount()); //退款金额,单位分
        refundOrder.setCurrency(rq.getCurrency()); //三位货币代码,人民币:cny
        refundOrder.setState(null); //退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败
        refundOrder.setResult(null); //退款结果:0-不确认结果,1-等待手动处理,2-确认成功,3-确认失败
        refundOrder.setClientIp(null); //客户端IP
        refundOrder.setRemark(null); //备注
        refundOrder.setChannelOrderNo(null); //渠道订单号
        refundOrder.setChannelErrCode(null); //渠道错误码
        refundOrder.setChannelErrMsg(null); //渠道错误描述
        refundOrder.setChannelExtra(null); //特定渠道发起时额外参数
        refundOrder.setNotifyUrl(null); //通知地址
        refundOrder.setExtParam(null); //扩展参数
        refundOrder.setSuccessTime(null); //订单退款成功时间
        refundOrder.setCreatedAt(new Date()); //创建时间

        return refundOrder;
    }


    /** 处理返回的渠道信息，并更新订单状态
     *  payOrder将对部分信息进行 赋值操作。
     * **/
    private void processChannelMsg(ChannelRetMsg channelRetMsg, PayOrder payOrder){

        //对象为空 || 上游返回状态为空， 则无需操作
        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return ;
        }

        String payOrderId = payOrder.getPayOrderId();

        //明确成功
        if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(PayOrder.STATE_SUCCESS, payOrder, channelRetMsg);
//            payMchNotifyService.payOrderNotify(payOrder);

            //明确失败
        }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(PayOrder.STATE_FAIL, payOrder, channelRetMsg);

            // 上游处理中 || 未知 || 上游接口返回异常  订单为支付中状态
        }else if( ChannelRetMsg.ChannelState.WAITING == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.UNKNOWN == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.API_RET_ERROR == channelRetMsg.getChannelState()

        ){
            this.updateInitOrderStateThrowException(PayOrder.STATE_ING, payOrder, channelRetMsg);

            // 系统异常：  订单不再处理。  为： 生成状态
        }else if( ChannelRetMsg.ChannelState.SYS_ERROR == channelRetMsg.getChannelState()){

        }else{

            throw new BizException("ChannelState 返回异常！");
        }

        //判断是否需要轮询查单
        if(channelRetMsg.isNeedQuery()){
//            mqChannelOrderQueryQueue.send(MqQueue4ChannelOrderQuery.buildMsg(payOrderId, 1), 5 * 1000);
        }

    }


    /** 更新订单状态 --》 订单生成--》 其他状态  (向外抛出异常) **/
    private void updateInitOrderStateThrowException(byte orderState, PayOrder payOrder, ChannelRetMsg channelRetMsg){

        payOrder.setState(orderState);
        payOrder.setChannelOrderNo(channelRetMsg.getChannelOrderId());
        payOrder.setErrCode(channelRetMsg.getChannelErrCode());
        payOrder.setErrMsg(channelRetMsg.getChannelErrMsg());


        boolean isSuccess = payOrderService.updateInit2Ing(payOrder.getPayOrderId(), payOrder.getIfCode(), payOrder.getWayCode());
        if(!isSuccess){
            throw new BizException("更新订单异常!");
        }

        isSuccess = payOrderService.updateIng2SuccessOrFail(payOrder.getPayOrderId(), payOrder.getState(),
                channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
        if(!isSuccess){
            throw new BizException("更新订单异常!");
        }
    }


}
