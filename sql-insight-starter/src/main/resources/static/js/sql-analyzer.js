/**
 * SQL Insight Analyzer - 前端脚本
 */

(function () {
  "use strict";

  // 工具函数：HTML转义
  function escapeHtml(text) {
    var div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
  }

  // 获取表格数据
  function getTableData() {
    var data = [];
    var headers = ["时间", "SQL ID", "等级", "分数", "SQL", "明细"];

    document.querySelectorAll("table tbody tr").forEach(function (tr) {
      var row = {};
      var t = tr.querySelector("td:nth-child(1)")?.innerText || "";
      var id = tr.querySelector("td:nth-child(2)")?.innerText || "";
      var lv = tr.querySelector("td:nth-child(3)")?.innerText || "";
      var sc = tr.querySelector("td:nth-child(4)")?.innerText || "";
      var sql = tr.querySelector("td:nth-child(5) pre")?.innerText || "";
      var detail = tr.querySelector("td:nth-child(6)")?.innerText || "";

      row[headers[0]] = t;
      row[headers[1]] = id;
      row[headers[2]] = lv;
      row[headers[3]] = sc;
      row[headers[4]] = sql;
      row[headers[5]] = detail;
      data.push(row);
    });

    return { headers: headers, data: data };
  }

  // 导出为CSV
  function exportToCsv() {
    try {
      var tableData = getTableData();
      var rows = [];
      rows.push(tableData.headers.join(","));

      function esc(v) {
        return (
          '"' +
          String(v).replace(/"/g, '""').replace(/\n/g, " ").replace(/\r/g, "") +
          '"'
        );
      }

      tableData.data.forEach(function (row) {
        rows.push(
          [
            esc(row[tableData.headers[0]]),
            esc(row[tableData.headers[1]]),
            esc(row[tableData.headers[2]]),
            esc(row[tableData.headers[3]]),
            esc(row[tableData.headers[4]]),
            esc(row[tableData.headers[5]]),
          ].join(",")
        );
      });

      var blob = new Blob(["\ufeff" + rows.join("\n")], {
        type: "text/csv;charset=utf-8;",
      });
      var url = URL.createObjectURL(blob);
      var a = document.createElement("a");
      a.href = url;
      a.download = "sql-insight-" + new Date().getTime() + ".csv";
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error("导出CSV失败:", e);
      alert("导出CSV失败: " + e.message);
    }
  }

  // 导出为JSON
  function exportToJson() {
    try {
      var tableData = getTableData();
      var jsonData = {
        exportTime: new Date().toISOString(),
        total: tableData.data.length,
        records: tableData.data,
      };

      var blob = new Blob([JSON.stringify(jsonData, null, 2)], {
        type: "application/json;charset=utf-8;",
      });
      var url = URL.createObjectURL(blob);
      var a = document.createElement("a");
      a.href = url;
      a.download = "sql-insight-" + new Date().getTime() + ".json";
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error("导出JSON失败:", e);
      alert("导出JSON失败: " + e.message);
    }
  }

  // 导出为Excel（使用SheetJS库）
  function exportToExcel() {
    try {
      if (typeof XLSX === "undefined") {
        alert("Excel导出需要加载SheetJS库，请检查网络连接");
        return;
      }

      var tableData = getTableData();
      var ws = XLSX.utils.json_to_sheet(tableData.data);
      var wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, "SQL分析结果");

      XLSX.writeFile(wb, "sql-insight-" + new Date().getTime() + ".xlsx");
    } catch (e) {
      console.error("导出Excel失败:", e);
      alert("导出Excel失败: " + e.message);
    }
  }

  // 初始化：明细弹窗
  function initDetailModal() {
    if (typeof $ === "undefined") return;

    $("#detailModal").on("show.bs.modal", function (event) {
      var button = $(event.relatedTarget);
      var details = button.data("details");
      var $body = $("#detailBody");
      try {
        // 兼容：SSE行通过data-details存了encodeURIComponent(JSON)
        if (typeof details === "string") {
          try {
            details = JSON.parse(decodeURIComponent(details));
          } catch (e) {
            // 尝试直接解析
            try {
              details = JSON.parse(details);
            } catch (e2) {}
          }
        }
        if (!Array.isArray(details) || details.length === 0) {
          $body.text("无明细数据");
          return;
        }
        var html =
          '<table class="table table-sm table-bordered">' +
          "<thead><tr><th>规则ID</th><th>扣分</th><th>等级</th><th>原因</th></tr></thead><tbody>";
        details.forEach(function (item) {
          if (!item) return;
          var ruleId = item.ruleId || "";
          var score = item.score != null ? item.score : "";
          var level = item.level != null ? item.level : "";
          var reason = item.reason || "";
          html +=
            "<tr><td>" +
            escapeHtml(ruleId) +
            "</td><td>" +
            escapeHtml(String(score)) +
            "</td><td>" +
            escapeHtml(String(level)) +
            "</td><td>" +
            escapeHtml(reason) +
            "</td></tr>";
        });
        html += "</tbody></table>";
        $body.html(html);
      } catch (e) {
        $body.text("解析明细失败: " + e.message);
      }
    });
  }

  // 初始化：刷新按钮
  function initRefreshBtn() {
    var refreshBtn = document.getElementById("refreshBtn");
    if (refreshBtn) {
      refreshBtn.addEventListener("click", function () {
        window.location.reload();
      });
    }
  }

  // 初始化：导出下拉菜单
  function initExportDropdown() {
    var exportBtn = document.getElementById("exportBtn");
    if (!exportBtn) return;

    var dropdownMenu = document.querySelector("#exportDropdown .dropdown-menu");
    if (!dropdownMenu) return;

    // 绑定导出选项
    var csvItem = dropdownMenu.querySelector('[data-export="csv"]');
    var jsonItem = dropdownMenu.querySelector('[data-export="json"]');
    var excelItem = dropdownMenu.querySelector('[data-export="excel"]');

    if (csvItem) {
      csvItem.addEventListener("click", function (e) {
        e.preventDefault();
        exportToCsv();
      });
    }

    if (jsonItem) {
      jsonItem.addEventListener("click", function (e) {
        e.preventDefault();
        exportToJson();
      });
    }

    if (excelItem) {
      excelItem.addEventListener("click", function (e) {
        e.preventDefault();
        exportToExcel();
      });
    }
  }

  // 初始化：SSE实时推送
  function initSse() {
    try {
      if (!window.EventSource) return;

      var es = new EventSource("/sql-analyzer/sse");
      es.onmessage = function (evt) {
        try {
          var r = JSON.parse(evt.data);
          var tbody = document.querySelector("table tbody");
          if (!tbody) return;

          var tr = document.createElement("tr");
          var t = new Date(r.ts)
            .toISOString()
            .replace("T", " ")
            .substring(0, 19);
          var lvl = r.result && r.result.level ? r.result.level : "";
          var sc = r.result && r.result.score != null ? r.result.score : "";

          tr.innerHTML =
            "<td>" +
            t +
            "</td>" +
            "<td>" +
            escapeHtml(r.sqlId || "") +
            "</td>" +
            "<td>" +
            escapeHtml(String(lvl)) +
            "</td>" +
            "<td>" +
            escapeHtml(String(sc)) +
            "</td>" +
            "<td><pre>" +
            escapeHtml(r.sql || "") +
            "</pre></td>" +
            '<td><button class="btn btn-sm btn-outline-secondary view-detail" data-toggle="modal" data-target="#detailModal" data-details="' +
            encodeURIComponent(
              JSON.stringify(r.result ? r.result.details : [])
            ) +
            '">查看</button></td>';

          // 追加到顶部
          if (tbody.firstChild) {
            tbody.insertBefore(tr, tbody.firstChild);
          } else {
            tbody.appendChild(tr);
          }
        } catch (e) {
          console.warn("SSE消息处理失败:", e);
        }
      };

      es.onerror = function (e) {
        console.warn("SSE连接错误:", e);
      };
    } catch (e) {
      console.warn("SSE初始化失败:", e);
    }
  }

  // DOM加载完成后初始化
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", function () {
      initDetailModal();
      initRefreshBtn();
      initExportDropdown();
      initSse();
    });
  } else {
    initDetailModal();
    initRefreshBtn();
    initExportDropdown();
    initSse();
  }

  // 导出全局函数（供外部调用）
  window.SqlAnalyzer = {
    exportToCsv: exportToCsv,
    exportToJson: exportToJson,
    exportToExcel: exportToExcel,
    escapeHtml: escapeHtml,
  };
})();
