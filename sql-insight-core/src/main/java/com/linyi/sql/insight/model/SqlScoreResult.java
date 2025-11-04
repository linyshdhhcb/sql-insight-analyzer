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
 * 评分结果（聚合分与明细）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class SqlScoreResult {

    /**
     * 评分（0-100）
     */
    private int score;

    /**
     * 等级（OK/WARN/CRIT）
     */
    private AnalysisLevel level;

    /**
     * 评分明细（结构化）
     */
    private final List<SqlScoreResultDetail> details = new ArrayList<>();

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public AnalysisLevel getLevel() {
        return level;
    }

    public void setLevel(AnalysisLevel level) {
        this.level = level;
    }

    public void addDetail(SqlScoreResultDetail d) {
        if (d != null) {
            details.add(d);
        }
    }

    public void setDetails(List<SqlScoreResultDetail> ds) {
        details.clear();
        if (ds != null)
            details.addAll(ds);
    }

    public List<SqlScoreResultDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }
}
