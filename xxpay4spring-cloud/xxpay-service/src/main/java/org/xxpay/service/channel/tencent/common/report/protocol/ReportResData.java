package org.xxpay.service.channel.tencent.common.report.protocol;

/**
 * User: rizenguo
 * Date: 2014/11/12
 * Time: 17:06
 */
public class ReportResData {

    //以下是API接口返回的对应数据
    private String return_code;
    private String return_msg;
    private String result_code;

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

    public String getResult_code() {
        return result_code;
    }

    public void setResult_code(String result_code) {
        this.result_code = result_code;
    }
}
