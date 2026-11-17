package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
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
        stage.setStatus(status);

        return stageMapper.fromStage(stageRepository.save(stage));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StageDto> searchStage(String keyword) {
        return stageRepository.findBySujetIgnoreCaseContaining(keyword).stream()
                .map(stageMapper::fromStage)
                .toList();
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
        stageRepository.deleteById(id);
    }

    @Override
    public StageDto updateStage(StageDto stageDto, Long id) {
        Stage existingStage = findStage(id);

        StageStatus cible = stageDto.getStatus() != null ? stageDto.getStatus() : existingStage.getStatus();
        verifierTransition(existingStage.getStatus(), cible);
        verifierCoherenceDates(cible, stageDto.getDateDebut(), stageDto.getDateFin());

        existingStage.setSujet(stageDto.getSujet());
        existingStage.setEncadrant(findEncadrant(stageDto.getEncadrant()));
        existingStage.setDateDebut(stageDto.getDateDebut());
        existingStage.setDateFin(stageDto.getDateFin());
        existingStage.setStatus(cible);
        return stageMapper.fromStage(stageRepository.save(existingStage));
    }

    @Override
    @Transactional(readOnly = true)
    public StageDto getStageById(Long id) {
        return stageMapper.fromStage(findStage(id));
    }

    @Override
    public StageDto changerStatut(Long id, StageStatus cible) {
        Stage stage = findStage(id);
        verifierTransition(stage.getStatus(), cible);
        verifierCoherenceDates(cible, stage.getDateDebut(), stage.getDateFin());
        stage.setStatus(cible);
        return stageMapper.fromStage(stageRepository.save(stage));
    }

    @Override
    public int mettreAJourStatutsAutomatiques() {
        LocalDate today = LocalDate.now(clock);
        List<Stage> modifies = new ArrayList<>();

        for (Stage stage : stageRepository.findByStatusAndDateDebutLessThanEqual(StageStatus.PLANIFIE, today)) {
            // Un stage planifié dont la fin est déjà passée est directement terminé.
            boolean finDepassee = stage.getDateFin() != null && stage.getDateFin().isBefore(today);
            stage.setStatus(finDepassee ? StageStatus.TERMINE : StageStatus.EN_COURS);
            modifies.add(stage);
        }
        for (Stage stage : stageRepository.findByStatusAndDateFinBefore(StageStatus.EN_COURS, today)) {
            stage.setStatus(StageStatus.TERMINE);
            modifies.add(stage);
        }

        stageRepository.saveAll(modifies);
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
