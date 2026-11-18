package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.candidature.AcceptationRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureDto;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureRequest;
import com.vala.gestionStagiaires.DTOs.candidature.DepartementPublic;
import com.vala.gestionStagiaires.DTOs.candidature.FichierCv;
import com.vala.gestionStagiaires.DTOs.candidature.RefusRequest;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.exceptions.FichierInvalideException;
import com.vala.gestionStagiaires.securite.SecuriteService;
import com.vala.gestionStagiaires.service.CandidatureService;
import com.vala.gestionStagiaires.service.CvValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final SecuriteService securite;

    /**
     * Dépôt avec CV (formulaire public) : requête multipart avec une partie
     * « candidature » (JSON) et une partie « cv » (PDF, 5 Mo maximum).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidatureDto> soumettreAvecCv(
            @Valid @RequestPart("candidature") CandidatureRequest request,
            @RequestPart(value = "cv", required = false) MultipartFile cv) {
        return deposer(request, cv);
    }

    /** Dépôt sans fichier (JSON) : réservé aux RH et administrateurs, le CV étant obligatoire pour un visiteur. */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CandidatureDto> soumettre(@Valid @RequestBody CandidatureRequest request) {
        return deposer(request, null);
    }

    /** Départements proposés aux candidats (public, sans le nom du responsable). */
    @GetMapping("/departements")
    public ResponseEntity<List<DepartementPublic>> departements() {
        return ResponseEntity.ok(candidatureService.departementsPublics());
    }

    /** CV de la candidature, ouvert directement dans le navigateur. */
    @GetMapping("/{id}/cv")
    public ResponseEntity<byte[]> cv(@PathVariable Long id) {
        FichierCv cv = candidatureService.cv(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(cv.nomFichier()).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "sandbox") // neutralise un éventuel PDF piégé ouvert directement
                .body(cv.contenu());
    }

    /** Ajoute ou remplace le CV d'une candidature en cours de traitement. */
    @PutMapping(value = "/{id}/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidatureDto> remplacerCv(@PathVariable Long id, @RequestPart("cv") MultipartFile cv) {
        return ResponseEntity.ok(candidatureService.remplacerCv(id, CvValidator.valider(cv)));
    }

    private ResponseEntity<CandidatureDto> deposer(CandidatureRequest request, MultipartFile cv) {
        boolean sansCv = cv == null || cv.isEmpty();
        if (sansCv && !securite.estGestionnaire()) {
            throw new FichierInvalideException("Le CV (PDF) est obligatoire.");
        }
        FichierCv fichier = sansCv ? null : CvValidator.valider(cv);
        return ResponseEntity.status(HttpStatus.CREATED).body(candidatureService.soumettre(request, fichier));
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
