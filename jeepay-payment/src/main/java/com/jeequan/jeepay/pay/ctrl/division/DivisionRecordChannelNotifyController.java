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
package com.jeequan.jeepay.pay.ctrl.division;

import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.AbstractDivisionRecordChannelNotifyService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.msg.DivisionChannelNotifyModel;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import com.jeequan.jeepay.service.impl.PayOrderDivisionRecordService;
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
import java.util.List;

/*
* 分账渠道侧的通知入口Controller
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2023/3/29 15:35
*/
@Slf4j
@Controller
public class DivisionRecordChannelNotifyController extends AbstractCtrl {

    @Autowired private PayOrderDivisionRecordService payOrderDivisionRecordService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayOrderProcessService payOrderProcessService;


    /** 异步回调入口 **/
    @ResponseBody
    @RequestMapping(value= {"/api/divisionRecordChannelNotify/{ifCode}"})
    public ResponseEntity doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode){

        String divisionBatchId = null;
        String logPrefix = "进入[" +ifCode+ "]分账回调";
        log.info("===== {} =====" , logPrefix);

        try {

            // 参数有误
            if(StringUtils.isEmpty(ifCode)){
                return ResponseEntity.badRequest().body("ifCode is empty");
            }

            //查询支付接口是否存在
            AbstractDivisionRecordChannelNotifyService divisionNotifyService = SpringBeansUtil.getBean(ifCode + "DivisionRecordChannelNotifyService", AbstractDivisionRecordChannelNotifyService.class);

            // 支付通道接口实现不存在
            if(divisionNotifyService == null){
                log.error("{}, interface not exists ", logPrefix);
                return ResponseEntity.badRequest().body("[" + ifCode + "] interface not exists");
            }

            // 解析批次号 和 请求参数
            MutablePair<String, Object> mutablePair = divisionNotifyService.parseParams(request);
            if(mutablePair == null){ // 解析数据失败， 响应已处理
                log.error("{}, mutablePair is null ", logPrefix);
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            //解析到订单号
            divisionBatchId = mutablePair.left;
            log.info("{}, 解析数据为：divisionBatchId:{}, params:{}", logPrefix, divisionBatchId, mutablePair.getRight());


            // 通过 batchId 查询出列表（ 注意：  需要按照ID 排序！！！！ ）
            List<PayOrderDivisionRecord> recordList = payOrderDivisionRecordService.list(PayOrderDivisionRecord.gw()
                    .eq(PayOrderDivisionRecord::getState, PayOrderDivisionRecord.STATE_ACCEPT)
                    .eq(PayOrderDivisionRecord::getBatchOrderId, divisionBatchId)
                    .orderByAsc(PayOrderDivisionRecord::getRecordId)
            );

            // 订单不存在
            if(recordList == null || recordList.isEmpty()){
                log.error("{}, 待处理订单不存在. divisionBatchId={} ", logPrefix, divisionBatchId);
                return divisionNotifyService.doNotifyOrderNotExists(request);
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(recordList.get(0).getMchNo(), recordList.get(0).getAppId());

            //调起接口的回调判断
            DivisionChannelNotifyModel notifyResult = divisionNotifyService.doNotify(request, mutablePair.getRight(), recordList, mchAppConfigContext);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if(notifyResult == null || notifyResult.getApiRes() == null){
                log.error("{}, 处理回调事件异常  notifyResult data error, notifyResult ={} ",logPrefix, notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            if(notifyResult.getRecordResultMap() != null && !notifyResult.getRecordResultMap().isEmpty()){

                for (Long divisionId : notifyResult.getRecordResultMap().keySet()) {

                    // 单条结果
                    ChannelRetMsg retMsgItem = notifyResult.getRecordResultMap().get(divisionId);

                    // 明确成功
                    if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == retMsgItem.getChannelState()){

                        payOrderDivisionRecordService.updateRecordSuccessOrFailBySingleItem(divisionId, PayOrderDivisionRecord.STATE_SUCCESS, retMsgItem.getChannelOriginResponse());

                    } else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == retMsgItem.getChannelState()){ // 明确失败

                        payOrderDivisionRecordService.updateRecordSuccessOrFailBySingleItem(divisionId, PayOrderDivisionRecord.STATE_FAIL, StringUtils.defaultIfEmpty(retMsgItem.getChannelErrMsg(), retMsgItem.getChannelOriginResponse()));
                    }

                }
            }

            log.info("===== {}, 通知完成。 divisionBatchId={}, parseState = {} =====", logPrefix, divisionBatchId, notifyResult);

            return notifyResult.getApiRes();

        } catch (BizException e) {
            log.error("{}, divisionBatchId={}, BizException", logPrefix, divisionBatchId, e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseException e) {
            log.error("{}, divisionBatchId={}, ResponseException", logPrefix, divisionBatchId, e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error("{}, divisionBatchId={}, 系统异常", logPrefix, divisionBatchId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
