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

package com.linyi.sql.insight.starter.ui;

import com.linyi.sql.insight.model.SqlInsightLogDto;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 内存分析存储
 *
 * @author linyi
 * @since 1.0.0
 */
public class InMemoryAnalysisStore {
    private final Deque<Record> deque;
    private final int capacity;

    public InMemoryAnalysisStore(int capacity) {
        this.capacity = Math.max(10, capacity);
        this.deque = new ArrayDeque<>(this.capacity);
    }

    public synchronized void add(SqlInsightLogDto dto) {
        if (deque.size() >= capacity) {
            deque.removeFirst();
        }
        deque.addLast(new Record(Instant.now().toEpochMilli(), dto));
    }

    public synchronized List<Record> recent(int limit) {
        int n = Math.min(limit <= 0 ? capacity : limit, deque.size());
        List<Record> list = new ArrayList<>(n);
        Object[] arr = deque.toArray();
        for (int i = arr.length - n; i < arr.length; i++) {
            list.add((Record) arr[i]);
        }
        return list;
    }

    public static class Record {
        public final long ts;
        public final SqlInsightLogDto dto;

        public Record(long ts, SqlInsightLogDto dto) {
            this.ts = ts;
            this.dto = dto;
        }
    }
}
