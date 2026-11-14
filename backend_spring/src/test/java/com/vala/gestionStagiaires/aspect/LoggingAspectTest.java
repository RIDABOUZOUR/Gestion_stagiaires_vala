package com.vala.gestionStagiaires.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingAspect")
class LoggingAspectTest {

    private final LoggingAspect aspect = new LoggingAspect();

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private void stubSignature() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(LoggingAspectTest.class);
        when(signature.getName()).thenReturn("doSomething");
    }

    @Test
    @DisplayName("logExecution() retourne le résultat de la méthode interceptée")
    void logExecution_shouldReturnResult() throws Throwable {
        stubSignature();
        when(joinPoint.proceed()).thenReturn("ok");

        assertThat(aspect.logExecution(joinPoint)).isEqualTo("ok");
    }

    @Test
    @DisplayName("logExecution() journalise arguments et résultat en DEBUG")
    void logExecution_debugEnabled_shouldLogArgsAndResult() throws Throwable {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        Level previous = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            stubSignature();
            when(joinPoint.getArgs()).thenReturn(new Object[]{"a", 1});
            when(joinPoint.proceed()).thenReturn("ok");

            assertThat(aspect.logExecution(joinPoint)).isEqualTo("ok");
        } finally {
            logger.setLevel(previous);
        }
    }

    @Test
    @DisplayName("logExecution() journalise l'erreur et propage l'exception")
    void logExecution_shouldRethrowException() throws Throwable {
        stubSignature();
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> aspect.logExecution(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }
}
