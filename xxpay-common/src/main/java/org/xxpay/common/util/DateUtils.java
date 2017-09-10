package org.xxpay.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间格式化工具
 * Created by admin on 2016/5/4.
 */
public class DateUtils {

    private static final String DEFAULT_CONVERT_PATTERN = "yyyyMMddHHmmssSSS";

    /**
     * 获取当前时间字符串(默认格式:yyyyMMddHHmmssSSS)
     *
     * @return
     */
    public static String getCurrentTimeStrDefault() {
        return getCurrentTimeStr(DEFAULT_CONVERT_PATTERN);
    }

    /**
     * 获取指定时间字符串(默认格式:yyyyMMddHHmmssSSS)
     * @param date
     * @return
     */
    public static String getTimeStrDefault(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_CONVERT_PATTERN);
        return dateFormat.format(date);
    }

    /**
     * 获取当前时间字符串
     *
     * @param pattern 转换格式
     * @return
     */
    public static String getCurrentTimeStr(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(new Date());
    }

    /**
     * 获取指定时间字符串
     * @param date
     * @return
     */
    public static String getTimeStr(Date date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    /**
     * 判断时间字符串是否为默认格式
     * @param dateTimeStr
     * @return
     */
    public static boolean isValidDefaultFormat(String dateTimeStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_CONVERT_PATTERN);
        try {
            dateFormat.parse(dateTimeStr);
            return true;
        } catch (Exception e) {
            // 如果抛出异常，说明格式不正确
            return false;
        }
    }

}
