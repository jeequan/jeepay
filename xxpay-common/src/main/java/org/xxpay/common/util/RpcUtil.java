package org.xxpay.common.util;

import org.xxpay.common.constant.Constant;
import org.xxpay.common.domain.BaseParam;
import org.xxpay.common.domain.RpcBaseParam;
import org.xxpay.common.enumm.RetEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/9
 * @description:
 */
public class RpcUtil {

    public static BaseParam getBaseParam(Map<String, Object> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }
        BaseParam baseParam = BeanConvertUtils.map2Bean(paramMap, BaseParam.class);
        paramMap.remove("rpcSrcSysId");
        paramMap.remove("rpcDateTime");
        paramMap.remove("rpcSeqNo");
        paramMap.remove("rpcSignType");
        paramMap.remove("rpcSign");
        paramMap.remove("bizSeqNo");
        paramMap.remove("bizSign");
        baseParam.setBizParamMap(paramMap);
        return baseParam;
    }

    /**
     * 构建成功返回结果
     * @param baseParam
     * @param obj
     * @return
     */
    public static Map<String, Object> createBizResult(RpcBaseParam baseParam, Object obj) {
        Map<String, Object> resultMap = createResultMap(baseParam, RetEnum.RET_SUCCESS);
        resultMap.put(Constant.BIZ_RESULT_KEY, obj);
        return resultMap;
    }
    public static Map<String, Object> createBizResultWithDBError(RpcBaseParam baseParam, Object obj,
                                                          String dbErrorCode, String dbErrorMsg) {
        Map<String, Object> resultMap = createResultMapWithDBError(baseParam, RetEnum.RET_SUCCESS, dbErrorCode, dbErrorMsg);
        resultMap.put(Constant.BIZ_RESULT_KEY, obj);
        return resultMap;
    }

    /**
     * 构建失败返回结果
     * @param rpcBaseParam
     * @param retEnum
     * @return
     */
    public static Map<String, Object> createFailResult(RpcBaseParam rpcBaseParam, RetEnum retEnum) {
        if (retEnum == null) {
            retEnum = RetEnum.RET_PARAM_NOT_FOUND;
        }
        return createResultMap(rpcBaseParam, retEnum);
    }

    public static Map<String, Object> createFailResultWithDBError(RpcBaseParam rpcBaseParam, RetEnum retEnum,
                                                           String dbErrorCode, String dbErrorMsg) {
        if (retEnum == null) {
            retEnum = RetEnum.RET_PARAM_NOT_FOUND;
        }
        return createResultMapWithDBError(rpcBaseParam, retEnum, dbErrorCode, dbErrorMsg);
    }

    private static Map<String, Object> createResultMap(RpcBaseParam rpcBaseParam, RetEnum retEnum) {
        Map<String, Object> resultMap = null;
        if (rpcBaseParam != null) {
            resultMap = rpcBaseParam.convert2Map();
        } else {
            resultMap = new HashMap<String, Object>();
        }
        resultMap.put("rpcRetCode", retEnum.getCode());
        resultMap.put("rpcRetMsg", retEnum.getMessage());
        return resultMap;
    }

    private static Map<String, Object> createResultMapWithDBError(RpcBaseParam rpcBaseParam, RetEnum retEnum,
                                                           String dbErrorCode, String dbErrorMsg) {
        Map<String, Object> resultMap = null;
        if (rpcBaseParam != null) {
            resultMap = rpcBaseParam.convert2Map();
        } else {
            resultMap = new HashMap<String, Object>();
        }
        resultMap.put("rpcRetCode", retEnum.getCode());
        resultMap.put("rpcRetMsg", retEnum.getMessage());
        resultMap.put("dbErrorCode", dbErrorCode);
        resultMap.put("dbErrorMsg", dbErrorMsg);
        return resultMap;
    }

    public static String createBaseParam(Map<String, Object> paramMap) {
        BaseParam baseParam = new BaseParam("102", "rpc-src-sys-vvlive-config-key", Constant.CF_BIZ_SEQUENCE_NO_PREFIX);
        baseParam.setBizParamMap(paramMap);
        return baseParam.toJson();
    }

    public static String mkRet(Map<String, Object> result) {
        //_log.info("调用dal返回result={}", result);
        if(result == null) return null;
        String retCode = (String)result.get("rpcRetCode");
        if("0000".equals(retCode)) {
            if(result.get("bizResult") == null) return null;
            return result.get("bizResult").toString();
        }
        return null;
    }

    public static Boolean isSuccess(Map<String, Object> result) {
        if(result == null) return false;
        String retCode = (String) result.get("rpcRetCode");
        if("0000".equals(retCode) && result.get("bizResult") != null) return true;
        return false;
    }

}
