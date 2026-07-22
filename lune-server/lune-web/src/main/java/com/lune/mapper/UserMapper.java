package com.lune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lune.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
