package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.suivi.CommentaireRequest;
import com.vala.gestionStagiaires.DTOs.suivi.PresenceDto;
import com.vala.gestionStagiaires.DTOs.suivi.PresenceRequest;
import com.vala.gestionStagiaires.DTOs.suivi.RapportDto;
import com.vala.gestionStagiaires.DTOs.suivi.RapportRequest;
import com.vala.gestionStagiaires.DTOs.suivi.RevisionRequest;
import com.vala.gestionStagiaires.service.PresenceService;
import com.vala.gestionStagiaires.service.RapportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Suivi d'un stagiaire : présences et rapports hebdomadaires. */
@RestController
@RequiredArgsConstructor
public class SuiviController {

    private final PresenceService presenceService;
    private final RapportService rapportService;

    // ---- Présences -----------------------------------------------------

    @PutMapping("/stagiaires/{id}/presences/{date}")
    public ResponseEntity<PresenceDto> enregistrerPresence(
            @PathVariable Long id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody PresenceRequest request) {
        return ResponseEntity.ok(presenceService.enregistrer(id, date, request));
    }

    @GetMapping("/stagiaires/{id}/presences")
    public ResponseEntity<List<PresenceDto>> listerPresences(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return ResponseEntity.ok(presenceService.lister(id, du, au));
    }

    @DeleteMapping("/stagiaires/{id}/presences/{date}")
    public ResponseEntity<Void> supprimerPresence(
            @PathVariable Long id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        presenceService.supprimer(id, date);
        return ResponseEntity.noContent().build();
    }

    // ---- Rapports hebdomadaires ---------------------------------------

    @PostMapping("/stagiaires/{id}/rapports")
    public ResponseEntity<RapportDto> deposerRapport(@PathVariable Long id, @Valid @RequestBody RapportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rapportService.deposer(id, request));
    }

    @GetMapping("/stagiaires/{id}/rapports")
    public ResponseEntity<List<RapportDto>> listerRapports(@PathVariable Long id) {
        return ResponseEntity.ok(rapportService.lister(id));
    }

    @GetMapping("/rapports/{id}")
    public ResponseEntity<RapportDto> getRapport(@PathVariable Long id) {
        return ResponseEntity.ok(rapportService.getRapport(id));
    }

    @PutMapping("/rapports/{id}")
    public ResponseEntity<RapportDto> modifierRapport(@PathVariable Long id, @Valid @RequestBody RapportRequest request) {
        return ResponseEntity.ok(rapportService.modifier(id, request));
    }

    @PatchMapping("/rapports/{id}/valider")
    public ResponseEntity<RapportDto> validerRapport(
            @PathVariable Long id, @Valid @RequestBody(required = false) CommentaireRequest request) {
        return ResponseEntity.ok(rapportService.valider(id, request != null ? request.commentaire() : null));
    }

    @PatchMapping("/rapports/{id}/a-revoir")
    public ResponseEntity<RapportDto> demanderRevision(@PathVariable Long id, @Valid @RequestBody RevisionRequest request) {
        return ResponseEntity.ok(rapportService.demanderRevision(id, request.commentaire()));
    }
}
