package com.lune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lune.entity.VisitLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {

    /** 回收删除后释放的磁盘空间（InnoDB 下等价于 ALTER TABLE ... ENGINE=InnoDB） */
    @Update("OPTIMIZE TABLE visit_log")
    void optimize();
}
