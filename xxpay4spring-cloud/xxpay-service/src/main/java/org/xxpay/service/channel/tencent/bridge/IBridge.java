package org.xxpay.service.channel.tencent.bridge;

/**
 * User: rizenguo
 * Date: 2014/12/1
 * Time: 17:11
 */
public interface IBridge {

    /**
     * 获取auth_code，这个是扫码终端设备从用户手机上扫取到的支付授权号，这个号是跟用户用来支付的银行卡绑定的，有效期是1分钟
     * @return 授权码
     */
    public String getAuthCode();

    /**
     * 获取out_trade_no，这个是商户系统内自己可以用来唯一标识该笔订单的字符串，可以包含字母和数字，不超过32位
     * @return 订单号
     */
    public String getOutTradeNo();

    /**
     * 获取body:要支付的商品的描述信息，用户会在支付成功页面里看到这个信息
     * @return 描述信息
     */
    public String getBody();


    /**
     * 获取attach:支付订单里面可以填的附加数据，API会将提交的这个附加数据原样返回，有助于商户自己可以注明该笔消费的具体内容，方便后续的运营和记录
     * @return 附加数据
     */
    public String getAttach();

    /**
     * 获取订单总额
     * @return 订单总额
     */
    public int getTotalFee();

    /**
     * 获取device_info:商户自己定义的扫码支付终端设备号，方便追溯这笔交易发生在哪台终端设备上
     * @return 支付终端设备号
     */
    public String getDeviceInfo();

    /**
     * 获取机器的ip地址
     * @return 机器设备的ip地址
     */
    public String getUserIp();

    /**
     * 获取spBillCreateIP:订单生成的机器IP
     * @return 订单生成的机器IP
     */
    public String getSpBillCreateIP();

    /**
     * 获取time_start:订单生成时间
     * @return 订单生成时间
     */
    public String getTimeStart();

    /**
     * 获取time_end:订单生成时间
     * @return 订单失效时间
     */
    public String getTimeExpire();

    /**
     * 获取goods_tag:商品标记，微信平台配置的商品标记，用于优惠券或者满减使用
     * @return 商品标记
     */
    public String getGoodsTag();

    /**
     * 获取transaction_id:微信平台支付成功时给分配的唯一交易号，一般只要有这个tracnsacion_id，后续的查询、撤销、退款都建议优先用这个，而不是商户自己的那个out_trade_no
     * @return 微信平台官方分配的交易号
     */
    public String getTransactionID();

    /**
     * 获取out_refund_no:商户系统内部的退款单号，商户系统内部唯一，同一退款单号多次请求只退一笔
     * @return 商户系统内部的退款单号
     */
    public String getOutRefundNo();

    /**
     * 获取refund_fee:获取本次退款请求所要退的具体金额，这个金额不能比这个订单的total_fee（总金额）还大
     * @return 本次退款请求所要退的具体金额
     */
    public int getRefundFee();

    /**
     * 获取refund_id:微信平台退款成功时给分配的唯一退款号，一般只要有这个refund_id，后续的查询建议优先用这个
     * @return 微信平台官方分配的退款号
     */
    public String getRefundID();

    /**
     * 获取bill_date:获取对账单API需要的日期，格式是yyyyMMdd
     * @return 要查询对账单的日期
     */
    public String getBillDate();

    /**
     * 获取bill_type:获取对账单API需要的数据类型，这些类型在DownloadBillService里面有定义
     * @return 要查询对账单的类型
     */
    public String getBillType();

    /**
     * 获取操作员的ID，默认等于商户号
     * @return 返回操作员的ID
     */
    public String getOpUserID();

    /**
     * 获取退款货币类型，符合ISO 4217标准的三位字母代码，默认为CNY（人民币）
     * @return 获取退款货币类型
     */
    public String getRefundFeeType();

}
