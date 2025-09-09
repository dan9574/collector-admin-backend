package com.example.demo.demos.web.template;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Template> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) String collectType,
            @RequestParam(required = false) String status,
            // 注意：Spring Data 分页是 0 基
            @RequestParam(name = "page", defaultValue = "0") int page0,
            // 兼容两种参数名：size / pageSize
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(required = false) String sortType
    ) {
        int pageSizeVal = (size != null ? size : (pageSize != null ? pageSize : 8));
        return service.findTemplates(search, scene, collectType, status, page0, pageSizeVal, sortType);
    }

    @GetMapping("/{id}")
    public Template get(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new RuntimeException("模板不存在"));
    }

    @PostMapping
    public Template create(@RequestBody Template t) {
        return service.save(t);
    }

    @PutMapping("/{id}")
    public Template update(@PathVariable Long id, @RequestBody Template t) {
        t.setId(id);
        return service.save(t);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}