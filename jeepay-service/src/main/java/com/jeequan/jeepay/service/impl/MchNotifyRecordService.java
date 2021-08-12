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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.service.mapper.MchNotifyRecordMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商户通知表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Service
public class MchNotifyRecordService extends ServiceImpl<MchNotifyRecordMapper, MchNotifyRecord> {

    /** 根据订单号和类型查询 */
    public MchNotifyRecord findByOrderAndType(String orderId, Byte orderType){
        return getOne(
                MchNotifyRecord.gw().eq(MchNotifyRecord::getOrderId, orderId).eq(MchNotifyRecord::getOrderType, orderType)
        );
    }

    /** 查询支付订单 */
    public MchNotifyRecord findByPayOrder(String orderId){
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_PAY_ORDER);
    }

    /** 查询退款订单订单 */
    public MchNotifyRecord findByRefundOrder(String orderId){
        return findByOrderAndType(orderId, MchNotifyRecord.TYPE_REFUND_ORDER);
    }

    /** 查询退款订单订单 */
    public MchNotifyRecord findByTransferOrder(String transferId){
        return findByOrderAndType(transferId, MchNotifyRecord.TYPE_TRANSFER_ORDER);
    }

    public Integer updateNotifyResult(Long notifyId, Byte state, String resResult){
        return baseMapper.updateNotifyResult(notifyId, state, resResult);
    }



}
