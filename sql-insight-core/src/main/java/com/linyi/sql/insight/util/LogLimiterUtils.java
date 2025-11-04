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

package com.linyi.sql.insight.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易日志收敛：相同 key 在 suppressMs 窗口内仅允许一次。
 *
 * @author linyi
 * @since 1.0.0
 */
public class LogLimiterUtils {

    private final long suppressMs;
    private final Map<String, Long> lastLogAt = new ConcurrentHashMap<>();

    public LogLimiterUtils(long suppressMs) {
        this.suppressMs = suppressMs;
    }

    /**
     * 判断指定键是否允许执行操作，用于控制操作频率
     *
     * @param key 需要检查的键值
     * @return true表示允许执行，false表示需要抑制执行
     */
    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        // 记录当前时间并获取上次记录的时间
        Long last = lastLogAt.put(key, now);
        // 如果是第一次访问该键，则允许执行
        if (last == null)
            return true;
        // 判断距离上次执行的时间是否超过抑制时间间隔
        return now - last >= suppressMs;
    }

}
