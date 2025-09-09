package com.example.demo.demos.web.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 演示用控制器：为了避免与 JPA 实体 User 冲突，
 * 这里使用内部类 DemoUser（包含 name/age）。
 */
@Controller
public class BasicController {

    // ---- 仅供本控制器使用的简单用户模型 ----
    public static class DemoUser {
        private String name;
        private Integer age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    // http://127.0.0.1:8081/hello?name=lisi
    @GetMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam(name = "name", defaultValue = "unknown user") String name) {
        return "Hello " + name;
    }

    // http://127.0.0.1:8081/user
    @GetMapping("/user")
    @ResponseBody
    public DemoUser user() {
        DemoUser user = new DemoUser();
        user.setName("theonefx");
        user.setAge(666);
        return user;
    }

    // http://127.0.0.1:8081/save_user?name=newName&age=11
    @GetMapping("/save_user")
    @ResponseBody
    public String saveUser(DemoUser u) {
        return "user will save: name=" + u.getName() + ", age=" + u.getAge();
    }

    // http://127.0.0.1:8081/html  -> 返回视图名（若有模板/静态资源）
    @GetMapping("/html")
    public String html() {
        return "index.html";
    }

    // 统一给 DemoUser 设默认值（可选）
    @ModelAttribute
    public void parseUser(@RequestParam(name = "name", defaultValue = "unknown user") String name,
                          @RequestParam(name = "age", defaultValue = "12") Integer age,
                          DemoUser user) {
        user.setName("zhangsan");
        user.setAge(18);
    }
}
