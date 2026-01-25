package socialMediaApp.api;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

public final class StripeTestSignatures {

    private StripeTestSignatures() {}

    public static String stripeSignatureHeader(String payload, String webhookSecret) {
        long ts = Instant.now().getEpochSecond();
        String signedPayload = ts + "." + payload;
        String sig = hmacSha256Hex(webhookSecret, signedPayload);
        return "t=" + ts + ",v1=" + sig;
    }

    private static String hmacSha256Hex(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
