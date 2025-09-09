package com.example.demo.demos.web.export;

import com.example.demo.demos.web.auth.SessionStore;
import com.example.demo.demos.web.task.Task;
import com.example.demo.demos.web.task.TaskRepository;
import com.example.demo.demos.web.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.demos.web.user.UserRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exports")
public class ExportController {

    @Autowired
    private ExportRepository repo;

    // === 新增依赖（用于根据 taskId 补全字段 & 从 sid 取用户ID） ===
    @Autowired private TaskRepository taskRepo;
    @Autowired private SessionStore  sessionStore;
    @Autowired private UserRepository userRepo;


    // GET /api/exports?status=running&taskId=1&creatorId=2&q=房产&from=2025-08-17T00:00:00&to=2025-08-22T00:00:00
    @GetMapping
    public ResponseEntity<List<ExportRecord>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        String s = empty(status);
        Long   t = taskId;

        String kw = empty(q);
        if (kw != null) kw = "%" + kw.toLowerCase() + "%";

        boolean noFilter = (s == null && t == null && creatorId == null && kw == null && from == null && to == null);
        if (noFilter) {
            return ResponseEntity.ok(repo.findAllByOrderByExportedAtDesc());
        }
        return ResponseEntity.ok(repo.search(s, t, creatorId, kw, from, to));
    }

    // GET /api/exports/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ExportRecord> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/exports  （保留：原始直接写入）
    @PostMapping
    public ResponseEntity<ExportRecord> create(@RequestBody ExportRecord r) {
        r.setId(null); // 自增
        if (r.getFileSizeBytes() == null) r.setFileSizeBytes(0L);
        if (r.getRecordCount() == null)   r.setRecordCount(0);
        if (r.getExportStatus() == null)  r.setExportStatus("running");
        if (r.getCreatorId() == null)     r.setCreatorId(0L);
        return ResponseEntity.ok(repo.save(r));
    }

    // PUT /api/exports/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ExportRecord> update(@PathVariable Long id, @RequestBody ExportRecord r) {
        return repo.findById(id)
                .map(exist -> {
                    if (r.getTaskId() != null)        exist.setTaskId(r.getTaskId());
                    if (r.getTaskName() != null)      exist.setTaskName(r.getTaskName());
                    if (r.getExportStatus() != null)  exist.setExportStatus(r.getExportStatus());
                    if (r.getFileSizeBytes() != null) exist.setFileSizeBytes(r.getFileSizeBytes());
                    if (r.getRecordCount() != null)   exist.setRecordCount(r.getRecordCount());
                    if (r.getCreatorId() != null)     exist.setCreatorId(r.getCreatorId());
                    // exportedAt 由 DB now() 控制
                    return ResponseEntity.ok(repo.save(exist));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/exports/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 预留：下载文件
    @GetMapping("/{id}/file")
    public ResponseEntity<?> download(@PathVariable Long id) {
        return ResponseEntity.notFound().build();
    }

    // =============== 新增：根据 taskId 写导出记录（单个/批量） ===============

    // POST /api/exports/createByTask
    @PostMapping("/createByTask")
    public ResponseEntity<ExportRecord> createByTask(@RequestBody CreateByTaskReq req,
                                                     HttpServletRequest http) {
        Long creatorId = (req.creatorId != null ? req.creatorId : currentUserId(http));
        Task task = (req.taskId != null ? taskRepo.findById(req.taskId).orElse(null) : null);

        ExportRecord r = new ExportRecord();
        r.setId(null);
        r.setTaskId(task != null ? task.getId() : req.taskId);
        r.setTaskName(task != null ? task.getTaskName()
                : (req.taskName != null ? req.taskName : "未命名任务"));
        r.setExportStatus("completed");
        r.setFileSizeBytes(8192L);
        r.setRecordCount(task != null
                ? (task.getRecordCount() == null ? 0 : task.getRecordCount())
                : (req.recordCount != null ? req.recordCount : 0));
        r.setCreatorId(creatorId);

        return ResponseEntity.ok(repo.save(r));
    }

    // POST /api/exports/batch
    @PostMapping("/batch")
    public ResponseEntity<List<ExportRecord>> batch(@RequestBody BatchReq req,
                                                    HttpServletRequest http) {
        if (req.taskIds == null || req.taskIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Long creatorId = (req.creatorId != null ? req.creatorId : currentUserId(http));

        Map<Long, Task> taskMap = taskRepo.findAllById(req.taskIds).stream()
                .collect(Collectors.toMap(Task::getId, t -> t));

        List<ExportRecord> list = new ArrayList<>();
        for (Long id : req.taskIds) {
            Task task = taskMap.get(id);
            ExportRecord r = new ExportRecord();
            r.setId(null);
            r.setTaskId(id);
            r.setTaskName(task != null ? task.getTaskName() : "未命名任务");
            r.setExportStatus("completed");
            r.setFileSizeBytes(8192L);
            r.setRecordCount(task != null ? (task.getRecordCount() == null ? 0 : task.getRecordCount()) : 0);
            r.setCreatorId(creatorId);
            list.add(r);
        }
        return ResponseEntity.ok(repo.saveAll(list));
    }

    // =============== 辅助 ===============

    // imports 顶部补这一行
    private Long currentUserId(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if ("sid".equals(c.getName())) {
                // 用你现在的 SessionStore：getUser(sid) -> Optional<User>
                return sessionStore.getUser(c.getValue())
                        .map(User::getId)      // 若不想 import User，则换成 .map(u -> u.getId())
                        .orElse(null);
            }
        }
        return null;
    }




    private String empty(String s) { return (s == null || s.trim().isEmpty()) ? null : s.trim(); }

    // =============== DTO ===============

    public static class CreateByTaskReq {
        public Long taskId;
        public String taskName;      // 可选
        public Integer recordCount;  // 可选
        public Long creatorId;       // 可选（一般不传，走 sid）
    }

    public static class BatchReq {
        public List<Long> taskIds;   // 必填
        public Long creatorId;       // 可选（一般不传，走 sid）
    }
}
