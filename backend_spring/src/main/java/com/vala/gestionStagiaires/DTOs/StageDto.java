package com.vala.gestionStagiaires.DTOs;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vala.gestionStagiaires.enums.StageStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;


@Data
public class StageDto {

    private Long id;

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(max = 100, message = "Le sujet ne doit pas dépasser 100 caractères")
    private String sujet;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    private StageStatus status;

    @NotNull(message = "L'encadrant est obligatoire")
    private EncadrantDto encadrant;

    @JsonIgnore
    @AssertTrue(message = "La date de début doit être antérieure à la date de fin")
    public boolean isDateDebutBeforeDateFin() {
        if (dateDebut == null || dateFin == null) return true;
        return dateDebut.isBefore(dateFin);
    }

}
