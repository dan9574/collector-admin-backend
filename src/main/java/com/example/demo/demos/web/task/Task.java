package com.example.demo.demos.web.task;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", schema = "public")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Column(name = "collect_status", nullable = false, length = 50)
    private String collectStatus;

    @Column(name = "collect_duration", nullable = false)
    private Integer collectDuration;

    @Column(name = "record_count", nullable = false)
    private Integer recordCount;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    // 让 DB 的 now() 默认值生效：不参与 insert/update
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- getters/setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getCollectStatus() { return collectStatus; }
    public void setCollectStatus(String collectStatus) { this.collectStatus = collectStatus; }

    public Integer getCollectDuration() { return collectDuration; }
    public void setCollectDuration(Integer collectDuration) { this.collectDuration = collectDuration; }

    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
