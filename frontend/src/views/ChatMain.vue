<template>
  <div class="chat-main">
    <!-- 左侧：联系人列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <span class="user-nickname">{{ userStore.currentUser?.nickname }}</span>
        <div class="sidebar-actions">
          <button class="btn-add-friend" @click="showAddFriend = true" title="添加好友">+</button>
          <button v-if="isAdmin" class="btn-admin" @click="goAdmin">管理</button>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </div>
      </div>

      <!-- 好友请求通知 -->
      <FriendRequests ref="friendRequestsRef" @changed="loadContacts" />

      <!-- 连接状态 -->
      <div class="conn-status" :class="{ connected: wsConnected }">
        {{ wsConnected ? '🟢 已连接' : '🔴 未连接' }}
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
          <span v-if="chatStore.activeContact.online" class="online-tag">在线</span>
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

    <!-- 添加好友弹窗 -->
    <AddFriendDialog
      v-if="showAddFriend"
      @close="showAddFriend = false"
      @added="onFriendAdded"
    />
  </div>
</template>

<script setup>
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import ContactList from '@/components/ContactList.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import AddFriendDialog from '@/components/AddFriendDialog.vue'
import FriendRequests from '@/components/FriendRequests.vue'
import api from '@/utils/api'
import { isAdminUser } from '@/utils/mock-auth'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const isAdmin = computed(() => isAdminUser(userStore.currentUser))
const wsConnected = computed(() => chatStore.wsConnected)
const showAddFriend = ref(false)
const friendRequestsRef = ref(null)

function goAdmin() {
  router.push('/admin')
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  chatStore.connect(userStore.currentUser.id, userStore.token)
  await loadContacts()
})

async function loadContacts() {
  if (import.meta.env.VITE_USE_MOCK !== 'false') {
    chatStore.contacts = [
      { id: 'user_001', nickname: '张三', avatar: '', online: true },
      { id: 'user_002', nickname: '李四', avatar: '', online: true },
      { id: 'user_003', nickname: '王五', avatar: '', online: false }
    ]
    return
  }
  try {
    chatStore.contacts = await api.getFriendList()
  } catch (e) {
    console.error('加载联系人失败:', e)
  }
}

function onFriendAdded() {
  friendRequestsRef.value?.loadRequests()
}

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

.sidebar-actions {
  display: flex;
  gap: 6px;
}

.btn-add-friend {
  width: 30px;
  height: 30px;
  border: 1px dashed #ccc;
  border-radius: 50%;
  background: #fff;
  color: #999;
  cursor: pointer;
  font-size: 18px;
  font-weight: 300;
  line-height: 28px;
  text-align: center;
  padding: 0;
}

.btn-add-friend:hover {
  border-color: #667eea;
  color: #667eea;
  background: #f5f7ff;
}

.btn-admin {
  padding: 6px 14px;
  border: 1px solid #667eea;
  border-radius: 4px;
  background: #fff;
  color: #667eea;
  cursor: pointer;
  font-size: 12px;
}

.btn-admin:hover {
  background: #667eea;
  color: #fff;
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

/* 连接状态 */
.conn-status {
  padding: 6px 16px;
  font-size: 11px;
  color: #f56c6c;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.conn-status.connected {
  color: #67c23a;
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
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
}

.online-tag {
  font-size: 11px;
  font-weight: 400;
  color: #67c23a;
  background: #f0f9eb;
  padding: 2px 10px;
  border-radius: 10px;
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
