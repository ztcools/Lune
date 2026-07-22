package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Record;
import com.lune.mapper.RecordMapper;
import com.lune.service.RecordService;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;

    public RecordServiceImpl(RecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Override
    public PageResult<Record> listRecords(int page, int size, Long categoryId) {
        var wrapper = new LambdaQueryWrapper<Record>()
                .orderByDesc(Record::getCreateTime);
        if (categoryId != null) {
            wrapper.eq(Record::getCategoryId, categoryId);
        }
        var result = recordMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Record createRecord(Record record) {
        record.setUserId(1L);
        recordMapper.insert(record);
        return record;
    }

    @Override
    public Record updateRecord(Long id, Record record) {
        var exist = recordMapper.selectById(id);
        if (exist == null) throw new BusinessException("记录不存在");
        exist.setCategoryId(record.getCategoryId());
        exist.setTitle(record.getTitle());
        exist.setContent(record.getContent());
        exist.setCover(record.getCover());
        recordMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deleteRecord(Long id) {
        recordMapper.deleteById(id);
    }
}
