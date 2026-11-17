package com.vala.gestionStagiaires.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Sécurité de l'API : sans état, authentification par jeton Bearer (JWT HS256).
 *
 * <p>Matrice des droits (les URL fixent le rôle minimal ; pour un ENCADRANT,
 * des {@code @PreAuthorize("@securite...")} sur les contrôleurs limitent en plus
 * l'accès à ses propres stagiaires) :
 * <pre>
 *   Public           POST /auth/login, POST /candidatures (avec CV), GET /candidatures/departements,
 *                    documentation Swagger
 *   ADMIN            /utilisateurs/**
 *   ADMIN, RH        candidatures, statistiques, exports CSV, attestations,
 *                    création / modification / suppression des données de référence
 *   + ENCADRANT      lecture des stagiaires, stages, encadrants, départements ;
 *                    présences, rapports et évaluation de ses stagiaires
 *   Authentifié      GET /auth/me
 *   Tout le reste    refusé
 * </pre>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] TOUS_LES_ROLES = {"ADMIN", "RH", "ENCADRANT"};
    private static final String[] GESTION = {"ADMIN", "RH"};
    private static final String[] DONNEES_DE_REFERENCE = {"/stagiaires/**", "/stages/**", "/encadrants/**", "/departements/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ReponsesSecurite reponses,
                                                   JwtAuthenticationConverter jwtConverter) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // jeton Bearer, aucun cookie de session : pas d'exposition au CSRF
                .cors(Customizer.withDefaults()) // règles de config/CorsConfig
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/candidatures").permitAll()
                        .requestMatchers(HttpMethod.GET, "/candidatures/departements").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()

                        .requestMatchers("/auth/me").authenticated()
                        .requestMatchers("/utilisateurs/**").hasRole("ADMIN")
                        .requestMatchers("/candidatures/**", "/statistiques/**", "/stagiaires/*/attestation",
                                "/stagiaires/export.csv", "/stages/export.csv").hasAnyRole(GESTION)

                        // Suivi : ouvert aux encadrants, restreint à leurs stagiaires par @PreAuthorize.
                        .requestMatchers("/stagiaires/*/presences/**", "/stagiaires/*/rapports/**",
                                "/stagiaires/*/evaluation", "/rapports/**").hasAnyRole(TOUS_LES_ROLES)

                        .requestMatchers(HttpMethod.GET, DONNEES_DE_REFERENCE).hasAnyRole(TOUS_LES_ROLES)
                        .requestMatchers(DONNEES_DE_REFERENCE).hasAnyRole(GESTION)
                        .anyRequest().denyAll())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                        .authenticationEntryPoint(reponses.nonAuthentifie())
                        .accessDeniedHandler(reponses.accesRefuse()))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(reponses.nonAuthentifie())
                        .accessDeniedHandler(reponses.accesRefuse()));
        return http.build();
    }

    @Bean
    public SecretKey cleJwt(SecuriteProperties proprietes) {
        return new SecretKeySpec(proprietes.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey cleJwt, SecuriteProperties proprietes) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(cleJwt).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(proprietes.emetteur()));
        return decoder;
    }

    /** Le claim {@code role} (ex. "RH") devient l'autorité {@code ROLE_RH}. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter autorites = new JwtGrantedAuthoritiesConverter();
        autorites.setAuthoritiesClaimName(JwtService.CLAIM_ROLE);
        autorites.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(autorites);
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
