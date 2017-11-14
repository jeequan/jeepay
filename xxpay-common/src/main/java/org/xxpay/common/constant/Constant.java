package org.xxpay.common.constant;

/**
 * Created by admin on 2016/4/27.
 */
public class Constant {

    // 账户业务模块流水号前缀(account)
    public static final String AC_BIZ_SEQUENCE_NO_PREFIX = "ac";
    // 账户业务模块流水号前缀(config)
    public static final String CF_BIZ_SEQUENCE_NO_PREFIX = "cf";
    // 账户业务模块流水号前缀(metadata)
    public static final String MD_BIZ_SEQUENCE_NO_PREFIX = "md";
    public static final String ME_BIZ_SEQUENCE_NO_PREFIX = "me";
    // 认证业务模块流水号前缀(auth)
    public static final String AU_BIZ_SEQUENCE_NO_PREFIX = "au";
    // 交易业务模块流水号前缀(trans)
    public static final String TRANS_BIZ_SEQUENCE_NO_PREFIX = "tn";
    // 日志业务模块流水号前缀(log)
    public static final String LG_BIZ_SEQUENCE_NO_PREFIX = "lg";
    // zookeeper监控业务模块流水号前缀(zk)
    public static final String ZK_BIZ_SEQUENCE_NO_PREFIX = "zk";
    // 朋友圈流水号前缀(moments)
    public static final String MM_BIZ_SEQUENCE_NO_PREFIX = "mm";
    // 背包流水号前缀(pack)
    public static final String PK_BIZ_SEQUENCE_NO_PREFIX = "pk";

    // 随机通讯码不重复的时间间隔(ms)
    public static final long RPC_SEQ_NO_NOT_REPEAT_INTERVAL = 5 * 1000;

    // 服务端返回map中业务数据结果对应的key名称
    public static final String BIZ_RESULT_KEY = "bizResult";

}
