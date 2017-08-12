package org.xxpay.service.channel.tencent.protocol.transfers_protocol;

/**
 * Created by dingzhiwei on 16/6/30.
 */
public class GetTransfersResData {

    //协议层
    private String return_code = "";        // 返回状态码
    private String return_msg = "";         // 返回信息

    //协议返回的具体数据（以下字段在return_code 为SUCCESS 的时候有返回）
    private String result_code = "";        // 业务结果
    private String err_code = "";           // 错误代码
    private String err_code_des = "";       // 错误代码描述

    //业务返回的具体数据（以下字段在return_code 和result_code 都为SUCCESS 的时候有返回）
    private String partner_trade_no = "";         // 商户订单号
    private String mch_id = "";             // 商户号

/*

    商户单号	partner_trade_no	是	10000098201411111234567890	String(28)	商户使用查询API填写的单号的原路返回.
    商户号	mch_id	是	10000098	String(32)	微信支付分配的商户号
    付款单号	detail_id	是	1000000000201503283103439304	String(32)	调用企业付款API时，微信系统内部产生的单号
    转账状态	status	是	SUCCESS	string(16)
    SUCCESS:转账成功
    FAILED:转账失败
    PROCESSING:处理中
    失败原因	reason	否	余额不足	String	如果失败则有失败原因
    收款用户openid	openid	是	oxTWIuGaIt6gTKsQRLau2M0yL16E	 	转账的openid
    收款用户姓名	transfer_name	否	马华	String	收款用户姓名
    付款金额	payment_amount	是	5000	int	付款金额单位分）
    转账时间	transfer_time	是	2015-04-21 20:00:00	String	发起转账的时间
    付款描述	desc	是	车险理赔	String	付款时候的描述

    */

    private String detail_id = "";          // 付款单号
    private String status = "";             // 状态,    SUCCESS:转账成功 FAILED:转账失败 PROCESSING:处理中
    private String reason = "";             // 失败原因
    private String openid = "";             // 收款用户openid
    private String transfer_name = "";      // 收款用户姓名
    private String payment_amount = "";     // 付款金额（单位分）
    private String transfer_time = "";      // 发起转账的时间
    private String desc = "";               // 付款时候的描述

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDetail_id() {
        return detail_id;
    }

    public void setDetail_id(String detail_id) {
        this.detail_id = detail_id;
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

    public String getMch_id() {
        return mch_id;
    }

    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getPartner_trade_no() {
        return partner_trade_no;
    }

    public void setPartner_trade_no(String partner_trade_no) {
        this.partner_trade_no = partner_trade_no;
    }

    public String getPayment_amount() {
        return payment_amount;
    }

    public void setPayment_amount(String payment_amount) {
        this.payment_amount = payment_amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransfer_name() {
        return transfer_name;
    }

    public void setTransfer_name(String transfer_name) {
        this.transfer_name = transfer_name;
    }

    public String getTransfer_time() {
        return transfer_time;
    }

    public void setTransfer_time(String transfer_time) {
        this.transfer_time = transfer_time;
    }

    @Override
    public String toString() {
        return "GetTransfersResData{" +
                "desc='" + desc + '\'' +
                ", return_code='" + return_code + '\'' +
                ", return_msg='" + return_msg + '\'' +
                ", result_code='" + result_code + '\'' +
                ", err_code='" + err_code + '\'' +
                ", err_code_des='" + err_code_des + '\'' +
                ", partner_trade_no='" + partner_trade_no + '\'' +
                ", mch_id='" + mch_id + '\'' +
                ", detail_id='" + detail_id + '\'' +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", openid='" + openid + '\'' +
                ", transfer_name='" + transfer_name + '\'' +
                ", payment_amount='" + payment_amount + '\'' +
                ", transfer_time='" + transfer_time + '\'' +
                '}';
    }
}
