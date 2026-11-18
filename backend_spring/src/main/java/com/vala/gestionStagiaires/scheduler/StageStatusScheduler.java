package com.vala.gestionStagiaires.scheduler;

import com.vala.gestionStagiaires.service.StageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tâche quotidienne : fait avancer les stages selon leurs dates
 * (PLANIFIE → EN_COURS à la date de début, EN_COURS → TERMINE après la date de fin).
 */
@Component
@RequiredArgsConstructor
public class StageStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(StageStatusScheduler.class);

    private final StageService stageService;

    @Value("${app.scheduling.enabled:true}")
    private boolean planificationActive;

    /** Rattrapage au démarrage : si l'application était arrêtée à l'heure du cron, les statuts restent à jour. */
    @EventListener(ApplicationReadyEvent.class)
    public void auDemarrage() {
        if (planificationActive) {
            mettreAJourStatuts();
        }
    }

    @Scheduled(cron = "${app.stages.status-cron:0 5 0 * * *}", zone = "${app.timezone:Africa/Casablanca}")
    public void mettreAJourStatuts() {
        int modifies = stageService.mettreAJourStatutsAutomatiques();
        log.info("Mise à jour automatique des statuts : {} stage(s) modifié(s)", modifies);
    }
}
