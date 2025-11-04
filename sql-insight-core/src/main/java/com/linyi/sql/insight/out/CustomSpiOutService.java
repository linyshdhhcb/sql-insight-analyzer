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

package com.linyi.sql.insight.out;

import com.linyi.sql.insight.model.SqlScoreResult;
import com.linyi.sql.insight.model.SqlAnalysisResultList;

/**
 * 通过反射加载用户自定义实现（实现 SqlScoreResultOutService）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class CustomSpiOutService implements SqlScoreResultOutService {

    private final SqlScoreResultOutService delegate;

    @SuppressWarnings("unchecked")
    public CustomSpiOutService(String className) {
        this.delegate = instantiate(className);
    }

    /**
     * 根据类名实例化SqlScoreResultOutService对象
     *
     * @param className 要实例化的类名
     * @return 实例化成功的SqlScoreResultOutService对象
     * @throws IllegalArgumentException 当类名无效或实例化失败时抛出
     */
    private SqlScoreResultOutService instantiate(String className) {
        try {
            // 通过反射创建指定类的实例
            Class<?> clz = Class.forName(className);
            Object obj = clz.getDeclaredConstructor().newInstance();
            if (obj instanceof SqlScoreResultOutService) {
                return (SqlScoreResultOutService) obj;
            }
        } catch (Exception ignore) {
            // 忽略反射异常，统一抛出IllegalArgumentException
        }
        throw new IllegalArgumentException("Invalid custom out service: " + className);
    }

    @Override
    public void publish(SqlScoreResult result, String sqlId, String sql, SqlAnalysisResultList plan) {
        delegate.publish(result, sqlId, sql, plan);
    }
}
