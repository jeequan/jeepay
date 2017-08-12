package org.xxpay.service.channel.tencent.service;

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.protocol.transfers_protocol.GetTransfersReqData;
import org.springframework.stereotype.Service;

/**
 * User: dingzhiwei
 * Date: 2016/06/30
 * Time: 16:49
 */
@Service
public class GetTransfersService extends BaseService {

    public GetTransfersService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.GET_TRANSFERS_API);
    }

    /**
     * 请求红包服务
     * @param getTransfersReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的数据
     * @throws Exception
     */
    public String request(GetTransfersReqData getTransfersReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(getTransfersReqData);

        return responseString;
    }
}
