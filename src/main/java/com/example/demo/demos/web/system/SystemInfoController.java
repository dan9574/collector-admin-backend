package com.example.demo.demos.web.system;

import org.springframework.boot.SpringBootVersion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class SystemInfoController {

    @GetMapping("/api/_env")
    public Map<String, Object> env() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("javaVersion", System.getProperty("java.version"));
        m.put("springBootVersion", SpringBootVersion.getVersion());
        m.put("frontendMode", "static-cdn"); // 现在这种模式
        m.put("react", "18.2.0 (UMD via CDN)");
        m.put("antd", "5.18.3 (UMD via CDN)");
        m.put("router", "custom-hash-router (no react-router)");
        m.put("nginx", Boolean.FALSE);
        m.put("nodeUsed", Boolean.FALSE);
        return m;
    }
}
