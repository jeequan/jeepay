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
public class QueryRedpackReqData {
    private String nonce_str = "";          // 随机字符串
    private String sign = "";               // 签名
    private String mch_billno = "";         // 商户订单号,商户订单号（每个订单号必须唯一）组成：mch_id+yyyymmdd+10位一天内不能重复的数字
    private String mch_id = "";             // 商户号
    private String appid = "";              // 公众号ID
    private String bill_type = "";          // 订单类型,MCHT:通过商户订单号获取红包信息

    /**
     *
     * @param mch_billno
     * @param bill_type
     */
    public QueryRedpackReqData(Configure configure, String mch_billno, String bill_type){
        //微信分配的公众号ID（开通公众号之后可以获取到）
        setAppid(configure.getAppID());

        //微信支付分配的商户号ID（开通公众号的微信支付功能之后可以获取到）
        setMch_id(configure.getMchID());

        setMch_billno(mch_billno);
        setBill_type(bill_type);

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

    public String getBill_type() {
        return bill_type;
    }

    public void setBill_type(String bill_type) {
        this.bill_type = bill_type;
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
