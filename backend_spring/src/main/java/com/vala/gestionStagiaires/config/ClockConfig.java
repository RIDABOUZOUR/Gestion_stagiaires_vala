package com.vala.gestionStagiaires.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Horloge injectable : les règles métier qui dépendent de la date du jour
 * l'utilisent au lieu de {@code LocalDate.now()}, ce qui les rend testables.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
