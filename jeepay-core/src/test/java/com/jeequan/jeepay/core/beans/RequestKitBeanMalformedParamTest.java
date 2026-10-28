package com.jeequan.jeepay.core.beans;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link RequestKitBean#reqParam2JSON()} 对畸形参数名的处理。
 * <p>
 * 仅有 "[abc]" 形态的参数名才表示 JSON 对象属性；容器允许任意参数名，
 * 因此缺少 "]"（或 "]" 出现在 "[" 之前）的名称必须按普通键处理，不能中断整个参数解析。
 */
public class RequestKitBeanMalformedParamTest {

    /** 仅实现本用例用到的方法，其余返回类型默认值 **/
    private HttpServletRequest stubRequest(Map<String, String[]> paramMap) {

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParameterMap":
                            return paramMap;
                        case "getContentType":
                            return null; // 非 JSON 请求体，走 parameterMap 分支
                        default:
                            Class<?> rt = method.getReturnType();
                            if (rt == boolean.class) return false;
                            if (rt == int.class) return 0;
                            if (rt == long.class) return 0L;
                            return null;
                    }
                });
    }

    private JSONObject parse(Map<String, String[]> paramMap) {
        RequestKitBean bean = new RequestKitBean();
        bean.request = stubRequest(paramMap);
        return bean.reqParam2JSON();
    }

    @Test
    @DisplayName("参数名含未闭合的 [ 时应按普通键保留，而不是中断解析")
    public void unclosedBracketIsKeptAsPlainKey() {

        Map<String, String[]> params = new LinkedHashMap<>();
        params.put("ps[abc", new String[]{"1"});

        JSONObject result = assertDoesNotThrow(() -> parse(params));
        assertEquals("1", result.getString("ps[abc"));
    }

    @Test
    @DisplayName("] 出现在 [ 之前时应按普通键保留")
    public void closingBracketBeforeOpeningIsKeptAsPlainKey() {

        Map<String, String[]> params = new LinkedHashMap<>();
        params.put("a]b[", new String[]{"2"});

        JSONObject result = assertDoesNotThrow(() -> parse(params));
        assertEquals("2", result.getString("a]b["));
    }
}
