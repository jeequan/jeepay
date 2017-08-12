package org.xxpay.mgr.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.xxpay.dal.dao.mapper.PayChannelMapper;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dal.dao.model.PayChannelExample;

import java.util.List;

/**
 * Created by dingzhiwei on 17/5/7.
 */
@Component
public class PayChannelService {

    @Autowired
    private PayChannelMapper payChannelMapper;

    public int addPayChannel(PayChannel payChannel) {
        return payChannelMapper.insertSelective(payChannel);
    }

    public int updatePayChannel(PayChannel payChannel) {
        return payChannelMapper.updateByPrimaryKeySelective(payChannel);
    }

    public PayChannel selectPayChannel(String channelId, String mchId) {
        PayChannelExample example = new PayChannelExample();
        PayChannelExample.Criteria criteria = example.createCriteria();
        criteria.andChannelIdEqualTo(channelId);
        criteria.andMchIdEqualTo(mchId);
        List<PayChannel> payChannelList = payChannelMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(payChannelList)) return null;
        return payChannelList.get(0);
    }

    public PayChannel selectPayChannel(int id) {
        return payChannelMapper.selectByPrimaryKey(id);
    }

    public List<PayChannel> getPayChannelList(int offset, int limit, PayChannel payChannel) {
        PayChannelExample example = new PayChannelExample();
        example.setOrderByClause("mchId ASC, channelId ASC, createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        PayChannelExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payChannel);
        return payChannelMapper.selectByExample(example);
    }

    public Integer count(PayChannel payChannel) {
        PayChannelExample example = new PayChannelExample();
        PayChannelExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payChannel);
        return payChannelMapper.countByExample(example);
    }

    void setCriteria(PayChannelExample.Criteria criteria, PayChannel payChannel) {
        if(payChannel != null) {
            if(StringUtils.isNotBlank(payChannel.getMchId())) criteria.andMchIdEqualTo(payChannel.getMchId());
            if(StringUtils.isNotBlank(payChannel.getChannelId())) criteria.andChannelIdEqualTo(payChannel.getChannelId());
        }
    }

}
