package org.xxpay.service.channel.tencent.common.report.protocol;

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.common.RandomStringGenerator;
import org.xxpay.service.channel.tencent.common.Signature;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * User: rizenguo
 * Date: 2014/11/12
 * Time: 17:05
 */
public class ReportReqData {

    //每个字段具体的意思请查看API文档
    private String appid;
    private String mch_id;
    private String sub_mch_id;
    private String device_info;
    private String nonce_str;
    private String sign;

    //上报对应的接口的完整URL，类似：https://api.mch.weixin.qq.com/pay/unifiedorder
    private String interface_url;
    //接口耗时情况，单位为毫秒
    private int execute_time_cost;
    //发起接口调用时的机器IP
    private String user_ip;
    //上报该统计请求时的系统时间，格式为yyyyMMddHHmmss
    private String time;

    //以下是API接口返回的对应数据
    private String return_code;
    private String return_msg;
    private String result_code;
    private String err_code;
    private String err_code_des;
    private String out_trade_no;

    /**
     * 请求统计上报API
     * @param deviceInfo 微信支付分配的终端设备号，商户自定义
     * @param interfaceUrl 上报对应的接口的完整URL，类似： https://api.mch.weixin.qq.com/pay/unifiedorder
     * @param executeTimeCost 接口耗时情况，单位为毫秒
     * @param returnCode API返回的对应字段
     * @param returnMsg API返回的对应字段
     * @param resultCode API返回的对应字段
     * @param errCode API返回的对应字段
     * @param errCodeDes API返回的对应字段
     * @param outTradeNo API返回的对应字段
     * @param userIp 发起接口调用时的机器IP
     */
    public ReportReqData(Configure configure, String deviceInfo, String interfaceUrl, int executeTimeCost, String returnCode, String returnMsg, String resultCode, String errCode, String errCodeDes, String outTradeNo, String userIp){

        //微信分配的公众号ID（开通公众号之后可以获取到）
        setAppid(configure.getAppID());

        //微信支付分配的商户号ID（开通公众号的微信支付功能之后可以获取到）
        setMch_id(configure.getMchID());

        //商户系统自己生成的唯一的订单号
        setOut_trade_no(outTradeNo);
        setDevice_info(deviceInfo);
        setInterface_url(interfaceUrl);
        setExecute_time_cost(executeTimeCost);
        setReturn_code(returnCode);
        setReturn_msg(returnMsg);
        setResult_code(resultCode);
        setErr_code(errCode);
        setErr_code_des(errCodeDes);
        setUser_ip(userIp);
        setTime(getTime());

        //随机字符串，不长于32 位
        setNonce_str(RandomStringGenerator.getRandomStringByLength(32));

        //根据API给的签名规则进行签名
        String sign = Signature.getSign(toMap(), configure.getKey());
        setSign(sign);//把签名数据设置到Sign这个属性中
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

    public String getInterface_url() {
        return interface_url;
    }

    public void setInterface_url(String interface_url) {
        this.interface_url = interface_url;
    }

    public int getExecute_time_cost() {
        return execute_time_cost;
    }

    public void setExecute_time_cost(int execute_time) {
        this.execute_time_cost = execute_time;
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

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getUser_ip() {
        return user_ip;
    }

    public void setUser_ip(String user_ip) {
        this.user_ip = user_ip;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<String, Object>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object obj;
            try {
                obj = field.get(this);
                if(obj!=null){
                    map.put(field.getName(), obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
