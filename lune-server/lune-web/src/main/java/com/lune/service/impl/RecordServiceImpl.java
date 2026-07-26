package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Record;
import com.lune.mapper.RecordMapper;
import com.lune.mapper.UserMapper;
import com.lune.service.RecordService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final UserMapper userMapper;

    public RecordServiceImpl(RecordMapper recordMapper, UserMapper userMapper) {
        this.recordMapper = recordMapper;
        this.userMapper = userMapper;
    }

    private void populateUserInfo(java.util.List<Record> list) {
        Set<Long> userIds = list.stream()
                .map(Record::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        var users = userMapper.selectBatchIds(userIds);
        Map<Long, com.lune.entity.User> userMap = users.stream()
                .collect(Collectors.toMap(com.lune.entity.User::getId, Function.identity()));
        for (Record r : list) {
            var u = userMap.get(r.getUserId());
            if (u != null) {
                r.setUsername(u.getUsername());
                r.setNickname(u.getNickname());
                r.setAvatar(u.getAvatar());
            }
        }
    }

    @Override
    public PageResult<Record> listRecords(int page, int size, Long categoryId) {
        var wrapper = new LambdaQueryWrapper<Record>()
                .orderByDesc(Record::getCreateTime);
        if (categoryId != null) {
            wrapper.eq(Record::getCategoryId, categoryId);
        }
        var result = recordMapper.selectPage(new Page<>(page, size), wrapper);
        populateUserInfo(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Record createRecord(Record record) {
        record.setUserId(1L);
        recordMapper.insert(record);
        var user = userMapper.selectById(1L);
        if (user != null) {
            record.setUsername(user.getUsername());
            record.setNickname(user.getNickname());
            record.setAvatar(user.getAvatar());
        }
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
        exist.setMedia(record.getMedia());
        recordMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deleteRecord(Long id) {
        recordMapper.deleteById(id);
    }
}
