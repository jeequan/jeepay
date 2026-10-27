package com.jeequan.jeepay.core.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AmountUtil#convertDollar2Cent 回归测试。
 * <p>
 * 该方法的唯一调用方是支付宝退款回调
 * （AlipayRefundService 中 Long.parseLong(AmountUtil.convertDollar2Cent(response.getRefundAmount()))），
 * 用于把渠道返回的“元”金额换算成“分”后与库中金额比对。
 * 原实现经 double 中转并使用随 JVM 默认 Locale 变化的 DecimalFormat，
 * 因此存在两类问题：非 “.” 小数点分隔符的 Locale 下直接抛异常；超过两位小数时按 double 精度舍入。
 */
public class AmountUtilDollar2CentTest {

    private final Locale originalLocale = Locale.getDefault();

    @AfterEach
    public void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    @DisplayName("换算结果不应依赖 JVM 默认 Locale 的小数点分隔符")
    public void shouldNotDependOnDefaultLocale() {

        // 德语/法语等 Locale 的小数点分隔符是逗号，不是点
        Locale.setDefault(Locale.GERMANY);

        assertDoesNotThrow(() -> AmountUtil.convertDollar2Cent("123.45"));
        assertEquals("12345", AmountUtil.convertDollar2Cent("123.45"));
    }

    @Test
    @DisplayName("两位以上小数应按十进制四舍五入，而不是经 double 丢精度")
    public void shouldRoundOnDecimalNotBinary() {

        // 1.115 的 double 取值小于 1.115，经 double + DecimalFormat(HALF_EVEN) 会得到 111
        assertEquals("112", AmountUtil.convertDollar2Cent("1.115"));
        assertEquals("268", AmountUtil.convertDollar2Cent("2.675"));

        // 十进制精确计算：0.005 元 = 0.5 分 -> 四舍五入为 1 分
        assertEquals("1", AmountUtil.convertDollar2Cent("0.005"));
    }

    @Test
    @DisplayName("既有换算契约保持不变")
    public void shouldKeepExistingContract() {

        assertEquals("1000", AmountUtil.convertDollar2Cent("10.00"));
        assertEquals("100", AmountUtil.convertDollar2Cent("1"));
        assertEquals("5", AmountUtil.convertDollar2Cent("0.05"));
        assertEquals("0", AmountUtil.convertDollar2Cent("0.00"));
        assertEquals("-150", AmountUtil.convertDollar2Cent("-1.5"));

        // 大额退款单也必须保持十进制精确（double 中转会在 15 位以上丢精度）。
        // 期望值写成字面量：用 movePointRight(2) 反推等于拿实现验实现，换不动的断言。
        assertEquals("1234567890123456", AmountUtil.convertDollar2Cent("12345678901234.56"));
        assertEquals("-1234567890123456", AmountUtil.convertDollar2Cent("-12345678901234.56"));
        assertTrue(AmountUtil.convertDollar2Cent("12345678901234.56").chars()
                .allMatch(ch -> Character.isDigit(ch) || ch == '-'));
    }
}
