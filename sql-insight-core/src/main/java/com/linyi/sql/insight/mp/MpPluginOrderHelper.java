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

package com.linyi.sql.insight.mp;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * MyBatis-Plus 插件顺序助手。
 *
 * @author linyi
 * @since 1.0.0
 */
public class MpPluginOrderHelper {
    /**
     * 将自定义拦截器放在 MyBatis-Plus 相关拦截器（如
     * MybatisPlusInterceptor、PaginationInnerInterceptor）之后。
     * 如果未发现 MP 拦截器，则保持原顺序。
     */
    public static void ensureAfterMybatisPlus(Configuration configuration, Interceptor target) {
        if (configuration == null || target == null) {
            return;
        }
        List<Interceptor> chain = configuration.getInterceptors();
        if (chain.isEmpty()) {
            return;
        }
        int targetIdx = indexOf(chain, target);
        if (targetIdx < 0) {
            return;
        }
        int lastMpIndex = lastMybatisPlusIndex(chain);
        if (lastMpIndex < 0) {
            // 未包含 MP，保持原顺序
            return;
        }
        if (targetIdx > lastMpIndex) {
            // 已在 MP 之后
            return;
        }
        // 移动到 MP 最后一个之后
        chain.remove(targetIdx);
        // 注意：如果 target 原位置在 lastMpIndex 之前，被移除后 lastMpIndex 需要减 1
        if (targetIdx < lastMpIndex) {
            lastMpIndex -= 1;
        }
        chain.add(lastMpIndex + 1, target);
    }

    /**
     * 在拦截器链中查找目标拦截器的索引位置
     *
     * @param chain  拦截器链列表
     * @param target 要查找的目标拦截器
     * @return 目标拦截器在链中的索引位置，如果未找到则返回-1
     */
    private static int indexOf(List<Interceptor> chain, Interceptor target) {
        // 遍历拦截器链查找目标拦截器
        for (int i = 0; i < chain.size(); i++) {
            if (chain.get(i) == target) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 找到链中最后一个 MyBatis-Plus 相关拦截器的位置。
     * 通过类名包含关键字来判断，避免显式依赖 MP 包。
     */
    private static int lastMybatisPlusIndex(List<Interceptor> chain) {
        int last = -1;
        for (int i = 0; i < chain.size(); i++) {
            String cn = chain.get(i).getClass().getName();
            if (isMybatisPlusInterceptor(cn)) {
                last = i;
            }
        }
        return last;
    }

    /**
     * 判断给定的类名是否为Mybatis-Plus拦截器相关类
     *
     * @param className 待判断的类名
     * @return 如果是Mybatis-Plus拦截器相关类则返回true，否则返回false
     */
    private static boolean isMybatisPlusInterceptor(String className) {
        if (className == null)
            return false;
        String cn = className;
        // 判断类名是否以Mybatis-Plus包名开头或包含常见的拦截器类名
        return cn.startsWith("com.baomidou.mybatisplus.")
                || cn.contains("MybatisPlusInterceptor")
                || cn.contains("PaginationInnerInterceptor")
                || cn.contains("TenantLineInnerInterceptor")
                || cn.contains("DynamicTableNameInnerInterceptor");
    }

}
