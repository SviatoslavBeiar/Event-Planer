package socialMediaApp.it;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractMySqlMailhogTcIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // MailHog: SMTP 1025, HTTP API/UI 8025
    @Container
    static final GenericContainer<?> MAILHOG = new GenericContainer<>("mailhog/mailhog:v1.0.1")
            .withExposedPorts(1025, 8025);

    @BeforeAll
    static void info() {
        System.out.println("MYSQL JDBC: " + MYSQL.getJdbcUrl());
        System.out.println("MAILHOG SMTP: " + MAILHOG.getHost() + ":" + MAILHOG.getMappedPort(1025));
        System.out.println("MAILHOG HTTP: " + MAILHOG.getHost() + ":" + MAILHOG.getMappedPort(8025));
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // DB
        r.add("spring.datasource.url", MYSQL::getJdbcUrl);
        r.add("spring.datasource.username", MYSQL::getUsername);
        r.add("spring.datasource.password", MYSQL::getPassword);

        // Hibernate schema only for tests
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        r.add("spring.mail.host", MAILHOG::getHost);
        r.add("spring.mail.port", () -> MAILHOG.getMappedPort(1025));
        r.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        r.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");

        r.add("test.mailhog.httpBase", () -> "http://" + MAILHOG.getHost() + ":" + MAILHOG.getMappedPort(8025));
    }
}
