package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.model.params.starpos.StarposNormalMchParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StarposConfigTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("environmentMappings")
    void shouldMapOnlySupportedEnvironment(String environment, String expectedBaseUrl) {
        StarposNormalMchParams params = validParams();
        params.setEnvironment(environment);

        assertEquals(expectedBaseUrl, StarposConfig.resolveBaseUrl(params));
    }

    @ParameterizedTest(name = "缺失 {0}")
    @MethodSource("missingRequiredFields")
    void shouldRejectMissingRequiredField(String field, Consumer<StarposNormalMchParams> clearField) {
        StarposNormalMchParams params = validParams();
        clearField.accept(params);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> StarposConfig.resolveBaseUrl(params)
        );
        assertEquals(field + " 不能为空", exception.getMessage());
    }

    @Test
    void shouldRejectMissingParams() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> StarposConfig.resolveBaseUrl(null)
        );
        assertEquals("星驿付商户参数不能为空", exception.getMessage());
    }

    @Test
    void shouldRejectUnknownEnvironment() {
        StarposNormalMchParams params = validParams();
        params.setEnvironment("staging");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> StarposConfig.resolveBaseUrl(params)
        );
        assertEquals("不支持的星驿付环境: staging", exception.getMessage());
    }

    @Test
    void shouldProvideChannelConstantsAndDefaultVersion() {
        assertEquals("starpos", CS.IF_CODE.STARPOS);
        assertEquals("STARPOS_QR", CS.PAY_WAY_CODE.STARPOS_QR);
        assertEquals("1.0.0", new StarposNormalMchParams().getVersion());
    }

    @Test
    void shouldDesensitizePublicKey() {
        StarposNormalMchParams params = validParams();

        JSONObject desensitized = JSON.parseObject(params.deSenData());

        assertEquals("LOCA******TURE", desensitized.getString("publicKey"));
        assertFalse(desensitized.getString("publicKey").contains("PUBLIC_KEY"));
    }

    private static Stream<Arguments> environmentMappings() {
        return Stream.of(
                Arguments.of("test", "https://xyf-server-test.postar.cn"),
                Arguments.of("uat", "https://xyzscxm.postar.cn"),
                Arguments.of("prod", "https://yyfsvxm.postar.cn")
        );
    }

    private static Stream<Arguments> missingRequiredFields() {
        return Stream.of(
                Arguments.of("environment", (Consumer<StarposNormalMchParams>) params -> params.setEnvironment(" ")),
                Arguments.of("agetId", (Consumer<StarposNormalMchParams>) params -> params.setAgetId(null)),
                Arguments.of("custId", (Consumer<StarposNormalMchParams>) params -> params.setCustId("")),
                Arguments.of("publicKey", (Consumer<StarposNormalMchParams>) params -> params.setPublicKey(null)),
                Arguments.of("version", (Consumer<StarposNormalMchParams>) params -> params.setVersion(" "))
        );
    }

    private static StarposNormalMchParams validParams() {
        StarposNormalMchParams params = new StarposNormalMchParams();
        params.setEnvironment("test");
        params.setAgetId("LOCAL_AGET_ID");
        params.setCustId("LOCAL_CUST_ID");
        params.setPublicKey("LOCAL_PUBLIC_KEY_FIXTURE");
        return params;
    }
}
