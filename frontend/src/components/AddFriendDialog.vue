<template>
  <div class="overlay" @click.self="$emit('close')">
    <div class="dialog">
      <div class="dialog-header">
        <h3>添加好友</h3>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <!-- 搜索 -->
      <div class="search-row">
        <input
          v-model="keyword"
          type="text"
          placeholder="输入用户名或昵称搜索..."
          @keydown.enter="handleSearch"
        />
        <button class="btn-search" @click="handleSearch" :disabled="!keyword.trim()">
          搜索
        </button>
      </div>

      <!-- 搜索结果 -->
      <div class="results" v-if="results.length > 0">
        <div
          v-for="u in results"
          :key="u.id"
          class="user-row"
        >
          <div class="user-info">
            <span class="avatar">{{ u.nickname.charAt(0) }}</span>
            <div>
              <div class="name">{{ u.nickname }}</div>
              <div class="username-sub">@{{ u.username }}</div>
            </div>
          </div>
          <button
            class="btn-add"
            :disabled="u.sent"
            @click="handleSendRequest(u)"
          >
            {{ u.sent ? '已发送' : '加好友' }}
          </button>
        </div>
      </div>

      <div class="empty" v-else-if="searched && results.length === 0">
        未找到用户
      </div>

      <p class="msg" v-if="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/utils/api'

const emit = defineEmits(['close', 'added'])

const keyword = ref('')
const results = ref([])
const searched = ref(false)
const message = ref('')

async function handleSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  searched.value = true
  message.value = ''
  try {
    results.value = await api.searchUser(kw)
    results.value.forEach(u => { u.sent = false })
  } catch (e) {
    message.value = e.message
    results.value = []
  }
}

async function handleSendRequest(u) {
  try {
    await api.sendFriendRequest(u.id)
    u.sent = true
    message.value = '好友请求已发送给 ' + u.nickname
    emit('added')
  } catch (e) {
    message.value = e.message
  }
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.dialog {
  width: 420px;
  max-height: 520px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.btn-close {
  width: 28px;
  height: 28px;
  border: none;
  background: none;
  font-size: 16px;
  cursor: pointer;
  border-radius: 4px;
  color: #999;
}

.btn-close:hover { background: #f0f0f0; }

.search-row {
  display: flex;
  gap: 8px;
  padding: 16px 20px;
}

.search-row input {
  flex: 1;
  height: 38px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.search-row input:focus { border-color: #667eea; }

.btn-search {
  padding: 0 20px;
  border: none;
  border-radius: 6px;
  background: #667eea;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.btn-search:disabled { opacity: 0.5; cursor: not-allowed; }

.results {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px;
}

.user-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #667eea;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.name { font-weight: 500; font-size: 14px; }
.username-sub { font-size: 12px; color: #999; }

.btn-add {
  padding: 6px 16px;
  border: 1px solid #667eea;
  border-radius: 4px;
  background: #fff;
  color: #667eea;
  font-size: 13px;
  cursor: pointer;
}

.btn-add:hover { background: #667eea; color: #fff; }
.btn-add:disabled { opacity: 0.4; cursor: not-allowed; }

.empty { text-align: center; color: #ccc; padding: 40px; }
.msg { text-align: center; color: #67c23a; padding: 12px 20px; font-size: 13px; }
</style>
