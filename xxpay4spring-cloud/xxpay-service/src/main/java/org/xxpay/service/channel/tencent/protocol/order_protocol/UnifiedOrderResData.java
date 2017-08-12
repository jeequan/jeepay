package org.xxpay.service.channel.tencent.protocol.order_protocol;

/**
 * User: dingzhiwei
 * Date: 2016/04/29
 * Time: 18:42
 */

/**
 * 统一下单提交Post数据给到API之后，API会返回XML格式的数据，这个类用来装这些数据
 */
public class UnifiedOrderResData {

    //协议层
    private String return_code = "";        // 返回状态码
    private String return_msg = "";         // 返回信息

    //协议返回的具体数据（以下字段在return_code 为SUCCESS 的时候有返回）
    private String appid = "";              // 应用APPID
    private String mch_id = "";             // 商户号
    private String device_info = "";        // 设备号
    private String nonce_str = "";          // 随机字符串
    private String sign = "";               // 签名
    private String result_code = "";        // 业务结果
    private String err_code = "";           // 错误代码
    private String err_code_des = "";       // 错误代码描述

    //业务返回的具体数据（以下字段在return_code 和result_code 都为SUCCESS 的时候有返回）
    private String trade_type = "";         // 交易类型
    private String prepay_id = "";          // 预支付交易会话标识
    private String code_url = "";           // 二维码链接

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

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getMch_id() {
        return mch_id;
    }

    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
    }

    public String getDevice_info() {
        return device_info;
    }

    public void setDevice_info(String device_info) {
        this.device_info = device_info;
    }

    public String getNonce_str() {
        return nonce_str;
    }

    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getResult_code() {
        return result_code;
    }

    public void setResult_code(String result_code) {
        this.result_code = result_code;
    }

    public String getErr_code() {
        return err_code;
    }

    public void setErr_code(String err_code) {
        this.err_code = err_code;
    }

    public String getErr_code_des() {
        return err_code_des;
    }

    public void setErr_code_des(String err_code_des) {
        this.err_code_des = err_code_des;
    }

    public String getTrade_type() {
        return trade_type;
    }

    public void setTrade_type(String trade_type) {
        this.trade_type = trade_type;
    }

    public String getPrepay_id() {
        return prepay_id;
    }

    public void setPrepay_id(String prepay_id) {
        this.prepay_id = prepay_id;
    }

    public String getCode_url() {
        return code_url;
    }

    public void setCode_url(String code_url) {
        this.code_url = code_url;
    }
}
