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

import com.linyi.sql.insight.model.SqlAnalysisResult;
import com.linyi.sql.insight.model.SqlAnalysisResultList;
import com.linyi.sql.insight.model.SqlScoreResultDetail;
import com.linyi.sql.insight.model.SqlScoreRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 简化规则引擎：仅支持条件表达式：
 * - type == "..."
 * - extra contains "..."
 * - rows >/>=/</<= N
 * - filtered >/>=/</<= X
 * 并支持 AND/OR 组合（无括号，OR 优先按左到右顶层拆分）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class DefaultRuleEngine implements RuleEngine {

    /**
     * 评估SQL分析结果并根据规则生成评分详情列表
     *
     * @param plan  SQL分析结果列表，包含待评估的SQL分析结果
     * @param rules SQL评分规则列表，用于评估SQL分析结果
     * @return SQL评分结果详情列表，如果输入参数为空或规则列表为空则返回空列表
     */
    @Override
    public List<SqlScoreResultDetail> evaluate(SqlAnalysisResultList plan, List<SqlScoreRule> rules) {
        if (plan == null || rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }
        // 按优先级排序规则列表
        List<SqlScoreRule> sorted = new ArrayList<>(rules);
        sorted.sort(Comparator.comparingInt(SqlScoreRule::getPriority));
        List<SqlScoreResultDetail> details = new ArrayList<>();
        // 遍历所有分析结果，按规则进行匹配和评分
        for (SqlAnalysisResult r : plan.getResults()) {
            for (SqlScoreRule rule : sorted) {
                if (matchExpr(r, rule.getCondition())) {
                    SqlScoreResultDetail d = new SqlScoreResultDetail();
                    d.setRuleId(rule.getId());
                    d.setScore(rule.getScore());
                    d.setLevel(rule.getLevel());
                    d.setReason(rule.getReason());
                    details.add(d);
                }
            }
        }
        return details;
    }

    /**
     * 匹配SQL表达式
     *
     * @param r    SQL分析结果对象，用于获取匹配所需的数据上下文
     * @param expr 待匹配的表达式字符串，可能包含逻辑运算符和括号
     * @return 如果表达式匹配成功返回true，否则返回false
     */
    private boolean matchExpr(SqlAnalysisResult r, String expr) {
        if (expr == null)
            return false;
        String e = expr.trim();
        // 去除外围括号
        while (e.startsWith("(") && e.endsWith(")") && isBalancedOuter(e)) {
            e = e.substring(1, e.length() - 1).trim();
        }
        // 顶层 OR 拆分
        List<String> orParts = splitTopLevel(e, "||");
        for (String orPart : orParts) {
            List<String> andParts = splitTopLevel(orPart, "&&");
            boolean all = true;
            for (String andPart : andParts) {
                String c = andPart.trim();
                if (c.isEmpty())
                    continue;
                // 判断是否需要递归处理表达式或进行原子匹配
                if (!(c.contains("||") || c.contains("&&") || (c.startsWith("(") && c.endsWith(")")))) {
                    if (!match(r, c)) {
                        all = false;
                        break;
                    }
                } else {
                    if (!matchExpr(r, c)) {
                        all = false;
                        break;
                    }
                }
            }
            // 如果当前OR分支的所有AND条件都满足，则返回true
            if (all)
                return true;
        }
        return false;
    }

    /**
     * 检查字符串中的括号是否平衡且满足特定条件
     * 该函数不仅检查括号是否匹配平衡，还确保最外层的括号包裹整个字符串
     *
     * @param s 待检查的字符串，只应包含括号字符
     * @return 如果字符串中的括号平衡且最外层括号包裹整个字符串则返回true，否则返回false
     */
    private boolean isBalancedOuter(String s) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '(')
                depth++;
            else if (ch == ')') {
                depth--;
                // 当深度为0时，如果当前不是最后一个字符，说明有多个顶层表达式，返回false
                if (depth == 0 && i != s.length() - 1)
                    return false;
            }
            // 如果深度小于0，说明右括号多于左括号，括号不匹配
            if (depth < 0)
                return false;
        }
        // 最后检查所有括号是否完全匹配
        return depth == 0;
    }

    /**
     * 将字符串按照指定分隔符进行分割，但只分割顶层（即不在括号内的）分隔符
     *
     * @param s   待分割的字符串
     * @param sep 用作分割符的字符串
     * @return 分割后的字符串列表，不包含空字符串
     */
    private List<String> splitTopLevel(String s, String sep) {
        List<String> out = new ArrayList<>();
        int depth = 0;
        int i = 0;
        int start = 0;

        // 遍历字符串，根据括号层级和分隔符位置进行分割
        while (i < s.length()) {
            char ch = s.charAt(i);
            if (ch == '(')
                depth++;
            else if (ch == ')')
                depth--;
            // 当处于顶层且找到分隔符时，进行分割
            if (depth == 0 && i + sep.length() <= s.length() && s.regionMatches(i, sep, 0, sep.length())) {
                out.add(s.substring(start, i));
                i += sep.length();
                start = i;
                continue;
            }
            i++;
        }
        out.add(s.substring(start));

        // 过滤掉空字符串并返回结果
        List<String> trimmed = new ArrayList<>();
        for (String part : out)
            if (part != null && !part.trim().isEmpty())
                trimmed.add(part);
        return trimmed;
    }

    /**
     * 匹配SQL分析结果与给定条件
     *
     * @param r    SQL分析结果对象
     * @param cond 匹配条件字符串
     * @return 如果匹配成功返回true，否则返回false
     */
    private boolean match(SqlAnalysisResult r, String cond) {
        if (r == null || cond == null)
            return false;
        String c = cond.trim();
        // key 等值判断与空/null 判断
        if (c.startsWith("key") && c.contains("==")) {
            String[] arr = c.split("==", 2);
            if (arr.length == 2) {
                String rightRaw = arr[1].trim();
                // 右侧可能是 null、"" 或 普通字符串（带/不带引号）
                if (rightRaw.equalsIgnoreCase("null")) {
                    return r.getKey() == null;
                }
                String right = unquote(rightRaw);
                String left = r.getKey();
                return left == null ? (right == null || right.isEmpty()) : left.equalsIgnoreCase(right);
            }
        }
        // type == "ALL"
        if (c.startsWith("type") && c.contains("==")) {
            String[] arr = c.split("==", 2);
            if (arr.length == 2) {
                String right = unquote(arr[1].trim());
                return r.getType() != null && r.getType().equalsIgnoreCase(right);
            }
        }
        // extra contains "filesort"
        if (c.startsWith("extra") && c.toLowerCase().contains("contains")) {
            int idx = c.toLowerCase().indexOf("contains");
            String right = unquote(c.substring(idx + "contains".length()).trim());
            return r.getExtra() != null && r.getExtra().toLowerCase().contains(right.toLowerCase());
        }
        // rows 阈值：rows > N / >= / < / <=
        if (c.startsWith("rows")) {
            return compareNumber(r.getRows() == null ? null : r.getRows().doubleValue(), c.substring(4).trim());
        }
        // filtered 阈值：filtered < X（百分比数值字符串）
        if (c.startsWith("filtered")) {
            Double fv = null;
            try {
                fv = r.getFiltered() == null ? null : Double.parseDouble(r.getFiltered());
            } catch (Exception ignore) {
            }
            return compareNumber(fv, c.substring(8).trim());
        }
        return false;
    }

    /**
     * 比较数字与表达式中的数值条件
     *
     * @param left 待比较的数字值
     * @param expr 包含比较操作符和数值的字符串表达式，支持 >=、<=、>、< 操作符
     * @return 如果参数为空或表达式格式不正确返回false，否则返回比较结果
     */
    private boolean compareNumber(Double left, String expr) {
        // 参数校验，如果任一参数为空或表达式为空则返回false
        if (left == null || expr == null || expr.isEmpty())
            return false;
        String e = expr.trim();
        try {
            // 根据表达式前缀的操作符进行相应的数值比较
            if (e.startsWith(">="))
                return left >= Double.parseDouble(e.substring(2).trim());
            if (e.startsWith("<="))
                return left <= Double.parseDouble(e.substring(2).trim());
            if (e.startsWith(">"))
                return left > Double.parseDouble(e.substring(1).trim());
            if (e.startsWith("<"))
                return left < Double.parseDouble(e.substring(1).trim());
        } catch (Exception ignore) {
            // 解析表达式中的数值出现异常时忽略并返回false
        }
        return false;
    }

    /**
     * 去除字符串两端的引号
     *
     * @param s 待处理的字符串
     * @return 如果字符串两端都是双引号或单引号，则返回去除引号后的字符串；否则返回原字符串
     */
    private String unquote(String s) {
        // 检查字符串是否以相同类型的引号开始和结束（双引号或单引号）
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            // 去除字符串两端的引号并返回
            return s.substring(1, s.length() - 1);
        }
        // 如果不满足引号条件，返回原字符串
        return s;
    }

}
