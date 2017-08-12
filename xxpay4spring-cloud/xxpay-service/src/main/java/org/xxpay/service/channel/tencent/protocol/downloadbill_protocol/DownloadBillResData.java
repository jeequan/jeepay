package org.xxpay.service.channel.tencent.protocol.downloadbill_protocol;

/**
 * User: rizenguo
 * Date: 2014/10/25
 * Time: 16:48
 */
public class DownloadBillResData {

    //协议层
    private String return_code = "";
    private String return_msg = "";

    public String getReturn_code() {
        return return_code;
    }

    public void setReturn_code(String return_code) {
        this.return_code = return_code;
    }

    public String getReturn_msg() {
        return return_msg;
    }

    public void setReturn_msg(String return_msg) {
        this.return_msg = return_msg;
    }
}
