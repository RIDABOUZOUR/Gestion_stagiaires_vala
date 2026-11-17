package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.evaluation.AttestationPdf;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationDto;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationRequest;
import com.vala.gestionStagiaires.service.AttestationService;
import com.vala.gestionStagiaires.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Fin de stage : évaluation par l'encadrant et attestation PDF. */
@RestController
@RequiredArgsConstructor
public class EvaluationController {

    /** En-tête de réponse portant le numéro de l'attestation téléchargée. */
    public static final String HEADER_NUMERO = "X-Attestation-Numero";

    private final EvaluationService evaluationService;
    private final AttestationService attestationService;

    @PutMapping("/stagiaires/{id}/evaluation")
    @PreAuthorize("@securite.peutAccederStagiaire(#id)")
    public ResponseEntity<EvaluationDto> enregistrerEvaluation(@PathVariable Long id,
                                                               @Valid @RequestBody EvaluationRequest request) {
        return ResponseEntity.ok(evaluationService.enregistrer(id, request));
    }

    @GetMapping("/stagiaires/{id}/evaluation")
    @PreAuthorize("@securite.peutAccederStagiaire(#id)")
    public ResponseEntity<EvaluationDto> getEvaluation(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationService.getEvaluation(id));
    }

    @GetMapping("/stagiaires/{id}/attestation")
    public ResponseEntity<byte[]> telechargerAttestation(@PathVariable Long id) {
        AttestationPdf attestation = attestationService.generer(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(attestation.nomFichier()).build().toString())
                .header(HEADER_NUMERO, attestation.numero())
                .body(attestation.contenu());
    }
}
