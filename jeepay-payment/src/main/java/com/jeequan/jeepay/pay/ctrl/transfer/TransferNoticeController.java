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
package com.jeequan.jeepay.pay.ctrl.transfer;

import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.ITransferNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.service.impl.TransferOrderService;
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

/**
* 转账异步通知入口Controller
*
* @author zx
* @site https://www.jeequan.com
* @date 2022/12/30 10:26
*/
@Slf4j
@Controller
public class TransferNoticeController extends AbstractCtrl {

    @Autowired private TransferOrderService transferOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayMchNotifyService payMchNotifyService;


    /** 异步回调入口 **/
    @ResponseBody
    @RequestMapping(value= {"/api/transfer/notify/{ifCode}", "/api/transfer/notify/{ifCode}/{transferId}"})
    public ResponseEntity doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "transferId", required = false) String urlOrderId){

        String transferId = null;
        String logPrefix = "进入[" +ifCode+ "]转账回调：urlOrderId：["+ StringUtils.defaultIfEmpty(urlOrderId, "") + "] ";
        log.info("===== {} =====" , logPrefix);

        try {

            // 参数有误
            if(StringUtils.isEmpty(ifCode)){
                return ResponseEntity.badRequest().body("ifCode is empty");
            }

            //查询转账接口是否存在
            ITransferNoticeService transferNotifyService = SpringBeansUtil.getBean(ifCode + "TransferNoticeService", ITransferNoticeService.class);

            // 支付通道转账接口实现不存在
            if(transferNotifyService == null){
                log.error("{}, transfer interface not exists ", logPrefix);
                return ResponseEntity.badRequest().body("[" + ifCode + "] transfer interface not exists");
            }

            // 解析转账单号 和 请求参数
            MutablePair<String, Object> mutablePair = transferNotifyService.parseParams(request, urlOrderId);
            if(mutablePair == null){ // 解析数据失败， 响应已处理
                log.error("{}, mutablePair is null ", logPrefix);
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            // 解析到转账单号
            transferId = mutablePair.left;
            log.info("{}, 解析数据为：transferId:{}, params:{}", logPrefix, transferId, mutablePair.getRight());

            // 获取转账单号 和 转账单数据
            TransferOrder transferOrder = transferOrderService.getById(transferId);

            // 转账单不存在
            if(transferOrder == null){
                log.error("{}, 转账单不存在. transferId={} ", logPrefix, transferId);
                return transferNotifyService.doNotifyOrderNotExists(request);
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(transferOrder.getMchNo(), transferOrder.getAppId());

            //调起接口的回调判断
            ChannelRetMsg notifyResult = transferNotifyService.doNotice(request, mutablePair.getRight(), transferOrder, mchAppConfigContext);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if(notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null){
                log.error("{}, 处理回调事件异常  notifyResult data error, notifyResult ={} ",logPrefix, notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            // 转账单是 【转账中状态】
            if(transferOrder.getState() == TransferOrder.STATE_ING) {
                if(notifyResult.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                    // 转账成功
                    transferOrderService.updateIng2Success(transferId, notifyResult.getChannelOrderId());
                    payMchNotifyService.transferOrderNotify(transferOrderService.getById(transferId));

                }else if(notifyResult.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL){
                    // 转账失败
                    transferOrderService.updateIng2Fail(transferId, notifyResult.getChannelOrderId(), notifyResult.getChannelUserId(), notifyResult.getChannelErrCode());
                    payMchNotifyService.transferOrderNotify(transferOrderService.getById(transferId));
                }
            }

            log.info("===== {}, 转账单通知完成。 transferId={}, parseState = {} =====", logPrefix, transferId, notifyResult.getChannelState());

            return notifyResult.getResponseEntity();

        } catch (BizException e) {
            log.error("{}, transferId={}, BizException", logPrefix, transferId, e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseException e) {
            log.error("{}, transferId={}, ResponseException", logPrefix, transferId, e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error("{}, transferId={}, 系统异常", logPrefix, transferId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
