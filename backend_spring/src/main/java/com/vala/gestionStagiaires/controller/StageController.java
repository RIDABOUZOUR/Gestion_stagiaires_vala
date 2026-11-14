package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.service.StageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StageController {
    @Autowired
    private StageService stageService;

    @GetMapping("/stages")
    public ResponseEntity<List<StageDto>> getStages(){
        return ResponseEntity.ok(stageService.getStages());
    }

    @PostMapping("/stages")
    public ResponseEntity<StageDto> addStage(@Valid @RequestBody StageDto stageDto){
        StageDto created = stageService.addStage(stageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/stages/search")
    public ResponseEntity<List<StageDto>> searchStage(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return ResponseEntity.ok(stageService.searchStage(keyword));
    }

    @DeleteMapping("/stages/{id}")
    public ResponseEntity<Void> deleteStagiaire(@PathVariable Long id) {
        stageService.deleteStage(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stages/{id}")
    public ResponseEntity<StageDto> updateEncadrant(@Valid @RequestBody StageDto stageDto, @PathVariable Long id){
        return ResponseEntity.ok(stageService.updateStage(stageDto,id));
    }

    @GetMapping("/stages/{id}")
    public ResponseEntity<StageDto> getStageById(@PathVariable Long id){
        return ResponseEntity.ok(stageService.getStageById(id));
    }


}
