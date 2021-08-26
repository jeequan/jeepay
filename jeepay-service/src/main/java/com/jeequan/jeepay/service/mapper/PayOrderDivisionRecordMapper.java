package com.jeequan.jeepay.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;

/**
 * <p>
 * 分账记录表 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-08-19
 */
public interface PayOrderDivisionRecordMapper extends BaseMapper<PayOrderDivisionRecord> {

    /** 查询全部分账成功金额 **/
    Long sumSuccessDivisionAmount(String payOrderId);

}
