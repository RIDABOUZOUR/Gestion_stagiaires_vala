package com.vala.gestionStagiaires.securite;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Réponses 401 et 403 au même format JSON que les autres erreurs de l'API. */
@Component
public class ReponsesSecurite {

    private final ObjectMapper objectMapper;

    public ReponsesSecurite(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthenticationEntryPoint nonAuthentifie() {
        return (request, response, exception) -> {
            String message = exception instanceof InvalidBearerTokenException
                    ? "Session expirée ou jeton invalide : reconnectez-vous."
                    : "Authentification requise.";
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            ecrire(response, HttpStatus.UNAUTHORIZED, message);
        };
    }

    public AccessDeniedHandler accesRefuse() {
        return (request, response, exception) ->
                ecrire(response, HttpStatus.FORBIDDEN, "Accès refusé : vos droits ne permettent pas cette action.");
    }

    private void ecrire(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("timestamp", Instant.now().toString());
        corps.put("status", status.value());
        corps.put("message", message);
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), corps);
    }
}
