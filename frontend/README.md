# 仿QQ Web即时聊天系统

> 湖南科技大学 2024级 综合实践实训（一）课程设计
>
> 技术栈：Vue 3 + Vite（前端） + Spring Boot + WebSocket + MySQL（后端）

---

## 项目结构

```
web-chat-frontend/          # 前端（此仓库）
├── src/
│   ├── views/              # 页面
│   │   ├── Login.vue       # 登录/注册
│   │   └── ChatMain.vue    # 聊天主界面
│   ├── components/         # 组件
│   │   ├── ContactList.vue # 联系人列表
│   │   ├── ChatWindow.vue  # 聊天窗口
│   │   ├── MessageBubble.vue # 消息气泡
│   │   └── EmojiPicker.vue # 表情选择器
│   ├── store/              # 状态管理 (Pinia)
│   │   ├── user.js         # 用户状态
│   │   └── chat.js         # 聊天状态
│   ├── utils/              # 工具
│   │   ├── websocket.js    # WebSocket 管理（含断线重连、心跳）
│   │   ├── api.js          # HTTP 请求封装
│   │   └── storage.js      # 本地消息缓存（离线查看历史）
│   ├── router/index.js     # 路由配置（含登录守卫）
│   ├── App.vue
│   └── main.js
├── doc/
│   └── API接口规范.md       # ⭐ 接口文档，后端队友必读！
└── .env                     # 环境配置
```

## 快速开始

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器（Mock 模式，不依赖后端）
npm run dev

# 3. 打开浏览器 http://localhost:5173
```

## Mock 模式 vs 联调模式

| | Mock 模式（默认） | 联调模式 |
|---|---|---|
| 登录 | 任意用户名直接登录 | 调用后端 API |
| 联系人 | 使用假数据 | 从后端获取 |
| 消息 | 仅本地显示 | WebSocket 实时收发 |
| 配置 | `VITE_USE_MOCK=true` | `VITE_USE_MOCK=false` |

**切换到联调模式：**

1. 修改 `.env` 文件：
```
VITE_USE_MOCK=false
```

2. 取消 `vite.config.js` 中 proxy 的注释。

## 已实现功能

- [x] 用户登录/注册页面
- [x] 联系人列表（搜索、在线状态）
- [x] 点对点文本消息聊天（WebSocket）
- [x] 表情发送与显示
- [x] 图片发送与显示
- [x] 本地消息缓存（localStorage，离线可查看历史）
- [x] 消息气泡样式（区分收发方向）
- [x] WebSocket 断线自动重连 + 心跳保活
- [x] 路由守卫（未登录不能进聊天页）
- [ ] 与后端联调
- [ ] 用户权限管理

## 队友协作指南

1. **后端队友请先阅读 `doc/API接口规范.md`**，严格按接口文档开发
2. 前端代码 push 到此仓库的 `frontend` 分支，后端代码 push 到 `backend` 分支
3. 联调时双方坐在实验室一起调试，效率最高
