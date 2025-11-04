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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * 从 HTTP 端点加载规则（properties 文本）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class HttpRuleLoader extends RuleLoader {

    private final String url;

    public HttpRuleLoader(String url) {
        this.url = url;
    }

    /**
     * 加载SQL评分规则列表
     *
     * @return SQL评分规则列表，如果从URL加载失败或规则为空，则返回父类加载的默认规则
     */
    @Override
    public List<SqlScoreRule> loadRules() {
        Properties props = new Properties();
        try {
            // 通过HTTP GET请求从指定URL获取规则配置
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            try (InputStream is = conn.getInputStream()) {
                props.load(is);
            }
        } catch (Exception ignore) {
            // 忽略网络异常，使用空属性继续处理
        }
        // 解析属性文件中的规则配置
        List<SqlScoreRule> list = FileRuleLoader.parseRules(props);
        // 如果解析结果为空，则使用父类的默认规则加载方式
        return list.isEmpty() ? super.loadRules() : list;
    }

}
