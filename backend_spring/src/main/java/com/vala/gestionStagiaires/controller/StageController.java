package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.service.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin("*")
@RestController
public class StageController {
    @Autowired
    private StageService stageService;

    @GetMapping("/stages")
    public List<StageDto> getStages(){
        return stageService.getStages();
    }

    @PostMapping("/stages")
    public Stage addStage(@RequestBody StageDto stageDto){
       return stageService.addStage(stageDto);
    }

    @GetMapping("/stages/search")
    public List<StageDto> searchStage(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return stageService.searchStage(keyword);
    }

    @DeleteMapping("/stages/{id}")
    public void deleteStagiaire(@PathVariable Long id) {
        stageService.deleteStage(id);
    }

    @PutMapping("/stages/{id}")
    public Stage updateEncadrant(@RequestBody StageDto stageDto , @PathVariable Long id){
        return stageService.updateStage(stageDto,id);
    }

    @GetMapping("/stages/{id}")
    public StageDto getStageById(@PathVariable Long id){
        return stageService.getStageById(id);
    }


}
