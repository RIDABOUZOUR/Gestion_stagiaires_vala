package com.vala.gestionStagiaires.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque une méthode (d'un bean Spring) pour qu'elle soit journalisée par
 * {@link LoggingAspect}, même si elle n'appartient pas aux couches
 * {@code @Service} ou {@code @RestController}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {
}
