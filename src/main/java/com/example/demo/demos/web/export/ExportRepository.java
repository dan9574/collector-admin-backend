package com.example.demo.demos.web.export;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExportRepository extends JpaRepository<ExportRecord, Long> {

    @Query("select e from ExportRecord e " +
            "where (:status    is null or e.exportStatus = :status) " +
            "  and (:taskId    is null or e.taskId = :taskId) " +
            "  and (:creatorId is null or e.creatorId = :creatorId) " +
            "  and (:kw        is null or lower(e.taskName) like :kw) " +
            "  and (:fromTime  is null or e.exportedAt >= :fromTime) " +
            "  and (:toTime    is null or e.exportedAt <  :toTime) " +
            "order by e.exportedAt desc, e.id desc")
    List<ExportRecord> search(
            @Param("status") String status,
            @Param("taskId") Long taskId,
            @Param("creatorId") Long creatorId,
            @Param("kw") String kw,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    List<ExportRecord> findAllByOrderByExportedAtDesc();
}
