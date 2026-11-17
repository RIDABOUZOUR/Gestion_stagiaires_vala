package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.PresenceDto;
import com.vala.gestionStagiaires.DTOs.suivi.PresenceRequest;
import com.vala.gestionStagiaires.entities.Presence;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.PresenceRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

    private final PresenceRepository presenceRepository;
    private final StagiaireRepository stagiaireRepository;
    private final Clock clock;

    @Override
    public PresenceDto enregistrer(Long stagiaireId, LocalDate date, PresenceRequest request) {
        Stagiaire stagiaire = SuiviSupport.findStagiaire(stagiaireRepository, stagiaireId);
        Stage stage = SuiviSupport.stageSuivable(stagiaire);

        if (date.isBefore(stage.getDateDebut()) || date.isAfter(stage.getDateFin())) {
            throw new BusinessRuleException("La date " + date + " est en dehors de la période du stage (du "
                    + stage.getDateDebut() + " au " + stage.getDateFin() + ").");
        }
        if (date.isAfter(LocalDate.now(clock))) {
            throw new BusinessRuleException("Impossible de saisir une présence pour une date future.");
        }

        Presence presence = presenceRepository.findByStagiaireIdAndDate(stagiaireId, date)
                .orElseGet(() -> Presence.builder().stagiaire(stagiaire).date(date).build());
        presence.setStatut(request.statut());
        presence.setCommentaire(request.commentaire());
        return PresenceDto.of(presenceRepository.save(presence));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresenceDto> lister(Long stagiaireId, LocalDate du, LocalDate au) {
        Stagiaire stagiaire = SuiviSupport.findStagiaire(stagiaireRepository, stagiaireId);
        Stage stage = stagiaire.getStage();
        LocalDate debut = du != null ? du : (stage != null ? stage.getDateDebut() : null);
        LocalDate fin = au != null ? au : (stage != null ? stage.getDateFin() : null);
        if (debut == null || fin == null || debut.isAfter(fin)) {
            return List.of();
        }
        return presenceRepository.findByStagiaireIdAndDateBetweenOrderByDateAsc(stagiaireId, debut, fin).stream()
                .map(PresenceDto::of)
                .toList();
    }

    @Override
    public void supprimer(Long stagiaireId, LocalDate date) {
        Presence presence = presenceRepository.findByStagiaireIdAndDate(stagiaireId, date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune présence saisie le " + date + " pour le stagiaire " + stagiaireId + "."));
        presenceRepository.delete(presence);
    }
}
