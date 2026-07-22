package com.lune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lune.entity.Record;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {
}
