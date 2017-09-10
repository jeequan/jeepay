package org.xxpay.common.enumm;

/**
 * RPC调用返回码枚举类
 * 对应方法调用返回值中的rpcRetCode和rpcRetMsg
 * Created by admin on 2016/4/27.
 */
public enum RetEnum {

    // 0000: 成功
    RET_SUCCESS("0000", ""),

    // 失败(00开始标示通讯层相关错误码)
    RET_REMOTE_UNUSABLE("0001", "远程服务不可用"),
    RET_REMOTE_INVALID("0002", "客户端非法调用"),
    RET_NO_BIZ_SEQUENCE_NO("0003", "远程服务调用业务流水号不存在"),
    RET_REMOTE_CHECK_SIGN_FAIL("0004", "远程服务调用签名验证失败"),
    RET_REMOTE_RPC_SEQ_NO_REPEATED("0005", "随机通讯码在指定时间内重复"),
    RET_REMOTE_SIGN_INVALID("0006", "远程服务调用签名计算方式错误"),
    RET_REMOTE_DEAL_EXCEPTION("0007", "远程服务调用处理异常"),
    RET_REMOTE_PROTOCOL_INVALID("0008", "客户端调用协议非法"),
    RET_REMOTE_HTTP_METHOD_INVALID("0009", "客户端请求方式非法"),

    // 失败(01开始标示参数校验相关错误码)
    RET_PARAM_NOT_FOUND("0101", "参数不存在"),
    RET_PARAM_INVALID("0102", "无效的参数"),
    RET_PARAM_TOO_LARGE_LIST("0103", "列表超长"),
    RET_PARAM_TYPE_INVALID("0104", "参数类型错误"),
    RET_CURRENT_PAGE_INVALID("0105", "当前页码非法"),
    RET_VIEW_NUMBER_INVALID("0106", "分页显示数目非法"),
    RET_VIEW_LIMIT_INVALID("0107", "数据排列显示数目非法"),

    //  失败(02开始标示DB操作相关错误码)
    RET_DB_FAIL("0201", "数据库操作失败"),

    // 业务相关
    RET_BIZ_DATA_NOT_EXISTS("1001", "数据不存在"),
    RET_BIZ_SING_DATA_FAIL("1002", "商户签名数据不正确"),
    RET_BIZ_WX_PAY_CREATE_FAIL("1003", "微信支付下单失败"),
    RET_BIZ_ALI_PAY_CREATE_FAIL("1004", "支付宝支付下单失败"),
    RET_BIZ_PAY_NOTIFY_VERIFY_FAIL("1005", "支付通知数据验证不正确"),


    // 未知错误
    RET_UNKNOWN_ERROR("9999", "未知错误");

    private String code;
    private String message;

    private RetEnum(String code, String message) { this.code = code;
        this.message = message; }

    public String getCode()
    {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public static RetEnum getRetEnum(String code) {
        if (code == null) {
            return null;
        }

        RetEnum[] values = RetEnum.values();
        for (RetEnum e : values) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
