package com.jeequan.jeepay.pay.rqrs.payorder;

import com.jeequan.jeepay.core.constants.CS;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarposQrOrderRQTest {

    private static final String STARPOS_QR_RQ_CLASS =
            "com.jeequan.jeepay.pay.rqrs.payorder.payway.StarposQrOrderRQ";

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldBuildStarposQrOrderRequestWithPayUrl() {
        UnifiedOrderRQ bizRQ = buildBizRQ("{\"payDataType\":\"payUrl\"}");

        assertEquals(STARPOS_QR_RQ_CLASS, bizRQ.getClass().getName());
        CommonPayDataRQ payDataRQ = assertInstanceOf(CommonPayDataRQ.class, bizRQ);
        assertEquals(CS.PAY_WAY_CODE.STARPOS_QR, payDataRQ.getWayCode());
        assertEquals("payUrl", payDataRQ.getPayDataType());
    }

    @Test
    void shouldRejectMissingPayDataType() {
        UnifiedOrderRQ bizRQ = buildBizRQ("{}");

        assertHasViolation(bizRQ, "payDataType", "payDataType不能为空");
    }

    @Test
    void shouldRejectMissingPayDataTypeBeforeBizRequestConversion() {
        UnifiedOrderRQ rq = buildUnifiedOrderRQ("{}");

        assertHasUnifiedOrderViolation(rq);
    }

    @Test
    void shouldRejectUnsupportedPayDataType() {
        UnifiedOrderRQ bizRQ = buildBizRQ("{\"payDataType\":\"payurl\"}");

        assertHasViolation(bizRQ, "payDataType", "payDataType仅支持payUrl");
    }

    @Test
    void shouldRejectUnsupportedPayDataTypeBeforeBizRequestConversion() {
        UnifiedOrderRQ rq = buildUnifiedOrderRQ("{\"payDataType\":\"payurl\"}");

        assertHasUnifiedOrderViolation(rq);
    }

    private UnifiedOrderRQ buildBizRQ(String channelExtra) {
        return buildUnifiedOrderRQ(channelExtra).buildBizRQ();
    }

    private UnifiedOrderRQ buildUnifiedOrderRQ(String channelExtra) {
        UnifiedOrderRQ rq = new UnifiedOrderRQ();
        rq.setWayCode(CS.PAY_WAY_CODE.STARPOS_QR);
        rq.setChannelExtra(channelExtra);
        return rq;
    }

    private void assertHasViolation(UnifiedOrderRQ bizRQ, String property, String message) {
        Set<ConstraintViolation<UnifiedOrderRQ>> violations = validator.validate(bizRQ);

        assertTrue(
                violations.stream().anyMatch(violation ->
                        property.equals(violation.getPropertyPath().toString())
                                && message.equals(violation.getMessage())),
                () -> "未找到预期校验错误，实际为: " + violations
        );
    }

    private void assertHasUnifiedOrderViolation(UnifiedOrderRQ rq) {
        Set<ConstraintViolation<UnifiedOrderRQ>> violations = validator.validate(rq);

        assertTrue(
                violations.stream().anyMatch(violation ->
                        "starposQrPayDataTypeValid".equals(violation.getPropertyPath().toString())),
                () -> "统一下单基类校验未拦截非法 payDataType，实际为: " + violations
        );
    }
}
