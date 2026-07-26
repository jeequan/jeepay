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
    void rsa_boundary_is_replaceable_and_supports_local_round_trip() throws Exception {
        JSONObject rsa = loadFixture().getJSONObject("rsa");
        assertEquals("pending_formal_starpos_material", rsa.getString("status"));
        assertNotNull(rsa.getString("replacementNotes"));

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("amount", "1.00");
        request.put("merchantNo", "LOCAL_FIXTURE");

        String signature = StarposSigner.signRequest(request, pem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        assertFalse(signature.isBlank());

        Map<String, Object> notification = new LinkedHashMap<>(request);
        notification.put("sign", signature);
        assertTrue(StarposSigner.verifyNotification(
                notification,
                pem("PUBLIC KEY", keyPair.getPublic().getEncoded())
        ));

        notification.put("amount", "9.99");
        assertFalse(StarposSigner.verifyNotification(
                notification,
                pem("PUBLIC KEY", keyPair.getPublic().getEncoded())
        ));
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
