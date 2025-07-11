package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
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

}
