package com.vala.gestionStagiaires.DTOs;

import com.vala.gestionStagiaires.enums.StageStatus;
import lombok.Data;

import java.util.Date;


@Data
public class StageDto {

    private Long id;

    private String sujet;

    private Date dateDebut;

    private Date dateFin;

    private StageStatus status;

    private EncadrantDto encadrant;


}
