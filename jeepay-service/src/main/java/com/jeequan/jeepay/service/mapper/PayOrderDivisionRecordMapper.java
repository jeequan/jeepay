package com.jeequan.jeepay.service.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import org.apache.ibatis.annotations.Param;

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

    /**  batch_order_id 去重， 查询出所有的 分账已受理状态的订单， 支持分页。 */
    IPage<PayOrderDivisionRecord> distinctBatchOrderIdList(IPage<?> page, @Param("ew") Wrapper<PayOrderDivisionRecord> wrapper);

}
