package com.vala.gestionStagiaires.enums;

/** Qui a déclenché un changement de statut de stage. */
public enum OrigineChangement {
    /** Action d'un utilisateur (création, modification, démarrer/terminer/annuler). */
    MANUEL,
    /** Tâche planifiée quotidienne (avancement selon les dates). */
    AUTOMATIQUE
}
