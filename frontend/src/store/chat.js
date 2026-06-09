import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import ChatSocket from '@/utils/websocket'
import api from '@/utils/api'
import { saveMessage, loadHistory } from '@/utils/storage'

export const useChatStore = defineStore('chat', () => {
  // 联系人列表
  const contacts = ref([])
  // 当前正在聊天的人
  const activeContact = ref(null)
  // 当前聊天消息列表 (key: userId, value: messages[])
  const messages = ref({})

  const currentMessages = computed(() => {
    if (!activeContact.value) return []
    const raw = messages.value[activeContact.value.id] || []
    // 动态注入 direction，根据当前用户 ID 判断
    return raw.map(m => ({
      ...m,
      direction: m.from === _currentUserId ? 'out' : 'in'
    }))
  })

  let socket = null
  let _currentUserId = null
  const wsConnected = ref(false)

  // 连接 WebSocket
  function connect(userId, token) {
    _currentUserId = userId
    if (socket) { try { socket.close() } catch (_) {} }
    socket = new ChatSocket(userId, token)

    socket.onStatusChange((connected) => {
      wsConnected.value = connected
    })

    socket.onMessage((msg) => {
      if (msg.type === 'chat') {
        // 收消息：对方发来的，contactId 是发送者
        const contactId = msg.from === userId ? msg.to : msg.from
        addMessage(contactId, {
          id: msg.msgId || ('msg_' + Date.now()),
          from: msg.from,
          to: msg.to,
          content: msg.content,
          contentType: msg.contentType || 'text',
          timestamp: msg.timestamp || Date.now()
        })
        saveMessage(contactId, messages.value[contactId])
      }
    })

    socket.onSystem((msg) => {
      console.log('系统消息:', msg.content)
    })
  }

  // 发送消息
  function sendMessage(content, contentType = 'text') {
    if (!activeContact.value || !socket) return

    const msg = {
      id: 'msg_' + Date.now(),
      from: _currentUserId,
      to: activeContact.value.id,
      content,
      contentType,
      timestamp: Date.now()
    }

    addMessage(activeContact.value.id, msg)
    socket.send(activeContact.value.id, content, contentType)
    saveMessage(activeContact.value.id, messages.value[activeContact.value.id])
  }

  // 添加消息
  function addMessage(contactId, msg) {
    if (!messages.value[contactId]) {
      messages.value[contactId] = []
    }
    messages.value[contactId].push(msg)
  }

  // 加载本地历史消息
  function loadLocalHistory(contactId) {
    const history = loadHistory(contactId)
    if (history.length > 0) {
      messages.value[contactId] = history
    }
  }

  const _historyLoaded = ref({}) // 记录已从后端加载过的联系人

  // 切换联系人
  async function setActiveContact(contact) {
    activeContact.value = contact

    // 先加载本地缓存
    if (!messages.value[contact.id]) {
      loadLocalHistory(contact.id)
    }

    // 从后端拉取历史（每个联系人只加载一次）
    if (import.meta.env.VITE_USE_MOCK === 'false' && !_historyLoaded.value[contact.id]) {
      _historyLoaded.value[contact.id] = true
      try {
        const data = await api.getHistory(contact.id)
        const serverMsgs = (data.list || []).map(m => ({
          id: m.msgId,
          from: m.from,
          to: m.to,
          content: m.content,
          contentType: m.contentType || 'text',
          timestamp: m.timestamp
        }))
        // 合并去重，按时间排序
        const existingIds = new Set((messages.value[contact.id] || []).map(m => m.id))
        const merged = [
          ...(messages.value[contact.id] || []),
          ...serverMsgs.filter(m => !existingIds.has(m.id))
        ]
        merged.sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0))
        messages.value[contact.id] = merged
        saveMessage(contact.id, merged)
      } catch (e) {
        console.warn('加载历史消息失败:', e)
      }
    }
  }

  // 断开连接
  function disconnect() {
    if (socket) {
      socket.close()
      socket = null
    }
  }

  return {
    contacts,
    activeContact,
    messages,
    currentMessages,
    wsConnected,
    connect,
    sendMessage,
    setActiveContact,
    loadLocalHistory,
    disconnect
  }
})
