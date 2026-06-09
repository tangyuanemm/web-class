<template>
  <div class="admin-panel">
    <!-- 顶栏 -->
    <div class="admin-header">
      <h1>⚙️ 管理面板</h1>
      <div class="header-right">
        <span class="admin-badge">管理员：{{ userStore.currentUser?.nickname }}</span>
        <button class="btn-back" @click="router.push('/chat')">← 返回聊天</button>
      </div>
    </div>

    <!-- 用户管理 -->
    <div class="section">
      <h2 class="section-title">👥 用户管理</h2>
      <p class="section-desc">共 {{ users.length }} 个注册用户</p>

      <table class="user-table" v-if="users.length > 0">
        <thead>
          <tr>
            <th>用户名</th>
            <th>昵称</th>
            <th>用户 ID</th>
            <th>角色</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id" :class="{ 'is-admin': u.role === 'admin' }">
            <td>
              <span class="username">{{ u.username }}</span>
              <span v-if="u.role === 'admin'" class="tag tag-admin">管理员</span>
            </td>
            <td>{{ u.nickname }}</td>
            <td class="id-cell">{{ u.id }}</td>
            <td>{{ u.role === 'admin' ? '管理员' : '普通用户' }}</td>
            <td class="actions">
              <template v-if="u.role !== 'admin'">
                <button class="btn-action btn-reset" @click="openReset(u)">重置密码</button>
                <button class="btn-action btn-delete" @click="handleDelete(u)">删除</button>
              </template>
              <span v-else class="no-action">—</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else class="empty">暂无用户</div>
    </div>

    <!-- 重置密码弹窗 -->
    <div v-if="resetTarget" class="modal-overlay" @click.self="resetTarget = null">
      <div class="modal-card">
        <h3>重置密码 — {{ resetTarget.username }}</h3>
        <input
          v-model="newPassword"
          type="text"
          class="modal-input"
          placeholder="请输入新密码（至少3位）"
        />
        <div class="modal-actions">
          <button class="btn-confirm" @click="handleReset">确认重置</button>
          <button class="btn-cancel" @click="resetTarget = null">取消</button>
        </div>
        <p class="modal-error" v-if="modalError">{{ modalError }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { isAdminUser, getAllUsers as mockGetUsers, deleteUser as mockDeleteUser, resetUserPassword as mockResetPassword } from '@/utils/mock-auth'
import api from '@/utils/api'

const useRealApi = import.meta.env.VITE_USE_MOCK === 'false'

const router = useRouter()
const userStore = useUserStore()

const users = ref([])
const resetTarget = ref(null)
const newPassword = ref('')
const modalError = ref('')

onMounted(async () => {
  // 非管理员不能访问
  if (!isAdminUser(userStore.currentUser)) {
    router.push('/chat')
    return
  }
  await loadUsers()
})

async function loadUsers() {
  try {
    if (useRealApi) {
      users.value = await api.getAdminUsers()
    } else {
      users.value = mockGetUsers()
    }
  } catch (e) {
    alert('加载用户列表失败: ' + e.message)
  }
}

async function handleDelete(u) {
  if (!confirm(`确定要删除用户「${u.nickname}（${u.username}）」吗？此操作不可撤销。`)) return
  try {
    if (useRealApi) {
      await api.deleteAdminUser(u.username)
    } else {
      mockDeleteUser(u.username)
    }
    await loadUsers()
  } catch (e) {
    alert(e.message)
  }
}

function openReset(u) {
  resetTarget.value = u
  newPassword.value = ''
  modalError.value = ''
}

async function handleReset() {
  modalError.value = ''
  try {
    if (useRealApi) {
      await api.resetAdminPassword(resetTarget.value.username, newPassword.value)
    } else {
      mockResetPassword(resetTarget.value.username, newPassword.value)
    }
    resetTarget.value = null
    alert('密码已重置')
  } catch (e) {
    modalError.value = e.message
  }
}
</script>

<style scoped>
.admin-panel {
  width: 100%;
  height: 100vh;
  background: #f5f6fa;
  overflow-y: auto;
}

/* 顶栏 */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 32px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.admin-header h1 {
  font-size: 22px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-badge {
  color: #667eea;
  font-weight: 500;
}

.btn-back {
  padding: 8px 18px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
}

.btn-back:hover {
  background: #f0f0f0;
}

/* 分区 */
.section {
  margin: 24px 32px;
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.section-desc {
  color: #999;
  font-size: 13px;
  margin-bottom: 20px;
}

/* 表格 */
.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.user-table th {
  font-weight: 500;
  color: #999;
  font-size: 13px;
}

.user-table tr:hover td {
  background: #fafafa;
}

.is-admin td {
  background: #f8f7ff;
}

.username {
  font-weight: 500;
}

.tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  margin-left: 8px;
}

.tag-admin {
  background: #667eea;
  color: #fff;
}

.id-cell {
  color: #bbb;
  font-size: 12px;
  font-family: monospace;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-action {
  padding: 5px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  background: #fff;
}

.btn-reset {
  color: #409eff;
  border-color: #409eff;
}

.btn-reset:hover {
  background: #ecf5ff;
}

.btn-delete {
  color: #f56c6c;
  border-color: #f56c6c;
}

.btn-delete:hover {
  background: #fef0f0;
}

.no-action {
  color: #ccc;
}

.empty {
  text-align: center;
  color: #ccc;
  padding: 40px;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-card {
  background: #fff;
  border-radius: 10px;
  padding: 28px 32px;
  width: 380px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}

.modal-card h3 {
  font-size: 16px;
  margin-bottom: 16px;
}

.modal-input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.modal-input:focus {
  border-color: #667eea;
}

.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}

.btn-confirm,
.btn-cancel {
  flex: 1;
  height: 38px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-confirm {
  background: #667eea;
  color: #fff;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.modal-error {
  color: #f56c6c;
  font-size: 13px;
  margin-top: 10px;
}
</style>
