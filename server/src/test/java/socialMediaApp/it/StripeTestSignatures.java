package socialMediaApp.it;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public final class StripeTestSignatures {
    private StripeTestSignatures(){}

    public static String stripeSignatureHeader(String payload, String webhookSecret) {
        long ts = Instant.now().getEpochSecond();
        String signedPayload = ts + "." + payload;
        String sig = hmacSha256Hex(webhookSecret, signedPayload);
        return "t=" + ts + ",v1=" + sig;
    }

    private static String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
