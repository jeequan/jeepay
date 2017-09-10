package org.xxpay.common.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 业务流水号工具类
 * Created by admin on 2016/4/28.
 */
public class BizSequenceUtils {

    private static Object lock = new Object();

    private static BizSequenceUtils instance;

    /**
     * The FieldPosition.
     */
    private static final FieldPosition HELPER_POSITION = new FieldPosition(0);

    /**
     * This Format for format the number to special format.
     */
    private final static NumberFormat numberFormat = new DecimalFormat("00000000");

    /**
     * This int is the sequence number ,the default value is 1.
     */
    private static AtomicInteger seq = new AtomicInteger(1);

    private static final int MAX = 99999999;

    private BizSequenceUtils() {
    }

    public static BizSequenceUtils getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new BizSequenceUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 生成业务流水号(唯一标示一笔业务)
     *
     * @param bizSeqNoPrefix 业务前缀(2字符,参见Constant.**_BIZ_SEQUENCE_NO_PREFIX)
     * @return
     */
    public String generateBizSeqNo(String bizSeqNoPrefix) {
        StringBuffer bizSeqNo = new StringBuffer();
        bizSeqNo.append(bizSeqNoPrefix)
                .append(DateUtils.getCurrentTimeStr("yyMMddHHmmss"))
                .append(getSeq());
        return bizSeqNo.toString();
    }

    private String getSeq() {
        StringBuffer sb = new StringBuffer();
        numberFormat.format(seq, sb, HELPER_POSITION);
        if (!seq.compareAndSet(MAX, 0)) {
            seq.incrementAndGet();
        }
        return sb.toString();
    }

}
