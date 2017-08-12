package org.xxpay.service.channel.tencent.protocol.redpack_protocol;

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.common.RandomStringGenerator;
import org.xxpay.service.channel.tencent.common.Signature;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dingzhiwei on 2016/6/3.
 */
public class SendRedpackReqData {

    private String nonce_str = "";          // 随机字符串
    private String sign = "";               // 签名
    private String mch_billno = "";         // 商户订单号,商户订单号（每个订单号必须唯一）组成：mch_id+yyyymmdd+10位一天内不能重复的数字
    private String mch_id = "";             // 商户号
    private String wxappid = "";            // 公众号ID
    private String send_name = "";          // 商户名称,红包发送者名称
    private String re_openid = "";          // 接受红包的用户
    private String total_amount = "";       // 付款金额
    private String total_num = "";          // 红包发放总人数
    private String wishing = "";            // 红包祝福语
    private String client_ip = "";          // 调用接口的机器Ip地址
    private String act_name = "";           // 活动名称
    private String remark = "";             // 备注

    /**
     *
     * @param mch_billno
     * @param send_name
     * @param re_openid
     * @param total_amount
     * @param total_num
     * @param wishing
     * @param act_name
     * @param remark
     */
    public SendRedpackReqData(Configure configure, String mch_billno, String send_name, String re_openid,
                              String total_amount, String total_num, String wishing, String act_name, String remark){
        //微信分配的公众号ID（开通公众号之后可以获取到）
        setWxappid(configure.getAppID());

        //微信支付分配的商户号ID（开通公众号的微信支付功能之后可以获取到）
        setMch_id(configure.getMchID());

        setMch_billno(mch_billno);
        setSend_name(send_name);
        setRe_openid(re_openid);
        setTotal_amount(total_amount);
        setTotal_num(total_num);
        setWishing(wishing);
        setClient_ip(configure.getIp());
        setAct_name(act_name);
        setRemark(remark);

        //随机字符串，不长于32 位
        setNonce_str(RandomStringGenerator.getRandomStringByLength(32));

        //根据API给的签名规则进行签名
        String sign = Signature.getSign(toMap(), configure.getKey());
        setSign(sign);//把签名数据设置到Sign这个属性中
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

    public String getMch_billno() {
        return mch_billno;
    }

    public void setMch_billno(String mch_billno) {
        this.mch_billno = mch_billno;
    }

    public String getMch_id() {
        return mch_id;
    }

    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
    }

    public String getWxappid() {
        return wxappid;
    }

    public void setWxappid(String wxappid) {
        this.wxappid = wxappid;
    }

    public String getSend_name() {
        return send_name;
    }

    public void setSend_name(String send_name) {
        this.send_name = send_name;
    }

    public String getRe_openid() {
        return re_openid;
    }

    public void setRe_openid(String re_openid) {
        this.re_openid = re_openid;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getTotal_num() {
        return total_num;
    }

    public void setTotal_num(String total_num) {
        this.total_num = total_num;
    }

    public String getWishing() {
        return wishing;
    }

    public void setWishing(String wishing) {
        this.wishing = wishing;
    }

    public String getClient_ip() {
        return client_ip;
    }

    public void setClient_ip(String client_ip) {
        this.client_ip = client_ip;
    }

    public String getAct_name() {
        return act_name;
    }

    public void setAct_name(String act_name) {
        this.act_name = act_name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
