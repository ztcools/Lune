package com.lune.controller;

import com.lune.common.Result;
import com.lune.entity.Family;
import com.lune.service.FamilyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping
    public Result<List<Family>> list() {
        return Result.success(familyService.listFamilies());
    }
}
