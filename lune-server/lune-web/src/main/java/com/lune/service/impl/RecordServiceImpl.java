package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Record;
import com.lune.mapper.RecordMapper;
import com.lune.security.SecurityUtils;
import com.lune.service.RecordService;
import com.lune.service.support.UserInfoFiller;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final UserInfoFiller userInfoFiller;

    public RecordServiceImpl(RecordMapper recordMapper, UserInfoFiller userInfoFiller) {
        this.recordMapper = recordMapper;
        this.userInfoFiller = userInfoFiller;
    }

    @Override
    public PageResult<Record> listRecords(int page, int size, Long categoryId) {
        var wrapper = new LambdaQueryWrapper<Record>()
                .orderByDesc(Record::getCreateTime);
        if (categoryId != null) {
            wrapper.eq(Record::getCategoryId, categoryId);
        }
        var result = recordMapper.selectPage(new Page<>(page, size), wrapper);
        userInfoFiller.fill(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Record createRecord(Record record) {
        record.setUserId(SecurityUtils.getCurrentUserId());
        recordMapper.insert(record);
        userInfoFiller.fillOne(record);
        return record;
    }

    @Override
    public Record updateRecord(Long id, Record record) {
        var exist = recordMapper.selectById(id);
        if (exist == null) throw new BusinessException(404, "记录不存在");
        exist.setCategoryId(record.getCategoryId());
        exist.setTitle(record.getTitle());
        exist.setContent(record.getContent());
        exist.setCover(record.getCover());
        exist.setMedia(record.getMedia());
        recordMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deleteRecord(Long id) {
        recordMapper.deleteById(id);
    }
}
