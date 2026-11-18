package com.vala.gestionStagiaires.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Horloge injectable : les règles métier qui dépendent de la date du jour
 * l'utilisent au lieu de {@code LocalDate.now()}, ce qui les rend testables.
 * Le fuseau est explicite ({@code APP_TIMEZONE}) : une image Docker tourne en UTC,
 * ce qui décalerait « aujourd'hui » de part et d'autre de minuit.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${app.timezone:Africa/Casablanca}") String fuseau) {
        return Clock.system(ZoneId.of(fuseau));
    }
}
