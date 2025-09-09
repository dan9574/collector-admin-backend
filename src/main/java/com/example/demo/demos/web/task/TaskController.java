package com.example.demo.demos.web.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    // GET /api/tasks?creatorId=123  （不传则查全部）
    @GetMapping
    public ResponseEntity<List<Task>> list(@RequestParam(required = false) Long creatorId) {
        if (creatorId != null) {
            return ResponseEntity.ok(taskRepository.findByCreatorIdOrderByIdDesc(creatorId));
        }
        return ResponseEntity.ok(taskRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> get(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        task.setId(null); // 自增
        if (task.getCreatorId() == null) task.setCreatorId(0L); // 兼容前端未传
        return ResponseEntity.ok(taskRepository.save(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody Task t) {
        return taskRepository.findById(id)
                .map(existing -> {
                    if (t.getTaskName() != null)        existing.setTaskName(t.getTaskName());
                    if (t.getCollectStatus() != null)   existing.setCollectStatus(t.getCollectStatus());
                    if (t.getCollectDuration() != null) existing.setCollectDuration(t.getCollectDuration());
                    if (t.getRecordCount() != null)     existing.setRecordCount(t.getRecordCount());
                    // creatorId 一般不允许修改
                    return ResponseEntity.ok(taskRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
