package org.xxpay.common.enumm;

/**
 * RPC通讯层签名计算方法枚举类
 * Created by admin on 2016/5/4.
 */
public enum RpcSignTypeEnum {

    NOT_SIGN(0),// 明文
    SHA1_SIGN(1);// SHA-1签名

    private Integer code;

    private RpcSignTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode()
    {
        return this.code;
    }

    public static RpcSignTypeEnum getRpcSignTypeEnum(Integer code) {
        if (code == null) {
            return null;
        }

        RpcSignTypeEnum[] values =RpcSignTypeEnum.values();
        for (RpcSignTypeEnum e : values) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
