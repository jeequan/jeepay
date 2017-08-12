package org.xxpay.service.channel.tencent.service;

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.protocol.order_protocol.UnifiedOrderReqData;
import org.springframework.stereotype.Service;

/**
 * User: dingzhiwei
 * Date: 2016/04/29
 * Time: 19:03
 */
@Service
public class UnifiedOrderService extends BaseService {

    public UnifiedOrderService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.UNIFIED_ORDER_API);
    }

    /**
     * 请求支付服务
     * @param unifiedOrderReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的数据
     * @throws Exception
     */
    public String request(UnifiedOrderReqData unifiedOrderReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(unifiedOrderReqData);

        return responseString;
    }
}
