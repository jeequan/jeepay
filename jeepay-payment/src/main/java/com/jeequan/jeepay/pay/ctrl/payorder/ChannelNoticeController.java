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
package com.jeequan.jeepay.pay.ctrl.payorder;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/*
* 渠道侧的通知入口Controller 【分为同步跳转（doReturn）和异步回调(doNotify) 】
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:26
*/
@Slf4j
@Controller
public class ChannelNoticeController extends AbstractCtrl {

    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private PayOrderProcessService payOrderProcessService;

    /**
     * 同步通知入口
     *
     * payOrderId 前缀为 ONLYJUMP_，表示直接跳转
     **/
    @RequestMapping(value= {"/api/pay/return/{ifCode}", "/api/pay/return/{ifCode}/{payOrderId}"})
    public String doReturn(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "payOrderId", required = false) String urlOrderId){

        String payOrderId = null;
        String logPrefix = "进入[" +ifCode+ "]支付同步跳转：urlOrderId：["+ StringUtils.defaultIfEmpty(urlOrderId, "") + "] ";
        log.info("===== {} =====" , logPrefix);

        try {

            // 参数有误
            if(StringUtils.isEmpty(ifCode)){
                return this.toReturnPage("ifCode is empty");
            }

            //查询支付接口是否存在
            IChannelNoticeService payNotifyService = SpringBeansUtil.getBean(ifCode + "ChannelNoticeService", IChannelNoticeService.class);

            // 支付通道接口实现不存在
            if(payNotifyService == null){
                log.error("{}, interface not exists ", logPrefix);
                return this.toReturnPage("[" + ifCode + "] interface not exists");
            }

            // 仅做跳转，直接跳转订单的returnUrl
            onlyJump(urlOrderId, logPrefix);

            // 解析订单号 和 请求参数
            MutablePair<String, Object> mutablePair = payNotifyService.parseParams(request, urlOrderId, IChannelNoticeService.NoticeTypeEnum.DO_RETURN);
            if(mutablePair == null){ // 解析数据失败， 响应已处理
                log.error("{}, mutablePair is null ", logPrefix);
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            //解析到订单号
            payOrderId = mutablePair.left;
            log.info("{}, 解析数据为：payOrderId:{}, params:{}", logPrefix, payOrderId, mutablePair.getRight());

            if(StringUtils.isNotEmpty(urlOrderId) && !urlOrderId.equals(payOrderId)){
                log.error("{}, 订单号不匹配. urlOrderId={}, payOrderId={} ", logPrefix, urlOrderId, payOrderId);
                throw new BizException("订单号不匹配！");
            }

            //获取订单号 和 订单数据
            PayOrder payOrder = payOrderService.getById(payOrderId);

            // 订单不存在
            if(payOrder == null){
                log.error("{}, 订单不存在. payOrderId={} ", logPrefix, payOrderId);
                return this.toReturnPage("支付订单不存在");
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());

            //调起接口的回调判断
            ChannelRetMsg notifyResult = payNotifyService.doNotice(request, mutablePair.getRight(), payOrder, mchAppConfigContext, IChannelNoticeService.NoticeTypeEnum.DO_RETURN);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if(notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null){
                log.error("{}, 处理回调事件异常  notifyResult data error, notifyResult ={} ",logPrefix, notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            //判断订单状态
            if(notifyResult.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                payOrder.setState(PayOrder.STATE_SUCCESS);
            }else if(notifyResult.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL) {
                payOrder.setState(PayOrder.STATE_FAIL);
            }

            boolean hasReturnUrl = StringUtils.isNotBlank(payOrder.getReturnUrl());
            log.info("===== {}, 订单通知完成。 payOrderId={}, parseState = {}, hasReturnUrl={} =====", logPrefix, payOrderId, notifyResult.getChannelState(), hasReturnUrl);

            //包含通知地址时
            if(hasReturnUrl){
                // 重定向
                response.sendRedirect(payMchNotifyService.createReturnUrl(payOrder, mchAppConfigContext.getMchApp().getAppSecret()));
                return null;
            }else{

                //跳转到支付成功页面
                return this.toReturnPage(null);
            }

        } catch (BizException e) {
            log.error("{}, payOrderId={}, BizException", logPrefix, payOrderId, e);
            return this.toReturnPage(e.getMessage());

        } catch (ResponseException e) {
            log.error("{}, payOrderId={}, ResponseException", logPrefix, payOrderId, e);
            return this.toReturnPage(e.getMessage());

        } catch (Exception e) {
            log.error("{}, payOrderId={}, 系统异常", logPrefix, payOrderId, e);
            return this.toReturnPage(e.getMessage());
        }
    }

    /** 异步回调入口 **/
    @ResponseBody
    @RequestMapping(value= {"/api/pay/notify/{ifCode}", "/api/pay/notify/{ifCode}/{payOrderId}"})
    public ResponseEntity doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "payOrderId", required = false) String urlOrderId){

        String payOrderId = null;
        String logPrefix = "进入[" +ifCode+ "]支付回调：urlOrderId：["+ StringUtils.defaultIfEmpty(urlOrderId, "") + "] ";
        log.info("===== {} =====" , logPrefix);

        try {

            // 参数有误
            if(StringUtils.isEmpty(ifCode)){
                return ResponseEntity.badRequest().body("ifCode is empty");
            }

            //查询支付接口是否存在
            IChannelNoticeService payNotifyService = SpringBeansUtil.getBean(ifCode + "ChannelNoticeService", IChannelNoticeService.class);

            // 支付通道接口实现不存在
            if(payNotifyService == null){
                log.error("{}, interface not exists ", logPrefix);
                return ResponseEntity.badRequest().body("[" + ifCode + "] interface not exists");
            }

            // 解析订单号 和 请求参数
            MutablePair<String, Object> mutablePair = payNotifyService.parseParams(request, urlOrderId, IChannelNoticeService.NoticeTypeEnum.DO_NOTIFY);
            if(mutablePair == null){ // 解析数据失败， 响应已处理
                log.error("{}, mutablePair is null ", logPrefix);
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            //解析到订单号
            payOrderId = mutablePair.left;
            log.info("{}, 解析数据为：payOrderId:{}, params:{}", logPrefix, payOrderId, mutablePair.getRight());

            if(StringUtils.isNotEmpty(urlOrderId) && !urlOrderId.equals(payOrderId)){
                log.error("{}, 订单号不匹配. urlOrderId={}, payOrderId={} ", logPrefix, urlOrderId, payOrderId);
                throw new BizException("订单号不匹配！");
            }

            //获取订单号 和 订单数据
            PayOrder payOrder = payOrderService.getById(payOrderId);

            // 订单不存在
            if(payOrder == null){
                log.error("{}, 订单不存在. payOrderId={} ", logPrefix, payOrderId);
                return payNotifyService.doNotifyOrderNotExists(request);
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());


            //调起接口的回调判断
            ChannelRetMsg notifyResult = payNotifyService.doNotice(request, mutablePair.getRight(), payOrder, mchAppConfigContext, IChannelNoticeService.NoticeTypeEnum.DO_NOTIFY);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if(notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null){
                log.error("{}, 处理回调事件异常  notifyResult data error, notifyResult ={} ",logPrefix, notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            boolean updateOrderSuccess = true; //默认更新成功
            // 订单是 【支付中状态】
            if(payOrder.getState() == PayOrder.STATE_ING) {

                //明确成功
                if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == notifyResult.getChannelState()) {

                    updateOrderSuccess = payOrderService.updateIng2Success(payOrderId, notifyResult.getChannelOrderId(), notifyResult.getChannelUserId());

                    //明确失败
                }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == notifyResult.getChannelState()) {

                    updateOrderSuccess = payOrderService.updateIng2Fail(payOrderId, notifyResult.getChannelOrderId(), notifyResult.getChannelUserId(), notifyResult.getChannelErrCode(), notifyResult.getChannelErrMsg());
                }
            }

            // 更新订单 异常
            if(!updateOrderSuccess){
                log.error("{}, updateOrderSuccess = {} ",logPrefix, updateOrderSuccess);
                return payNotifyService.doNotifyOrderStateUpdateFail(request);
            }

            //订单支付成功 其他业务逻辑
            if(notifyResult.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS){
                payOrderProcessService.confirmSuccess(payOrder);
            }

            log.info("===== {}, 订单通知完成。 payOrderId={}, parseState = {} =====", logPrefix, payOrderId, notifyResult.getChannelState());

            return notifyResult.getResponseEntity();

        } catch (BizException e) {
            log.error("{}, payOrderId={}, BizException", logPrefix, payOrderId, e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseException e) {
            log.error("{}, payOrderId={}, ResponseException", logPrefix, payOrderId, e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error("{}, payOrderId={}, 系统异常", logPrefix, payOrderId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /*  跳转到支付成功页面 **/
    private String toReturnPage(String errInfo){


        return "cashier/returnPage";
    }

    private void onlyJump(String urlOrderId, String logPrefix) throws IOException {

        if (StringUtils.isNotBlank(urlOrderId) && urlOrderId.startsWith(CS.PAY_RETURNURL_FIX_ONLY_JUMP_PREFIX)) {

            String payOrderId = urlOrderId.substring(CS.PAY_RETURNURL_FIX_ONLY_JUMP_PREFIX.length());

            //获取订单号 和 订单数据
            PayOrder payOrder = payOrderService.getById(payOrderId);

            // 订单不存在
            if(payOrder == null){
                log.error("{}, 订单不存在. payOrderId={} ", logPrefix, payOrderId);
                this.toReturnPage("支付订单不存在");
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());

            if (StringUtils.isBlank(payOrder.getReturnUrl())) {
                this.toReturnPage(null);
            }
            response.sendRedirect(payMchNotifyService.createReturnUrl(payOrder, mchAppConfigContext.getMchApp().getAppSecret()));
        }
    }

}
