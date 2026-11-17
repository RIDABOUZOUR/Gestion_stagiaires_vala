package com.vala.gestionStagiaires.DTOs;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Page de résultats renvoyée par les endpoints paginés.
 * Format stable, indépendant de la sérialisation interne de Spring Data.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
