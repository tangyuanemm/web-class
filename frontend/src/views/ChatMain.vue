<template>
  <div class="chat-main">
    <!-- 左侧：联系人列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <span class="user-nickname">{{ userStore.currentUser?.nickname }}</span>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
      <ContactList
        :contacts="chatStore.contacts"
        :active-id="chatStore.activeContact?.id"
        @select="chatStore.setActiveContact"
      />
    </div>

    <!-- 右侧：聊天窗口 -->
    <div class="chat-area">
      <template v-if="chatStore.activeContact">
        <div class="chat-header">
          <span>{{ chatStore.activeContact.nickname }}</span>
        </div>
        <ChatWindow
          :messages="chatStore.currentMessages"
          :current-user-id="userStore.currentUser?.id"
          @send="handleSend"
        />
      </template>
      <div v-else class="no-contact">
        <div class="no-contact-icon">💬</div>
        <p>请选择一个联系人开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import ContactList from '@/components/ContactList.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import api from '@/utils/api'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

onMounted(async () => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  // 连接 WebSocket
  chatStore.connect(userStore.currentUser.id, userStore.token)

  // ===== Mock 模式：加载假联系人（联调时替换为真实 API）=====
  if (import.meta.env.VITE_USE_MOCK !== 'false') {
    chatStore.contacts = [
      { id: 'user_001', nickname: '张三', avatar: '', online: true },
      { id: 'user_002', nickname: '李四', avatar: '', online: true },
      { id: 'user_003', nickname: '王五', avatar: '', online: false }
    ]
    return
  }

  // 真实 API：加载联系人列表
  try {
    chatStore.contacts = await api.getContacts()
  } catch (e) {
    console.error('加载联系人失败:', e)
  }
})

function handleSend({ content, contentType }) {
  chatStore.sendMessage(content, contentType)
}

function handleLogout() {
  chatStore.disconnect()
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.chat-main {
  display: flex;
  width: 100%;
  height: 100vh;
  background: #fff;
}

/* ===== 左侧栏 ===== */
.sidebar {
  width: 280px;
  min-width: 280px;
  background: #f0f0f0;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
}

.user-nickname {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.btn-logout {
  padding: 6px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  color: #666;
  cursor: pointer;
  font-size: 12px;
}

.btn-logout:hover {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
}

/* ===== 右侧聊天区 ===== */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  height: 56px;
  line-height: 56px;
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
  background: #fff;
}

.no-contact {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #bbb;
}

.no-contact-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.no-contact p {
  font-size: 16px;
}
</style>
