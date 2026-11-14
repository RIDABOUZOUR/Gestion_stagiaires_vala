package com.vala.gestionStagiaires.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect AOP de journalisation.
 *
 * <p>Intercepte les méthodes publiques de :
 * <ul>
 *   <li>la couche métier ({@code @Service}) ;</li>
 *   <li>la couche web ({@code @RestController}) ;</li>
 *   <li>toute méthode annotée {@link Loggable}.</li>
 * </ul>
 *
 * <p>Comportement par appel :
 * <ul>
 *   <li>entrée + arguments → niveau {@code DEBUG} ;</li>
 *   <li>sortie + durée → niveau {@code INFO} ;</li>
 *   <li>exception → niveau {@code ERROR} (avec la pile).</li>
 * </ul>
 *
 * <p>Le niveau est réglable via
 * {@code logging.level.com.vala.gestionStagiaires.aspect} (INFO par défaut,
 * passer à DEBUG pour voir les arguments et les résultats).
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /** Toutes les classes annotées {@code @Service}. */
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {
    }

    /** Tous les contrôleurs REST. */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerLayer() {
    }

    /** Toute méthode annotée {@link Loggable}. */
    @Pointcut("@annotation(com.vala.gestionStagiaires.aspect.Loggable)")
    public void loggableMethod() {
    }

    @Around("serviceLayer() || controllerLayer() || loggableMethod()")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();

        if (log.isDebugEnabled()) {
            log.debug("→ {} : {}", method, Arrays.toString(joinPoint.getArgs()));
        }

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("← {} terminé en {} ms", method, duration);
            if (log.isDebugEnabled()) {
                log.debug("   résultat {} = {}", method, result);
            }
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("✗ {} échoué en {} ms : {}", method, duration, ex.getMessage(), ex);
            throw ex;
        }
    }
}
