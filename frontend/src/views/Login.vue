<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">💬 仿QQ Web聊天</h1>
      <p class="login-subtitle">{{ isRegister ? '注册新账号' : '登录你的账号' }}</p>

      <form @submit.prevent="handleSubmit" class="login-form">
        <div class="form-item">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="请输入用户名" required />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" required />
        </div>
        <div class="form-item" v-if="isRegister">
          <label>昵称</label>
          <input v-model="form.nickname" type="text" placeholder="取个昵称吧" required />
        </div>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '处理中...' : (isRegister ? '注册' : '登录') }}
        </button>
      </form>

      <p class="toggle-mode">
        {{ isRegister ? '已有账号？' : '没有账号？' }}
        <a href="#" @click.prevent="toggleMode">{{ isRegister ? '去登录' : '去注册' }}</a>
      </p>

      <p class="error-msg" v-if="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import api from '@/utils/api'
import { mockLogin, mockRegister } from '@/utils/mock-auth'

const router = useRouter()
const userStore = useUserStore()

const isRegister = ref(false)
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: '',
  nickname: ''
})

function toggleMode() {
  isRegister.value = !isRegister.value
  error.value = ''
  form.username = ''
  form.password = ''
  form.nickname = ''
}

async function handleSubmit() {
  loading.value = true
  error.value = ''
  try {
    // ===== Mock 模式：localStorage 模拟真实注册/登录 =====
    if (import.meta.env.VITE_USE_MOCK !== 'false') {
      if (isRegister.value) {
        const result = mockRegister(form.username, form.password, form.nickname)
        userStore.login(result.user, result.token)
      } else {
        const result = mockLogin(form.username, form.password)
        userStore.login(result.user, result.token)
      }
      router.push('/chat')
      return
    }

    // 真实 API 调用
    if (isRegister.value) {
      await api.register(form.username, form.password, form.nickname)
    }
    const data = await api.login(form.username, form.password)
    userStore.login(data.user, data.token)
    router.push('/chat')
  } catch (e) {
    error.value = e.message || '操作失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
}

.login-subtitle {
  text-align: center;
  color: #999;
  margin-bottom: 32px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #555;
}

.form-item input {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.form-item input:focus {
  border-color: #667eea;
}

.btn-primary {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 8px;
  transition: opacity 0.2s;
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toggle-mode {
  text-align: center;
  margin-top: 20px;
  color: #999;
}

.toggle-mode a {
  color: #667eea;
}

.error-msg {
  color: #e74c3c;
  text-align: center;
  margin-top: 12px;
}
</style>
