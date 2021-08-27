/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.core.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 金额工具类
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
public class AmountUtil {

    /**
     * 将字符串"元"转换成"分"
     * @param str
     * @return
     */
    public static String convertDollar2Cent(String str) {
        DecimalFormat df = new DecimalFormat("0.00");
        StringBuffer sb = df.format(Double.parseDouble(str),
                new StringBuffer(), new FieldPosition(0));
        int idx = sb.toString().indexOf(".");
        sb.deleteCharAt(idx);
        for (; sb.length() != 1;) {
            if(sb.charAt(0) == '0') {
                sb.deleteCharAt(0);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串"分"转换成"元"（长格式），如：100分被转换为1.00元。
     * @param s
     * @return
     */
    public static String convertCent2Dollar(String s) {
        if("".equals(s) || s ==null){
            return "";
        }
        long l;
        if(s.length() != 0) {
            if(s.charAt(0) == '+') {
                s = s.substring(1);
            }
            l = Long.parseLong(s);
        } else {
            return "";
        }
        boolean negative = false;
        if(l < 0) {
            negative = true;
            l = Math.abs(l);
        }
        s = Long.toString(l);
        if(s.length() == 1) {
            return(negative ? ("-0.0" + s) : ("0.0" + s));
        }
        if(s.length() == 2) {
            return(negative ? ("-0." + s) : ("0." + s));
        } else {
            return(negative ? ("-" + s.substring(0, s.length() - 2) + "." + s
                    .substring(s.length() - 2)) : (s.substring(0,
                    s.length() - 2)
                    + "." + s.substring(s.length() - 2)));
        }
    }



    /**
     * 将Long "分"转换成"元"（长格式），如：100分被转换为1.00元。
     * @param s
     * @return
     */
    public static String convertCent2Dollar(Long s){
        if(s == null) {
            return "";
        }
        return new BigDecimal(s).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 将字符串"分"转换成"元"（短格式），如：100分被转换为1元。
     * @param s
     * @return
     */
    public static String convertCent2DollarShort(String s) {
        String ss = convertCent2Dollar(s);
        ss = "" + Double.parseDouble(ss);
        if(ss.endsWith(".0")) {
            return ss.substring(0, ss.length() - 2);
        }
        if(ss.endsWith(".00")) {
            return ss.substring(0, ss.length() - 3);
        } else {
            return ss;
        }
    }


    /**
    * 计算百分比类型的各种费用值  （订单金额 * 真实费率  结果四舍五入并保留0位小数 ）
    *
    * @author terrfly
    * @site https://www.jeequan.com
    * @date 2021/8/20 14:53
    *  @param amount 订单金额  （保持与数据库的格式一致 ，单位：分）
    * @param rate 费率   （保持与数据库的格式一致 ，真实费率值，如费率为0.55%，则传入 0.0055）
    */
    public static Long calPercentageFee(Long amount, BigDecimal rate){
        return calPercentageFee(amount, rate, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算百分比类型的各种费用值  （订单金额 * 真实费率  结果四舍五入并保留0位小数 ）
     *
     * @author terrfly
     * @site https://www.jeequan.com
     * @date 2021/8/20 14:53
     * @param amount 订单金额  （保持与数据库的格式一致 ，单位：分）
     * @param rate 费率   （保持与数据库的格式一致 ，真实费率值，如费率为0.55%，则传入 0.0055）
     * @param mode 模式 参考：BigDecimal.ROUND_HALF_UP(四舍五入)   BigDecimal.ROUND_FLOOR（向下取整）
     */
    public static Long calPercentageFee(Long amount, BigDecimal rate, int mode){
        //费率乘以订单金额   结果四舍五入并保留0位小数
        return new BigDecimal(amount).multiply(rate).setScale(0, mode).longValue();
    }

}
