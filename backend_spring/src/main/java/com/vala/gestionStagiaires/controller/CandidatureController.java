package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.candidature.AcceptationRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureDto;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureRequest;
import com.vala.gestionStagiaires.DTOs.candidature.RefusRequest;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.service.CandidatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;

    @PostMapping
    public ResponseEntity<CandidatureDto> soumettre(@Valid @RequestBody CandidatureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidatureService.soumettre(request));
    }

    /**
     * Recherche paginée, ex. :
     * {@code /candidatures/page?keyword=ensa&statut=SOUMISE&departementId=2&sort=dateSoumission,desc}
     */
    @GetMapping("/page")
    public ResponseEntity<PageResponse<CandidatureDto>> rechercher(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CandidatureStatus statut,
            @RequestParam(required = false) Long departementId,
            @PageableDefault(size = 20) @SortDefault(sort = "dateSoumission", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(candidatureService.rechercher(new CandidatureFiltre(keyword, statut, departementId), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidatureDto> getCandidature(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.getCandidature(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidatureDto> modifier(@PathVariable Long id, @Valid @RequestBody CandidatureRequest request) {
        return ResponseEntity.ok(candidatureService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        candidatureService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Workflow -------------------------------------------------------

    @PatchMapping("/{id}/etudier")
    public ResponseEntity<CandidatureDto> etudier(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.etudier(id));
    }

    @PatchMapping("/{id}/refuser")
    public ResponseEntity<CandidatureDto> refuser(@PathVariable Long id, @Valid @RequestBody RefusRequest request) {
        return ResponseEntity.ok(candidatureService.refuser(id, request.motif()));
    }

    @PatchMapping("/{id}/retirer")
    public ResponseEntity<CandidatureDto> retirer(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.retirer(id));
    }

    /** Accepte la candidature : crée le stagiaire et l'affecte au stage indiqué. */
    @PostMapping("/{id}/accepter")
    public ResponseEntity<CandidatureDto> accepter(@PathVariable Long id, @Valid @RequestBody AcceptationRequest request) {
        return ResponseEntity.ok(candidatureService.accepter(id, request));
    }
}
