package com.lune.controller.admin;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.Record;
import com.lune.service.RecordService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/records")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecordController {

    private final RecordService recordService;

    public AdminRecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public Result<PageResult<Record>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "12") int size) {
        return Result.success(recordService.listRecords(page, size, null));
    }

    @PostMapping
    public Result<Record> create(@RequestBody Record record) {
        return Result.success(recordService.createRecord(record));
    }

    @PutMapping("/{id}")
    public Result<Record> update(@PathVariable Long id, @RequestBody Record record) {
        return Result.success(recordService.updateRecord(id, record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return Result.success();
    }
}
