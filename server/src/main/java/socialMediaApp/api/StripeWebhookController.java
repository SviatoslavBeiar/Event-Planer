package socialMediaApp.api;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import socialMediaApp.services.TicketService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    private final TicketService ticketService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        if (sigHeader == null) {
            log.warn("Stripe webhook: missing Stripe-Signature header");
            return ResponseEntity.badRequest().body("missing signature");
        }

        final Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook: invalid signature. Check stripe.webhookSecret");
            return ResponseEntity.badRequest().body("invalid signature");
        }

        log.info("Stripe webhook received: type={}", event.getType());

        if ("checkout.session.completed".equals(event.getType())
                || "checkout.session.async_payment_succeeded".equals(event.getType())) {

            Session session = extractSession(event);
            if (session == null) {
                log.warn("checkout.session.*: session is null (deserialization failed)");
                return ResponseEntity.ok("ok");
            }

            Map<String, String> md = session.getMetadata();
            String postIdStr = md == null ? null : md.get("postId");
            String userIdStr = md == null ? null : md.get("userId");

            log.info("Session: id={}, payment_status={}, postId={}, userId={}, payment_intent={}",
                    session.getId(),
                    session.getPaymentStatus(),
                    postIdStr,
                    userIdStr,
                    session.getPaymentIntent()
            );

            boolean okToCreate =
                    "paid".equals(session.getPaymentStatus())
                            || "checkout.session.async_payment_succeeded".equals(event.getType());

            if (okToCreate && postIdStr != null && userIdStr != null) {
                int postId = Integer.parseInt(postIdStr);
                int userId = Integer.parseInt(userIdStr);

                String paymentIntentId = session.getPaymentIntent(); // може бути null
                String sessionId = session.getId();

                ticketService.registerPaid(postId, userId, paymentIntentId, sessionId);
                log.info("Ticket ensured via session event for postId={}, userId={}", postId, userId);
            } else {
                log.warn("Session not eligible or missing metadata. okToCreate={}, postId={}, userId={}",
                        okToCreate, postIdStr, userIdStr);
            }

            return ResponseEntity.ok("ok");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {

            PaymentIntent pi = extractPaymentIntent(event);
            if (pi == null) {
                log.warn("payment_intent.succeeded: paymentIntent is null (deserialization failed)");
                return ResponseEntity.ok("ok");
            }

            Map<String, String> md = pi.getMetadata();
            String postIdStr = md == null ? null : md.get("postId");
            String userIdStr = md == null ? null : md.get("userId");

            log.info("PaymentIntent: id={}, postId={}, userId={}", pi.getId(), postIdStr, userIdStr);

            if (postIdStr != null && userIdStr != null) {
                int postId = Integer.parseInt(postIdStr);
                int userId = Integer.parseInt(userIdStr);

                // Тут sessionId може бути null — ми все одно створимо квиток по paymentIntentId
                ticketService.registerPaid(postId, userId, pi.getId(), null);
                log.info("Ticket ensured via payment_intent for postId={}, userId={}", postId, userId);
            } else {
                log.warn("PaymentIntent missing metadata postId/userId (add PaymentIntentData metadata in PaymentController)");
            }

            return ResponseEntity.ok("ok");
        }

        return ResponseEntity.ok("ok");
    }

    // ---------------- helpers ----------------

    private Session extractSession(Event event) {
        try {
            EventDataObjectDeserializer des = event.getDataObjectDeserializer();

            StripeObject obj = des.getObject().orElse(null);
            if (obj == null) {
                try {
                    obj = des.deserializeUnsafe();
                } catch (EventDataObjectDeserializationException e) {
                    log.warn("Failed to deserialize Session (unsafe): {}", e.getMessage());
                    return null;
                }
            }

            return (obj instanceof Session) ? (Session) obj : null;
        } catch (Exception e) {
            log.warn("Failed to extract Session: {}", e.getMessage());
            return null;
        }
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        try {
            EventDataObjectDeserializer des = event.getDataObjectDeserializer();

            StripeObject obj = des.getObject().orElse(null);
            if (obj == null) {
                try {
                    obj = des.deserializeUnsafe();
                } catch (EventDataObjectDeserializationException e) {
                    log.warn("Failed to deserialize PaymentIntent (unsafe): {}", e.getMessage());
                    return null;
                }
            }

            return (obj instanceof PaymentIntent) ? (PaymentIntent) obj : null;
        } catch (Exception e) {
            log.warn("Failed to extract PaymentIntent: {}", e.getMessage());
            return null;
        }
    }
}
