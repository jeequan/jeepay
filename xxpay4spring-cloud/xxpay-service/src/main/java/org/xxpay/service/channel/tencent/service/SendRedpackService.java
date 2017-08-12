package org.xxpay.service.channel.tencent.service;

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.protocol.redpack_protocol.SendRedpackReqData;
import org.springframework.stereotype.Service;

/**
 * User: dingzhiwei
 * Date: 2016/06/03
 * Time: 22:57
 */
@Service
public class SendRedpackService extends BaseService {

    public SendRedpackService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.SEND_REDPACK_API);
    }

    /**
     * 请求红包服务
     * @param sendRedpackReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的数据
     * @throws Exception
     */
    public String request(SendRedpackReqData sendRedpackReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(sendRedpackReqData);

        return responseString;
    }
}
