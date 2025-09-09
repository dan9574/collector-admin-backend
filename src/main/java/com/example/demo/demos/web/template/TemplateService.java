package com.example.demo.demos.web.template;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {
    private final TemplateRepository repo;
    public TemplateService(TemplateRepository repo) { this.repo = repo; }

    public Page<Template> findTemplates(String search,
                                        String scene,
                                        String collectType,
                                        String status,
                                        int page0, int size,
                                        String sortType) {
        Specification<Template> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                ps.add(cb.or(
                        cb.like(root.get("templateName"), like),
                        cb.like(root.get("templateDescription"), like)
                ));
            }
            if (scene != null && !"全部".equals(scene) && !scene.isEmpty()) {
                ps.add(cb.equal(root.get("category"), scene));
            }
            if (collectType != null && !"全部".equals(collectType) && !collectType.isEmpty()) {
                ps.add(cb.equal(root.get("collectType"), collectType));
            }
            if (status != null && !"全部".equals(status) && !status.isEmpty()) {
                ps.add(cb.equal(root.get("status"), status));
            }
            return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        if ("使用次数".equals(sortType)) {
            sort = Sort.by(Sort.Direction.DESC, "usageCount");
        }
        Pageable pageable = PageRequest.of(Math.max(0, page0), Math.max(1, size), sort);
        return repo.findAll(spec, pageable);
    }

    public Optional<Template> findById(Long id) { return repo.findById(id); }
    public Template save(Template t) { return repo.save(t); }
    public void delete(Long id) { repo.deleteById(id); }
}
