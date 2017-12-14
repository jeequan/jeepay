package org.xxpay.mgr.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.dal.dao.mapper.TransOrderMapper;
import org.xxpay.dal.dao.model.TransOrder;
import org.xxpay.dal.dao.model.TransOrderExample;

import java.util.List;

/**
 * Created by dingzhiwei on 17/11/03.
 */
@Component
public class TransOrderService {

    @Autowired
    private TransOrderMapper transOrderMapper;

    public TransOrder selectTransOrder(String transOrderId) {
        return transOrderMapper.selectByPrimaryKey(transOrderId);
    }

    public List<TransOrder> getTransOrderList(int offset, int limit, TransOrder transOrder) {
        TransOrderExample example = new TransOrderExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        TransOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, transOrder);
        return transOrderMapper.selectByExample(example);
    }

    public Integer count(TransOrder transOrder) {
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, transOrder);
        return transOrderMapper.countByExample(example);
    }

    void setCriteria(TransOrderExample.Criteria criteria, TransOrder transOrder) {
        if(transOrder != null) {
            if(StringUtils.isNotBlank(transOrder.getMchId())) criteria.andMchIdEqualTo(transOrder.getMchId());
            if(StringUtils.isNotBlank(transOrder.getTransOrderId())) criteria.andTransOrderIdEqualTo(transOrder.getTransOrderId());
            if(StringUtils.isNotBlank(transOrder.getMchTransNo())) criteria.andMchTransNoEqualTo(transOrder.getMchTransNo());
            if(StringUtils.isNotBlank(transOrder.getChannelOrderNo())) criteria.andChannelOrderNoEqualTo(transOrder.getChannelOrderNo());
            if(transOrder.getStatus() != null && transOrder.getStatus() != -99) criteria.andStatusEqualTo(transOrder.getStatus());
        }
    }

}
