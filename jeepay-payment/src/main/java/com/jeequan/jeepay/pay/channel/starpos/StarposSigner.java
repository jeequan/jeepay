package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 星驿付签名边界。
 *
 * <p>canonical JSON 和 SHA-256 规则由当前项目契约锁定。RSA 部分集中在
 * {@link RsaCodec} 内，正式星驿付资料接入时只需替换该边界及测试 fixture，
 * 不影响调用方的签名内容构造。</p>
 */
public final class StarposSigner {

    private static final String SIGN_FIELD = "sign";
    private static final RsaCodec RSA_CODEC = new Sha256WithRsaCodec();

    private StarposSigner() {
    }

    /**
     * 生成签名用 canonical JSON。
     *
     * <ul>
     *     <li>顶层 key 按 ASCII 大小写敏感顺序排序；</li>
     *     <li>顶层 null 值和 sign 字段排除；</li>
     *     <li>空字符串保留；</li>
     *     <li>嵌套对象保留调用方提供的输入顺序。</li>
     * </ul>
     */
    public static String canonicalJson(Map<String, Object> fields) {
        Objects.requireNonNull(fields, "fields");

        Map<String, Object> sortedFields = new TreeMap<>();
        fields.forEach((key, value) -> {
            Objects.requireNonNull(key, "field key");
            if (value != null && !SIGN_FIELD.equals(key)) {
                sortedFields.put(key, value);
            }
        });
        return JSON.toJSONString(sortedFields);
    }

    /**
     * 返回 UTF-8 内容的 SHA-256 小写十六进制摘要。
     */
    public static String sha256Hex(String content) {
        Objects.requireNonNull(content, "content");
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    /**
     * 对 canonical JSON 执行 RSA 签名并返回 Base64。
     *
     * <p>当前实现采用标准 SHA256withRSA 适配器；传入材料为 PKCS#8 私钥。
     * 正式星驿付签名方向和协议样例由可替换 RSA 边界及 fixture 锁定。</p>
     */
    public static String signRequest(Map<String, Object> fields, String keyMaterial) {
        return RSA_CODEC.sign(canonicalJson(fields), keyMaterial);
    }

    /**
     * 使用通知中的 sign 字段验证 canonical JSON。
     *
     * <p>传入材料为 X.509 公钥；格式错误或签名不匹配均返回 false。</p>
     */
    public static boolean verifyNotification(Map<String, Object> fields, String keyMaterial) {
        Objects.requireNonNull(fields, "fields");
        Object signature = fields.get(SIGN_FIELD);
        if (!(signature instanceof String) || ((String) signature).isBlank()) {
            return false;
        }
        return RSA_CODEC.verify(canonicalJson(fields), (String) signature, keyMaterial);
    }

    private interface RsaCodec {

        String sign(String content, String keyMaterial);

        boolean verify(String content, String signature, String keyMaterial);
    }

    /**
     * RSA 算法适配边界。正式协议资料到位时替换此实现即可。
     */
    private static final class Sha256WithRsaCodec implements RsaCodec {

        @Override
        public String sign(String content, String keyMaterial) {
            try {
                Signature signer = Signature.getInstance("SHA256withRSA");
                signer.initSign(readPrivateKey(keyMaterial));
                signer.update(content.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(signer.sign());
            } catch (GeneralSecurityException | IllegalArgumentException e) {
                throw new IllegalArgumentException("RSA 私钥签名失败", e);
            }
        }

        @Override
        public boolean verify(String content, String signature, String keyMaterial) {
            try {
                Signature verifier = Signature.getInstance("SHA256withRSA");
                verifier.initVerify(readPublicKey(keyMaterial));
                verifier.update(content.getBytes(StandardCharsets.UTF_8));
                return verifier.verify(Base64.getDecoder().decode(signature));
            } catch (GeneralSecurityException | IllegalArgumentException e) {
                return false;
            }
        }

        private static PrivateKey readPrivateKey(String keyMaterial) throws GeneralSecurityException {
            byte[] encoded = decodePemOrBase64(keyMaterial, "PRIVATE KEY");
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
        }

        private static PublicKey readPublicKey(String keyMaterial) throws GeneralSecurityException {
            byte[] encoded = decodePemOrBase64(keyMaterial, "PUBLIC KEY");
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        }

        private static byte[] decodePemOrBase64(String keyMaterial, String type) {
            Objects.requireNonNull(keyMaterial, "keyMaterial");
            String normalized = keyMaterial
                    .replace("-----BEGIN " + type + "-----", "")
                    .replace("-----END " + type + "-----", "")
                    .replaceAll("\\s+", "");
            return Base64.getDecoder().decode(normalized);
        }
    }
}
