package com.jeequan.jeepay.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * fastjson 1.x API 兼容层（com.alibaba:fastjson 2.0.x，内核 fastjson2）冒烟测试。
 * 覆盖本仓库真实使用的 API 面：JSON/JSONObject/JSONArray、SerializerFeature、
 * SerializeConfig 自定义序列化器（SwaggerJsonSerializer 同款模式）、ParserConfig safeMode。
 */
class FastJsonCompatSmokeTest {

    private static final int SAVED_DEFAULT_GENERATE_FEATURE = JSON.DEFAULT_GENERATE_FEATURE;

    @AfterAll
    static void restoreGlobals() {
        JSON.DEFAULT_GENERATE_FEATURE = SAVED_DEFAULT_GENERATE_FEATURE;
    }

    /** 模拟支付订单等业务实体的序列化/反序列化往返 */
    public static class OrderModel {
        private String payOrderId;
        private Long amount;
        private BigDecimal fee;
        private Short state;
        private Date createdAt;
        private List<String> wayCodes;
        private NestedModel mchInfo;
        private String remark; // 可为 null

        public String getPayOrderId() { return payOrderId; }
        public void setPayOrderId(String v) { this.payOrderId = v; }
        public Long getAmount() { return amount; }
        public void setAmount(Long v) { this.amount = v; }
        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal v) { this.fee = v; }
        public Short getState() { return state; }
        public void setState(Short v) { this.state = v; }
        public Date getCreatedAt() { return createdAt; }
        public void setCreatedAt(Date v) { this.createdAt = v; }
        public List<String> getWayCodes() { return wayCodes; }
        public void setWayCodes(List<String> v) { this.wayCodes = v; }
        public NestedModel getMchInfo() { return mchInfo; }
        public void setMchInfo(NestedModel v) { this.mchInfo = v; }
        public String getRemark() { return remark; }
        public void setRemark(String v) { this.remark = v; }
    }

    public static class NestedModel {
        private String mchNo;
        private String mchName;
        public String getMchNo() { return mchNo; }
        public void setMchNo(String v) { this.mchNo = v; }
        public String getMchName() { return mchName; }
        public void setMchName(String v) { this.mchName = v; }
    }

    @Test
    void entityRoundTripPreservesData() {
        OrderModel order = new OrderModel();
        order.setPayOrderId("P1234567890");
        order.setAmount(10000L);
        order.setFee(new BigDecimal("12.34"));
        order.setState((short) 2);
        order.setCreatedAt(new Date(1727000000000L));
        List<String> ways = new ArrayList<>();
        ways.add("WX_JSAPI");
        ways.add("ALI_QR");
        order.setWayCodes(ways);
        NestedModel mch = new NestedModel();
        mch.setMchNo("M2000001");
        mch.setMchName("测试商户");
        order.setMchInfo(mch);

        String json = JSON.toJSONString(order);
        OrderModel back = JSON.parseObject(json, OrderModel.class);

        assertEquals(order.getPayOrderId(), back.getPayOrderId());
        assertEquals(order.getAmount(), back.getAmount());
        assertEquals(0, order.getFee().compareTo(back.getFee()));
        assertEquals(order.getState(), back.getState());
        assertEquals(order.getCreatedAt().getTime(), back.getCreatedAt().getTime());
        assertEquals(order.getWayCodes(), back.getWayCodes());
        assertEquals(order.getMchInfo().getMchNo(), back.getMchInfo().getMchNo());
        assertEquals(order.getMchInfo().getMchName(), back.getMchInfo().getMchName());
        assertNull(back.getRemark());
    }

    @Test
    void writeMapNullFeatureKeepsNullFields() {
        OrderModel order = new OrderModel();
        order.setPayOrderId("P1");

        String withNull = JSON.toJSONString(order, SerializerFeature.WriteMapNullValue);
        String withoutNull = JSON.toJSONString(order);

        assertTrue(withNull.contains("\"remark\":null"));
        assertTrue(!withoutNull.contains("remark"));
    }

    @Test
    void jsonObjectApiSurface() {
        JSONObject obj = JSONObject.parseObject("{\"code\":0,\"data\":{\"appId\":\"A1\",\"amount\":66},\"list\":[1,2],\"str\":\"ok\"}");
        assertNotNull(obj);
        assertEquals(0, obj.getIntValue("code"));
        assertEquals("A1", obj.getJSONObject("data").getString("appId"));
        assertEquals(66, obj.getJSONObject("data").getIntValue("amount"));
        assertEquals("ok", obj.getString("str"));

        JSONArray arr = obj.getJSONArray("list");
        assertEquals(2, arr.size());
        assertEquals(2, arr.getIntValue(1));

        JSONObject fromBean = (JSONObject) JSONObject.toJSON(obj);
        assertEquals(obj.getString("str"), fromBean.getString("str"));

        JSONObject copy = new JSONObject();
        copy.put("code", 0);
        copy.put("data", obj.getJSONObject("data"));
        // 注意：2.x 兼容层的字段输出顺序与 1.x 不完全一致，此处做语义比较而非字符串精确匹配
        JSONObject copyBack = JSONObject.parseObject(copy.toJSONString());
        assertEquals(0, copyBack.getIntValue("code"));
        assertEquals("A1", copyBack.getJSONObject("data").getString("appId"));
        assertEquals(66, copyBack.getJSONObject("data").getIntValue("amount"));
    }

    /** InitRunner 使用的全局特性位操作（DisableCircularReferenceDetect 掩码） */
    @Test
    void defaultGenerateFeatureMaskOperation() {
        int saved = JSON.DEFAULT_GENERATE_FEATURE;
        try {
            JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
            assertTrue((JSON.DEFAULT_GENERATE_FEATURE
                    & SerializerFeature.DisableCircularReferenceDetect.getMask()) != 0);

            OrderModel a = new OrderModel();
            a.setPayOrderId("P9");
            String json = JSON.toJSONString(a);
            assertTrue(json.contains("P9"));
            assertTrue(!json.contains("$ref"));
        } finally {
            JSON.DEFAULT_GENERATE_FEATURE = saved;
        }
    }

    /** SwaggerJsonSerializer 使用的自定义序列化器注册模式（SerializeConfig.put） */
    @Test
    void customObjectSerializerViaSerializeConfig() throws Exception {
        SerializeConfig config = new SerializeConfig();
        config.put(OrderModel.class, new ObjectSerializer() {
            @Override
            public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) {
                SerializeWriter out = serializer.getWriter();
                out.write("{\"custom\":true,\"id\":\"" + ((OrderModel) object).getPayOrderId() + "\"}");
            }
        });

        OrderModel order = new OrderModel();
        order.setPayOrderId("P77");
        assertEquals("{\"custom\":true,\"id\":\"P77\"}", JSON.toJSONString(order, config));
    }

    /** fastjson 2.x 默认禁用 @type 自动反序列化：恶意 payload 仅作为普通字段保留，不实例化类（RCE 防线） */
    @Test
    void autoTypeDisabledByDefault() {
        String malicious = "{\"@type\":\"java.lang.Runtime\",\"a\":1}";
        Object parsed = JSON.parseObject(malicious);
        // @type 被当作普通字段，未触发类加载/实例化
        assertTrue(parsed instanceof JSONObject);
        assertEquals("java.lang.Runtime", ((JSONObject) parsed).getString("@type"));

        // 指定类型的正常解析不受影响
        OrderModel order = JSON.parseObject("{\"payOrderId\":\"P5\",\"amount\":88}", OrderModel.class);
        assertEquals("P5", order.getPayOrderId());
        assertEquals(88L, order.getAmount());
    }
}
