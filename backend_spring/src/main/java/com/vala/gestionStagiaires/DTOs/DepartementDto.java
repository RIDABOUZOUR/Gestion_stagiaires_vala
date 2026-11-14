package com.vala.gestionStagiaires.DTOs;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartementDto {
    private Long id;

    @Size(min = 3, max = 40, message = "Le nom ne doit pas dépasser 40 caractères")
    private String nom;

    @Size(min = 3, max = 40, message = "Le nom du responsable ne doit pas dépasser 40 caractères")
    private String responsable;

}
