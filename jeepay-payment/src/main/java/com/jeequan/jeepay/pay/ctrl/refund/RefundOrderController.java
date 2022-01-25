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
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.pay.channel.IRefundService;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRQ;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/*
* 商户发起退款 controller
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/16 15:54
*/
@Slf4j
@RestController
public class RefundOrderController extends ApiController {

    @Autowired private PayOrderService payOrderService;
    @Autowired private RefundOrderService refundOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private ConfigContextQueryService configContextQueryService;


    /** 申请退款 **/
    @PostMapping("/api/refund/refundOrder")
    public ApiRes refundOrder(){


        RefundOrder refundOrder = null;

        //获取参数 & 验签
        RefundOrderRQ rq = getRQByWithMchSign(RefundOrderRQ.class);

        try {

            if(StringUtils.isAllEmpty(rq.getMchOrderNo(), rq.getPayOrderId())){
                throw new BizException("mchOrderNo 和 payOrderId不能同时为空");
            }

            PayOrder payOrder = payOrderService.queryMchOrder(rq.getMchNo(), rq.getPayOrderId(), rq.getMchOrderNo());
            if(payOrder == null){
                throw new BizException("退款订单不存在");
            }

            if(payOrder.getState() != PayOrder.STATE_SUCCESS){
                throw new BizException("订单状态不正确， 无法完成退款");
            }

            if(payOrder.getRefundState() == PayOrder.REFUND_STATE_ALL || payOrder.getRefundAmount() >= payOrder.getAmount()){
                throw new BizException("订单已全额退款，本次申请失败");
            }

            if(payOrder.getRefundAmount() + rq.getRefundAmount() > payOrder.getAmount()){
                throw new BizException("申请金额超出订单可退款余额，请检查退款金额");
            }

            if(refundOrderService.count(RefundOrder.gw().eq(RefundOrder::getPayOrderId, payOrder.getPayOrderId()).eq(RefundOrder::getState, RefundOrder.STATE_ING)) > 0){
                throw new BizException("支付订单具有在途退款申请，请稍后再试");
            }

            //全部退款金额 （退款订单表）
            Long sumSuccessRefundAmount = refundOrderService.getBaseMapper().sumSuccessRefundAmount(payOrder.getPayOrderId());
            if(sumSuccessRefundAmount >= payOrder.getAmount()){
                throw new BizException("退款单已完成全部订单退款，本次申请失败");
            }

            if(sumSuccessRefundAmount + rq.getRefundAmount() > payOrder.getAmount()){
                throw new BizException("申请金额超出订单可退款余额，请检查退款金额");
            }

            String mchNo = rq.getMchNo();
            String appId = rq.getAppId();

            // 校验退款单号是否重复
            if(refundOrderService.count(RefundOrder.gw().eq(RefundOrder::getMchNo, mchNo).eq(RefundOrder::getMchRefundNo, rq.getMchRefundNo())) > 0){
                throw new BizException("商户退款订单号["+rq.getMchRefundNo()+"]已存在");
            }

            if(StringUtils.isNotEmpty(rq.getNotifyUrl()) && !StringKit.isAvailableUrl(rq.getNotifyUrl())){
                throw new BizException("异步通知地址协议仅支持http:// 或 https:// !");
            }

            //获取支付参数 (缓存数据) 和 商户信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);
            if(mchAppConfigContext == null){
                throw new BizException("获取商户应用信息失败");
            }

            MchInfo mchInfo = mchAppConfigContext.getMchInfo();
            MchApp mchApp = mchAppConfigContext.getMchApp();


            //获取退款接口
            IRefundService refundService = SpringBeansUtil.getBean(payOrder.getIfCode() + "RefundService", IRefundService.class);
            if(refundService == null){
                throw new BizException("当前通道不支持退款！");
            }

            refundOrder = genRefundOrder(rq, payOrder, mchInfo, mchApp);

            //退款单入库 退款单状态：生成状态  此时没有和任何上游渠道产生交互。
            refundOrderService.save(refundOrder);

            // 调起退款接口
            ChannelRetMsg channelRetMsg = refundService.refund(rq, refundOrder, payOrder, mchAppConfigContext);


            //处理退款单状态
            this.processChannelMsg(channelRetMsg, refundOrder);

            RefundOrderRS bizRes = RefundOrderRS.buildByRefundOrder(refundOrder);
            return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());


        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());

        } catch (ChannelException e) {

            //处理上游返回数据
            this.processChannelMsg(e.getChannelRetMsg(), refundOrder);

            if(e.getChannelRetMsg().getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR ){
                return ApiRes.customFail(e.getMessage());
            }

            RefundOrderRS bizRes = RefundOrderRS.buildByRefundOrder(refundOrder);
            return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());


        } catch (Exception e) {
            log.error("系统异常：{}", e);
            return ApiRes.customFail("系统异常");
        }

    }

    private RefundOrder genRefundOrder(RefundOrderRQ rq, PayOrder payOrder, MchInfo mchInfo, MchApp mchApp){

        Date nowTime = new Date();
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(SeqKit.genRefundOrderId()); //退款订单号
        refundOrder.setPayOrderId(payOrder.getPayOrderId()); //支付订单号
        refundOrder.setChannelPayOrderNo(payOrder.getChannelOrderNo()); //渠道支付单号
        refundOrder.setMchNo(mchInfo.getMchNo()); //商户号
        refundOrder.setIsvNo(mchInfo.getIsvNo()); //服务商号
        refundOrder.setAppId(mchApp.getAppId()); //商户应用ID
        refundOrder.setMchName(mchInfo.getMchShortName()); //商户名称
        refundOrder.setMchType(mchInfo.getType()); //商户类型
        refundOrder.setMchRefundNo(rq.getMchRefundNo()); //商户退款单号
        refundOrder.setWayCode(payOrder.getWayCode()); //支付方式代码
        refundOrder.setIfCode(payOrder.getIfCode()); //支付接口代码
        refundOrder.setPayAmount(payOrder.getAmount()); //支付金额,单位分
        refundOrder.setRefundAmount(rq.getRefundAmount()); //退款金额,单位分
        refundOrder.setCurrency(rq.getCurrency()); //三位货币代码,人民币:cny
        refundOrder.setState(RefundOrder.STATE_INIT); //退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败
        refundOrder.setClientIp(StringUtils.defaultIfEmpty(rq.getClientIp(), getClientIp())); //客户端IP
        refundOrder.setRefundReason(rq.getRefundReason()); //退款原因
        refundOrder.setChannelOrderNo(null); //渠道订单号
        refundOrder.setErrCode(null); //渠道错误码
        refundOrder.setErrMsg(null); //渠道错误描述
        refundOrder.setChannelExtra(rq.getChannelExtra()); //特定渠道发起时额外参数
        refundOrder.setNotifyUrl(rq.getNotifyUrl()); //通知地址
        refundOrder.setExtParam(rq.getExtParam()); //扩展参数
        refundOrder.setExpiredTime(DateUtil.offsetHour(nowTime, 2)); //订单超时关闭时间 默认两个小时
        refundOrder.setSuccessTime(null); //订单退款成功时间
        refundOrder.setCreatedAt(nowTime); //创建时间

        return refundOrder;
    }


    /** 处理返回的渠道信息，并更新退款单状态
     *  payOrder将对部分信息进行 赋值操作。
     * **/
    private void processChannelMsg(ChannelRetMsg channelRetMsg, RefundOrder refundOrder){

        //对象为空 || 上游返回状态为空， 则无需操作
        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return ;
        }

        //明确成功
        if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(RefundOrder.STATE_SUCCESS, refundOrder, channelRetMsg);
            payMchNotifyService.refundOrderNotify(refundOrder);

            //明确失败
        }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(RefundOrder.STATE_FAIL, refundOrder, channelRetMsg);
            payMchNotifyService.refundOrderNotify(refundOrder);

            // 上游处理中 || 未知 || 上游接口返回异常  退款单为退款中状态
        }else if( ChannelRetMsg.ChannelState.WAITING == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.UNKNOWN == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.API_RET_ERROR == channelRetMsg.getChannelState()

        ){
            this.updateInitOrderStateThrowException(RefundOrder.STATE_ING, refundOrder, channelRetMsg);

            // 系统异常：  退款单不再处理。  为： 生成状态
        }else if( ChannelRetMsg.ChannelState.SYS_ERROR == channelRetMsg.getChannelState() ){

        }else{

            throw new BizException("ChannelState 返回异常！");
        }

    }


    /** 更新退款单状态 --》 退款单生成--》 其他状态  (向外抛出异常) **/
    private void updateInitOrderStateThrowException(byte orderState, RefundOrder refundOrder, ChannelRetMsg channelRetMsg){

        refundOrder.setState(orderState);
        refundOrder.setChannelOrderNo(channelRetMsg.getChannelOrderId());
        refundOrder.setErrCode(channelRetMsg.getChannelErrCode());
        refundOrder.setErrMsg(channelRetMsg.getChannelErrMsg());


        boolean isSuccess = refundOrderService.updateInit2Ing(refundOrder.getRefundOrderId(), channelRetMsg.getChannelOrderId());
        if(!isSuccess){
            throw new BizException("更新退款单异常!");
        }

        isSuccess = refundOrderService.updateIng2SuccessOrFail(refundOrder.getRefundOrderId(), refundOrder.getState(),
                channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
        if(!isSuccess){
            throw new BizException("更新退款单异常!");
        }
    }

}
