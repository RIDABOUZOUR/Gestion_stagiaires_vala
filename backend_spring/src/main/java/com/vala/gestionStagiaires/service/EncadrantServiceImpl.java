package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.Mappers.EncadrantMapper;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class EncadrantServiceImpl implements EncadrantService {
    private final EncadrantRepository encadrantRepository;
    private final StageRepository stageRepository;
    private final EncadrantMapper encadrantMapper;
    @Override
    @Transactional(readOnly = true)
    public List<EncadrantDto> getEncadrant() {
        return encadrantRepository.findAll().stream()
                .map(encadrantMapper::fromEncadrant)
                .toList();
    }

    @Override
    public EncadrantDto addEncadrant(EncadrantDto encadrantDto) {
        verifierEmailDisponible(encadrantDto.getEmail(), null);
        Encadrant encadrant = encadrantMapper.fromEncadrantDto(encadrantDto);
        encadrant.setId(null);
        return encadrantMapper.fromEncadrant(encadrantRepository.save(encadrant));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EncadrantDto> searchEncadrant(String keyword) {
        return encadrantRepository.findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(keyword,keyword).stream()
                .map(encadrantMapper::fromEncadrant)
                .toList();
    }

    @Override
    public void deleteEncadrant(Long id) {
        if (!encadrantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Encadrant", id);
        }
        if (stageRepository.existsByEncadrantId(id)) {
            throw new BusinessRuleException("Impossible de supprimer cet encadrant : il encadre encore des stages.");
        }
        encadrantRepository.deleteById(id);
    }

    @Override
    public EncadrantDto updateEncadrant(EncadrantDto encadrantDto, Long id) {
        Encadrant existingEncadrant = encadrantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encadrant", id));
        verifierEmailDisponible(encadrantDto.getEmail(), id);
        existingEncadrant.setNom(encadrantDto.getNom());
        existingEncadrant.setPrenom(encadrantDto.getPrenom());
        existingEncadrant.setTelephone(encadrantDto.getTelephone());
        existingEncadrant.setEmail(encadrantDto.getEmail());
        return encadrantMapper.fromEncadrant(encadrantRepository.save(existingEncadrant));
    }

    @Override
    @Transactional(readOnly = true)
    public EncadrantDto getEncadrantById(Long id) {
        return encadrantMapper.fromEncadrant(encadrantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encadrant", id)));
    }

    /** Deux encadrants ne peuvent pas partager le même email (insensible à la casse). */
    private void verifierEmailDisponible(String email, Long idCourant) {
        if (email == null || email.isBlank()) {
            return;
        }
        boolean pris = idCourant == null
                ? encadrantRepository.existsByEmailIgnoreCase(email)
                : encadrantRepository.existsByEmailIgnoreCaseAndIdNot(email, idCourant);
        if (pris) {
            throw new BusinessRuleException("Un encadrant utilise déjà l'email " + email + ".");
        }
    }
}
