package org.xxpay.mgr.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.dal.dao.mapper.RefundOrderMapper;
import org.xxpay.dal.dao.model.RefundOrder;
import org.xxpay.dal.dao.model.RefundOrderExample;

import java.util.List;

/**
 * Created by dingzhiwei on 17/11/03.
 */
@Component
public class RefundOrderService {

    @Autowired
    private RefundOrderMapper refundOrderMapper;

    public RefundOrder selectRefundOrder(String refundOrderId) {
        return refundOrderMapper.selectByPrimaryKey(refundOrderId);
    }

    public List<RefundOrder> getRefundOrderList(int offset, int limit, RefundOrder refundOrder) {
        RefundOrderExample example = new RefundOrderExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        RefundOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, refundOrder);
        return refundOrderMapper.selectByExample(example);
    }

    public Integer count(RefundOrder refundOrder) {
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, refundOrder);
        return refundOrderMapper.countByExample(example);
    }

    void setCriteria(RefundOrderExample.Criteria criteria, RefundOrder refundOrder) {
        if(refundOrder != null) {
            if(StringUtils.isNotBlank(refundOrder.getMchId())) criteria.andMchIdEqualTo(refundOrder.getMchId());
            if(StringUtils.isNotBlank(refundOrder.getRefundOrderId())) criteria.andRefundOrderIdEqualTo(refundOrder.getRefundOrderId());
            if(StringUtils.isNotBlank(refundOrder.getRefundOrderId())) criteria.andMchRefundNoEqualTo(refundOrder.getMchRefundNo());
            if(StringUtils.isNotBlank(refundOrder.getChannelOrderNo())) criteria.andChannelOrderNoEqualTo(refundOrder.getChannelOrderNo());
            if(refundOrder.getStatus() != null && refundOrder.getStatus() != -99) criteria.andStatusEqualTo(refundOrder.getStatus());
        }
    }

}
