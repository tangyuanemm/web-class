# 仿QQ Web即时聊天系统

> 湖南科技大学 2024级 综合实践实训（一）课程设计
>
> **二人小组** | 前端：Vue 3 + Vite | 后端：Spring Boot + WebSocket + MySQL

## 项目结构

```
├── frontend/          # 前端 — Vue 3 + Vite
│   ├── src/           # 源代码
│   ├── doc/           # API 接口文档（后端必读！）
│   └── ...
├── backend/           # 后端 — Spring Boot（待添加）
└── README.md
```

## 分支策略

| 分支 | 用途 |
|------|------|
| `main` | 最终可运行代码（稳定版本） |
| `frontend` | 前端日常开发 |
| `backend` | 后端日常开发 |

## 快速开始（前端）

```bash
cd frontend
npm install
npm run dev        # Mock 模式，不依赖后端
```

## API 接口文档

👉 [doc/API接口规范.md](frontend/doc/API接口规范.md)

后端队友请严格按此文档开发！
