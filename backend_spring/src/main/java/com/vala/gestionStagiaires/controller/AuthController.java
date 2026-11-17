package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.auth.LoginRequest;
import com.vala.gestionStagiaires.DTOs.auth.LoginResponse;
import com.vala.gestionStagiaires.DTOs.auth.UtilisateurCourant;
import com.vala.gestionStagiaires.enums.Role;
import com.vala.gestionStagiaires.securite.JwtService;
import com.vala.gestionStagiaires.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Connexion : renvoie un jeton JWT ; 401 si les identifiants sont incorrects. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.connecter(request));
    }

    /** Utilisateur correspondant au jeton envoyé. */
    @GetMapping("/me")
    public ResponseEntity<UtilisateurCourant> moi(@AuthenticationPrincipal Jwt jwt) {
        Object encadrant = jwt.getClaim(JwtService.CLAIM_ENCADRANT);
        return ResponseEntity.ok(new UtilisateurCourant(jwt.getSubject(),
                Role.valueOf(jwt.getClaimAsString(JwtService.CLAIM_ROLE)),
                encadrant instanceof Number n ? n.longValue() : null));
    }
}
