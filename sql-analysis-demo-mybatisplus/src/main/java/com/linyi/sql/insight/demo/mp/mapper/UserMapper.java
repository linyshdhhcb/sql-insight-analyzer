package com.linyi.sql.insight.demo.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.sql.insight.demo.mp.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}


