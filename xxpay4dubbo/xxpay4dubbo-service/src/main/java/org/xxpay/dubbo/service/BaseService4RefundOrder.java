package org.xxpay.dubbo.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.dal.dao.mapper.RefundOrderMapper;
import org.xxpay.dal.dao.mapper.TransOrderMapper;
import org.xxpay.dal.dao.model.RefundOrder;
import org.xxpay.dal.dao.model.RefundOrderExample;
import org.xxpay.dal.dao.model.TransOrder;
import org.xxpay.dal.dao.model.TransOrderExample;

import java.util.Date;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 17/10/30
 * @description:
 */
@Service
public class BaseService4RefundOrder extends BaseService{

    @Autowired
    private RefundOrderMapper refundOrderMapper;

    public int baseCreateRefundOrder(RefundOrder refundOrder) {
        return refundOrderMapper.insertSelective(refundOrder);
    }

    public RefundOrder baseSelectRefundOrder(String refundOrderId) {
        return refundOrderMapper.selectByPrimaryKey(refundOrderId);
    }

    public RefundOrder baseSelectByMchIdAndRefundOrderId(String mchId, String refundOrderId) {
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        List<RefundOrder> refundOrderList = refundOrderMapper.selectByExample(example);
        return CollectionUtils.isEmpty(refundOrderList) ? null : refundOrderList.get(0);
    }

    public RefundOrder baseSelectByMchIdAndMchRefundNo(String mchId, String mchRefundNo) {
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andMchRefundNoEqualTo(mchRefundNo);
        List<RefundOrder> refundOrderList = refundOrderMapper.selectByExample(example);
        return CollectionUtils.isEmpty(refundOrderList) ? null : refundOrderList.get(0);
    }

    public int baseUpdateStatus4Ing(String refundOrderId, String channelOrderNo) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setStatus(PayConstant.REFUND_STATUS_REFUNDING);
        if(channelOrderNo != null) refundOrder.setChannelOrderNo(channelOrderNo);
        refundOrder.setRefundSuccTime(new Date());
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        criteria.andStatusEqualTo(PayConstant.REFUND_STATUS_INIT);
        return refundOrderMapper.updateByExampleSelective(refundOrder, example);
    }

    public int baseUpdateStatus4Success(String refundOrderId) {
        return baseUpdateStatus4Success(refundOrderId, null);
    }

    public int baseUpdateStatus4Success(String refundOrderId, String channelOrderNo) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(refundOrderId);
        refundOrder.setStatus(PayConstant.REFUND_STATUS_SUCCESS);
        refundOrder.setResult(PayConstant.REFUND_RESULT_SUCCESS);
        refundOrder.setRefundSuccTime(new Date());
        if(StringUtils.isNotBlank(channelOrderNo)) refundOrder.setChannelOrderNo(channelOrderNo);
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        criteria.andStatusEqualTo(PayConstant.REFUND_STATUS_REFUNDING);
        return refundOrderMapper.updateByExampleSelective(refundOrder, example);
    }

    public int baseUpdateStatus4Complete(String refundOrderId) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(refundOrderId);
        refundOrder.setStatus(PayConstant.REFUND_STATUS_COMPLETE);
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        List values = CollectionUtils.arrayToList(new Byte[] {
                PayConstant.REFUND_STATUS_SUCCESS, PayConstant.REFUND_STATUS_FAIL
        });
        criteria.andStatusIn(values);
        return refundOrderMapper.updateByExampleSelective(refundOrder, example);
    }

    public int baseUpdateStatus4Fail(String refundOrderId, String channelErrCode, String channelErrMsg) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setStatus(PayConstant.REFUND_STATUS_FAIL);
        refundOrder.setResult(PayConstant.REFUND_RESULT_FAIL);
        if(channelErrCode != null) refundOrder.setChannelErrCode(channelErrCode);
        if(channelErrMsg != null) refundOrder.setChannelErrMsg(channelErrMsg);
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        criteria.andStatusEqualTo(PayConstant.REFUND_STATUS_REFUNDING);
        return refundOrderMapper.updateByExampleSelective(refundOrder, example);
    }

}
