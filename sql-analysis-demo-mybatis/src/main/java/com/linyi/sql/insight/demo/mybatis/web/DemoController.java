package com.linyi.sql.insight.demo.mybatis.web;

import com.linyi.sql.insight.demo.mybatis.mapper.UserMapper;
import com.linyi.sql.insight.demo.mybatis.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DemoController {
    private final UserMapper userMapper;
    public DemoController(UserMapper userMapper) { this.userMapper = userMapper; }

    @GetMapping("/u/{id}")
    public User get(@PathVariable Long id) { return userMapper.selectById(id); }

    @GetMapping("/u/search")
    public List<User> search(@RequestParam("q") String q) { return userMapper.search(q); }

    @GetMapping("/u/incr/{id}")
    public int incr(@PathVariable Long id) { return userMapper.incrAge(id); }
}


