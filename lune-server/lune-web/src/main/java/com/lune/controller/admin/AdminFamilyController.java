package com.lune.controller.admin;

import com.lune.common.Result;
import com.lune.entity.Family;
import com.lune.service.FamilyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/family")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFamilyController {

    private final FamilyService familyService;

    public AdminFamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping
    public Result<List<Family>> list() {
        return Result.success(familyService.listFamilies());
    }

    @PostMapping
    public Result<Family> create(@RequestBody Family family) {
        return Result.success(familyService.createFamily(family));
    }

    @PutMapping("/{id}")
    public Result<Family> update(@PathVariable Long id, @RequestBody Family family) {
        return Result.success(familyService.updateFamily(id, family));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        familyService.deleteFamily(id);
        return Result.success();
    }
}
