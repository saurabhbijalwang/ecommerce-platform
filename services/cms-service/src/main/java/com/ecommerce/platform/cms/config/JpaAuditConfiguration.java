package com.ecommerce.platform.cms.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfiguration {

    @Bean
    AuditorAware<String> auditorAware() {
        // Phase 6 replaces this local-development actor with the authenticated principal.
        return () -> Optional.of("system");
    }
}
