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

package com.linyi.sql.insight.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 统一的执行计划结果集合。
 *
 * @author linyi
 * @since 1.0.0
 */
public class SqlAnalysisResultList {

    private final List<SqlAnalysisResult> results = new ArrayList<>();

    /**
     * 添加SQL分析结果到结果集合中
     *
     * @param r SQL分析结果对象，如果为null则不进行添加操作
     */
    public void add(SqlAnalysisResult r) {
        // 只有当结果对象不为null时才添加到结果集合中
        if (r != null) {
            results.add(r);
        }
    }


    /**
     * 获取结果集合
     *
     * @return 结果集合
     */
    public List<SqlAnalysisResult> getResults() {
        return Collections.unmodifiableList(results);
    }
}
