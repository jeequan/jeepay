package org.xxpay.shop.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * 日期工具类
 * 
 */

public class DateUtil {
	
	private final SimpleDateFormat format;

    public DateUtil(SimpleDateFormat format) {
        this.format = format;
    }
    
    public SimpleDateFormat getFormat() {
        return format;
    }
    
    //紧凑型日期格式，也就是纯数字类型yyyyMMdd
	public static final DateUtil COMPAT = new DateUtil(new SimpleDateFormat("yyyyMMdd"));
	
	//常用日期格式，yyyy-MM-dd
	public static final DateUtil COMMON = new DateUtil(new SimpleDateFormat("yyyy-MM-dd"));
	public static final DateUtil COMMON_FULL = new DateUtil(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	
	//使用斜线分隔的，西方多采用，yyyy/MM/dd
	public static final DateUtil SLASH = new DateUtil(new SimpleDateFormat("yyyy/MM/dd"));
	
	//中文日期格式常用，yyyy年MM月dd日
	public static final DateUtil CHINESE = new DateUtil(new SimpleDateFormat("yyyy年MM月dd日"));
	public static final DateUtil CHINESE_FULL = new DateUtil(new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒"));
	
	/**
	 * 日期获取字符串
	 */
	public String getDateText(Date date){
		return getFormat().format(date);
	}
	
	/**
	 * 字符串获取日期
	 * @throws ParseException 
	 */
	public Date getTextDate(String text) throws ParseException{
		return getFormat().parse(text);
	}
	
	/**
	 * 日期获取字符串
	 */
	public static String getDateText(Date date ,String format){
		return new SimpleDateFormat(format).format(date);
	}
	
	/**
	 * 字符串获取日期
	 * @throws ParseException 
	 */
	public static Date getTextDate(String dateText ,String format) throws ParseException{
		return new SimpleDateFormat(format).parse(dateText);
	}
	
	/**
	 * 根据日期，返回其星期数，周一为1，周日为7
	 * @param date
	 * @return
	 */
	public static int getWeekDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int w = calendar.get(Calendar.DAY_OF_WEEK);
		int ret;
		if (w == Calendar.SUNDAY)
			ret = 7;
		else
			ret = w - 1;
		return ret;
	}

	public static String getSeqString() {
		SimpleDateFormat fm = new SimpleDateFormat("yyyyMMddHHmmss");
		return fm.format(new Date());
	}
	
}

