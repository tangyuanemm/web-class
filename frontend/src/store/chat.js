import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import ChatSocket from '@/utils/websocket'
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
    return messages.value[activeContact.value.id] || []
  })

  let socket = null

  // 连接 WebSocket
  function connect(userId, token) {
    if (socket) socket.close()
    socket = new ChatSocket(userId, token)

    socket.onMessage((msg) => {
      if (msg.type === 'chat') {
        const contactId = msg.from
        addMessage(contactId, {
          id: msg.msgId || Date.now().toString(),
          from: msg.from,
          to: msg.to,
          content: msg.content,
          contentType: msg.contentType || 'text',
          timestamp: msg.timestamp || Date.now(),
          direction: 'in'
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
      id: Date.now().toString(),
      from: 'me',
      to: activeContact.value.id,
      content,
      contentType,
      timestamp: Date.now(),
      direction: 'out'
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

  // 切换联系人
  function setActiveContact(contact) {
    activeContact.value = contact
    if (!messages.value[contact.id]) {
      loadLocalHistory(contact.id)
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
    connect,
    sendMessage,
    setActiveContact,
    loadLocalHistory,
    disconnect
  }
})
