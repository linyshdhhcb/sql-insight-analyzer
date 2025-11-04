package com.linyi.sql.insight.demo.mp.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linyi.sql.insight.demo.mp.mapper.UserMapper;
import com.linyi.sql.insight.demo.mp.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DemoController {
    private final UserMapper userMapper;
    public DemoController(UserMapper userMapper) { this.userMapper = userMapper; }

    @GetMapping("/mp/u/{id}")
    public User get(@PathVariable Long id) { return userMapper.selectById(id); }

    @GetMapping("/mp/u/search")
    public List<User> search(@RequestParam("q") String q) {
        return userMapper.selectList(new QueryWrapper<User>().like("name", q).orderByDesc("id").last("limit 50"));
    }
}


