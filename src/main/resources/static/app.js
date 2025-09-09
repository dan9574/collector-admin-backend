// app.js（完整替换：无 react-router 依赖，内置极简 Hash 路由，基于 Cookie 会话）
(function () {
    const mount = document.getElementById('root');

    // 动态加载脚本/样式（容错）
    function load(url) {
        return new Promise((resolve, reject) => {
            const s = document.createElement('script');
            s.src = url; s.defer = true;
            s.onload = resolve; s.onerror = () => reject(new Error('load fail: ' + url));
            document.head.appendChild(s);
        });
    }
    function loadCss(href){
        const l = document.createElement('link');
        l.rel = 'stylesheet'; l.href = href;
        document.head.appendChild(l);
    }

    async function boot() {
        try {
            // 兜底加载（多数情况下 index.html 已经加载好了）
            if (!window.React)    await load('https://cdn.jsdelivr.net/npm/react@18.2.0/umd/react.production.min.js');
            if (!window.ReactDOM) await load('https://cdn.jsdelivr.net/npm/react-dom@18.2.0/umd/react-dom.production.min.js');
            if (!window.antd) {
                await load('https://cdn.jsdelivr.net/npm/antd@5.18.3/dist/antd.min.js');
                loadCss('https://cdn.jsdelivr.net/npm/antd@5.18.3/dist/reset.css');
            }

            // ===== 极简 Hash 路由（替代 react-router）=====
            function useHashLocation() {
                const { useEffect, useState } = React;
                const [path, setPath] = useState(() => {
                    if (!location.hash) location.hash = '#/';
                    return location.hash.slice(1) || '/';
                });
                useEffect(() => {
                    const onHashChange = () => setPath(location.hash.slice(1) || '/');
                    window.addEventListener('hashchange', onHashChange);
                    return () => window.removeEventListener('hashchange', onHashChange);
                }, []);
                const navigate = (to, { replace = false } = {}) => {
                    const target = to.startsWith('#') ? to.slice(1) : to.replace(/^\/?/, '');
                    if (replace) location.replace('#' + target);
                    else location.hash = target;
                };
                return [path, navigate];
            }
            function Router({ routes, fallback }) {
                const [path] = useHashLocation();
                // 精确匹配，否则用通配 '*'
                const exact = routes.find(r => r.path === path);
                if (exact) return exact.element;
                const star = routes.find(r => r.path === '*');
                return star ? star.element : (fallback || null);
            }
            function Navigate({ to, replace = true }) {
                const target = to.startsWith('#') ? to.slice(1) : to.replace(/^\/?/, '');
                if (replace) location.replace('#' + target); else location.hash = target;
                return null;
            }
            function Outlet({ children }) { return children || null; }

            // ===== React / AntD 引用 =====
            const { createContext, useContext, useEffect, useMemo, useState } = React;
            const { createRoot } = ReactDOM;
            const { ConfigProvider, Layout, Menu, Card, Form, Input, Button, message, Spin, Tag, Table, Select, Alert } = antd;

            // ===== API（与后端交互）=====  ★★★ 关键改造：基于 Cookie，会自动带 sid
            async function api(path, { method = 'GET', headers = {}, body } = {}) {
                const resp = await fetch('/api' + path, {
                    method,
                    credentials: 'include', // 让浏览器自动携带 HttpOnly 的 sid
                    headers: {
                        'Content-Type': 'application/json',
                        ...headers
                    },
                    body: body ? JSON.stringify(body) : undefined
                });
                if (resp.status === 401) {
                    // 未登录或会话失效
                    throw new Error('unauthorized');
                }
                // 兼容非 JSON 场景
                const ct = resp.headers.get('content-type') || '';
                if (ct.includes('application/json')) return resp.json();
                return resp.text();
            }

            // ===== Auth 上下文（纯 Cookie 会话） =====
            const AuthCtx = createContext(null);
            function AuthProvider({ children }) {
                const [user, setUser] = useState(null);
                const [loading, setLd] = useState(true);

                // 启动时直接尝试拉 /profile，判断是否已登录（会自动带 sid）
                useEffect(() => {
                    api('/profile')
                        .then(d => setUser(d))
                        .catch(() => setUser(null))
                        .finally(() => setLd(false));
                }, []);

                // 登录：成功即可，随后再拉一次 profile 获取用户信息
                const login = async (username, password) => {
                    const data = await api('/login', { method: 'POST', body: { username, password } });
                    if (!data || !data.success) {
                        throw new Error((data && data.message) || '登录失败');
                    }
                    // 登录成功后，浏览器已通过 Set-Cookie 收到 sid；再拿个人信息
                    try {
                        const me = await api('/profile');
                        setUser(me);
                    } catch {
                        // 兜底：至少展示用户名
                        setUser({ username: data.username || username, role: data.role || 'USER' });
                    }
                };

                // 退出：本地清除 sid（同时建议后端提供 /logout 以删除服务端会话）
                const logout = async () => {
                    // 可选：若后端有 /logout，请解开下一行：
                    // try { await api('/logout', { method: 'POST' }); } catch {}
                    document.cookie = 'sid=; Max-Age=0; path=/'; // 清浏览器端
                    setUser(null);
                    location.hash = '/login';
                };

                const v = useMemo(() => ({ user, loading, login, logout }), [user, loading]);
                return React.createElement(AuthCtx.Provider, { value: v }, children);
            }
            const useAuth = () => useContext(AuthCtx);

            // ===== 受保护包装 =====
            function Protected({ requireRole, children }) {
                const { user, loading } = useAuth();
                if (loading) return React.createElement('div', { style: { padding: 40, textAlign: 'center' } }, React.createElement(Spin));
                if (!user) return React.createElement(Navigate, { to: '/login', replace: true });
                if (requireRole && user.role !== requireRole) return React.createElement(Navigate, { to: '/', replace: true });
                return children || React.createElement(Outlet);
            }

            // ===== 布局 =====
            function AppLayout({ children }) {
                const { Header, Sider, Content } = Layout;
                const { user, logout } = useAuth();
                const current = (location.hash.slice(1) || '/');
                const items = [
                    { key: '/', label: '仪表盘' },
                    { key: '/projects', label: '项目' },
                    ...(user?.role === 'ADMIN' ? [{ key: '/system', label: '系统' }] : [])
                ];
                return React.createElement(Layout, { style: { minHeight: '100%' } },
                    React.createElement(Sider, null,
                        React.createElement(Menu, {
                            theme: 'dark', mode: 'inline',
                            selectedKeys: [current],
                            items,
                            onClick: e => { location.hash = e.key; }
                        })
                    ),
                    React.createElement(Layout, null,
                        React.createElement(Header, {
                                style: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#fff' }
                            },
                            React.createElement('div', null, '数据采集平台'),
                            React.createElement('div', null,
                                (user && user.username) || '',
                                ' | ',
                                React.createElement('a', { onClick: logout, style: { color: '#fff', cursor: 'pointer' } }, '退出')
                            )
                        ),
                        React.createElement(Content, { style: { padding: 16 } }, children)
                    )
                );
            }

            // ===== 页面 =====
            function LoginPage() {
                const { login } = useAuth();
                const onFinish = async (v) => {
                    try {
                        await login(v.username, v.password);
                        location.hash = '/';
                    } catch (e) {
                        console.error(e);
                        message.error(e.message === 'unauthorized' ? '用户名或密码错误' : '登录失败');
                    }
                };
                return React.createElement('div', { style: { display: 'grid', placeItems: 'center', height: '100%' } },
                    React.createElement(Card, { title: '登录', style: { width: 360 } },
                        React.createElement(Form, { layout: 'vertical', onFinish },
                            React.createElement(Form.Item, { name: 'username', label: '账户', rules: [{ required: true }] }, React.createElement(Input)),
                            React.createElement(Form.Item, { name: 'password', label: '密码', rules: [{ required: true }] }, React.createElement(Input.Password)),
                            React.createElement(Button, { type: 'primary', htmlType: 'submit', block: true }, '登录')
                        )
                    )
                );
            }

            function Dashboard() {
                return React.createElement(Card, { title: '仪表盘（示例）' },
                    React.createElement('div', { style: { marginBottom: 8 } }, '区域选择（AntD 下拉框 Select）：'),
                    React.createElement(Select, {
                        mode: 'multiple', allowClear: true, style: { minWidth: 280 },
                        options: [
                            { label: '华东(上海)', value: 'sh' },
                            { label: '华南(广州)', value: 'gz' },
                            { label: '华北(北京)', value: 'bj' },
                            { label: '西南(成都)', value: 'cd' },
                        ],
                        placeholder: '请选择区域'
                    })
                );
            }

            function Projects() {
                const data = [
                    { id: 1, name: '小红书采集', status: 'RUNNING' },
                    { id: 2, name: '大众点评采集', status: 'PENDING' },
                ];
                const columns = [
                    { title: 'ID', dataIndex: 'id' },
                    { title: '名称', dataIndex: 'name' },
                    {
                        title: '状态', dataIndex: 'status',
                        render: s => React.createElement(Tag, { color: s === 'RUNNING' ? 'green' : 'blue' }, s)
                    }
                ];
                return React.createElement(Card, { title: '项目' },
                    React.createElement(Table, { rowKey: 'id', dataSource: data, columns })
                );
            }

            function SystemPage() {
                return React.createElement(Card, { title: '系统管理（仅 ADMIN 可见）' },
                    React.createElement(Alert, { type: 'info', message: '这里后续接 代理池、用户管理等模块。' })
                );
            }

            // ===== App（使用内置路由）=====
            function App() {
                const routes = [
                    { path: '/login', element: React.createElement(LoginPage) },

                    // 受保护区域
                    { path: '/', element:
                            React.createElement(Protected, null,
                                React.createElement(AppLayout, null, React.createElement(Dashboard))
                            )
                    },
                    { path: '/projects', element:
                            React.createElement(Protected, null,
                                React.createElement(AppLayout, null, React.createElement(Projects))
                            )
                    },
                    { path: '/system', element:
                            React.createElement(Protected, { requireRole: 'ADMIN' },
                                React.createElement(AppLayout, null, React.createElement(SystemPage))
                            )
                    },

                    // 兜底
                    { path: '*', element: React.createElement(Navigate, { to: '/', replace: true }) },
                ];

                return React.createElement(ConfigProvider, null,
                    React.createElement(AuthProvider, null,
                        React.createElement(Router, { routes })
                    )
                );
            }

            // ===== 挂载 =====
            if (!ReactDOM.createRoot && typeof ReactDOM.render === 'function') {
                ReactDOM.createRoot = function(container){
                    return { render: element => ReactDOM.render(element, container) };
                };
            }
            if (typeof ReactDOM.createRoot !== 'function') {
                throw new Error('React 18 createRoot 不可用，请检查 React/ReactDOM 版本与加载顺序。');
            }
            ReactDOM.createRoot(mount).render(React.createElement(App));

        } catch (e) {
            mount.innerHTML = '<pre style="white-space:pre-wrap;padding:12px;background:#111;color:#0f0">' +
                (e && (e.stack || e.message) || e) + '</pre>';
        }
    }

    boot().catch(e => {
        mount.innerHTML = '<pre style="white-space:pre-wrap;padding:12px;background:#111;color:#0f0">' +
            ('Boot error: ' + (e && (e.stack || e.message) || e)) + '</pre>';
    });
})();
