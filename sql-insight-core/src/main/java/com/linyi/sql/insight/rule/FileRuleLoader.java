/*
 * Copyright (c) 2025 Lin Yi (linyi)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.linyi.sql.insight.rule;

import com.linyi.sql.insight.model.SqlScoreRule;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 从文件/类路径加载规则（properties 格式）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class FileRuleLoader extends RuleLoader {

    // 支持 classpath: 前缀或绝对/相对路径
    private final String location;

    public FileRuleLoader(String location) {
        this.location = location;
    }

    /**
     * 加载SQL评分规则列表
     *
     * <p>
     * 该方法根据配置的位置信息加载规则配置文件，支持classpath和文件系统两种方式。
     * 首先尝试从classpath加载，如果失败则尝试从文件系统加载。
     * 加载成功后解析配置属性为规则对象列表，如果解析结果为空则返回父类的默认规则。
     * </p>
     *
     * @return 规则列表，如果加载或解析失败则返回父类的默认规则列表
     */
    @Override
    public List<SqlScoreRule> loadRules() {
        Properties props = new Properties();
        // 根据位置信息加载配置文件
        if (location != null && location.startsWith("classpath:")) {
            String path = location.substring("classpath:".length());
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is != null)
                    props.load(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception ignore) {
            }
        } else {
            try (InputStream is = Files.newInputStream(Paths.get(location))) {
                props.load(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception ignore) {
            }
        }
        // 解析规则配置并返回，空规则时使用默认规则
        List<SqlScoreRule> list = parseRules(props);
        return list.isEmpty() ? super.loadRules() : list; // 空规则保护
    }

    /**
     * 解析配置属性生成SQL评分规则列表
     *
     * @param props 配置属性对象，包含规则配置信息
     * @return 解析后的SQL评分规则列表，如果输入为null则返回空列表
     */
    static List<SqlScoreRule> parseRules(Properties props) {
        if (props == null)
            return Collections.emptyList();

        // 使用Map存储规则，以规则ID为键避免重复
        Map<String, SqlScoreRule> map = new HashMap<>();
        for (String name : props.stringPropertyNames()) {
            // 只处理以"rule."开头的配置项
            if (!name.startsWith("rule."))
                continue;
            String[] arr = name.split("\\.");
            // 配置项格式应为 rule.{id}.{field}
            if (arr.length < 3)
                continue;
            String id = arr[1];
            String field = arr[2];
            // 获取或创建规则对象
            SqlScoreRule rule = map.computeIfAbsent(id, k -> new SqlScoreRule());
            rule.setId(id);
            String value = props.getProperty(name);
            // 根据字段名设置规则属性
            switch (field) {
                case "condition":
                    rule.setCondition(value);
                    break;
                case "score":
                    rule.setScore(parseInt(value, 0));
                    break;
                case "level":
                    try {
                        rule.setLevel(com.linyi.sql.insight.model.AnalysisLevel
                                .valueOf(value.trim().toUpperCase(Locale.ROOT)));
                    } catch (Exception ignore) {
                        // 无效等级，留空以便后续校验过滤
                    }
                    break;
                case "reason":
                    rule.setReason(value);
                    break;
                case "priority":
                    rule.setPriority(parseInt(value, 100));
                    break;
                default:
                    break;
            }
        }

        // 语法校验：必填字段
        List<SqlScoreRule> rules = new ArrayList<>();
        for (SqlScoreRule r : map.values()) {
            // 只保留包含必需字段的规则
            if (r.getId() != null && r.getCondition() != null && r.getLevel() != null) {
                rules.add(r);
            }
        }
        // 按优先级排序
        rules.sort(Comparator.comparingInt(SqlScoreRule::getPriority));
        return rules;
    }

    /**
     * 解析字符串为整数，如果解析失败则返回默认值
     *
     * @param s   待解析的字符串
     * @param def 解析失败时返回的默认值
     * @return 解析成功的整数或默认值
     */
    private static int parseInt(String s, int def) {
        try {
            // 尝试将字符串解析为整数
            return Integer.parseInt(s);
        } catch (Exception e) {
            // 解析失败时返回默认值
            return def;
        }
    }

}
