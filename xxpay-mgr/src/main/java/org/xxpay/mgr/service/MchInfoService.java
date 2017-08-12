package org.xxpay.mgr.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.xxpay.dal.dao.mapper.MchInfoMapper;
import org.xxpay.dal.dao.model.MchInfo;
import org.xxpay.dal.dao.model.MchInfoExample;

import java.util.List;

/**
 * Created by dingzhiwei on 17/5/4.
 */
@Component
public class MchInfoService {

    @Autowired
    private MchInfoMapper mchInfoMapper;

    public int addMchInfo(MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        example.setOrderByClause("mchId DESC");
        example.setOffset(0);
        example.setLimit(1);
        List<MchInfo> mchInfos = mchInfoMapper.selectByExample(example);
        String mchId = "10000000";
        if(!CollectionUtils.isEmpty(mchInfos)) {
            mchId = String.valueOf(Integer.parseInt(mchInfos.get(0).getMchId()) + 1);
        }
        mchInfo.setMchId(mchId);
        return mchInfoMapper.insertSelective(mchInfo);
    }

    public int updateMchInfo(MchInfo mchInfo) {
        return mchInfoMapper.updateByPrimaryKeySelective(mchInfo);
    }

    public MchInfo selectMchInfo(String mchId) {
        return mchInfoMapper.selectByPrimaryKey(mchId);
    }

    public List<MchInfo> getMchInfoList(int offset, int limit, MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        return mchInfoMapper.selectByExample(example);
    }

    public Integer count(MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        return mchInfoMapper.countByExample(example);
    }

    void setCriteria(MchInfoExample.Criteria criteria, MchInfo mchInfo) {
        if(mchInfo != null) {
            if(StringUtils.isNotBlank(mchInfo.getMchId())) criteria.andMchIdEqualTo(mchInfo.getMchId());
            if(mchInfo.getType() != null && !"-99".equals(mchInfo.getType())) criteria.andTypeEqualTo(mchInfo.getType());
        }
    }

}
