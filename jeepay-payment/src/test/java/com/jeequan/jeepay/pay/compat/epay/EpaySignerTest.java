package com.jeequan.jeepay.pay.compat.epay;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EpaySignerTest {

    @Test
    void canonicalSortsKeysAndOmitsEmptySignFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("z", "last");
        fields.put("empty", "");
        fields.put("zero", 0);
        fields.put("sign_type", "MD5");
        fields.put("sign", "old");
        fields.put("a", "first");

        assertThat(EpayCanonicalizer.canonical(fields))
                .isEqualTo("a=first&z=last&zero=0");
    }

    @Test
    void md5UsesLowercaseHexOfCanonicalPlusKey() {
        Map<String, Object> fields = Map.of("pid", "1001", "money", "1.00");

        assertThat(EpaySigner.signMd5(fields, "secret"))
                .isEqualTo("db2367d18af244cca18fd9ca6ad7b6e1");
    }

    @Test
    void rsaRoundTripUsesSha256Pkcs1v15WithPemKeys() throws Exception {
        KeyPair pair = keyPair();
        Map<String, Object> fields = Map.of("money", "1.00", "pid", "1001");

        String signature = EpaySigner.signRsa(fields, pem(pair.getPrivate()));

        assertThat(EpaySigner.verifyRsa(fields, pem(pair.getPublic()), signature)).isTrue();
        assertThat(EpaySigner.verifyRsa(
                Map.of("money", "2.00", "pid", "1001"),
                pem(pair.getPublic()),
                signature)).isFalse();
    }

    @Test
    void rsaRoundTripUsesSha256Pkcs1v15WithBase64DerKeys() throws Exception {
        KeyPair pair = keyPair();
        Map<String, Object> fields = Map.of("money", "1.00", "pid", "1001");

        String signatureFromDerPrivateKey = EpaySigner.signRsa(fields, base64Der(pair.getPrivate()));
        String signatureFromPemPrivateKey = EpaySigner.signRsa(fields, pem(pair.getPrivate()));

        assertThat(EpaySigner.verifyRsa(fields, base64Der(pair.getPublic()), signatureFromDerPrivateKey)).isTrue();
        assertThat(EpaySigner.verifyRsa(fields, pem(pair.getPublic()), signatureFromDerPrivateKey)).isTrue();
        assertThat(EpaySigner.verifyRsa(fields, base64Der(pair.getPublic()), signatureFromPemPrivateKey)).isTrue();
        assertThat(EpaySigner.verifyRsa(
                Map.of("money", "2.00", "pid", "1001"),
                base64Der(pair.getPublic()),
                signatureFromDerPrivateKey)).isFalse();
    }

    private static KeyPair keyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    private static String pem(PrivateKey privateKey) {
        return "-----BEGIN PRIVATE KEY-----\n" + wrapBase64(privateKey.getEncoded()) + "\n-----END PRIVATE KEY-----";
    }

    private static String pem(PublicKey publicKey) {
        return "-----BEGIN PUBLIC KEY-----\n" + wrapBase64(publicKey.getEncoded()) + "\n-----END PUBLIC KEY-----";
    }

    private static String base64Der(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    private static String base64Der(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    private static String wrapBase64(byte[] der) {
        return Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(der);
    }
}
