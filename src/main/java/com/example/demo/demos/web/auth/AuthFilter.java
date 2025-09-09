package com.example.demo.demos.web.auth;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

@Component
@Order(1)
public class AuthFilter implements Filter {

    private final SessionStore sessionStore;

    public AuthFilter(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 1) 预检直接放行
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 2) 白名单：静态资源 + 首页/登录页 + 登录接口
        String uri = req.getRequestURI();
        if (isPublic(uri, req)) {
            chain.doFilter(request, response);
            return;
        }

        // 3) 校验会话
        String sid = extractToken(req);
        SessionStore.Session sess = (sid == null ? null : sessionStore.get(sid).orElse(null));
        if (sess != null) {
            req.setAttribute("username", sess.username);
            chain.doFilter(request, response);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"error\":\"unauthorized\"}");
        }
    }

    private boolean isPublic(String uri, HttpServletRequest req) {
        // 常见前端静态资源统一放行（注意 /app.js 这类根路径脚本）
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            if (uri.equals("/") ||
                    uri.equals("/index.html") ||
                    uri.equals("/login.html") ||
                    uri.startsWith("/static/") ||
                    uri.startsWith("/assets/") ||
                    uri.startsWith("/favicon") ||
                    uri.endsWith(".js") ||
                    uri.endsWith(".css") ||
                    uri.endsWith(".map") ||
                    uri.endsWith(".ico") ||
                    uri.endsWith(".png") ||
                    uri.endsWith(".jpg") ||
                    uri.endsWith(".jpeg") ||
                    uri.endsWith(".svg") ||
                    uri.endsWith(".webp")) {
                return true;
            }
        }
        // 登录接口放行 + 新增登出放行
        return "/api/login".equals(uri) || "/api/logout".equals(uri);

    }

    private String extractToken(HttpServletRequest req) {
        String authz = req.getHeader("Authorization");
        if (authz != null && authz.startsWith("Bearer ")) {
            return authz.substring(7);
        }
        String xtoken = req.getHeader("X-Token");
        if (xtoken != null && !xtoken.isEmpty()) {
            return xtoken;
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("sid".equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }
}
