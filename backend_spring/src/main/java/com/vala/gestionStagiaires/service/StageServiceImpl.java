package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.DTOs.suivi.StageHistoriqueDto;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.config.ReglesMetierProperties;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.StageHistorique;
import com.vala.gestionStagiaires.enums.OrigineChangement;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.StageHistoriqueRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.specification.StageSpecifications;
import com.vala.gestionStagiaires.securite.SecuriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StageServiceImpl implements StageService {

    private final StageRepository stageRepository;
    private final EncadrantRepository encadrantRepository;
    private final StagiaireRepository stagiaireRepository;
    private final StageMapper stageMapper;
    private final Clock clock;
    private final ReglesMetierProperties regles;
    private final StageHistoriqueRepository historiqueRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StageDto> getStages() {
        return stageRepository.findAll().stream()
                .map(stageMapper::fromStage)
                .toList();
    }

    @Override
    public StageDto addStage(StageDto stageDto) {
        Stage stage = stageMapper.fromStageDto(stageDto);
        stage.setId(null);
        stage.setEncadrant(findEncadrant(stageDto.getEncadrant()));

        StageStatus status = stageDto.getStatus() != null ? stageDto.getStatus() : StageStatus.PLANIFIE;
        verifierCoherenceDates(status, stage.getDateDebut(), stage.getDateFin());
        verifierCapaciteEncadrant(stage.getEncadrant(), status, null);
        stage.setStatus(status);

        Stage saved = stageRepository.save(stage);
        tracer(saved, null, status, OrigineChangement.MANUEL);
        return stageMapper.fromStage(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StageDto> searchStage(String keyword) {
        return stageRepository.findBySujetIgnoreCaseContaining(keyword).stream()
                .map(stageMapper::fromStage)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StageDto> rechercherStages(StageFiltre filtre, Pageable pageable) {
        return PageResponse.of(stageRepository
                .findAll(StageSpecifications.avecFiltre(filtre), pageable)
                .map(stageMapper::fromStage));
    }

    @Override
    public void deleteStage(Long id) {
        if (!stageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stage", id);
        }
        if (stagiaireRepository.existsByStageId(id)) {
            throw new BusinessRuleException(
                    "Impossible de supprimer ce stage : des stagiaires y sont encore affectés.");
        }
        historiqueRepository.supprimerParStage(id);
        stageRepository.deleteById(id);
    }

    @Override
    public StageDto updateStage(StageDto stageDto, Long id) {
        Stage existingStage = findStage(id);
        StageStatus ancien = existingStage.getStatus();

        StageStatus cible = stageDto.getStatus() != null ? stageDto.getStatus() : existingStage.getStatus();
        verifierTransition(existingStage.getStatus(), cible);
        verifierCoherenceDates(cible, stageDto.getDateDebut(), stageDto.getDateFin());

        Encadrant encadrant = findEncadrant(stageDto.getEncadrant());
        verifierCapaciteEncadrant(encadrant, cible, id);

        existingStage.setSujet(stageDto.getSujet());
        existingStage.setEncadrant(encadrant);
        existingStage.setDateDebut(stageDto.getDateDebut());
        existingStage.setDateFin(stageDto.getDateFin());
        existingStage.setStatus(cible);
        Stage saved = stageRepository.save(existingStage);
        tracer(saved, ancien, cible, OrigineChangement.MANUEL);
        return stageMapper.fromStage(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StageDto getStageById(Long id) {
        return stageMapper.fromStage(findStage(id));
    }

    @Override
    public StageDto changerStatut(Long id, StageStatus cible) {
        Stage stage = findStage(id);
        StageStatus ancien = stage.getStatus();
        verifierTransition(ancien, cible);
        verifierCoherenceDates(cible, stage.getDateDebut(), stage.getDateFin());
        if (stage.getStatus() != cible) {
            verifierCapaciteEncadrant(stage.getEncadrant(), cible, id);
        }
        stage.setStatus(cible);
        Stage saved = stageRepository.save(stage);
        tracer(saved, ancien, cible, OrigineChangement.MANUEL);
        return stageMapper.fromStage(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StageHistoriqueDto> getHistorique(Long id) {
        if (!stageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stage", id);
        }
        return historiqueRepository.findByStageIdOrderByDateChangementAscIdAsc(id).stream()
                .map(StageHistoriqueDto::of)
                .toList();
    }

    @Override
    public int mettreAJourStatutsAutomatiques() {
        LocalDate today = LocalDate.now(clock);
        List<Stage> modifies = new ArrayList<>();
        List<StageHistorique> historiques = new ArrayList<>();

        for (Stage stage : stageRepository.findByStatusAndDateDebutLessThanEqual(StageStatus.PLANIFIE, today)) {
            // Un stage planifié dont la fin est déjà passée est directement terminé.
            boolean finDepassee = stage.getDateFin() != null && stage.getDateFin().isBefore(today);
            StageStatus cible = finDepassee ? StageStatus.TERMINE : StageStatus.EN_COURS;
            historiques.add(historique(stage, StageStatus.PLANIFIE, cible, OrigineChangement.AUTOMATIQUE));
            stage.setStatus(cible);
            modifies.add(stage);
        }
        for (Stage stage : stageRepository.findByStatusAndDateFinBefore(StageStatus.EN_COURS, today)) {
            historiques.add(historique(stage, StageStatus.EN_COURS, StageStatus.TERMINE, OrigineChangement.AUTOMATIQUE));
            stage.setStatus(StageStatus.TERMINE);
            modifies.add(stage);
        }

        stageRepository.saveAll(modifies);
        historiqueRepository.saveAll(historiques);
        return modifies.size();
    }

    // ------------------------------------------------------------------
    // Règles métier
    // ------------------------------------------------------------------

    private void verifierTransition(StageStatus actuel, StageStatus cible) {
        if (actuel == null || actuel == cible) {
            return;
        }
        if (!actuel.canTransitionTo(cible)) {
            throw new BusinessRuleException(
                    "Transition de statut interdite : " + actuel + " → " + cible + ".");
        }
    }

    private void verifierCoherenceDates(StageStatus status, LocalDate dateDebut, LocalDate dateFin) {
        LocalDate today = LocalDate.now(clock);
        if (status == StageStatus.EN_COURS && dateDebut != null && dateDebut.isAfter(today)) {
            throw new BusinessRuleException(
                    "Un stage ne peut pas être en cours avant sa date de début.");
        }
        if (status == StageStatus.TERMINE && dateFin != null && dateFin.isAfter(today)) {
            throw new BusinessRuleException(
                    "Un stage ne peut pas être terminé avant sa date de fin.");
        }
    }

    /**
     * Limite le nombre de stages EN_COURS par encadrant (si configuré).
     * La mise à jour automatique nocturne n'applique pas cette limite : elle
     * ne fait que constater l'avancement du calendrier.
     */
    private void verifierCapaciteEncadrant(Encadrant encadrant, StageStatus status, Long stageId) {
        int max = regles.maxStagesEnCoursParEncadrant();
        if (max <= 0 || status != StageStatus.EN_COURS || encadrant == null || encadrant.getId() == null) {
            return;
        }
        long enCours = stageId == null
                ? stageRepository.countByEncadrantIdAndStatus(encadrant.getId(), StageStatus.EN_COURS)
                : stageRepository.countByEncadrantIdAndStatusAndIdNot(encadrant.getId(), StageStatus.EN_COURS, stageId);
        if (enCours >= max) {
            throw new BusinessRuleException("Cet encadrant suit déjà " + enCours
                    + " stage(s) en cours (maximum : " + max + ").");
        }
    }

    // ------------------------------------------------------------------
    // Historique
    // ------------------------------------------------------------------

    /** Enregistre le changement de statut s'il y en a un. */
    private void tracer(Stage stage, StageStatus ancien, StageStatus nouveau, OrigineChangement origine) {
        if (ancien != nouveau) {
            historiqueRepository.save(historique(stage, ancien, nouveau, origine));
        }
    }

    private StageHistorique historique(Stage stage, StageStatus ancien, StageStatus nouveau, OrigineChangement origine) {
        return StageHistorique.builder()
                .stage(stage)
                .ancienStatut(ancien)
                .nouveauStatut(nouveau)
                .dateChangement(Instant.now(clock))
                .origine(origine)
                .auteur(origine == OrigineChangement.MANUEL ? SecuriteService.utilisateurCourant() : null)
                .build();
    }

    private Stage findStage(Long id) {
        return stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage", id));
    }

    /** Charge l'encadrant existant en base au lieu de le reconstruire depuis le JSON reçu. */
    private Encadrant findEncadrant(EncadrantDto encadrantDto) {
        if (encadrantDto == null || encadrantDto.getId() == null) {
            throw new BusinessRuleException("Le stage doit être rattaché à un encadrant existant.");
        }
        Long encadrantId = encadrantDto.getId();
        return encadrantRepository.findById(encadrantId)
                .orElseThrow(() -> new ResourceNotFoundException("Encadrant", encadrantId));
    }
}
