package socialMediaApp.api;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import socialMediaApp.models.Post;
import socialMediaApp.models.User;
import socialMediaApp.services.PostService;
import socialMediaApp.services.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.Currency;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PostService postService;
    private final UserService userService;

    @Value("${app.frontendBaseUrl}")
    private String frontendBaseUrl;

    @PostMapping("/checkout-session/{postId}")
    public Map<String, Object> createCheckoutSession(@PathVariable int postId, Principal principal) throws StripeException {
        User me = userService.getByEmailEntity(principal.getName());
        Post post = postService.getById(postId);

        if (!Boolean.TRUE.equals(post.getPaid())
                || post.getPrice() == null
                || post.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("EVENT_IS_FREE");
        }

        long unitAmount = toMinorUnits(post.getPrice(), post.getCurrency());
        String currency = (post.getCurrency() == null ? "PLN" : post.getCurrency()).toLowerCase();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)

                .setSuccessUrl(frontendBaseUrl + "/posts/" + postId + "?paid=1&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendBaseUrl + "/posts/" + postId + "?pay=cancel")

                .putMetadata("postId", String.valueOf(postId))
                .putMetadata("userId", String.valueOf(me.getId()))

                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("postId", String.valueOf(postId))
                                .putMetadata("userId", String.valueOf(me.getId()))
                                .build()
                )

                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(unitAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(post.getTitle() == null ? "Event ticket" : post.getTitle())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        return Map.of(
                "url", session.getUrl(),
                "sessionId", session.getId()
        );
    }

    private long toMinorUnits(BigDecimal amount, String currencyCode) {
        Currency cur = Currency.getInstance((currencyCode == null ? "PLN" : currencyCode).toUpperCase());
        int fraction = cur.getDefaultFractionDigits();
        return amount.movePointRight(fraction).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
