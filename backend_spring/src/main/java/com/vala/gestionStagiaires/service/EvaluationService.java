package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationDto;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationRequest;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationStatistiques;

public interface EvaluationService {

    /** Crée ou met à jour l'évaluation du stagiaire (stage TERMINE uniquement). */
    EvaluationDto enregistrer(Long stagiaireId, EvaluationRequest request);

    EvaluationDto getEvaluation(Long stagiaireId);

    EvaluationStatistiques statistiques();
}
