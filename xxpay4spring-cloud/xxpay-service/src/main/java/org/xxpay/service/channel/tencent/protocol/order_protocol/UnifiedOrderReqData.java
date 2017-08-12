package org.xxpay.service.channel.tencent.protocol.order_protocol;

/**
 * User: dingzhiwei
 * Date: 2016/04/29
 * Time: 18:06
 */

import org.xxpay.service.channel.tencent.common.Configure;
import org.xxpay.service.channel.tencent.common.RandomStringGenerator;
import org.xxpay.service.channel.tencent.common.Signature;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求统一下单API需要提交的数据
 */
public class UnifiedOrderReqData {

    private String appid = "";              // 公众号或应用ID
    private String mch_id = "";             // 商户号
    private String device_info = "";        // 设备号
    private String nonce_str = "";          // 随机字符串
    private String sign = "";               // 签名
    private String body = "";               // 商品描述
    private String detail = "";             // 商品详情
    private String attach = "";             // 附加数据
    private String out_trade_no = "";       // 商户订单号
    private String fee_type = "";           // 货币类型
    private String total_fee = "";          // 总金额
    private String spbill_create_ip = "";   // 终端IP
    private String time_start = "";         // 交易起始时间
    private String time_expire = "";        // 交易结束时间
    private String goods_tag = "";          // 商品标记
    private String notify_url = "";         // 通知地址
    private String trade_type = "";         // 交易类型
    private String product_id = "";         // 商品ID
    private String limit_pay = "";          // 指定支付方式
    private String openid = "";             // 用户标识

    /**
     *
     * @param deviceInfo 终端设备号(门店号或收银设备ID)，注意：PC网页或公众号内支付请传"WEB"
     * @param body 商品或支付单简要描述
     * @param detail 商品名称明细列表
     * @param attach 附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据
     * @param outTradeNo 商户系统内部的订单号,32个字符内、可包含字母, 其他说明见商户订单号
     * @param feeType 符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
     * @param totalFee 订单总金额，单位为分，详见支付金额
     * @param spBillCreateIP APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP
     * @param timeStart 订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
     * @param timeExpire 订单失效时间，格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010。其他详见时间规则,注意：最短失效时间间隔必须大于5分钟
     * @param goodsTag 商品标记，代金券或立减优惠功能的参数，说明详见代金券或立减优惠
     * @param notifyUrl 接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数
     * @param tradeType 取值：JSAPI--公众号支付、NATIVE--原生扫码支付、APP--app支付
     * @param productId trade_type=NATIVE，此参数必传。此id为二维码中包含的商品ID，商户自行定义
     * @param limitPay no_credit--指定不能使用信用卡支付
     * @param openId trade_type=JSAPI，此参数必传，用户在商户appid下的唯一标识。
     */
    public UnifiedOrderReqData(Configure configure, String deviceInfo, String body, String detail, String attach, String outTradeNo, String feeType,
                               String totalFee, String spBillCreateIP, String timeStart, String timeExpire, String goodsTag,
                               String notifyUrl, String tradeType, String productId, String limitPay, String openId){

        //微信分配的公众号ID（开通公众号之后可以获取到）
        setAppid(configure.getAppID());

        //微信支付分配的商户号ID（开通公众号的微信支付功能之后可以获取到）
        setMch_id(configure.getMchID());

        //支付终端设备号，方便追溯这笔交易发生在哪台终端设备上
        setDevice_info(deviceInfo);

        //要支付的商品的描述信息，用户会在支付成功页面里看到这个信息
        setBody(body);
        setDetail(detail);

        //商户系统内部的订单号,32个字符内可包含字母, 确保在商户系统唯一
        setOut_trade_no(outTradeNo);
        setFee_type(feeType);
        //订单总金额，单位为“分”，只能整数
        setTotal_fee(totalFee);

        //订单生成的机器IP
        setSpbill_create_ip(spBillCreateIP);

        //订单生成时间， 格式为yyyyMMddHHmmss，如2009年12 月25 日9 点10 分10 秒表示为20091225091010。时区为GMT+8 beijing。该时间取自商户服务器
        setTime_start(timeStart);

        //订单失效时间，格式同上
        setTime_expire(timeExpire);

        //商品标记，微信平台配置的商品标记，用于优惠券或者满减使用
        setGoods_tag(goodsTag);
        setNotify_url(notifyUrl);
        setTrade_type(tradeType);
        setProduct_id(productId);
        setLimit_pay(limitPay);
        setOpenid(openId);
        //随机字符串，不长于32 位
        setNonce_str(RandomStringGenerator.getRandomStringByLength(32));

        //附加信息
        setAttach(attach);

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getSpbill_create_ip() {
        return spbill_create_ip;
    }

    public void setSpbill_create_ip(String spbill_create_ip) {
        this.spbill_create_ip = spbill_create_ip;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_expire() {
        return time_expire;
    }

    public void setTime_expire(String time_expire) {
        this.time_expire = time_expire;
    }

    public String getGoods_tag() {
        return goods_tag;
    }

    public void setGoods_tag(String goods_tag) {
        this.goods_tag = goods_tag;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getFee_type() {
        return fee_type;
    }

    public void setFee_type(String fee_type) {
        this.fee_type = fee_type;
    }

    public String getNotify_url() {
        return notify_url;
    }

    public void setNotify_url(String notify_url) {
        this.notify_url = notify_url;
    }

    public String getTrade_type() {
        return trade_type;
    }

    public void setTrade_type(String trade_type) {
        this.trade_type = trade_type;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getLimit_pay() {
        return limit_pay;
    }

    public void setLimit_pay(String limit_pay) {
        this.limit_pay = limit_pay;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
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
