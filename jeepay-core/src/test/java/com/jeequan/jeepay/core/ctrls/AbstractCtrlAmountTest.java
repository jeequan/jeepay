package com.jeequan.jeepay.core.ctrls;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 元转分的舍入行为。
 *
 * AbstractCtrl 的三个入参金额换算方法此前用 {@code multiply(100).longValue()}，
 * 而 {@link java.math.BigDecimal#longValue()} 是向零截断，会把不足 1 分的部分直接丢掉，
 * 例如入参 1.005 元换算出 100 分而不是 101 分；负数方向还正好相反。
 *
 * 本仓库取整到分已有的约定是四舍五入（{@code AmountUtil.calPercentageFee(amount, rate,
 * BigDecimal.ROUND_HALF_UP)} 内部就是 {@code setScale(0, mode)}），这三处应保持一致。
 */
public class AbstractCtrlAmountTest {

    /** 只暴露待测的金额换算方法，请求参数用固定 JSON 顶替，避免依赖 Servlet 上下文。 **/
    static class TestCtrl extends AbstractCtrl {
        private final JSONObject params = new JSONObject();

        TestCtrl(String key, String value) {
            params.put(key, value);
        }

        JSONObject params() {
            return params;
        }

        @Override
        protected JSONObject getReqParamJSON() {
            return params;
        }

        Long callGetRequiredAmountL(String name) {
            return getRequiredAmountL(name);
        }

        Long callGetAmountL(String name) {
            return getAmountL(name);
        }

        void callHandleParamAmount(String name) {
            handleParamAmount(name);
        }
    }

    @Test
    @DisplayName("必填金额按四舍五入换算成分，而不是向零截断")
    public void requiredAmountShouldRoundHalfUp() {

        Assertions.assertEquals(101L, new TestCtrl("amount", "1.005").callGetRequiredAmountL("amount"));
        Assertions.assertEquals(10L, new TestCtrl("amount", "0.09999").callGetRequiredAmountL("amount"));
        Assertions.assertEquals(-101L, new TestCtrl("amount", "-1.005").callGetRequiredAmountL("amount"));

        // 两位小数的常规入参行为不变
        Assertions.assertEquals(1005L, new TestCtrl("amount", "10.05").callGetRequiredAmountL("amount"));
    }

    @Test
    @DisplayName("非必填金额同样按四舍五入换算")
    public void optionalAmountShouldRoundHalfUp() {

        Assertions.assertEquals(101L, new TestCtrl("amount", "1.005").callGetAmountL("amount"));
        Assertions.assertNull(new TestCtrl("other", "x").callGetAmountL("amount"), "缺省参数仍返回 null");
    }

    @Test
    @DisplayName("批量换算参数中的金额时也按四舍五入")
    public void handleParamAmountShouldRoundHalfUp() {

        TestCtrl ctrl = new TestCtrl("amount", "1.005");
        ctrl.callHandleParamAmount("amount");
        Assertions.assertEquals(101L, ctrl.params().getLongValue("amount"));
    }
}
