package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.service.mapper.PayOrderDivisionRecordMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分账记录表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-08-19
 */
@Service
public class PayOrderDivisionRecordService extends ServiceImpl<PayOrderDivisionRecordMapper, PayOrderDivisionRecord> {


    /** 更新分账记录为分账成功**/
    public void updateRecordSuccessOrFail(List<PayOrderDivisionRecord> records, Byte state, String channelBatchOrderId, String channelRespResult){

        if(records == null || records.isEmpty()){
            return ;
        }

        List<Long> recordIds = new ArrayList<>();
        records.stream().forEach(r -> recordIds.add(r.getRecordId()));

        PayOrderDivisionRecord updateRecord = new PayOrderDivisionRecord();
        updateRecord.setState(state);
        updateRecord.setChannelBatchOrderId(channelBatchOrderId);
        updateRecord.setChannelRespResult(channelRespResult);
        update(updateRecord, PayOrderDivisionRecord.gw().in(PayOrderDivisionRecord::getRecordId, recordIds).eq(PayOrderDivisionRecord::getState, PayOrderDivisionRecord.STATE_WAIT));

    }





}
