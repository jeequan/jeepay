package org.xxpay.dubbo.service;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.dal.dao.mapper.TransOrderMapper;
import org.xxpay.dal.dao.model.TransOrder;
import org.xxpay.dal.dao.model.TransOrderExample;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 17/10/30
 * @description:
 */
@Service
public class BaseService4TransOrder extends BaseService{

    @Autowired
    private TransOrderMapper transOrderMapper;

    public int baseCreateTransOrder(TransOrder transOrder) {
        return transOrderMapper.insertSelective(transOrder);
    }

    public TransOrder baseSelectTransOrder(String transOrderId) {
        return transOrderMapper.selectByPrimaryKey(transOrderId);
    }

    public TransOrder baseSelectByMchIdAndTransOrderId(String mchId, String transOrderId) {
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andTransOrderIdEqualTo(transOrderId);
        List<TransOrder> transOrderList = transOrderMapper.selectByExample(example);
        return CollectionUtils.isEmpty(transOrderList) ? null : transOrderList.get(0);
    }

    public TransOrder baseSelectByMchIdAndMchTransNo(String mchId, String mchTransNo) {
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andMchTransNoEqualTo(mchTransNo);
        List<TransOrder> transOrderList = transOrderMapper.selectByExample(example);
        return CollectionUtils.isEmpty(transOrderList) ? null : transOrderList.get(0);
    }

    public int baseUpdateStatus4Ing(String transOrderId, String channelOrderNo) {
        TransOrder transOrder = new TransOrder();
        transOrder.setStatus(PayConstant.TRANS_STATUS_TRANING);
        if(channelOrderNo != null) transOrder.setChannelOrderNo(channelOrderNo);
        transOrder.setTransSuccTime(new Date());
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        criteria.andTransOrderIdEqualTo(transOrderId);
        List<Byte> list = new LinkedList<>();
        list.add(PayConstant.TRANS_STATUS_INIT);
        list.add(PayConstant.TRANS_STATUS_FAIL);
        criteria.andStatusIn(list);
        return transOrderMapper.updateByExampleSelective(transOrder, example);
    }

    public int baseUpdateStatus4Success(String transOrderId) {
        return baseUpdateStatus4Success(transOrderId, null);
    }

    public int baseUpdateStatus4Success(String transOrderId, String channelOrderNo) {
        TransOrder transOrder = new TransOrder();
        transOrder.setTransOrderId(transOrderId);
        transOrder.setStatus(PayConstant.TRANS_STATUS_SUCCESS);
        transOrder.setResult(PayConstant.TRANS_RESULT_SUCCESS);
        transOrder.setTransSuccTime(new Date());
        if(StringUtils.isNotBlank(channelOrderNo)) transOrder.setChannelOrderNo(channelOrderNo);
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        criteria.andTransOrderIdEqualTo(transOrderId);
        criteria.andStatusEqualTo(PayConstant.TRANS_STATUS_TRANING);
        return transOrderMapper.updateByExampleSelective(transOrder, example);
    }

    public int baseUpdateStatus4Complete(String transOrderId) {
        TransOrder transOrder = new TransOrder();
        transOrder.setTransOrderId(transOrderId);
        transOrder.setStatus(PayConstant.TRANS_STATUS_COMPLETE);
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        criteria.andTransOrderIdEqualTo(transOrderId);
        List values = CollectionUtils.arrayToList(new Byte[] {
                PayConstant.TRANS_STATUS_SUCCESS, PayConstant.TRANS_STATUS_FAIL
        });
        criteria.andStatusIn(values);
        return transOrderMapper.updateByExampleSelective(transOrder, example);
    }

    public int baseUpdateStatus4Fail(String transOrderId, String channelErrCode, String channelErrMsg) {
        TransOrder transOrder = new TransOrder();
        transOrder.setStatus(PayConstant.TRANS_STATUS_FAIL);
        transOrder.setResult(PayConstant.TRANS_RESULT_FAIL);
        if(channelErrCode != null) transOrder.setChannelErrCode(channelErrCode);
        if(channelErrMsg != null) transOrder.setChannelErrMsg(channelErrMsg);
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        criteria.andTransOrderIdEqualTo(transOrderId);
        criteria.andStatusEqualTo(PayConstant.TRANS_STATUS_TRANING);
        return transOrderMapper.updateByExampleSelective(transOrder, example);
    }

}
