package com.jeequan.jeepay.core.beans;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * RequestKitBean#reqParam2JSON 回归测试。
 * <p>
 * 该方法是运营端/商户端/开放接口所有 getReqParamJSON() 取值的入口，
 * Servlet 容器对同名多值参数返回 String[]，转换规则必须保证：
 * 重复标量仍取末值；列表保持单个逗号分隔字符串的已有契约，
 * 空值数组取空串，不沿用上一个参数的残留值。
 */
public class RequestKitBeanParamJSONTest {

    /** 仅实现本用例需要的方法，其余返回类型默认值 **/
    private HttpServletRequest stubRequest(Map<String, String[]> paramMap) {

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParameterMap":
                            return paramMap;
                        case "getContentType":
                            return null; // 非 JSON 请求体, 走 parameterMap 分支
                        default:
                            Class<?> rt = method.getReturnType();
                            if (rt == boolean.class) return false;
                            if (rt == int.class) return 0;
                            if (rt == long.class) return 0L;
                            return null;
                    }
                });
    }

    private JSONObject toParamJSON(Map<String, String[]> paramMap) {

        RequestKitBean kitBean = new RequestKitBean();
        kitBean.request = stubRequest(paramMap);
        return kitBean.reqParam2JSON();
    }

    @Test
    @DisplayName("重复标量保留末值，分页和金额转换不受逗号拼接影响")
    public void repeatedScalarsKeepLastValueAndRemainConvertible() {
        Map<String, String[]> paramMap = new LinkedHashMap<>();
        paramMap.put("pageNumber", new String[]{"1", "2"});
        paramMap.put("pageSize", new String[]{"10", "20"});
        paramMap.put("state", new String[]{"0", "1"});
        paramMap.put("amount", new String[]{"100", "200"});
        paramMap.put("ps[wayCode]", new String[]{"WX_LITE", "WX_BAR"});

        JSONObject json = toParamJSON(paramMap);
        PaginationCtrl ctrl = new PaginationCtrl(json);
        assertEquals(2, ctrl.pageIndex());
        assertEquals(20, ctrl.pageSize());
        assertEquals(1, json.getInteger("state"));
        assertEquals(200L, json.getLong("amount"));
        assertEquals("WX_BAR", json.getJSONObject("ps").getString("wayCode"));
    }

    @Test
    @DisplayName("列表保持单个逗号分隔字符串契约，不擅自改变重复参数语义")
    public void commaSeparatedListKeepsExistingContract() {
        Map<String, String[]> paramMap = new LinkedHashMap<>();
        paramMap.put("wayCodeStrs", new String[]{"WX_LITE,WX_BAR,WX_JSAPI"});
        JSONObject json = toParamJSON(paramMap);
        assertArrayEquals(new String[]{"WX_LITE", "WX_BAR", "WX_JSAPI"},
                json.getString("wayCodeStrs").split(","));

        paramMap.put("wayCodeStrs", new String[]{"WX_LITE", "WX_BAR"});
        assertEquals("WX_BAR", toParamJSON(paramMap).getString("wayCodeStrs"));
    }

    // 调用真实分页转换逻辑，而不是只断言中间 JSON 字符串。
    private static class PaginationCtrl extends AbstractCtrl {
        private final JSONObject params;
        PaginationCtrl(JSONObject params) { this.params = params; }
        @Override
        protected JSONObject getReqParamJSON() { return params; }
        int pageIndex() { return getPageIndex(); }
        int pageSize() { return getPageSize(); }
    }

    @Test
    @DisplayName("空值数组不应越界，也不应残留上一个参数的取值")
    public void emptyValueArrayShouldNotLeakPreviousValue() {

        Map<String, String[]> paramMap = new LinkedHashMap<>();
        // 单个空值参数：历史实现会对循环外声明的 value 做 substring(0, -1) 而抛异常
        paramMap.put("wayCodeStrs", new String[0]);
        paramMap.put("ifCode", new String[]{"WX_PAY"});
        paramMap.put("emptyAfterValue", new String[0]);
        paramMap.put("missingValue", null);

        JSONObject json = assertDoesNotThrow(() -> toParamJSON(paramMap));

        assertEquals("", json.getString("wayCodeStrs"));
        assertEquals("WX_PAY", json.getString("ifCode"));
        assertEquals("", json.getString("emptyAfterValue"));
        assertEquals("", json.getString("missingValue"));
    }

    @Test
    @DisplayName("单值参数与嵌套参数 [x] 的既有转换结果保持不变")
    public void keepExistingSingleValueBehavior() {

        Map<String, String[]> paramMap = new LinkedHashMap<>();
        paramMap.put("pageNumber", new String[]{"2"});
        paramMap.put("pageSize", new String[]{"20"});
        paramMap.put("ps[wayCode]", new String[]{"WX_LITE"});

        JSONObject json = toParamJSON(paramMap);

        assertEquals("2", json.getString("pageNumber"));
        assertEquals("20", json.getString("pageSize"));
        assertEquals("WX_LITE", json.getJSONObject("ps").getString("wayCode"));
    }
}
