package org.xxpay.common.domain;

import org.apache.commons.lang3.StringUtils;
import org.xxpay.common.enumm.RpcSignTypeEnum;
import org.xxpay.common.util.*;

import java.util.*;

/**
 * Created by admin on 2016/7/11.
 */
public class BaseParam extends RpcBaseParam {

    private Map<String, Object> bizParamMap;

    public BaseParam() {}

    /**
     * 不需要业务签名的构造器
     * @param rpcSrcSysId
     * @param rpcSignKey
     * @param bizSeqNoPrefix
     */
    public BaseParam(String rpcSrcSysId, String rpcSignKey, String bizSeqNoPrefix) {
        super.rpcSrcSysId = rpcSrcSysId;
        super.rpcDateTime = DateUtils.getCurrentTimeStrDefault();
        super.rpcSeqNo = RandomStrUtils.getInstance().getRandomString();
        super.rpcSignType = RpcSignTypeEnum.SHA1_SIGN.getCode();
        super.bizSeqNo = BizSequenceUtils.getInstance().generateBizSeqNo(bizSeqNoPrefix);
        StringBuffer decriptBuffer = new StringBuffer();
        decriptBuffer.append(rpcSignKey)
                .append(super.rpcSrcSysId)
                .append(super.rpcDateTime)
                .append(super.rpcSignType)
                .append(super.bizSeqNo);
        super.rpcSign = RpcSignUtils.sha1(decriptBuffer.toString());
        this.bizParamMap = new HashMap<String, Object>();
    }

    /**
     * 需要业务签名的构造器
     * @param rpcSrcSysId
     * @param rpcSignKey
     * @param bizSeqNoPrefix
     * @param bizSign
     */
    public BaseParam(String rpcSrcSysId, String rpcSignKey, String bizSeqNoPrefix, String bizSign) {
        super.rpcSrcSysId = rpcSrcSysId;
        super.rpcDateTime = DateUtils.getCurrentTimeStrDefault();
        super.rpcSeqNo = RandomStrUtils.getInstance().getRandomString();
        super.rpcSignType = RpcSignTypeEnum.SHA1_SIGN.getCode();
        super.bizSeqNo = BizSequenceUtils.getInstance().generateBizSeqNo(bizSeqNoPrefix);
        super.bizSign = bizSign;
        StringBuffer decriptBuffer = new StringBuffer();
        decriptBuffer.append(rpcSignKey)
                .append(super.rpcSrcSysId)
                .append(super.rpcDateTime)
                .append(super.rpcSignType)
                .append(super.bizSeqNo)
                .append(super.bizSign);
        super.rpcSign = RpcSignUtils.sha1(decriptBuffer.toString());
        this.bizParamMap = new HashMap<String, Object>();
    }

    public Map<String, Object> getBizParamMap() {
        return bizParamMap;
    }

    public void setBizParamMap(Map<String, Object> bizParamMap) {
        this.bizParamMap = bizParamMap;
    }

    public String toJson() {
        return JsonUtil.object2Json(this);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BaseParam{");
        sb.append("rpcSrcSysId='").append(rpcSrcSysId).append('\'');
        sb.append(", rpcDateTime='").append(rpcDateTime).append('\'');
        sb.append(", rpcSeqNo='").append(rpcSeqNo).append('\'');
        sb.append(", rpcSignType=").append(rpcSignType);
        sb.append(", rpcSign='").append(rpcSign).append('\'');
        sb.append(", bizSeqNo='").append(bizSeqNo).append('\'');
        sb.append(", bizSign='").append(bizSign).append('\'');
        sb.append(", bizParamMap=").append(bizParamMap);
        sb.append('}');
        return sb.toString();
    }

    public boolean isNullValue(String key) {
        Object objValue = this.bizParamMap.get(key);
        return objValue == null || StringUtils.isBlank(objValue.toString());
    }

    /**
     * 判断bizParamMap中的参数是否全部为空，是则返回true，否则返回false
     * @param excludeKeys
     * @return
     */
    public boolean isInvalidMapValue(Object... excludeKeys) {
        if (this.bizParamMap == null || this.bizParamMap.isEmpty()) {
            return true;
        }
        List<Object> list = Arrays.asList(excludeKeys);
        for (Map.Entry<String, Object> entry : this.bizParamMap.entrySet()) {
            if (list.contains(entry.getKey())) {
                continue;
            }
            Object value = entry.getValue();
            if (value != null ) {
                if (value instanceof String) {
                    if (StringUtils.isNotBlank(value.toString())) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取long类型参数
     * @param name
     * @return
     */
    public Long getLongBizParam(String name) {
        if (isNullValue(name)) {
            return null;
        }
        return Long.valueOf(bizParamMap.get(name).toString());
    }

    /**
     * 获取int类型参数
     * @param name
     * @return
     */
    public Integer getIntBizParam(String name) {
        if (isNullValue(name)) {
            return null;
        }
        return Integer.valueOf(bizParamMap.get(name).toString());
    }

    /**
     * 获取int类型参数,如果为空返回defaultValue
     * @param name
     * @param defaultValue
     * @return
     */
    public Integer getIntBizParam(String name, int defaultValue) {
        if (isNullValue(name)) {
            return defaultValue;
        }
        return Integer.valueOf(bizParamMap.get(name).toString());
    }

    /**
     * 获取short类型参数
     * @param name
     * @return
     */
    public Short getShortBizParam(String name) {
        if (isNullValue(name)) {
            return null;
        }
        return Short.valueOf(bizParamMap.get(name).toString());
    }

    /**
     * 获取string类型参数,如果为空返回defaultValue
     * @param name
     * @param
     * @return
     */
    public String getStringBizParam(String name, String defaultValue){
        if (isNullValue(name)) {
            return defaultValue;
        }
        return bizParamMap.get(name).toString();
    }

    /**
     * 获取string类型参数
     * @param name
     * @param
     * @return
     */
    public String getStringBizParam(String name){
        if (isNullValue(name)) {
            return null;
        }
        return bizParamMap.get(name).toString();
    }

    public List<Short> getShortListBizParam(String name) {
        if (isNullValue(name)) {
            return null;
        }
        List<Number> numberList = (List<Number>) bizParamMap.get(name);
        if (numberList == null) {
            return null;
        }
        List<Short> shortList = new ArrayList<Short>(numberList.size());
        for (Number number : numberList) {
            Short value = number.shortValue();
            shortList.add(value);
        }
        return shortList;
    }

    public List<Integer> getIntegerListBizParam(String name) {
        if (isNullValue(name)) {
            return null;
        }
        List<Number> numberList = (List<Number>) bizParamMap.get(name);
        if (numberList == null) {
            return null;
        }
        List<Integer> integerList = new ArrayList<Integer>(numberList.size());
        for (Number number : numberList) {
            Integer value = number.intValue();
            integerList.add(value);
        }
        return integerList;
    }

    public List<Long> getLongListBizParam(String name) {
        if (isNullValue(name)) {
            return null;
        }
        List<Number> numberList = (List<Number>) bizParamMap.get(name);
        if (numberList == null) {
            return null;
        }
        List<Long> longList = new ArrayList<Long>(numberList.size());
        for (Number number : numberList) {
            Long value = number.longValue();
            longList.add(value);
        }
        return longList;
    }

    public static void main(String[] args) {
        BaseParam baseParam = new BaseParam();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", null);
        map.put("b", "");
        baseParam.setBizParamMap(map);
        System.out.println(baseParam.isInvalidMapValue(""));
    }

}
