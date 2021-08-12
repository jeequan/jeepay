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
package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.service.mapper.TransferOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 转账订单表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-08-11
 */
@Service
public class TransferOrderService extends ServiceImpl<TransferOrderMapper, TransferOrder> {


    /** 更新转账订单状态  【转账订单生成】 --》 【转账中】 **/
    public boolean updateInit2Ing(String transferId){

        TransferOrder updateRecord = new TransferOrder();
        updateRecord.setState(TransferOrder.STATE_ING);

        return update(updateRecord, new LambdaUpdateWrapper<TransferOrder>()
                .eq(TransferOrder::getTransferId, transferId).eq(TransferOrder::getState, TransferOrder.STATE_INIT));
    }


    /** 更新转账订单状态  【转账中】 --》 【转账成功】 **/
    @Transactional
    public boolean updateIng2Success(String transferId, String channelOrderNo){

        TransferOrder updateRecord = new TransferOrder();
        updateRecord.setState(TransferOrder.STATE_SUCCESS);
        updateRecord.setChannelOrderNo(channelOrderNo);
        updateRecord.setSuccessTime(new Date());

        //更新转账订单表数据
        if(! update(updateRecord, new LambdaUpdateWrapper<TransferOrder>()
                .eq(TransferOrder::getTransferId, transferId).eq(TransferOrder::getState, TransferOrder.STATE_ING))
        ){
            return false;
        }

        return true;
    }


    /** 更新转账订单状态  【转账中】 --》 【转账失败】 **/
    @Transactional
    public boolean updateIng2Fail(String transferId, String channelOrderNo, String channelErrCode, String channelErrMsg){

        TransferOrder updateRecord = new TransferOrder();
        updateRecord.setState(TransferOrder.STATE_FAIL);
        updateRecord.setErrCode(channelErrCode);
        updateRecord.setErrMsg(channelErrMsg);
        updateRecord.setChannelOrderNo(channelOrderNo);

        return update(updateRecord, new LambdaUpdateWrapper<TransferOrder>()
                .eq(TransferOrder::getTransferId, transferId).eq(TransferOrder::getState, TransferOrder.STATE_ING));
    }


    /** 更新转账订单状态  【转账中】 --》 【转账成功/转账失败】 **/
    @Transactional
    public boolean updateIng2SuccessOrFail(String transferId, Byte updateState, String channelOrderNo, String channelErrCode, String channelErrMsg){

        if(updateState == TransferOrder.STATE_ING){
            return true;
        }else if(updateState == TransferOrder.STATE_SUCCESS){
            return updateIng2Success(transferId, channelOrderNo);
        }else if(updateState == TransferOrder.STATE_FAIL){
            return updateIng2Fail(transferId, channelOrderNo, channelErrCode, channelErrMsg);
        }
        return false;
    }




}
