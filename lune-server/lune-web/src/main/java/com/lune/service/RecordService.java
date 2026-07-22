package com.lune.service;

import com.lune.common.PageResult;
import com.lune.entity.Record;

public interface RecordService {
    PageResult<Record> listRecords(int page, int size, Long categoryId);
    Record createRecord(Record record);
    Record updateRecord(Long id, Record record);
    void deleteRecord(Long id);
}
