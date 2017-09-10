package org.xxpay.common.util;

import org.xxpay.common.constant.Constant;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生成随机通讯码工具类
 * Created by admin on 2016/5/4.
 */
public class RandomStrUtils {

    private static Object lock = new Object();

    private static RandomStrUtils instance;

    private Map<String, Long> randomStrMap = new ConcurrentHashMap<String, Long>();

    private static final String[] BASE_STRING = new String[]{
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    };

    private static final int RANDOM_STRING_LENGTH = 6;

    private RandomStrUtils() {
    }

    public static RandomStrUtils getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new RandomStrUtils();
            }
        }
        return instance;
    }

    public String getRandomString() {
        Long nowTime = System.currentTimeMillis();
        String randomStr = null;

        synchronized (lock) {
            // 生成随机字符串
            randomStr = createRandomString(RANDOM_STRING_LENGTH, nowTime);

            // 删除一分钟前的随机字符串
            Iterator<Map.Entry<String, Long>> it = randomStrMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> entry = it.next();
                Long value = entry.getValue();
                if (nowTime - value > Constant.RPC_SEQ_NO_NOT_REPEAT_INTERVAL) {
                    it.remove();
                }
            }
        }

        return randomStr;
    }

    private String createRandomString(int len, Long nowTime) {
        Random random = new Random();
        int length = BASE_STRING.length;
        String randomString = "";
        for (int i = 0; i < length; i++) {
            randomString += BASE_STRING[random.nextInt(length)];
        }
        random = new Random(System.currentTimeMillis());
        String resultStr = "";
        for (int i = 0; i < len; i++) {
            resultStr += randomString.charAt(random.nextInt(randomString.length() - 1));
        }

        // 判断一分钟内是否重复
        Long randomStrCreateTime = randomStrMap.get(resultStr);
        if (randomStrCreateTime != null &&
                nowTime - randomStrCreateTime < Constant.RPC_SEQ_NO_NOT_REPEAT_INTERVAL) {
            resultStr = createRandomString(len, nowTime);
        }
        randomStrMap.put(resultStr, nowTime);
        return resultStr;
    }

}
