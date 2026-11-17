package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.auth.MotDePasseRequest;
import com.vala.gestionStagiaires.DTOs.auth.UtilisateurDto;
import com.vala.gestionStagiaires.DTOs.auth.UtilisateurModification;
import com.vala.gestionStagiaires.DTOs.auth.UtilisateurRequest;
import com.vala.gestionStagiaires.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Gestion des comptes (réservée aux administrateurs, voir SecurityConfig). */
@RestController
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public ResponseEntity<List<UtilisateurDto>> lister() {
        return ResponseEntity.ok(utilisateurService.lister());
    }

    @PostMapping
    public ResponseEntity<UtilisateurDto> creer(@Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDto> modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurModification modification) {
        return ResponseEntity.ok(utilisateurService.modifier(id, modification));
    }

    @PatchMapping("/{id}/mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(@PathVariable Long id, @Valid @RequestBody MotDePasseRequest request) {
        utilisateurService.changerMotDePasse(id, request.motDePasse());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        utilisateurService.supprimer(id, jwt != null ? jwt.getSubject() : null);
        return ResponseEntity.noContent().build();
    }
}
