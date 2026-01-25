package socialMediaApp.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class StripeConfig {
    @Value("${stripe.secretKey}")
    private String secretKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
    }
}
