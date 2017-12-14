package org.xxpay.dal.dao.model;

import java.io.Serializable;
import java.util.Date;

public class RefundOrder implements Serializable {
    /**
     * 退款订单号
     *
     * @mbggenerated
     */
    private String refundOrderId;

    /**
     * 支付订单号
     *
     * @mbggenerated
     */
    private String payOrderId;

    /**
     * 渠道支付单号
     *
     * @mbggenerated
     */
    private String channelPayOrderNo;

    /**
     * 商户ID
     *
     * @mbggenerated
     */
    private String mchId;

    /**
     * 商户退款单号
     *
     * @mbggenerated
     */
    private String mchRefundNo;

    /**
     * 渠道ID
     *
     * @mbggenerated
     */
    private String channelId;

    /**
     * 支付金额,单位分
     *
     * @mbggenerated
     */
    private Long payAmount;

    /**
     * 退款金额,单位分
     *
     * @mbggenerated
     */
    private Long refundAmount;

    /**
     * 三位货币代码,人民币:cny
     *
     * @mbggenerated
     */
    private String currency;

    /**
     * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-业务处理完成
     *
     * @mbggenerated
     */
    private Byte status;

    /**
     * 退款结果:0-不确认结果,1-等待手动处理,2-确认成功,3-确认失败
     *
     * @mbggenerated
     */
    private Byte result;

    /**
     * 客户端IP
     *
     * @mbggenerated
     */
    private String clientIp;

    /**
     * 设备
     *
     * @mbggenerated
     */
    private String device;

    /**
     * 备注
     *
     * @mbggenerated
     */
    private String remarkInfo;

    /**
     * 渠道用户标识,如微信openId,支付宝账号
     *
     * @mbggenerated
     */
    private String channelUser;

    /**
     * 用户姓名
     *
     * @mbggenerated
     */
    private String userName;

    /**
     * 渠道商户ID
     *
     * @mbggenerated
     */
    private String channelMchId;

    /**
     * 渠道订单号
     *
     * @mbggenerated
     */
    private String channelOrderNo;

    /**
     * 渠道错误码
     *
     * @mbggenerated
     */
    private String channelErrCode;

    /**
     * 渠道错误描述
     *
     * @mbggenerated
     */
    private String channelErrMsg;

    /**
     * 特定渠道发起时额外参数
     *
     * @mbggenerated
     */
    private String extra;

    /**
     * 通知地址
     *
     * @mbggenerated
     */
    private String notifyUrl;

    /**
     * 扩展参数1
     *
     * @mbggenerated
     */
    private String param1;

    /**
     * 扩展参数2
     *
     * @mbggenerated
     */
    private String param2;

    /**
     * 订单失效时间
     *
     * @mbggenerated
     */
    private Date expireTime;

    /**
     * 订单退款成功时间
     *
     * @mbggenerated
     */
    private Date refundSuccTime;

    /**
     * 创建时间
     *
     * @mbggenerated
     */
    private Date createTime;

