package org.xxpay.dubbo.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.dal.dao.mapper.MchInfoMapper;
import org.xxpay.dal.dao.mapper.MchNotifyMapper;
import org.xxpay.dal.dao.mapper.PayChannelMapper;
import org.xxpay.dal.dao.model.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 17/9/9
 * @description:
 */
@Service
public class BaseService {

    @Autowired
    private MchInfoMapper mchInfoMapper;

    @Autowired
    private PayChannelMapper payChannelMapper;

    @Autowired
    private MchNotifyMapper mchNotifyMapper;

    public MchInfo baseSelectMchInfo(String mchId) {
        return mchInfoMapper.selectByPrimaryKey(mchId);
    }

    public PayChannel baseSelectPayChannel(String mchId, String channelId) {
        PayChannelExample example = new PayChannelExample();
        PayChannelExample.Criteria criteria = example.createCriteria();
        criteria.andChannelIdEqualTo(channelId);
        criteria.andMchIdEqualTo(mchId);
        List<PayChannel> payChannelList = payChannelMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(payChannelList)) return null;
        return payChannelList.get(0);
    }

    public MchNotify baseSelectMchNotify(String orderId) {
        return mchNotifyMapper.selectByPrimaryKey(orderId);
    }

    public int baseInsertMchNotify(String orderId, String mchId, String mchOrderNo, String orderType, String notifyUrl) {
        MchNotify mchNotify = new MchNotify();
        mchNotify.setOrderId(orderId);
        mchNotify.setMchId(mchId);
        mchNotify.setMchOrderNo(mchOrderNo);
        mchNotify.setOrderType(orderType);
        mchNotify.setNotifyUrl(notifyUrl);
        return mchNotifyMapper.insertSelectiveOnDuplicateKeyUpdate(mchNotify);
    }

    public int baseUpdateMchNotifySuccess(String orderId, String result, byte notifyCount) {
        MchNotify mchNotify = new MchNotify();
        mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_SUCCESS);
        mchNotify.setResult(result);
        mchNotify.setNotifyCount(notifyCount);
        mchNotify.setLastNotifyTime(new Date());
        MchNotifyExample example = new MchNotifyExample();
        MchNotifyExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List values = new LinkedList<>();
        values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
        values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
        criteria.andStatusIn(values);
        return mchNotifyMapper.updateByExampleSelective(mchNotify, example);
    }

    public int baseUpdateMchNotifyFail(String orderId, String result, byte notifyCount) {
        MchNotify mchNotify = new MchNotify();
        mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_FAIL);
        mchNotify.setResult(result);
        mchNotify.setNotifyCount(notifyCount);
        mchNotify.setLastNotifyTime(new Date());
        MchNotifyExample example = new MchNotifyExample();
        MchNotifyExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List values = new LinkedList<>();
        values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
        values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
        return mchNotifyMapper.updateByExampleSelective(mchNotify, example);
    }


}
