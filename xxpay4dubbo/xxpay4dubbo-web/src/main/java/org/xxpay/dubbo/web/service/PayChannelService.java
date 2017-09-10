package org.xxpay.dubbo.web.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.common.util.RpcUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/9
 * @description:
 */
@Service
public class PayChannelService {

    @Autowired
    private RpcCommonService rpcCommonService;

    public JSONObject getByMchIdAndChannelId(String mchId, String channelId) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("mchId", mchId);
        paramMap.put("channelId", channelId);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = rpcCommonService.rpcPayChannelService.selectPayChannel(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) return null;
        return JSONObject.parseObject(s);
    }

}