    /**
     * 更新时间
     *
     * @mbggenerated
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public String getRefundOrderId() {
        return refundOrderId;
    }

    public void setRefundOrderId(String refundOrderId) {
        this.refundOrderId = refundOrderId;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public String getChannelPayOrderNo() {
        return channelPayOrderNo;
    }

    public void setChannelPayOrderNo(String channelPayOrderNo) {
        this.channelPayOrderNo = channelPayOrderNo;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getMchRefundNo() {
        return mchRefundNo;
    }

    public void setMchRefundNo(String mchRefundNo) {
        this.mchRefundNo = mchRefundNo;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Long getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Long payAmount) {
        this.payAmount = payAmount;
    }

    public Long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Byte getResult() {
        return result;
    }

    public void setResult(Byte result) {
        this.result = result;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getRemarkInfo() {
        return remarkInfo;
    }

    public void setRemarkInfo(String remarkInfo) {
        this.remarkInfo = remarkInfo;
    }

    public String getChannelUser() {
        return channelUser;
    }

    public void setChannelUser(String channelUser) {
        this.channelUser = channelUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getChannelMchId() {
        return channelMchId;
    }

    public void setChannelMchId(String channelMchId) {
        this.channelMchId = channelMchId;
    }

    public String getChannelOrderNo() {
        return channelOrderNo;
    }

    public void setChannelOrderNo(String channelOrderNo) {
        this.channelOrderNo = channelOrderNo;
    }

    public String getChannelErrCode() {
        return channelErrCode;
    }

    public void setChannelErrCode(String channelErrCode) {
        this.channelErrCode = channelErrCode;
    }

    public String getChannelErrMsg() {
        return channelErrMsg;
    }

    public void setChannelErrMsg(String channelErrMsg) {
        this.channelErrMsg = channelErrMsg;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getRefundSuccTime() {
        return refundSuccTime;
    }

    public void setRefundSuccTime(Date refundSuccTime) {
        this.refundSuccTime = refundSuccTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", refundOrderId=").append(refundOrderId);
        sb.append(", payOrderId=").append(payOrderId);
        sb.append(", channelPayOrderNo=").append(channelPayOrderNo);
        sb.append(", mchId=").append(mchId);
        sb.append(", mchRefundNo=").append(mchRefundNo);
        sb.append(", channelId=").append(channelId);
        sb.append(", payAmount=").append(payAmount);
        sb.append(", refundAmount=").append(refundAmount);
        sb.append(", currency=").append(currency);
        sb.append(", status=").append(status);
        sb.append(", result=").append(result);
        sb.append(", clientIp=").append(clientIp);
        sb.append(", device=").append(device);
        sb.append(", remarkInfo=").append(remarkInfo);
        sb.append(", channelUser=").append(channelUser);
        sb.append(", userName=").append(userName);
        sb.append(", channelMchId=").append(channelMchId);
        sb.append(", channelOrderNo=").append(channelOrderNo);
        sb.append(", channelErrCode=").append(channelErrCode);
        sb.append(", channelErrMsg=").append(channelErrMsg);
        sb.append(", extra=").append(extra);
        sb.append(", notifyUrl=").append(notifyUrl);
        sb.append(", param1=").append(param1);
        sb.append(", param2=").append(param2);
        sb.append(", expireTime=").append(expireTime);
        sb.append(", refundSuccTime=").append(refundSuccTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        RefundOrder other = (RefundOrder) that;
        return (this.getRefundOrderId() == null ? other.getRefundOrderId() == null : this.getRefundOrderId().equals(other.getRefundOrderId()))
            && (this.getPayOrderId() == null ? other.getPayOrderId() == null : this.getPayOrderId().equals(other.getPayOrderId()))
            && (this.getChannelPayOrderNo() == null ? other.getChannelPayOrderNo() == null : this.getChannelPayOrderNo().equals(other.getChannelPayOrderNo()))
            && (this.getMchId() == null ? other.getMchId() == null : this.getMchId().equals(other.getMchId()))
            && (this.getMchRefundNo() == null ? other.getMchRefundNo() == null : this.getMchRefundNo().equals(other.getMchRefundNo()))
            && (this.getChannelId() == null ? other.getChannelId() == null : this.getChannelId().equals(other.getChannelId()))
            && (this.getPayAmount() == null ? other.getPayAmount() == null : this.getPayAmount().equals(other.getPayAmount()))
            && (this.getRefundAmount() == null ? other.getRefundAmount() == null : this.getRefundAmount().equals(other.getRefundAmount()))
            && (this.getCurrency() == null ? other.getCurrency() == null : this.getCurrency().equals(other.getCurrency()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getResult() == null ? other.getResult() == null : this.getResult().equals(other.getResult()))
            && (this.getClientIp() == null ? other.getClientIp() == null : this.getClientIp().equals(other.getClientIp()))
            && (this.getDevice() == null ? other.getDevice() == null : this.getDevice().equals(other.getDevice()))
            && (this.getRemarkInfo() == null ? other.getRemarkInfo() == null : this.getRemarkInfo().equals(other.getRemarkInfo()))
            && (this.getChannelUser() == null ? other.getChannelUser() == null : this.getChannelUser().equals(other.getChannelUser()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getChannelMchId() == null ? other.getChannelMchId() == null : this.getChannelMchId().equals(other.getChannelMchId()))
            && (this.getChannelOrderNo() == null ? other.getChannelOrderNo() == null : this.getChannelOrderNo().equals(other.getChannelOrderNo()))
            && (this.getChannelErrCode() == null ? other.getChannelErrCode() == null : this.getChannelErrCode().equals(other.getChannelErrCode()))
            && (this.getChannelErrMsg() == null ? other.getChannelErrMsg() == null : this.getChannelErrMsg().equals(other.getChannelErrMsg()))
            && (this.getExtra() == null ? other.getExtra() == null : this.getExtra().equals(other.getExtra()))
            && (this.getNotifyUrl() == null ? other.getNotifyUrl() == null : this.getNotifyUrl().equals(other.getNotifyUrl()))
            && (this.getParam1() == null ? other.getParam1() == null : this.getParam1().equals(other.getParam1()))
            && (this.getParam2() == null ? other.getParam2() == null : this.getParam2().equals(other.getParam2()))
            && (this.getExpireTime() == null ? other.getExpireTime() == null : this.getExpireTime().equals(other.getExpireTime()))
            && (this.getRefundSuccTime() == null ? other.getRefundSuccTime() == null : this.getRefundSuccTime().equals(other.getRefundSuccTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRefundOrderId() == null) ? 0 : getRefundOrderId().hashCode());
        result = prime * result + ((getPayOrderId() == null) ? 0 : getPayOrderId().hashCode());
        result = prime * result + ((getChannelPayOrderNo() == null) ? 0 : getChannelPayOrderNo().hashCode());
        result = prime * result + ((getMchId() == null) ? 0 : getMchId().hashCode());
        result = prime * result + ((getMchRefundNo() == null) ? 0 : getMchRefundNo().hashCode());
        result = prime * result + ((getChannelId() == null) ? 0 : getChannelId().hashCode());
        result = prime * result + ((getPayAmount() == null) ? 0 : getPayAmount().hashCode());
        result = prime * result + ((getRefundAmount() == null) ? 0 : getRefundAmount().hashCode());
        result = prime * result + ((getCurrency() == null) ? 0 : getCurrency().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getResult() == null) ? 0 : getResult().hashCode());
        result = prime * result + ((getClientIp() == null) ? 0 : getClientIp().hashCode());
        result = prime * result + ((getDevice() == null) ? 0 : getDevice().hashCode());
        result = prime * result + ((getRemarkInfo() == null) ? 0 : getRemarkInfo().hashCode());
        result = prime * result + ((getChannelUser() == null) ? 0 : getChannelUser().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getChannelMchId() == null) ? 0 : getChannelMchId().hashCode());
        result = prime * result + ((getChannelOrderNo() == null) ? 0 : getChannelOrderNo().hashCode());
        result = prime * result + ((getChannelErrCode() == null) ? 0 : getChannelErrCode().hashCode());
        result = prime * result + ((getChannelErrMsg() == null) ? 0 : getChannelErrMsg().hashCode());
        result = prime * result + ((getExtra() == null) ? 0 : getExtra().hashCode());
        result = prime * result + ((getNotifyUrl() == null) ? 0 : getNotifyUrl().hashCode());
        result = prime * result + ((getParam1() == null) ? 0 : getParam1().hashCode());
        result = prime * result + ((getParam2() == null) ? 0 : getParam2().hashCode());
        result = prime * result + ((getExpireTime() == null) ? 0 : getExpireTime().hashCode());
        result = prime * result + ((getRefundSuccTime() == null) ? 0 : getRefundSuccTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }
}