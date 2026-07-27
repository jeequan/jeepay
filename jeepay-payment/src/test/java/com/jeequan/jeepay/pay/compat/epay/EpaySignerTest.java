package com.jeequan.jeepay.pay.compat.epay;

import org.junit.jupiter.api.Test;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
    void rsaRoundTripUsesSha256Pkcs1v15() throws Exception {
        KeyPair pair = keyPair();
        Map<String, Object> fields = Map.of("money", "1.00", "pid", "1001");

        String signature = EpaySigner.signRsa(fields, pem(pair.getPrivate()));

        assertThat(EpaySigner.verifyRsa(fields, pem(pair.getPublic()), signature)).isTrue();
        assertThat(EpaySigner.verifyRsa(
                Map.of("money", "2.00", "pid", "1001"),
                pem(pair.getPublic()),
                signature)).isFalse();
    }

    private static KeyPair keyPair() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(PRIVATE_KEY_DER)));
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(PUBLIC_KEY_DER)));
        return new KeyPair(publicKey, privateKey);
    }

    private static String pem(PrivateKey privateKey) {
        return "-----BEGIN PRIVATE KEY-----\n" + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(privateKey.getEncoded()) + "\n-----END PRIVATE KEY-----";
    }

    private static String pem(PublicKey publicKey) {
        return "-----BEGIN PUBLIC KEY-----\n" + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKey.getEncoded()) + "\n-----END PUBLIC KEY-----";
    }

    private static final String PRIVATE_KEY_DER = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKGcBgREGlRrjvAr"
            + "RoU2fBdgKQog3OeCE1Huf9uXSWV1+yw+6dR5Tc+b8PnTclZ5Il0VwbIWEX9zXGVm"
            + "iM/cOU7EIBsN4u0K5ldKcodoNzX7TVfoo52OWOUCEVdcCO/EYrNNgJGGqLWOEdC1"
            + "4FooBEZ2k+9qA40yjnUVaCYHFQDBAgMBAAECgYAxc6LLr2M3LeqBR1y+6psyM/Sa"
            + "6s9t7/mleouZUEPfTijbsyyuHahXvOoSKEe2ej6vqTaqHeKg01YUmTmIEWVfXlz4"
            + "BmtBtS1kf8WIImhinpwSfRX1mXyX+77PNJDoq405TIJ/WEa4zuiO/EYasEB4HW0H"
            + "iA7EIcY0cKUJFHaCgQJBAMvkd8OfRZN2UEzUfzk7QfIV2161GFbtViXP1C5pM/17"
            + "AhVef8a3Y2KfpckS4XsqsmmLJddp6joR1dbdLgVBPbkCQQDK6Tf3HmcY4cxBHv2W"
            + "wudcIgA2l7S8o4dmplKMrLaJh5umH57GBKjgwAntoZjDYQqeV7ETEZRCAxDfSwrh"
            + "iR9JAkAGdjYpd/m/g6dl1/I0QSGE55ZtOPYNgzYQurZxxhnEtcpKHRWVmahHruGV"
            + "LskAm0jOOX+4hP3MW1ZYmefkeL3ZAkEAl0xtCZum3jvHlKsqBZdQ8jb8F7jo8Fuz"
            + "I4xTM0e5WDVAjw820Yo57lPjU0hSYyThyQ20IHbUKKCmnQkUlPUs2QJAEdfBeftC"
            + "MMOCS8XXDTVgudh8anjLhPt+DzsbjJzu4A5iZHyqjU0dTmhjHdbqHiP1TX9sT1x+"
            + "nNwvrHRBmc9hfA==";

    private static final String PUBLIC_KEY_DER = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQChnAYERBpUa47wK0aFNnwXYCkK"
            + "INznghNR7n/bl0lldfssPunUeU3Pm/D503JWeSJdFcGyFhF/c1xlZojP3DlOxCAb"
            + "DeLtCuZXSnKHaDc1+01X6KOdjljlAhFXXAjvxGKzTYCRhqi1jhHQteBaKARGdpPv"
            + "agONMo51FWgmBxUAwQIDAQAB";
}
