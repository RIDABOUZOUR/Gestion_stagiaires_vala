package com.vala.gestionStagiaires.DTOs;

import com.vala.gestionStagiaires.enums.StageStaus;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class StageDto {

    private Long id;

    private String sujet;

    private Date dateDebut;

    private Date dateFin;

    private StageStaus status;

    private EncadrantDto encadrant;


}
