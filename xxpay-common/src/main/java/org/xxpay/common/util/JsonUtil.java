package org.xxpay.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created by admin on 2016/4/13.
 */
public class JsonUtil {

    static {
        System.setProperty("fastjson.compatibleWithJavaBean", "true");
    }

    public static String object2Json(Object object) {
        if (object == null) {
            return null;
        }
        return JSONObject.toJSONString(object);
    }

    public static <T> T getObjectFromJson(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    public static <T> List<T> getObjectListFromJson(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        return JSON.parseArray(json, clazz);
    }

    public static JSONObject getJSONObjectFromJson(String json) {
        if (json == null) {
            return null;
        }
        return JSONObject.parseObject(json);
    }

    public static JSONObject getJSONObjectFromObj(Object object) {
        if (object == null) {
            return null;
        }
        return (JSONObject) JSONObject.toJSON(object);
    }

}
