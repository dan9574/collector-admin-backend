package com.example.demo.demos.web.template;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TemplateRepository
        extends JpaRepository<Template, Long>, JpaSpecificationExecutor<Template> {
}
