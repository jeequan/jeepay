package com.jeequan.jeepay.pay.compat.epay;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

public final class EpaySigner {

    private EpaySigner() {
    }

    public static String signMd5(Map<String, ?> fields, String merchantKey) {
        byte[] digest = md5Bytes(EpayCanonicalizer.canonical(fields) + merchantKey);
        return HexFormat.of().formatHex(digest);
    }

    public static boolean verifyMd5(Map<String, ?> fields, String merchantKey, String signature) {
        if (signature == null) {
            return false;
        }
        byte[] expected = signMd5(fields, merchantKey).getBytes(StandardCharsets.UTF_8);
        byte[] actual = signature.toLowerCase().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    public static String signRsa(Map<String, ?> fields, String privateKeyPem) {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey(privateKeyPem));
            signer.update(EpayCanonicalizer.canonical(fields).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (Exception e) {
            throw new IllegalArgumentException("RSA签名失败", e);
        }
    }

    public static boolean verifyRsa(Map<String, ?> fields, String publicKeyPem, String signature) {
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey(publicKeyPem));
            verifier.update(EpayCanonicalizer.canonical(fields).getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] md5Bytes(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return digest.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("MD5签名失败", e);
        }
    }

    private static PrivateKey privateKey(String pem) throws Exception {
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der(pem)));
    }

    private static PublicKey publicKey(String pem) throws Exception {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der(pem)));
    }

    private static byte[] der(String pem) {
        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
