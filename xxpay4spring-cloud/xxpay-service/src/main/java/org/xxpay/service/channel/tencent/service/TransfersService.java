package org.xxpay.service.channel.tencent.service;

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.protocol.transfers_protocol.TransfersReqData;
import org.springframework.stereotype.Service;

/**
 * User: dingzhiwei
 * Date: 2016/06/30
 * Time: 16:37
 */
@Service
public class TransfersService extends BaseService {

    public TransfersService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.TRANSFERS_API);
    }

    /**
     * 请求红包服务
     * @param transfersReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的数据
     * @throws Exception
     */
    public String request(TransfersReqData transfersReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(transfersReqData);

        return responseString;
    }
}
