# 仿QQ Web聊天系统 — API 接口规范

> **给队友：请严格按照此文档开发后端接口。任何修改先沟通！**
>
> 通用约定：
> - 基础路径：`/api`
> - 认证方式：HTTP Header `Authorization: Bearer <token>`
> - 返回格式：JSON `{ "code": 200, "msg": "success", "data": ... }`

---

## 一、用户模块（HTTP）

### 1.1 注册
```
POST /api/user/register
Content-Type: application/json

请求：
{
  "username": "zhangsan",
  "password": "123456",
  "nickname": "张三"
}

返回成功：
{ "code": 200, "msg": "注册成功", "data": null }

返回失败：
{ "code": 400, "msg": "用户名已存在", "data": null }
```

### 1.2 登录
```
POST /api/user/login
Content-Type: application/json

请求：
{
  "username": "zhangsan",
  "password": "123456"
}

返回成功：
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": "user_001",
      "username": "zhangsan",
      "nickname": "张三",
      "avatar": ""
    }
  }
}
```

### 1.3 获取联系人列表
```
GET /api/user/contacts
Authorization: Bearer <token>

返回成功：
{
  "code": 200,
  "data": [
    { "id": "user_002", "username": "lisi", "nickname": "李四", "avatar": "", "online": true },
    { "id": "user_003", "username": "wangwu", "nickname": "王五", "avatar": "", "online": false }
  ]
}
```

### 1.4 搜索用户
```
GET /api/user/search?keyword=张

返回成功：
{
  "code": 200,
  "data": [
    { "id": "user_001", "username": "zhangsan", "nickname": "张三", "avatar": "" }
  ]
}
```

---

## 二、聊天记录模块（HTTP）

### 2.1 获取历史消息
```
GET /api/chat/history?with=user_002&page=1&size=50
Authorization: Bearer <token>

返回成功：
{
  "code": 200,
  "data": {
    "total": 120,
    "page": 1,
    "size": 50,
    "list": [
      {
        "msgId": "msg_001",
        "from": "user_002",
        "to": "user_001",
        "content": "在吗？",
        "contentType": "text",
        "timestamp": 1717833600000
      }
    ]
  }
}
```

---

## 三、文件上传模块（HTTP）

### 3.1 上传图片
```
POST /api/upload/image
Authorization: Bearer <token>
Content-Type: multipart/form-data

参数：file (图片文件，限制 10MB，支持 jpg/png/gif/webp)

返回成功：
{
  "code": 200,
  "data": { "url": "/uploads/images/20240608_abc123.jpg" }
}
```

---

## 四、WebSocket 即时通讯协议（核心）

### 4.1 连接
```
ws://localhost:8080/chat?userId=user_001&token=<token>
```
服务端验证 token，查询参数传递 userId 和 token。

连接成功后，服务端应将用户标记为"在线"，并广播上线通知给其所有联系人。

### 4.2 客户端 → 服务端

#### 发送聊天消息
```json
{
  "type": "chat",
  "to": "user_002",
  "content": "你好！",
  "contentType": "text"
}
```
contentType 可选值：`text` | `emoji` | `image`

#### 心跳
```json
{ "type": "ping" }
```

#### 正在输入（可选）
```json
{ "type": "typing", "to": "user_002" }
```

### 4.3 服务端 → 客户端

#### 收到新消息（转发给接收方）
```json
{
  "type": "chat",
  "msgId": "msg_12345",
  "from": "user_001",
  "to": "user_002",
  "content": "你好！",
  "contentType": "text",
  "timestamp": 1717833600000
}
```

#### 消息回执（发给发送方，确认送达）
```json
{
  "type": "ack",
  "msgId": "msg_12345",
  "status": "delivered"
}
```

#### 系统通知（上线/下线广播）
```json
{
  "type": "system",
  "userId": "user_002",
  "content": "user_002 已上线",
  "timestamp": 1717833600000
}
```

#### 心跳响应
```json
{ "type": "pong" }
```

---

## 五、服务端要求总结

| 功能 | 说明 |
|------|------|
| **用户认证** | JWT token，登录后所有请求带 Authorization header |
| **WebSocket** | 使用 Spring WebSocket，根据 URL 参数认证 |
| **消息持久化** | 聊天消息存入 MySQL，GET /api/chat/history 可查历史 |
| **在线状态** | 用户 WebSocket 连接断开时标记离线，广播通知联系人 |
| **图片上传** | 保存到服务器本地磁盘或 OSS，返回可访问的 URL |
| **跨域** | 添加 CORS 配置，允许前端开发端口（5173）的请求 |

---

## 六、状态码约定

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 / 业务错误 |
| 401 | 未登录 / token 过期 |
| 403 | 无权限 |
| 500 | 服务器错误 |
