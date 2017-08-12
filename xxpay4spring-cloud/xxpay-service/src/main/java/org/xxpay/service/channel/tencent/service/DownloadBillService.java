package org.xxpay.service.channel.tencent.service;

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.protocol.downloadbill_protocol.DownloadBillReqData;

/**
 * User: rizenguo
 * Date: 2014/10/29
 * Time: 16:04
 */
public class DownloadBillService extends BaseService {

    public DownloadBillService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.DOWNLOAD_BILL_API);
    }

    //ALL，返回当日所有订单信息，默认值
    public static final String BILL_TYPE_ALL = "ALL";

    //SUCCESS，返回当日成功支付的订单
    public static final String BILL_TYPE_SUCCESS = "SUCCESS";

    //REFUND，返回当日退款订单
    public static final String BILL_TYPE_REFUND = "REFUND";

    //REVOKED，已撤销的订单
    public static final String BILL_TYPE_REVOKE = "REVOKE";


    /**
     * 请求对账单下载服务
     * @param downloadBillReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
    public String request(DownloadBillReqData downloadBillReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(downloadBillReqData);

        return responseString;
    }

}
