package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import javax.crypto.Cipher;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarposSignerTest {

    private static final String SIGNING_VECTORS_RESOURCE = "contracts/starpos/signing-vectors.json";

    @ParameterizedTest(name = "{0}")
    @MethodSource("canonicalJsonVectors")
    void canonicalJson_matches_auditable_local_fixture(
            String name,
            Map<String, Object> fields,
            String expectedCanonicalJson
    ) {
        assertEquals(expectedCanonicalJson, StarposSigner.canonicalJson(fields), name);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("canonicalJsonVectors")
    void sha256Hex_matches_auditable_local_fixture(
            String name,
            Map<String, Object> fields,
            String expectedCanonicalJson,
            String expectedSha256Hex
    ) {
        assertEquals(expectedSha256Hex, StarposSigner.sha256Hex(expectedCanonicalJson), name);
        assertEquals(expectedSha256Hex, StarposSigner.sha256Hex(StarposSigner.canonicalJson(fields)), name);
    }

    @Test
    void canonicalJson_excludes_null_and_sign_but_keeps_empty_string() {
        JSONObject vector = vectorByName("null-is-excluded-empty-and-sign-are-handled");
        assertEquals(
                "{\"empty\":\"\",\"present\":\"value\"}",
                StarposSigner.canonicalJson(fieldsOf(vector))
        );
    }

    @Test
    void canonicalJson_keeps_nested_object_input_order() {
        JSONObject vector = vectorByName("nested-object-keeps-input-order");
        assertEquals(
                "{\"alpha\":\"first\",\"outer\":{\"z\":1,\"a\":2}}",
                StarposSigner.canonicalJson(fieldsOf(vector))
        );
    }

    @Test
    void rsa_fixture_documents_public_key_protocol() {
        JSONObject rsa = loadFixture().getJSONObject("rsa");
        assertEquals("protocol_locked", rsa.getString("status"));
        assertEquals("RSA/ECB/PKCS1Padding", rsa.getString("algorithm"));
        assertEquals(
                "SHA-256 lowercase hex over UTF-8 canonical parameter string",
                rsa.getString("hash")
        );
        assertEquals("X.509 PUBLIC KEY PEM/Base64", rsa.getString("keyFormat"));
        assertEquals("public-key encrypt", rsa.getJSONObject("direction").getString("request"));
        assertEquals("public-key decrypt", rsa.getJSONObject("direction").getString("notification"));
        assertEquals(
                "XyfClient.php: openssl_public_encrypt/openssl_public_decrypt; postar.cn/xyf/doc/7306848m0",
                rsa.getString("source")
        );
        assertEquals(
                "Generated local RSA public key; direction/implementation test, not a merchant key sample",
                rsa.getString("testMaterial")
        );
    }

    @Test
    void rsa_protocol_uses_public_key_encrypt_then_public_key_decrypt() throws Exception {
        KeyPair keyPair = generateKeyPair();
        Map<String, Object> request = requestFields();

        String publicKeyPem = pem("PUBLIC KEY", keyPair.getPublic().getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String signature = StarposSigner.signRequest(request, publicKeyPem);

        Cipher decryptor = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decryptor.init(Cipher.DECRYPT_MODE, keyPair.getPublic());
        String decryptedHash = new String(
                decryptor.doFinal(Base64.getDecoder().decode(signature)),
                StandardCharsets.UTF_8
        );

        assertEquals(
                StarposSigner.sha256Hex(StarposSigner.canonicalJson(request)),
                decryptedHash
        );

        Map<String, Object> notification = new LinkedHashMap<>(request);
        notification.put("sign", signature);
        assertTrue(StarposSigner.verifyNotification(notification, publicKeyBase64));
    }

    @Test
    void rsa_signature_tampering_fails() {
        KeyPair keyPair = generateKeyPair();
        Map<String, Object> notification = signedNotification(keyPair);
        byte[] tamperedCiphertext = Base64.getDecoder().decode((String) notification.get("sign"));
        tamperedCiphertext[tamperedCiphertext.length - 1] ^= 1;
        notification.put("sign", Base64.getEncoder().encodeToString(tamperedCiphertext));

        assertFalse(StarposSigner.verifyNotification(
                notification,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
        ));
    }

    @Test
    void rsa_field_tampering_fails() {
        KeyPair keyPair = generateKeyPair();
        Map<String, Object> notification = signedNotification(keyPair);
        notification.put("amount", "9.99");

        assertFalse(StarposSigner.verifyNotification(
                notification,
                pem("PUBLIC KEY", keyPair.getPublic().getEncoded())
        ));
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("failed to generate local RSA test key pair", e);
        }
    }

    private static Map<String, Object> signedNotification(KeyPair keyPair) {
        Map<String, Object> request = requestFields();
        String signature = StarposSigner.signRequest(
                request,
                pem("PUBLIC KEY", keyPair.getPublic().getEncoded())
        );
        Map<String, Object> notification = new LinkedHashMap<>(request);
        notification.put("sign", signature);
        return notification;
    }

    private static Map<String, Object> requestFields() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("amount", "1.00");
        request.put("merchantNo", "LOCAL_FIXTURE");
        return request;
    }

    private static Stream<Arguments> canonicalJsonVectors() {
        JSONArray vectors = loadFixture().getJSONArray("canonicalJsonVectors");
        return vectors.stream()
                .map(JSONObject.class::cast)
                .map(vector -> Arguments.of(
                        vector.getString("name"),
                        fieldsOf(vector),
                        vector.getString("canonicalJson"),
                        vector.getString("sha256Hex")
                ));
    }

    private static JSONObject vectorByName(String name) {
        return loadFixture().getJSONArray("canonicalJsonVectors").stream()
                .map(JSONObject.class::cast)
                .filter(vector -> Objects.equals(name, vector.getString("name")))
                .findFirst()
                .orElseThrow();
    }

    private static Map<String, Object> fieldsOf(JSONObject vector) {
        return new LinkedHashMap<>(vector.getJSONObject("fields"));
    }

    private static JSONObject loadFixture() {
        try (InputStream stream = StarposSignerTest.class.getClassLoader()
                .getResourceAsStream(SIGNING_VECTORS_RESOURCE)) {
            assertNotNull(stream, "missing test fixture: " + SIGNING_VECTORS_RESOURCE);
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return JSON.parseObject(json, Feature.OrderedField);
        } catch (IOException e) {
            throw new IllegalStateException("failed to load " + SIGNING_VECTORS_RESOURCE, e);
        }
    }

    private static String pem(String type, byte[] encoded) {
        String body = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        return "-----BEGIN " + type + "-----\n"
                + body
                + "\n-----END " + type + "-----";
    }
}
