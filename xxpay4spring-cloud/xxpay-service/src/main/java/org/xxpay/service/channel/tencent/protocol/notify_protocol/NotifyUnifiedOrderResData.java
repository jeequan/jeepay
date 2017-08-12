package org.xxpay.service.channel.tencent.protocol.notify_protocol;

/**
 * User: dingzhiwei
 * Date: 2016/05/13
 * Time: 15:20
 */

import java.io.Serializable;

/**
 * 统一下单通知响应数据
 */
public class NotifyUnifiedOrderResData implements Serializable {

    private static final long serialVersionUID = -2089583048909946750L;

    //协议层
    private String return_code;             // 返回状态码
    private String return_msg;              // 返回信息

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
