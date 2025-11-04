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

/**
 * 异常工具类。
 *
 * @author linyi
 * @since 1.0.0
 */
public class ExceptionUtils {

    /**
     * 获取异常的根本原因消息
     *
     * @param t 要分析的异常对象
     * @return 返回异常的根本原因消息，格式为"异常类名: 异常消息"，如果异常消息为空则只返回异常类名，
     * 如果参数为null则返回空字符串
     */
    public static String rootCauseMessage(Throwable t) {
        if (t == null)
            return "";
        // 遍历异常链，找到根本原因异常
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        // 获取根本原因异常的类名和消息，构造返回结果
        String name = cur.getClass().getSimpleName();
        String msg = cur.getMessage();
        return msg == null ? name : name + ": " + msg;
    }

}
