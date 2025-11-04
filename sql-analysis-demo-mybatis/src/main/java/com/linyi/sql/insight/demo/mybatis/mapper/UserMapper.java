package com.linyi.sql.insight.demo.mybatis.mapper;

import com.linyi.sql.insight.demo.mybatis.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select id, name, age from users where id = #{id}")
    User selectById(@Param("id") Long id);

    @Select("select id, name, age from users where name like concat('%', #{kw}, '%') order by id desc limit 50")
    List<User> search(@Param("kw") String keyword);

    @Update("update users set age = age + 1 where id = #{id}")
    int incrAge(@Param("id") Long id);
}


