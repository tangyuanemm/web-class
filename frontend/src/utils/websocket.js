/**
 * WebSocket 连接管理
 * - 自动重连（最多 5 次）
 * - 心跳保活
 * - 消息回调
 */

const MAX_RETRY = 5
const HEARTBEAT_INTERVAL = 30 * 1000 // 30秒

export default class ChatSocket {
  constructor(userId, token) {
    this.userId = userId
    this.token = token
    this.retryCount = 0
    this.handlers = {}
    this.heartbeatTimer = null
    this._connect()
  }

  _connect() {
    // WebSocket URL — 联调时改成队友的后端地址
    const host = import.meta.env.VITE_WS_URL || 'ws://localhost:8080'
    const url = `${host}/chat?userId=${this.userId}&token=${this.token}`

    console.log('[WS] 正在连接:', url)
    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      console.log('[WS] 连接成功')
      this.retryCount = 0
      this._startHeartbeat()
    }

    this.ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        this._dispatch(msg)
      } catch (e) {
        console.warn('[WS] 消息解析失败:', event.data)
      }
    }

    this.ws.onclose = (e) => {
      console.log('[WS] 连接关闭, 原因:', e.code, e.reason)
      this._stopHeartbeat()
      this._retry()
    }

    this.ws.onerror = (e) => {
      console.error('[WS] 连接错误:', e)
    }
  }

  // 注册消息回调
  onMessage(fn) { this.handlers['chat'] = fn }
  onSystem(fn) { this.handlers['system'] = fn }
  onAck(fn) { this.handlers['ack'] = fn }

  _dispatch(msg) {
    const handler = this.handlers[msg.type]
    if (handler) {
      handler(msg)
    } else {
      console.log('[WS] 未处理的消息类型:', msg.type, msg)
    }
  }

  // 发送消息
  send(to, content, contentType = 'text') {
    if (this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({
        type: 'chat',
        to,
        content,
        contentType
      }))
    } else {
      console.warn('[WS] 连接未就绪，消息发送失败')
    }
  }

  // 心跳保活
  _startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'ping' }))
      }
    }, HEARTBEAT_INTERVAL)
  }

  _stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  // 断线重连
  _retry() {
    if (this.retryCount >= MAX_RETRY) {
      console.error('[WS] 重连次数已用尽，放弃重连')
      return
    }
    const delay = Math.min(1000 * Math.pow(2, this.retryCount), 30000)
    this.retryCount++
    console.log(`[WS] ${delay}ms 后进行第 ${this.retryCount} 次重连`)
    setTimeout(() => this._connect(), delay)
  }

  close() {
    this._stopHeartbeat()
    this.retryCount = MAX_RETRY // 阻止自动重连
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }
}
