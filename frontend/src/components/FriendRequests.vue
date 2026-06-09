<template>
  <div class="friend-requests">
    <!-- 标题栏 -->
    <div class="requests-header" @click="expanded = !expanded">
      <span>🔔 好友请求</span>
      <span v-if="requests.length > 0" class="badge">{{ requests.length }}</span>
      <span class="arrow">{{ expanded ? '▾' : '▸' }}</span>
    </div>

    <!-- 请求列表 -->
    <div v-if="expanded" class="requests-body">
      <div v-if="requests.length === 0" class="empty">暂无待处理请求</div>
      <div
        v-for="r in requests"
        :key="r.id"
        class="request-item"
      >
        <span class="req-avatar">{{ r.nickname.charAt(0) }}</span>
        <div class="req-info">
          <div class="req-name">{{ r.nickname }}</div>
          <div class="req-username">@{{ r.username }}</div>
        </div>
        <div class="req-actions">
          <button class="btn-accept" @click="handleAccept(r)">接受</button>
          <button class="btn-reject" @click="handleReject(r)">拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/utils/api'

const emit = defineEmits(['changed'])

const requests = ref([])
const expanded = ref(false)

onMounted(() => loadRequests())

async function loadRequests() {
  try {
    requests.value = await api.getFriendRequests()
    if (requests.value.length > 0) {
      expanded.value = true
    }
  } catch (e) {
    console.warn('加载好友请求失败:', e)
  }
}

async function handleAccept(r) {
  try {
    await api.acceptFriendRequest(r.id)
    requests.value = requests.value.filter(x => x.id !== r.id)
    emit('changed')
  } catch (e) {
    alert(e.message)
  }
}

async function handleReject(r) {
  try {
    await api.rejectFriendRequest(r.id)
    requests.value = requests.value.filter(x => x.id !== r.id)
  } catch (e) {
    alert(e.message)
  }
}

defineExpose({ loadRequests })
</script>

<style scoped>
.friend-requests {
  border-bottom: 1px solid #e0e0e0;
}

.requests-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  user-select: none;
}

.requests-header:hover {
  background: #f5f5f5;
}

.badge {
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 10px;
  font-weight: 600;
}

.arrow { margin-left: auto; color: #bbb; }

.requests-body {
  padding: 0 12px 8px;
}

.request-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 4px;
  border-top: 1px solid #f5f5f5;
}

.req-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #a0cfff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.req-info { flex: 1; min-width: 0; }
.req-name { font-size: 13px; font-weight: 500; }
.req-username { font-size: 11px; color: #aaa; }

.req-actions { display: flex; gap: 4px; flex-shrink: 0; }

.btn-accept, .btn-reject {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 11px;
  cursor: pointer;
}

.btn-accept { background: #67c23a; color: #fff; }
.btn-accept:hover { background: #5daf34; }
.btn-reject { background: #f0f0f0; color: #999; }
.btn-reject:hover { background: #e0e0e0; }

.empty { text-align: center; color: #ccc; font-size: 12px; padding: 8px; }
</style>
