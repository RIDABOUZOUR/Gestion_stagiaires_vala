package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.Mappers.DepartementMapper;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartementServiceImpl")
class DepartementServiceImplTest {

    @Mock
    private DepartementRepository departementRepository;

    @Mock
    private StagiaireRepository stagiaireRepository;

    @Mock
    private com.vala.gestionStagiaires.repository.CandidatureRepository candidatureRepository;

    @Mock
    private DepartementMapper departementMapper;

    @InjectMocks
    private DepartementServiceImpl departementService;

    private DepartementDto dto(Long id, String nom, String responsable) {
        DepartementDto d = new DepartementDto();
        d.setId(id);
        d.setNom(nom);
        d.setResponsable(responsable);
        return d;
    }

    private Departement entity(Long id, String nom, String responsable) {
        return Departement.builder().id(id).nom(nom).responsable(responsable).build();
    }

    @Test
    @DisplayName("getDepartement() retourne la liste mappée")
    void getDepartement_shouldReturnMappedList() {
        Departement d1 = entity(1L, "Informatique", "M. Dupont");
        Departement d2 = entity(2L, "RH", "Mme Durand");
        DepartementDto dto1 = dto(1L, "Informatique", "M. Dupont");
        DepartementDto dto2 = dto(2L, "RH", "Mme Durand");
        when(departementRepository.findAll()).thenReturn(List.of(d1, d2));
        when(departementMapper.fromDepartement(d1)).thenReturn(dto1);
        when(departementMapper.fromDepartement(d2)).thenReturn(dto2);

        assertThat(departementService.getDepartement()).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("getDepartement() retourne une liste vide")
    void getDepartement_shouldReturnEmptyList() {
        when(departementRepository.findAll()).thenReturn(List.of());

        assertThat(departementService.getDepartement()).isEmpty();
    }

    @Test
    @DisplayName("addDepartement() mappe, sauvegarde et retourne le DTO")
    void addDepartement_shouldSaveAndReturnDto() {
        DepartementDto input = dto(null, "Informatique", "M. Dupont");
        Departement toSave = entity(null, "Informatique", "M. Dupont");
        Departement saved = entity(1L, "Informatique", "M. Dupont");
        DepartementDto output = dto(1L, "Informatique", "M. Dupont");
        when(departementMapper.fromDepartementDto(input)).thenReturn(toSave);
        when(departementRepository.save(toSave)).thenReturn(saved);
        when(departementMapper.fromDepartement(saved)).thenReturn(output);

        assertThat(departementService.addDepartement(input)).isEqualTo(output);
        verify(departementRepository).save(toSave);
    }

    @Test
    @DisplayName("addDepartement() ignore un id fourni : crée, n'écrase jamais un département existant")
    void addDepartement_shouldIgnoreProvidedId() {
        DepartementDto input = dto(1L, "Finance", "Mme Martin");
        Departement toSave = entity(1L, "Finance", "Mme Martin");
        when(departementMapper.fromDepartementDto(input)).thenReturn(toSave);
        when(departementRepository.save(toSave)).thenReturn(toSave);

        departementService.addDepartement(input);

        assertThat(toSave.getId()).isNull();
    }

    @Test
    @DisplayName("searchDepartement() filtre par nom")
    void searchDepartement_shouldReturnMappedList() {
        Departement d1 = entity(1L, "Informatique", "M. Dupont");
        DepartementDto dto1 = dto(1L, "Informatique", "M. Dupont");
        when(departementRepository.findByNomIgnoreCaseContaining("info")).thenReturn(List.of(d1));
        when(departementMapper.fromDepartement(d1)).thenReturn(dto1);

        assertThat(departementService.searchDepartement("info")).containsExactly(dto1);
    }

    @Test
    @DisplayName("deleteDepartement() supprime si rien n'y est rattaché")
    void deleteDepartement_shouldDeleteWhenUnused() {
        when(departementRepository.existsById(5L)).thenReturn(true);
        when(stagiaireRepository.existsByDepartementId(5L)).thenReturn(false);

        departementService.deleteDepartement(5L);

        verify(candidatureRepository).detacherDepartement(5L);
        verify(departementRepository).deleteById(5L);
    }

    @Test
    @DisplayName("deleteDepartement() lève ResourceNotFoundException si absent")
    void deleteDepartement_shouldThrowWhenNotFound() {
        when(departementRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> departementService.deleteDepartement(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(departementRepository, never()).deleteById(99L);
    }

    @Test
    @DisplayName("deleteDepartement() refuse la suppression si des éléments y sont rattachés")
    void deleteDepartement_shouldRefuseWhenStillReferenced() {
        when(departementRepository.existsById(5L)).thenReturn(true);
        when(stagiaireRepository.existsByDepartementId(5L)).thenReturn(true);

        assertThatThrownBy(() -> departementService.deleteDepartement(5L))
                .isInstanceOf(BusinessRuleException.class);
        verify(departementRepository, never()).deleteById(5L);
    }

    @Test
    @DisplayName("updateDepartement() met à jour l'entité existante")
    void updateDepartement_shouldUpdateExistingEntity() {
        DepartementDto input = dto(null, "Nouveau", "M. X");
        Departement existing = entity(1L, "Ancien", "M. Y");
        Departement saved = entity(1L, "Nouveau", "M. X");
        DepartementDto output = dto(1L, "Nouveau", "M. X");
        when(departementRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departementRepository.save(existing)).thenReturn(saved);
        when(departementMapper.fromDepartement(saved)).thenReturn(output);

        assertThat(departementService.updateDepartement(input, 1L)).isEqualTo(output);
        assertThat(existing.getNom()).isEqualTo("Nouveau");
        assertThat(existing.getResponsable()).isEqualTo("M. X");
    }

    @Test
    @DisplayName("updateDepartement() lève ResourceNotFoundException si absent")
    void updateDepartement_shouldThrowWhenNotFound() {
        DepartementDto input = dto(null, "Nouveau", "M. X");
        when(departementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departementService.updateDepartement(input, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getDepartementById() retourne le DTO")
    void getDepartementById_shouldReturnDto() {
        Departement d = entity(1L, "Informatique", "M. Dupont");
        DepartementDto dto = dto(1L, "Informatique", "M. Dupont");
        when(departementRepository.findById(1L)).thenReturn(Optional.of(d));
        when(departementMapper.fromDepartement(d)).thenReturn(dto);

        assertThat(departementService.getDepartementById(1L)).isEqualTo(dto);
    }

    @Test
    @DisplayName("getDepartementById() lève ResourceNotFoundException si absent")
    void getDepartementById_shouldThrowWhenNotFound() {
        when(departementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departementService.getDepartementById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
