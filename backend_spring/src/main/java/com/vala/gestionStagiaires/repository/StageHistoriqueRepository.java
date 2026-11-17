package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.StageHistorique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StageHistoriqueRepository extends JpaRepository<StageHistorique, Long> {

    List<StageHistorique> findByStageIdOrderByDateChangementAscIdAsc(Long stageId);

    @Modifying
    @Query("DELETE FROM StageHistorique h WHERE h.stage.id = :stageId")
    void supprimerParStage(Long stageId);
}
