package com.ecommerce.platform.auth.config;

import com.ecommerce.platform.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootstrapAdminConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAdminConfiguration.class);

    @Bean
    ApplicationRunner bootstrapAdmin(
        AuthService authService,
        @Value("${AUTH_BOOTSTRAP_ADMIN_EMAIL:}") String email,
        @Value("${AUTH_BOOTSTRAP_ADMIN_PASSWORD:}") String password
    ) {
        return args -> {
            if (email.isBlank() && password.isBlank()) {
                LOGGER.warn("No bootstrap administrator configured; set AUTH_BOOTSTRAP_ADMIN_EMAIL and AUTH_BOOTSTRAP_ADMIN_PASSWORD.");
                return;
            }
            if (email.isBlank() || password.isBlank()) {
                throw new IllegalStateException(
                    "Both AUTH_BOOTSTRAP_ADMIN_EMAIL and AUTH_BOOTSTRAP_ADMIN_PASSWORD must be configured together."
                );
            }
            authService.ensureBootstrapAdmin(email, password);
        };
    }
}
