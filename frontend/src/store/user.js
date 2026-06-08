import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref(JSON.parse(localStorage.getItem('chat_user') || 'null'))
  const token = ref(localStorage.getItem('chat_token') || '')
  const isLoggedIn = computed(() => !!currentUser.value && !!token.value)

  function login(user, t) {
    currentUser.value = user
    token.value = t
    localStorage.setItem('chat_user', JSON.stringify(user))
    localStorage.setItem('chat_token', t)
  }

  function logout() {
    currentUser.value = null
    token.value = ''
    localStorage.removeItem('chat_user')
    localStorage.removeItem('chat_token')
  }

  return { currentUser, token, isLoggedIn, login, logout }
})
