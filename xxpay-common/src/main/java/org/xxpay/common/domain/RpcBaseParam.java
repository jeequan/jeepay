package org.xxpay.common.domain;

import org.xxpay.common.enumm.RpcSignTypeEnum;
import org.xxpay.common.util.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务接口调用入参基类
 * Created by admin on 2016/4/27.
 */
public class RpcBaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 调用方ID(由RPC服务端分配)
     */
    protected String rpcSrcSysId;
    /**
     * 业务调用当前时间(格式:yyyyMMddHHmmssSSS)
     */
    protected String rpcDateTime;
    /**
     * 随机通讯码(要求一定时间段内不重复)
     */
    protected String rpcSeqNo;
    /**
     * 签名计算方法
     * 参见RpcSignTypeEnum
     * 0: 明文
     * 1: SHA-1
     */
    protected Integer rpcSignType;
    /**
     * 签名(用于验证调用方的合法性)
     * 签名计算方法: 签名方法(key(由RPC服务端分配,不在通讯中传递)+scrSysId+rpcDateTime(yyyyMMddHHmmssSSS)+rpcSignType+bizSeqNo+bizSign),如果字段为null则不参与
     * eg. sha1(key+srcSysId+curDateTime+rpcSignType+bizSeqNo+bizSign)
     */
    protected String rpcSign;
    /**
     * 业务流水号(唯一标示一笔业务)
     * 由业务前缀(2字符,参见Constant.MP_BIZ_SEQUENCE_NO_PREFIX)+日期时间(yyyyMMddHHmmss)+流水号(6位数字)组成
     * eg.  Constant.MP_BIZ_SEQUENCE_NO_PREFIX)+DateUtils.getCurrentTimeStr("yyyyMMddHHmmss")
     *          +BizSequenceUtils.getInstance().generateBizSeqNo()
     */
    protected String bizSeqNo;
    /**
     * 业务签名(计算由各业务系统定义)
     */
    protected String bizSign;

    public RpcBaseParam() {}

    /**
     * 不需要业务签名的构造器
     * @param rpcSrcSysId
     * @param rpcSignKey
     * @param bizSeqNoPrefix
     */
    public RpcBaseParam(String rpcSrcSysId, String rpcSignKey, String bizSeqNoPrefix) {
        this.rpcSrcSysId = rpcSrcSysId;
        this.rpcDateTime = DateUtils.getCurrentTimeStrDefault();
        this.rpcSeqNo = RandomStrUtils.getInstance().getRandomString();
        this.rpcSignType = RpcSignTypeEnum.SHA1_SIGN.getCode();
        this.bizSeqNo = BizSequenceUtils.getInstance().generateBizSeqNo(bizSeqNoPrefix);
        StringBuffer decriptBuffer = new StringBuffer();
        decriptBuffer.append(rpcSignKey)
                .append(this.rpcSrcSysId)
                .append(this.rpcDateTime)
                .append(this.rpcSignType)
                .append(this.bizSeqNo);
        this.rpcSign = RpcSignUtils.sha1(decriptBuffer.toString());
    }

    /**
     * 需要业务签名的构造器
     * @param rpcSrcSysId
     * @param rpcSignKey
     * @param bizSeqNoPrefix
     * @param bizSign
     */
    public RpcBaseParam(String rpcSrcSysId, String rpcSignKey, String bizSeqNoPrefix, String bizSign) {
        this.rpcSrcSysId = rpcSrcSysId;
        this.rpcDateTime = DateUtils.getCurrentTimeStrDefault();
        this.rpcSeqNo = RandomStrUtils.getInstance().getRandomString();
        this.rpcSignType = RpcSignTypeEnum.SHA1_SIGN.getCode();
        this.bizSeqNo = BizSequenceUtils.getInstance().generateBizSeqNo(bizSeqNoPrefix);
        this.bizSign = bizSign;
        StringBuffer decriptBuffer = new StringBuffer();
        decriptBuffer.append(rpcSignKey)
                .append(this.rpcSrcSysId)
                .append(this.rpcDateTime)
                .append(this.rpcSignType)
                .append(this.bizSeqNo)
                .append(this.bizSign);
        this.rpcSign = RpcSignUtils.sha1(decriptBuffer.toString());
    }

    public String getRpcSrcSysId() {
        return rpcSrcSysId;
    }

    public void setRpcSrcSysId(String rpcSrcSysId) {
        this.rpcSrcSysId = rpcSrcSysId;
    }

    public String getRpcDateTime() {
        return rpcDateTime;
    }

    public void setRpcDateTime(String rpcDateTime) {
        this.rpcDateTime = rpcDateTime;
    }

    public String getRpcSeqNo() {
        return rpcSeqNo;
    }

    public void setRpcSeqNo(String rpcSeqNo) {
        this.rpcSeqNo = rpcSeqNo;
    }

    public Integer getRpcSignType() {
        return rpcSignType;
    }

    public void setRpcSignType(Integer rpcSignType) {
        this.rpcSignType = rpcSignType;
    }

    public String getBizSeqNo() {
        return bizSeqNo;
    }

    public void setBizSeqNo(String bizSeqNo) {
        this.bizSeqNo = bizSeqNo;
    }

    public String getBizSign() {
        return bizSign;
    }

    public void setBizSign(String bizSign) {
        this.bizSign = bizSign;
    }

    public String getRpcSign() {
        return rpcSign;
    }

    public void setRpcSign(String rpcSign) {
        this.rpcSign = rpcSign;
    }

    /*public Map<String, Object> convert2Map() {
        if (this == null) {
            return null;
        }
        return BeanConvertUtils.bean2Map(this);
    }*/

    public static RpcBaseParam convert2Bean(Map<String, Object> map) {
        return BeanConvertUtils.map2Bean(map, RpcBaseParam.class);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RpcBaseParam{");
        sb.append("rpcSrcSysId='").append(rpcSrcSysId).append('\'');
        sb.append(", rpcDateTime='").append(rpcDateTime).append('\'');
        sb.append(", rpcSeqNo='").append(rpcSeqNo).append('\'');
        sb.append(", rpcSignType=").append(rpcSignType);
        sb.append(", rpcSign='").append(rpcSign).append('\'');
        sb.append(", bizSeqNo='").append(bizSeqNo).append('\'');
        sb.append(", bizSign='").append(bizSign).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public Map<String, Object> convert2Map() {
        Map<String, Object> rpcMap = new HashMap<String, Object>();
        rpcMap.put("rpcSrcSysId", rpcSrcSysId);
        rpcMap.put("rpcDateTime", rpcDateTime);
        rpcMap.put("rpcSeqNo", rpcSeqNo);
        rpcMap.put("rpcSignType", rpcSignType);
        rpcMap.put("rpcSign", rpcSign);
        rpcMap.put("bizSeqNo", bizSeqNo);
        rpcMap.put("bizSign", bizSign);
        return rpcMap;
    }

}
