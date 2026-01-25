package socialMediaApp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import socialMediaApp.security.JwtAuthFilter;
import socialMediaApp.services.TicketService;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StripeWebhookController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class }
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "stripe.webhookSecret=whsec_test_123")
class StripeWebhookControllerWebMvcTest {

    private static final String SECRET = "whsec_test_123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean TicketService ticketService;
    @MockBean JwtAuthFilter jwtAuthFilter;

    @Test
    void webhook_missingSignatureHeader_returns400() throws Exception {
        String payload = "{\"id\":\"evt_1\",\"type\":\"checkout.session.completed\"}";

        mvc.perform(post("/api/payments/webhook")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("missing signature"));

        verifyNoInteractions(ticketService);
    }

    @Test
    void webhook_invalidSignature_returns400() throws Exception {
        String payload = "{\"id\":\"evt_1\",\"type\":\"checkout.session.completed\"}";

        mvc.perform(post("/api/payments/webhook")
                        .contentType(APPLICATION_JSON)
                        .header("Stripe-Signature", "t=1,v1=deadbeef")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("invalid signature"));

        verifyNoInteractions(ticketService);
    }

    @Test
    void checkoutSessionCompleted_paid_callsRegisterPaid_withSessionId_andPaymentIntentId() throws Exception {
        String payload = buildCheckoutSessionEventJson(
                "checkout.session.completed",
                "cs_test_1",
                "paid",
                "pi_test_1",
                Map.of("postId", "2", "userId", "20")
        );
        String sig = StripeTestSignatures.stripeSignatureHeader(payload, SECRET);

        mvc.perform(post("/api/payments/webhook")
                        .contentType(APPLICATION_JSON)
                        .header("Stripe-Signature", sig)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("ok")).andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
        ;

        verify(ticketService).registerPaid(2, 20, "pi_test_1", "cs_test_1");
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void checkoutSessionCompleted_unpaid_doesNotCallRegisterPaid() throws Exception {
        String payload = buildCheckoutSessionEventJson(
                "checkout.session.completed",
                "cs_test_2",
                "unpaid",
                "pi_test_2",
                Map.of("postId", "2", "userId", "20")
        );
        String sig = StripeTestSignatures.stripeSignatureHeader(payload, SECRET);

        mvc.perform(post("/api/payments/webhook")
                        .contentType(APPLICATION_JSON)
                        .header("Stripe-Signature", sig)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verifyNoInteractions(ticketService);
    }

    @Test
    void checkoutSessionAsyncPaymentSucceeded_callsRegisterPaid_evenIfPaymentStatusNotPaid() throws Exception {
        // у твоєму коді okToCreate = ... || eventType == async_payment_succeeded
        String payload = buildCheckoutSessionEventJson(
                "checkout.session.async_payment_succeeded",
                "cs_async_1",
                "unpaid",
                "pi_async_1",
                Map.of("postId", "5", "userId", "7")
        );
        String sig = StripeTestSignatures.stripeSignatureHeader(payload, SECRET);

        mvc.perform(post("/api/payments/webhook")
                        .contentType(APPLICATION_JSON)
                        .header("Stripe-Signature", sig)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(ticketService).registerPaid(5, 7, "pi_async_1", "cs_async_1");
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void paymentIntentSucceeded_callsRegisterPaid_withNullSessionId() throws Exception {
        String payload = buildPaymentIntentEventJson(
                "payment_intent.succeeded",
                "pi_ok_123",
                Map.of("postId", "9", "userId", "11")
        );
        String sig = StripeTestSignatures.stripeSignatureHeader(payload, SECRET);

        mvc.perform(post("/api/payments/webhook")
                        .contentType(APPLICATION_JSON)
                        .header("Stripe-Signature", sig)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(ticketService).registerPaid(9, 11, "pi_ok_123", null);
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void otherEvent_returns200_andDoesNothing() throws Exception {
        String payload = buildGenericEventJson("customer.created");
        String sig = StripeTestSignatures.stripeSignatureHeader(payload, SECRET);

        mvc.perform(post("/api/payments/webhook")
                        .contentType(APPLICATION_JSON)
                        .header("Stripe-Signature", sig)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verifyNoInteractions(ticketService);
    }

    // -------------------- payload builders --------------------

    private String buildGenericEventJson(String type) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", "evt_test_1");
        root.put("object", "event");
        root.put("type", type);
        root.put("data", Map.of("object", Map.of()));
        return om.writeValueAsString(root);
    }

    private String buildCheckoutSessionEventJson(
            String eventType,
            String sessionId,
            String paymentStatus,
            String paymentIntent,
            Map<String, String> metadata
    ) throws Exception {
        Map<String, Object> sessionObj = new LinkedHashMap<>();
        sessionObj.put("id", sessionId);
        sessionObj.put("object", "checkout.session");
        sessionObj.put("payment_status", paymentStatus);
        sessionObj.put("payment_intent", paymentIntent);
        sessionObj.put("metadata", metadata);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", "evt_cs_1");
        root.put("object", "event");
        root.put("type", eventType);
        root.put("data", Map.of("object", sessionObj));

        return om.writeValueAsString(root);
    }

    private String buildPaymentIntentEventJson(
            String eventType,
            String paymentIntentId,
            Map<String, String> metadata
    ) throws Exception {
        Map<String, Object> piObj = new LinkedHashMap<>();
        piObj.put("id", paymentIntentId);
        piObj.put("object", "payment_intent");
        piObj.put("metadata", metadata);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", "evt_pi_1");
        root.put("object", "event");
        root.put("type", eventType);
        root.put("data", Map.of("object", piObj));

        return om.writeValueAsString(root);
    }
}
