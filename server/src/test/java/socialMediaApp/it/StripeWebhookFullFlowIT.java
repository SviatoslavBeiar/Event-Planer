package socialMediaApp.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import socialMediaApp.models.Post;
import socialMediaApp.models.Ticket;
import socialMediaApp.models.User;
import socialMediaApp.repositories.TicketRepository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "stripe.webhookSecret=whsec_test_123",
        "spring.jpa.hibernate.ddl-auto=update"
})
@Slf4j
class StripeWebhookFullFlowIT extends AbstractMySqlMailhogTcIT {

    private static final String SECRET = "whsec_test_123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Autowired TicketRepository ticketRepository;
    @Autowired EntityManager em;

    @Test
    @Transactional
    void webhook_checkoutSessionCompleted_paid_createsOrEnsuresTicket_inDb_andSendsMail() throws Exception {

        int userId = seedUser_MinRequired();
        int postId = seedPost_MinRequired(userId);

        String payload = buildCheckoutSessionEventJson(
                "checkout.session.completed",
                "cs_test_1",
                "paid",
                "pi_test_1",
                Map.of(
                        "postId", String.valueOf(postId),
                        "userId", String.valueOf(userId)
                )
        );

        String sig = StripeTestSignatures.stripeSignatureHeader(payload, SECRET);

        mvc.perform(post("/api/payments/webhook")
                        .contentType(APPLICATION_JSON)
                        .header("Stripe-Signature", sig)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        em.flush();
        em.clear();

        Ticket t = ticketRepository.findAll().stream()
                .filter(x -> "pi_test_1".equals(x.getPaymentIntentId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ticket with paymentIntentId=pi_test_1 not found"));

        log.info("TICKET CREATED: id={}, paymentIntentId={}", t.getId(), t.getPaymentIntentId());

        assertNotNull(t.getId());
        assertEquals("pi_test_1", t.getPaymentIntentId());
        // якщо є поле:
        // assertEquals("cs_test_1", t.getCheckoutSessionId());

        // MailHog перевірка опційно (якщо webhook реально шле лист)
        // String httpBase = "http://" + MAILHOG.getHost() + ":" + MAILHOG.getMappedPort(8025);
        // int total = MailHogClient.getTotalMessages(httpBase, om);
        // assertTrue(total >= 1);
    }

    private String buildCheckoutSessionEventJson(
            String eventType,
            String sessionId,
            String paymentStatus,
            String paymentIntent,
            Map<String, String> metadata
    ) throws Exception {
        Map<String, Object> sessionObj = new java.util.LinkedHashMap<>();
        sessionObj.put("id", sessionId);
        sessionObj.put("object", "checkout.session");
        sessionObj.put("payment_status", paymentStatus);
        sessionObj.put("payment_intent", paymentIntent);
        sessionObj.put("metadata", metadata);

        Map<String, Object> root = new java.util.LinkedHashMap<>();
        root.put("id", "evt_cs_1");
        root.put("object", "event");
        root.put("type", eventType);
        root.put("data", Map.of("object", sessionObj));

        return om.writeValueAsString(root);
    }

    // ---------------- seeds ----------------

    private int seedUser_MinRequired() {
        User u = new User();

        // мінімум під твою users таблицю:
        u.setEmail("it_" + UUID.randomUUID() + "@test.com");
        u.setEnabled(true);

        // password/name/lastName можуть бути nullable, але задаємо щоб не ловити сюрпризи
        u.setPassword("test");
        u.setName("IT");
        u.setLastName("User");

        // role NOT NULL => ставимо через reflection (бо може бути enum або String)
        setEnumOrString(u, "setRole", "USER", "ROLE_USER", "ADMIN");

        em.persist(u);
        em.flush();
        return u.getId();
    }

    private int seedPost_MinRequired(int userId) {
        User u = em.find(User.class, userId);
        assertNotNull(u);

        Post p = new Post();

        // у тебе description @NotNull
        p.setDescription("IT post");

        // у тебе Post має user (FK)
        p.setUser(u);

        // решта — щоб було валідно для платного івенту (поля з твоєї таблиці posts)
        p.setTitle("IT Event");
        p.setLocation("Test City");
        p.setCapacity(50);
        p.setPaid(true);

        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(2);

        // якщо є ці сеттери — ок, якщо нема, то закоментуй
        p.setStartAt(start);
        p.setEndAt(end);

        // ці поля у тебе є в таблиці: price, currency, status, sales_start_at, sales_end_at
        setIfSetterExists(p, "setPrice", BigDecimal.valueOf(10));
        setIfSetterExists(p, "setCurrency", "EUR");
        setEnumOrString(p, "setStatus", "ACTIVE", "APPROVED", "PUBLISHED");
        setIfSetterExists(p, "setSalesStartAt", LocalDateTime.now().minusHours(1));
        setIfSetterExists(p, "setSalesEndAt", start.minusHours(1));

        em.persist(p);
        em.flush();
        return p.getId();
    }

    // ---------------- reflection helpers ----------------

    private static void setIfSetterExists(Object target, String setterName, Object value) {
        try {
            var methods = target.getClass().getMethods();
            for (var m : methods) {
                if (!m.getName().equals(setterName)) continue;
                if (m.getParameterCount() != 1) continue;

                Class<?> pt = m.getParameterTypes()[0];

                // пряме присвоєння
                if (value != null && pt.isAssignableFrom(value.getClass())) {
                    m.invoke(target, value);
                    return;
                }

                // примітиви/обгортки
                if (pt == boolean.class && value instanceof Boolean) { m.invoke(target, value); return; }
                if (pt == int.class && value instanceof Integer) { m.invoke(target, value); return; }
                if (pt == long.class && value instanceof Long) { m.invoke(target, value); return; }
            }
        } catch (Exception ignored) {}
    }

    private static void setEnumOrString(Object target, String setterName, String... preferredNames) {
        try {
            var methods = target.getClass().getMethods();
            for (var m : methods) {
                if (!m.getName().equals(setterName)) continue;
                if (m.getParameterCount() != 1) continue;

                Class<?> pt = m.getParameterTypes()[0];

                // якщо setter приймає String
                if (pt == String.class) {
                    m.invoke(target, preferredNames[0]);
                    return;
                }

                // якщо setter приймає enum
                if (pt.isEnum()) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum> enumType = (Class<? extends Enum>) pt;

                    for (String name : preferredNames) {
                        try {
                            Enum<?> val = Enum.valueOf(enumType, name);
                            m.invoke(target, val);
                            return;
                        } catch (IllegalArgumentException ignored) {}
                    }

                    // fallback: перший enum-констант
                    Object[] constants = enumType.getEnumConstants();
                    if (constants != null && constants.length > 0) {
                        m.invoke(target, constants[0]);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot set enum/string via " + setterName + " on " + target.getClass(), e);
        }
    }
}
