package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.RapportDto;
import com.vala.gestionStagiaires.DTOs.suivi.RapportRequest;
import com.vala.gestionStagiaires.entities.RapportHebdomadaire;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StatutRapport;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.RapportHebdomadaireRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RapportServiceImpl implements RapportService {

    private final RapportHebdomadaireRepository rapportRepository;
    private final StagiaireRepository stagiaireRepository;
    private final Clock clock;

    @Override
    public RapportDto deposer(Long stagiaireId, RapportRequest request) {
        Stagiaire stagiaire = SuiviSupport.findStagiaire(stagiaireRepository, stagiaireId);
        Stage stage = SuiviSupport.stageSuivable(stagiaire);
        LocalDate lundi = lundiDe(request.semaine());

        if (lundi.plusDays(6).isBefore(stage.getDateDebut()) || lundi.isAfter(stage.getDateFin())) {
            throw new BusinessRuleException("La semaine du " + lundi + " ne fait pas partie de la période du stage (du "
                    + stage.getDateDebut() + " au " + stage.getDateFin() + ").");
        }
        if (lundi.isAfter(LocalDate.now(clock))) {
            throw new BusinessRuleException("Impossible de déposer un rapport pour une semaine future.");
        }
        if (rapportRepository.existsByStagiaireIdAndSemaine(stagiaireId, lundi)) {
            throw new BusinessRuleException("Un rapport existe déjà pour la semaine du " + lundi + ".");
        }

        RapportHebdomadaire rapport = RapportHebdomadaire.builder()
                .stagiaire(stagiaire)
                .semaine(lundi)
                .statut(StatutRapport.SOUMIS)
                .build();
        remplir(rapport, request);
        return RapportDto.of(rapportRepository.save(rapport));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RapportDto> lister(Long stagiaireId) {
        SuiviSupport.findStagiaire(stagiaireRepository, stagiaireId);
        return rapportRepository.findByStagiaireIdOrderBySemaineDesc(stagiaireId).stream()
                .map(RapportDto::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RapportDto getRapport(Long id) {
        return RapportDto.of(findRapport(id));
    }

    @Override
    public RapportDto modifier(Long id, RapportRequest request) {
        RapportHebdomadaire rapport = findRapport(id);
        if (rapport.getStatut() == StatutRapport.VALIDE) {
            throw new BusinessRuleException("Un rapport validé ne peut plus être modifié.");
        }
        if (!lundiDe(request.semaine()).equals(rapport.getSemaine())) {
            throw new BusinessRuleException("La semaine d'un rapport ne peut pas être modifiée.");
        }
        remplir(rapport, request);
        rapport.setStatut(StatutRapport.SOUMIS);
        return RapportDto.of(rapportRepository.save(rapport));
    }

    @Override
    public RapportDto valider(Long id, String commentaire) {
        return decider(id, StatutRapport.VALIDE, commentaire);
    }

    @Override
    public RapportDto demanderRevision(Long id, String commentaire) {
        if (commentaire == null || commentaire.isBlank()) {
            throw new BusinessRuleException("Un commentaire est obligatoire pour demander une révision.");
        }
        return decider(id, StatutRapport.A_REVOIR, commentaire);
    }

    // ------------------------------------------------------------------

    /** Seul un rapport SOUMIS (en attente de l'encadrant) peut être validé ou renvoyé. */
    private RapportDto decider(Long id, StatutRapport cible, String commentaire) {
        RapportHebdomadaire rapport = findRapport(id);
        if (rapport.getStatut() != StatutRapport.SOUMIS) {
            throw new BusinessRuleException("Seul un rapport soumis peut être examiné (statut actuel : "
                    + rapport.getStatut() + ").");
        }
        rapport.setStatut(cible);
        if (commentaire != null && !commentaire.isBlank()) {
            rapport.setCommentaireEncadrant(commentaire.trim());
        }
        return RapportDto.of(rapportRepository.save(rapport));
    }

    private void remplir(RapportHebdomadaire rapport, RapportRequest request) {
        rapport.setTravauxRealises(request.travauxRealises().trim());
        rapport.setDifficultes(request.difficultes());
        rapport.setObjectifs(request.objectifs());
        rapport.setDateDepot(Instant.now(clock));
    }

    private RapportHebdomadaire findRapport(Long id) {
        return rapportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport", id));
    }

    static LocalDate lundiDe(LocalDate jour) {
        return jour.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
