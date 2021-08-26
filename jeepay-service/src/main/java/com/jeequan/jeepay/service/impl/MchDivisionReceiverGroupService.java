package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.MchDivisionReceiverGroup;
import com.jeequan.jeepay.service.mapper.MchDivisionReceiverGroupMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 分账账号组 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-08-23
 */
@Service
public class MchDivisionReceiverGroupService extends ServiceImpl<MchDivisionReceiverGroupMapper, MchDivisionReceiverGroup> {


    /** 根据ID和商户号查询 **/
    public MchDivisionReceiverGroup findByIdAndMchNo(Long groupId, String mchNo){
        return getOne(MchDivisionReceiverGroup.gw().eq(MchDivisionReceiverGroup::getReceiverGroupId, groupId).eq(MchDivisionReceiverGroup::getMchNo, mchNo));
    }

}
