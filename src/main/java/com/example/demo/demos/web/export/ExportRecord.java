package com.example.demo.demos.web.export;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exports", schema = "public")
public class ExportRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 对应 tasks.id（任务可被删除，所以允许为 null）
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    // running / completed / failed
    @Column(name = "export_status", nullable = false, length = 20)
    private String exportStatus;

    // 字节
    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes = 0L;

    @Column(name = "record_count", nullable = false)
    private Integer recordCount = 0;

    @Column(name = "creator_id")
    private Long creatorId;

    // 让 DB 默认 now() 生效：不参与 insert/update
    @Column(name = "exported_at", insertable = false, updatable = false)
    private LocalDateTime exportedAt;

    // ===== getter/setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getExportStatus() { return exportStatus; }
    public void setExportStatus(String exportStatus) { this.exportStatus = exportStatus; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public LocalDateTime getExportedAt() { return exportedAt; }
    public void setExportedAt(LocalDateTime exportedAt) { this.exportedAt = exportedAt; }
}
