package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.PresenceDto;
import com.vala.gestionStagiaires.DTOs.suivi.PresenceRequest;

import java.time.LocalDate;
import java.util.List;

public interface PresenceService {

    /** Crée ou remplace la présence du stagiaire à cette date. */
    PresenceDto enregistrer(Long stagiaireId, LocalDate date, PresenceRequest request);

    /** Présences entre {@code du} et {@code au} inclus (par défaut : toute la période du stage). */
    List<PresenceDto> lister(Long stagiaireId, LocalDate du, LocalDate au);

    void supprimer(Long stagiaireId, LocalDate date);
}
