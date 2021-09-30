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
package com.jeequan.jeepay.pay.channel.ysfpay.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;
import java.util.TreeMap;

/**
 * 银联接口签名工具类
 *
 * @author terrfly
 * @modify pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
public class YsfSignUtils {

    private static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";  //私钥类型
    private static final String KEYSTORE_PROVIDER_BC = "BC";  //提供商
    private static final String ALGORITHM_SHA256WITHRSA = "SHA256withRSA";  //签名算法 sha256

    private static final String CERTIFICATE_TYPE_X509 = "X.509"; //公钥证书类型

    private static final Logger logger = LoggerFactory.getLogger(YsfSignUtils.class);
    static {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            logger.error("addProvider Error", e);
        }
    }

    /** 签名
     * 注意事项： 签名需商户申请 5.1.0版本证书；
     * 文档： https://open.unionpay.com/tjweb/acproduct/list?apiSvcId=468&index=2
     * 1. 排序并拼接为[key=value]格式；
     * 2. 对原始签名串使用SHA-256算法做摘要
     * 3. 使用商户私钥做签名（使用 SHA-256）
     * 4. 进行Base64处理
     * **/
    public static String signBy256(JSONObject params, String privateKeyFilePath, String certPwd) {

        try {

            //0. 将请求参数 转换成key1=value1&key2=value2的形式
            String stringSign = convertSignStringIncludeEmpty(params);

            //1. 通过SHA256进行摘要并转16进制
            byte[] signDigest = sha256X16(stringSign, "UTF-8");

            //2. /获取私钥证书的key
            PrivateKey privateKey = getSignCertPrivateKey(privateKeyFilePath, certPwd);

            //3. 使用 SHA-256算法 进行签名
            Signature st = Signature.getInstance(ALGORITHM_SHA256WITHRSA, KEYSTORE_PROVIDER_BC);
            st.initSign(privateKey);
            st.update(signDigest);
            byte[] result = st.sign();

            //4. 做base64 处理
            byte[] byteSign = Base64.encodeBase64(result);
            return new String(byteSign);

        } catch (Exception e) {
            logger.error("银联签名失败", e);
            return null;
        }
    }


    /** 验签 **/
    public static boolean validate(JSONObject params, String ysfpayPublicKey){

        //签名串
        String signature = params.getString("signature");

        // 将请求参数信息转换成key1=value1&key2=value2的形式
        String stringData = convertSignStringIncludeEmpty(params);
        try {

            //1. 通过SHA256进行摘要并转16进制
            byte[] signDigest = sha256X16(stringData, "UTF-8");

            //构造公钥证书
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decodeBase64(ysfpayPublicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            Signature st = Signature.getInstance(ALGORITHM_SHA256WITHRSA);
            st.initVerify(pubKey); //公钥
            st.update(signDigest);
            return st.verify(Base64.decodeBase64(signature.getBytes("UTF-8")));

        } catch (Exception e) {
            logger.error("验签失败！", e);
        }
        return false;
    }

    /** 进件验签 **/
    public static boolean applyValidate(JSONObject params, String ysfpayPublicKey){

        //签名串
        String signature = params.getString("signature");

        // 将请求参数信息转换成key1=value1&key2=value2的形式
        String stringData = convertSignApplyNotifyString(params);
        try {

            //1. 通过SHA256进行摘要并转16进制
            byte[] signDigest = sha256X16(stringData, "UTF-8");

            //构造公钥证书
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decodeBase64(ysfpayPublicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            Signature st = Signature.getInstance(ALGORITHM_SHA256WITHRSA);
            st.initVerify(pubKey); //公钥
            st.update(signDigest);
            return st.verify(Base64.decodeBase64(signature.getBytes("UTF-8")));

        } catch (Exception e) {
            logger.error("验签失败！", e);
        }
        return false;
    }

    /**  进件回调  将JSON中的数据转换成key1=value1&key2=value2的形式，忽略null内容 和 signature字段* */
    private static String convertSignApplyNotifyString(JSONObject params) {
        TreeMap<String, Object> tree = new TreeMap<>();

        //1. 所有参数进行排序
        params.keySet().stream().forEach( key -> tree.put(key, params.get(key)));

        //2. 拼接为 key=value&形式
        StringBuffer stringBuffer = new StringBuffer();
        tree.keySet().stream().forEach( key -> {

            if (tree.get(key) == null) {
                return ;
            }
            if("signature".equals(key)){ //签名串， 不参与签名
                return ;
            }

            stringBuffer.append(key).append("=").append(tree.get(key).toString()).append("&");
        });

        //3. 去掉最后一个&
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }


    /** 将JSON中的数据转换成key1=value1&key2=value2的形式, 忽略空内容 和 signature字段 **/
    private static String convertSignString(JSONObject params) {
        TreeMap<String, Object> tree = new TreeMap<>();

        //1. 所有参数进行排序
        params.keySet().stream().forEach( key -> tree.put(key, params.get(key)));

        //2. 拼接为 key=value&形式
        StringBuffer stringBuffer = new StringBuffer();
        tree.keySet().stream().forEach( key -> {

            if (tree.get(key) == null) {
                return ;
            }
            if(StringUtils.isAnyEmpty(key, tree.get(key).toString())){ //空值， 不参与签名
                return ;
            }
            if("signature".equals(key)){ //签名串， 不参与签名
                return ;
            }

            stringBuffer.append(key).append("=").append(tree.get(key).toString()).append("&");
        });

        //3. 去掉最后一个&
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }


    /**  进件回调  将JSON中的数据转换成key1=value1&key2=value2的形式，忽略null内容【空串也参与签名】 和 signature字段* */
    private static String convertSignStringIncludeEmpty(JSONObject params) {
        TreeMap<String, Object> tree = new TreeMap<>();

        //1. 所有参数进行排序
        params.keySet().stream().forEach( key -> tree.put(key, params.get(key)));

        //2. 拼接为 key=value&形式
        StringBuffer stringBuffer = new StringBuffer();
        tree.keySet().stream().forEach( key -> {

            if (tree.get(key) == null) {
                return ;
            }
            if("signature".equals(key)){ //签名串， 不参与签名
                return ;
            }

            stringBuffer.append(key).append("=").append(tree.get(key).toString()).append("&");
        });

        //3. 去掉最后一个&
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }


    /** 通过SHA256进行摘要并转16进制  **/
    private static byte[] sha256X16(String data, String encoding) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.reset();
        md.update(data.getBytes(encoding));
        byte[] bytes = md.digest();

        StringBuilder sha256StrBuff = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (Integer.toHexString(0xFF & bytes[i]).length() == 1) {
                sha256StrBuff.append("0").append(Integer.toHexString(0xFF & bytes[i]));
            } else {
                sha256StrBuff.append(Integer.toHexString(0xFF & bytes[i]));
            }
        }
        return sha256StrBuff.toString().toLowerCase().getBytes(encoding);
    }

    /** 获取证书私钥 **/
    private static PrivateKey getSignCertPrivateKey(String pfxkeyfile, String keypwd) {
        FileInputStream fis = null;

        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE_PKCS12, KEYSTORE_PROVIDER_BC);
            fis = new FileInputStream(pfxkeyfile);
            char[] nPassword = null == keypwd || "".equals(keypwd.trim()) ? null: keypwd.toCharArray();
            if (null != keyStore) {
                keyStore.load(fis, nPassword);
            }
            Enumeration<String> aliasenum = keyStore.aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) {
                keyAlias = aliasenum.nextElement();
            }
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keypwd.toCharArray());
            return privateKey;
        } catch (Exception e) {
            logger.error("获取证书私钥失败！", e);
            return null;
        }finally {
            if(null!=fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
