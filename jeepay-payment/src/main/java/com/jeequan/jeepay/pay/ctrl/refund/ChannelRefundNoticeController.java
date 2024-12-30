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

import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IChannelRefundNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.pay.service.RefundOrderProcessService;
import com.jeequan.jeepay.service.impl.RefundOrderService;
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

/*
* 渠道侧的退款通知入口Controller 【异步回调(doNotify) 】
*
* @author jmdhappy
* @site https://www.jeequan.com
* @date 2021/9/25 22:35
*/
@Slf4j
@Controller
public class ChannelRefundNoticeController extends AbstractCtrl {

    @Autowired private RefundOrderService refundOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private RefundOrderProcessService refundOrderProcessService;

    /** 异步回调入口 **/
    @ResponseBody
    @RequestMapping(value= {"/api/refund/notify/{ifCode}", "/api/refund/notify/{ifCode}/{refundOrderId}"})
    public ResponseEntity doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "refundOrderId", required = false) String urlOrderId){

        String refundOrderId = null;
        String logPrefix = "进入[" +ifCode+ "]退款回调：urlOrderId：["+ StringUtils.defaultIfEmpty(urlOrderId, "") + "] ";
        log.info("===== {} =====" , logPrefix);

        try {

            // 参数有误
            if(StringUtils.isEmpty(ifCode)){
                return ResponseEntity.badRequest().body("ifCode is empty");
            }

            //查询退款接口是否存在
            IChannelRefundNoticeService refundNotifyService = SpringBeansUtil.getBean(ifCode + "ChannelRefundNoticeService", IChannelRefundNoticeService.class);

            // 支付通道接口实现不存在
            if(refundNotifyService == null){
                log.error("{}, interface not exists ", logPrefix);
                return ResponseEntity.badRequest().body("[" + ifCode + "] interface not exists");
            }

            // 解析订单号 和 请求参数
            MutablePair<String, Object> mutablePair = refundNotifyService.parseParams(request, urlOrderId, IChannelRefundNoticeService.NoticeTypeEnum.DO_NOTIFY);
            if(mutablePair == null){ // 解析数据失败， 响应已处理
                log.error("{}, mutablePair is null ", logPrefix);
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            // 解析到订单号
            refundOrderId = mutablePair.left;
            log.info("{}, 解析数据为：refundOrderId:{}, params:{}", logPrefix, refundOrderId, mutablePair.getRight());

            if(StringUtils.isNotEmpty(urlOrderId) && !urlOrderId.equals(refundOrderId)){
                log.error("{}, 订单号不匹配. urlOrderId={}, refundOrderId={} ", logPrefix, urlOrderId, refundOrderId);
                throw new BizException("退款单号不匹配！");
            }

            //获取订单号 和 订单数据
            RefundOrder refundOrder = refundOrderService.getById(refundOrderId);

            // 订单不存在
            if(refundOrder == null){
                log.error("{}, 退款订单不存在. refundOrder={} ", logPrefix, refundOrder);
                return refundNotifyService.doNotifyOrderNotExists(request);
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(refundOrder.getMchNo(), refundOrder.getAppId());

            //调起接口的回调判断
            ChannelRetMsg notifyResult = refundNotifyService.doNotice(request, mutablePair.getRight(), refundOrder, mchAppConfigContext, IChannelRefundNoticeService.NoticeTypeEnum.DO_NOTIFY);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if(notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null){
                log.error("{}, 处理回调事件异常  notifyResult data error, notifyResult ={} ",logPrefix, notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }
            // 处理退款订单
            boolean updateOrderSuccess = refundOrderProcessService.handleRefundOrder4Channel(notifyResult, refundOrder);

            // 更新退款订单 异常
            if(!updateOrderSuccess){
                log.error("{}, updateOrderSuccess = {} ",logPrefix, updateOrderSuccess);
                return refundNotifyService.doNotifyOrderStateUpdateFail(request);
            }

            log.info("===== {}, 订单通知完成。 refundOrderId={}, parseState = {} =====", logPrefix, refundOrderId, notifyResult.getChannelState());

            return notifyResult.getResponseEntity();

        } catch (BizException e) {
            log.error("{}, refundOrderId={}, BizException", logPrefix, refundOrderId, e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseException e) {
            log.error("{}, refundOrderId={}, ResponseException", logPrefix, refundOrderId, e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error("{}, refundOrderId={}, 系统异常", logPrefix, refundOrderId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
