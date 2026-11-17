package com.vala.gestionStagiaires.securite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SecuriteProperties")
class SecuritePropertiesTest {

    @Test
    @DisplayName("refuse de démarrer sans JWT_SECRET ou avec une clé trop courte")
    void secretObligatoire() {
        assertThatThrownBy(() -> new SecuriteProperties(null, Duration.ofHours(8), "x", null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new SecuriteProperties("trop-court", Duration.ofHours(8), "x", null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> new SecuriteProperties("x".repeat(32), Duration.ofHours(8), "x", null, null))
                .doesNotThrowAnyException();
    }
}
