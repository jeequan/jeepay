package org.xxpay.service.channel.tencent.protocol.transfers_protocol;

/**
 * Created by dingzhiwei on 16/6/30.
 */
public class TransfersResData {

    //协议层
    private String return_code = "";        // 返回状态码
    private String return_msg = "";         // 返回信息

    //协议返回的具体数据（以下字段在return_code 为SUCCESS 的时候有返回）
    private String mch_appid = "";          // 公众号appid
    private String mch_id = "";             // 商户号
    private String device_info = "";        // 设备号
    private String nonce_str = "";          // 随机字符串
    private String result_code = "";        // 业务结果
    private String err_code = "";           // 错误代码
    private String err_code_des = "";       // 错误代码描述

    //业务返回的具体数据（以下字段在return_code 和result_code 都为SUCCESS 的时候有返回）
    private String partner_trade_no = "";   // 商户订单号
    private String payment_no = "";         // 微信订单号
    private String payment_time = "";       // 微信支付成功时间

    public String getDevice_info() {
        return device_info;
    }

    public void setDevice_info(String device_info) {
        this.device_info = device_info;
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

    public String getMch_appid() {
        return mch_appid;
    }

    public void setMch_appid(String mch_appid) {
        this.mch_appid = mch_appid;
    }

    public String getMch_id() {
        return mch_id;
    }

    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
    }

    public String getNonce_str() {
        return nonce_str;
    }

    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public String getPartner_trade_no() {
        return partner_trade_no;
    }

    public void setPartner_trade_no(String partner_trade_no) {
        this.partner_trade_no = partner_trade_no;
    }

    public String getPayment_no() {
        return payment_no;
    }

    public void setPayment_no(String payment_no) {
        this.payment_no = payment_no;
    }

    public String getPayment_time() {
        return payment_time;
    }

    public void setPayment_time(String payment_time) {
        this.payment_time = payment_time;
    }

    public String getResult_code() {
        return result_code;
    }

    public void setResult_code(String result_code) {
        this.result_code = result_code;
    }

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

    @Override
    public String toString() {
        return "TransfersResData{" +
                "device_info='" + device_info + '\'' +
                ", return_code='" + return_code + '\'' +
                ", return_msg='" + return_msg + '\'' +
                ", mch_appid='" + mch_appid + '\'' +
                ", mch_id='" + mch_id + '\'' +
                ", nonce_str='" + nonce_str + '\'' +
                ", result_code='" + result_code + '\'' +
                ", err_code='" + err_code + '\'' +
                ", err_code_des='" + err_code_des + '\'' +
                ", partner_trade_no='" + partner_trade_no + '\'' +
                ", payment_no='" + payment_no + '\'' +
                ", payment_time='" + payment_time + '\'' +
                '}';
    }
}
