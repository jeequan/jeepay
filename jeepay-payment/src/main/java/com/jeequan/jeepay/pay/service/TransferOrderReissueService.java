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
package com.jeequan.jeepay.pay.service;

import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.ITransferService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
* 转账补单服务实现类
*
* @author zx
* @site https://www.jeequan.com
* @date 2022/12/29 17:47
*/

@Service
@Slf4j
public class TransferOrderReissueService {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private TransferOrderService transferOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;


    /** 处理转账订单 **/
    public ChannelRetMsg processOrder(TransferOrder transferOrder){

        try {

            String transferId = transferOrder.getTransferId();

            // 查询转账接口是否存在
            ITransferService transferService = SpringBeansUtil.getBean(transferOrder.getIfCode() + "TransferService", ITransferService.class);

            // 支付通道转账接口实现不存在
            if(transferService == null){
                log.error("{} interface not exists!", transferOrder.getIfCode());
                return null;
            }

            // 查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(transferOrder.getMchNo(), transferOrder.getAppId());

            ChannelRetMsg channelRetMsg = transferService.query(transferOrder, mchAppConfigContext);
            if(channelRetMsg == null){
                log.error("channelRetMsg is null");
                return null;
            }

            log.info("补单[{}]查询结果为：{}", transferId, channelRetMsg);

            // 查询成功
            if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                // 转账成功
                transferOrderService.updateIng2Success(transferId, channelRetMsg.getChannelOrderId());
                payMchNotifyService.transferOrderNotify(transferOrderService.getById(transferId));

            }else if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL){
                // 转账失败
                transferOrderService.updateIng2Fail(transferId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(),channelRetMsg.getChannelErrMsg());
                payMchNotifyService.transferOrderNotify(transferOrderService.getById(transferId));
            }

            return channelRetMsg;

        } catch (Exception e) {  //继续下一次迭代查询
            log.error("error transferId = {}", transferOrder.getTransferId(), e);
            return null;
        }

    }


    /**
     * 处理返回的渠道信息，并更新订单状态
     *  TransferOrder将对部分信息进行 赋值操作。
     * **/
    public void processChannelMsg(ChannelRetMsg channelRetMsg, TransferOrder transferOrder){

        //对象为空 || 上游返回状态为空， 则无需操作
        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return ;
        }

        //明确成功
        if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(TransferOrder.STATE_SUCCESS, transferOrder, channelRetMsg);
            payMchNotifyService.transferOrderNotify(transferOrderService.getById(transferOrder.getTransferId()));

            //明确失败
        }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(TransferOrder.STATE_FAIL, transferOrder, channelRetMsg);
            payMchNotifyService.transferOrderNotify(transferOrderService.getById(transferOrder.getTransferId()));

            // 上游处理中 || 未知 || 上游接口返回异常  订单为支付中状态
        }else if( ChannelRetMsg.ChannelState.WAITING == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.UNKNOWN == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.API_RET_ERROR == channelRetMsg.getChannelState()

        ){
            this.updateInitOrderStateThrowException(TransferOrder.STATE_ING, transferOrder, channelRetMsg);

            // 系统异常：  订单不再处理。  为： 生成状态
        }else if( ChannelRetMsg.ChannelState.SYS_ERROR == channelRetMsg.getChannelState()){

        }else{

            throw new BizException("ChannelState 返回异常！");
        }

    }


    /** 更新转账单状态 --》 转账单生成--》 其他状态  (向外抛出异常) **/
    private void updateInitOrderStateThrowException(byte orderState, TransferOrder transferOrder, ChannelRetMsg channelRetMsg){

        transferOrder.setState(orderState);
        transferOrder.setChannelOrderNo(channelRetMsg.getChannelOrderId());
        transferOrder.setErrCode(channelRetMsg.getChannelErrCode());
        transferOrder.setErrMsg(channelRetMsg.getChannelErrMsg());
        transferOrder.setChannelResData(channelRetMsg.getChannelAttach());

        boolean isSuccess = transferOrderService.updateInit2Ing(transferOrder.getTransferId(), transferOrder.getChannelResData());
        if(!isSuccess){
            throw new BizException("更新转账单异常!");
        }

        isSuccess = transferOrderService.updateIng2SuccessOrFail(transferOrder.getTransferId(), transferOrder.getState(),
                channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
        if(!isSuccess){
            throw new BizException("更新转账订单异常!");
        }
    }


}
