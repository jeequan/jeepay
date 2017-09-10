package org.xxpay.common.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务接口调用返回值基类
 * Created by admin on 2016/4/27.
 */
public class RpcBaseResult extends RpcBaseParam {

    /**
     * RPC调用返回码
     * 0000: 成功
     * 其他: 失败(00开始标示通讯层相关错误码)
     */
    protected String rpcRetCode;
    /**
     * RPC调用返回错误描述
     */
    protected String rpcRetMsg;

    /**
     * DB返回的错误码
     */
    protected String dbErrorCode;

    /**
     * DB返回的错误信息
     */
    protected String dbErrorMsg;

    public String getRpcRetCode() {
        return rpcRetCode;
    }

    public void setRpcRetCode(String rpcRetCode) {
        this.rpcRetCode = rpcRetCode;
    }

    public String getRpcRetMsg() {
        return rpcRetMsg;
    }

    public void setRpcRetMsg(String rpcRetMsg) {
        this.rpcRetMsg = rpcRetMsg;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RpcBaseResult{");
        sb.append("rpcSrcSysId='").append(rpcSrcSysId).append('\'');
        sb.append(", rpcDateTime='").append(rpcDateTime).append('\'');
        sb.append(", rpcSeqNo='").append(rpcSeqNo).append('\'');
        sb.append(", rpcSignType=").append(rpcSignType);
        sb.append(", rpcSign='").append(rpcSign).append('\'');
        sb.append(", bizSeqNo='").append(bizSeqNo).append('\'');
        sb.append(", bizSign='").append(bizSign).append('\'');
        sb.append(", rpcRetCode='").append(rpcRetCode).append('\'');
        sb.append(", rpcRetMsg='").append(rpcRetMsg).append('\'');
        sb.append(", dbErrorCode='").append(dbErrorCode).append('\'');
        sb.append(", dbErrorMsg='").append(dbErrorMsg).append('\'');
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
        rpcMap.put("rpcRetCode", rpcRetCode);
        rpcMap.put("rpcRetMsg", rpcRetMsg);
        rpcMap.put("dbErrorCode", dbErrorCode);
        rpcMap.put("dbErrorMsg", dbErrorMsg);
        return rpcMap;
    }

}
