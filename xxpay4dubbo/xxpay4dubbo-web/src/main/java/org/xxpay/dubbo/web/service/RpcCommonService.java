package org.xxpay.dubbo.web.service;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;
import org.xxpay.dubbo.api.service.*;

/**
 * @author: dingzhiwei
 * @date: 17/9/10
 * @description:
 */
@Service
public class RpcCommonService {

    @Reference(version = "1.0.0", timeout = 10000, retries = 0)
    public IMchInfoService rpcMchInfoService;

    @Reference(version = "1.0.0", timeout = 10000, retries = 0)
    public IPayChannelService rpcPayChannelService;

    @Reference(version = "1.0.0", timeout = 10000, retries = 0)
    public IPayOrderService rpcPayOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = 0)
    public IPayChannel4WxService rpcPayChannel4WxService;

    @Reference(version = "1.0.0", timeout = 10000, retries = 0)
    public IPayChannel4AliService rpcPayChannel4AliService;

    @Reference(version = "1.0.0", timeout = 10000, retries = 0)
    public INotifyPayService rpcNotifyPayService;

    @Reference(version = "1.0.0", timeout = 10000, retries = 0)
    public ITransOrderService rpcTransOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = 0)
    public IRefundOrderService rpcRefundOrderService;

}
