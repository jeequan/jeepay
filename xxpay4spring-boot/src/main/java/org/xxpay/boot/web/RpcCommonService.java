package org.xxpay.boot.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.boot.service.*;

/**
 * @author: dingzhiwei
 * @date: 17/9/10
 * @description:
 */
@Service
public class RpcCommonService {

    @Autowired
    public IMchInfoService rpcMchInfoService;

    @Autowired
    public IPayChannelService rpcPayChannelService;

    @Autowired
    public IPayOrderService rpcPayOrderService;

    @Autowired
    public IPayChannel4WxService rpcPayChannel4WxService;

    @Autowired
    public IPayChannel4AliService rpcPayChannel4AliService;

    @Autowired
    public INotifyPayService rpcNotifyPayService;

}
