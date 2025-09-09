package com.example.demo.demos.web.template;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === 数据库真实字段 ===
    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @Column(name = "template_description", columnDefinition = "text")
    private String templateDescription;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "collect_type", length = 100)
    private String collectType;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "usage_count")
    private Integer usageCount;

    // ===== getters/setters（数据库字段）=====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getTemplateDescription() { return templateDescription; }
    public void setTemplateDescription(String templateDescription) { this.templateDescription = templateDescription; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCollectType() { return collectType; }
    public void setCollectType(String collectType) { this.collectType = collectType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    // ===== 前端需要的派生字段（不入库，强制出现在 JSON）=====
    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;

    /** 卡片标题 */
    @Transient
    @JsonProperty("name")
    public String getName() { return templateName; }

    /** 卡片描述 */
    @Transient
    @JsonProperty("desc")
    public String getDesc() { return templateDescription; }

    /** 灰色标签：category + collect_type */
    @Transient
    @JsonProperty("tags")
    public List<String> getTags() {
        List<String> tags = new ArrayList<>(2);
        if (category != null && !category.isEmpty()) tags.add(category);
        if (collectType != null && !collectType.isEmpty()) tags.add(collectType);
        return tags;
    }

    /** “更新于 yyyy-MM-dd” */
    @Transient
    @JsonProperty("updated")
    public String getUpdated() { return updatedAt == null ? null : DF.format(updatedAt); }

    /** 右上角状态颜色 */
    @Transient
    @JsonProperty("statusColor")
    public String getStatusColor() {
        if ("生效".equals(status))   return "bg-green-100 text-green-800";
        if ("失效".equals(status))   return "bg-gray-100 text-gray-400";
        if ("更新中".equals(status)) return "bg-blue-100 text-blue-700";
        return "bg-gray-100 text-gray-400";
    }

    /** 左侧小图（占位） */
    @Transient
    @JsonProperty("img")
    public String getImg() {
        return "https://ai-public.mastergo.com/gen_page/map_placeholder_1280x720.png?width=32&height=32&orientation=squarish";
    }
}
