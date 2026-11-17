package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.repository.StagiaireRepository;

/** Contrôles communs au suivi (présences, rapports) d'un stagiaire. */
final class SuiviSupport {

    private SuiviSupport() {
    }

    static Stagiaire findStagiaire(StagiaireRepository repository, Long stagiaireId) {
        return repository.findById(stagiaireId)
                .orElseThrow(() -> new StagiaireNotFoundException(stagiaireId));
    }

    /** Le stage du stagiaire, s'il a des dates et n'est pas annulé. */
    static Stage stageSuivable(Stagiaire stagiaire) {
        Stage stage = stagiaire.getStage();
        if (stage == null || stage.getDateDebut() == null || stage.getDateFin() == null) {
            throw new BusinessRuleException("Le stagiaire n'est rattaché à aucun stage daté.");
        }
        if (stage.getStatus() == StageStatus.ANNULE) {
            throw new BusinessRuleException("Le stage de ce stagiaire a été annulé : aucun suivi n'est possible.");
        }
        return stage;
    }
}
