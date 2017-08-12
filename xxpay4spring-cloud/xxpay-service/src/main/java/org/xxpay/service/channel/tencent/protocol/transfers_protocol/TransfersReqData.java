package org.xxpay.service.channel.tencent.protocol.transfers_protocol;

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.common.RandomStringGenerator;
import org.xxpay.service.channel.tencent.common.Signature;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dingzhiwei on 2016/6/30.
 */
public class TransfersReqData {

    private String mch_appid = "";          // 公众号ID
    private String mchid = "";              // 商户号
    private String device_info = "";        // 设备号
    private String nonce_str = "";          // 随机字符串
    private String sign = "";               // 签名
    private String partner_trade_no = "";   // 商户订单号,需保证唯一性
    private String openid = "";             // 用户openid
    private String check_name = "";         // NO_CHECK：不校验真实姓名
                                            // FORCE_CHECK：强校验真实姓名（未实名认证的用户会校验失败，无法转账）
                                            // OPTION_CHECK：针对已实名认证的用户才校验真实姓名（未实名认证用户不校验，可以转账成功）
    private String re_user_name = "";       // 收款用户姓名
    private String amount = "";             // 金额
    private String desc = "";               // 企业付款描述信息
    private String spbill_create_ip = "";   // Ip地址

    /**
     *
     * @param partner_trade_no
     * @param device_info
     * @param openid
     * @param check_name
     * @param re_user_name
     * @param amount
     * @param desc
     */
    public TransfersReqData(Configure configure, String partner_trade_no, String device_info, String openid, String check_name,
                            String re_user_name, String amount, String desc){

        //微信分配的公众号ID（开通公众号之后可以获取到）
        setMch_appid(configure.getAppID());

        //微信支付分配的商户号ID（开通公众号的微信支付功能之后可以获取到）
        setMchid(configure.getMchID());

        setPartner_trade_no(partner_trade_no);
        setDevice_info(device_info);
        setOpenid(openid);
        setCheck_name(check_name);
        setRe_user_name(re_user_name);
        setAmount(amount);
        setDesc(desc);
        setSpbill_create_ip(configure.getIp());

        //随机字符串，不长于32 位
        setNonce_str(RandomStringGenerator.getRandomStringByLength(32));

        //根据API给的签名规则进行签名
        String sign = Signature.getSign(toMap(), configure.getKey());
        setSign(sign);//把签名数据设置到Sign这个属性中
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCheck_name() {
        return check_name;
    }

    public void setCheck_name(String check_name) {
        this.check_name = check_name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDevice_info() {
        return device_info;
    }

    public void setDevice_info(String device_info) {
        this.device_info = device_info;
    }

    public String getMch_appid() {
        return mch_appid;
    }

    public void setMch_appid(String mch_appid) {
        this.mch_appid = mch_appid;
    }

    public String getMchid() {
        return mchid;
    }

    public void setMchid(String mchid) {
        this.mchid = mchid;
    }

    public String getNonce_str() {
        return nonce_str;
    }

    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
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

    public String getRe_user_name() {
        return re_user_name;
    }

    public void setRe_user_name(String re_user_name) {
        this.re_user_name = re_user_name;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSpbill_create_ip() {
        return spbill_create_ip;
    }

    public void setSpbill_create_ip(String spbill_create_ip) {
        this.spbill_create_ip = spbill_create_ip;
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
