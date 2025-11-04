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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.linyi.sql.insight.model.SqlInsightLogDto;

/**
 * 简易 UI 控制器：
 * - /sql-analyzer Thymeleaf 页面（支持 level/sqlId 过滤 + 分页）
 * - /sql-analyzer/recent JSON 数据（保留）
 * 
 * @author linyi
 * @since 1.0.0
 */
@Controller
@RequestMapping("/sql-analyzer")
public class SqlAnalyzerController {

    private final InMemoryAnalysisStore store;

    public SqlAnalyzerController(InMemoryAnalysisStore store) {
        this.store = store;
    }

    @GetMapping
    public String page(@RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
            @RequestParam(name = "level", required = false) String level,
            @RequestParam(name = "sqlId", required = false) String sqlId,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            Model model) {
        List<InMemoryAnalysisStore.Record> items = store.recent(limit);
        String lv = level == null ? null : level.trim().toUpperCase(Locale.ROOT);
        String idSub = sqlId == null ? null : sqlId.trim().toLowerCase(Locale.ROOT);
        List<InMemoryAnalysisStore.Record> filtered = items.stream().filter(r -> {
            SqlInsightLogDto dto = r.dto;
            boolean ok = true;
            String rl = dto != null && dto.scoreResult != null && dto.scoreResult.getLevel() != null
                    ? dto.scoreResult.getLevel().name()
                    : null;
            if (lv != null && !lv.isEmpty()) {
                ok &= rl != null && rl.equalsIgnoreCase(lv);
            }
            if (idSub != null && !idSub.isEmpty()) {
                ok &= dto.sqlId != null && dto.sqlId.toLowerCase(Locale.ROOT).contains(idSub);
            }
            return ok;
        }).collect(Collectors.toList());
        int total = filtered.size();
        int pageSize = Math.max(1, size);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        int current = Math.min(Math.max(1, page), totalPages);
        int from = Math.min((current - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<InMemoryAnalysisStore.Record> pageList = filtered.subList(from, to);

        model.addAttribute("records", pageList);
        model.addAttribute("limit", limit);
        model.addAttribute("level", level);
        model.addAttribute("sqlId", sqlId);
        model.addAttribute("page", current);
        model.addAttribute("size", pageSize);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        return "sql-analyzer";
    }

    @RestController
    @RequestMapping("/sql-analyzer")
    public static class RecentApi {
        private final InMemoryAnalysisStore store;
        private final SseHub sseHub;

        public RecentApi(InMemoryAnalysisStore store, SseHub sseHub) {
            this.store = store;
            this.sseHub = sseHub;
        }

        @GetMapping("/recent")
        public List<SqlInsightLogDto> recent(
                @RequestParam(name = "limit", required = false, defaultValue = "100") int limit) {
            return store.recent(limit).stream().map(r -> r.dto).collect(Collectors.toList());
        }

        @GetMapping("/sse")
        public SseEmitter sse() {
            return sseHub.register();
        }
    }
}
