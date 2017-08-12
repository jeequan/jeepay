package org.xxpay.common.constant;

/**
 * @Description: 支付返回码定义
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
public enum PayEnum {

    /*
    0010|系统错误 |系统超时或异常|系统异常，请用相同参数重新调用
    0011|请使用post方法 |未使用post传递参数|请检查请求参数是否通过post方法提交
    0012|post数据为空 |post数据不能为空|请检查post数据是否为空
    0013|签名错误 |参数签名结果不正确|请检查签名参数和方法是否都符合签名算法要求
    0014|参数错误 |缺少参数或参数格式不正确|请根据具体的原因检查参数
    0015|商户不存在 |传入的商户ID在支付中心不存在|请检查mchID参数是否正确
    0110|第三方超时 |调用第三方支付系统超时|请重新调用
    0111|第三方异常 |调用第三方支付系统异常|根据提示错误信息检查
    0112|订单不存在 |商户订单不存在|请检查商户订单payOrderId参数
    0113|订单已支付 |商户订单已支付,无需重复操作|商户订单已支付,无需重复操作
    */

    ERR_0001("0001", "商户签名异常"),

    ERR_0010("0010", "系统错误"),
    ERR_0011("0011", "请使用post方法"),
    ERR_0012("0012", "post数据为空"),
    ERR_0013("0013", "签名错误"),
    ERR_0014("0014", "参数错误"),
    ERR_0015("0015", "商户不存在"),
    ERR_0110("0110", "第三方超时"),
    ERR_0111("0111", "第三方异常"),
    ERR_0112("0112", "订单不存在"),
    ERR_0113("0113", "订单已支付"),
    ERR_0114("0114", "商品不存在"),
    ERR_0115("0115", "价格不对"),
    ERR_0116("0116", "物品数量不对"),
    ERR_0117("0117", "过程返回255"),
    ERR_0118("0118", "DB错误");

    private String code;
    private String message;

    PayEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode()
    {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

}
