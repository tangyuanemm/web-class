/**
 * HTTP 请求封装
 * 后端联调时修改 BASE_URL 为队友的后端地址
 */

const BASE_URL = import.meta.env.VITE_API_URL || '/api'

// 获取 token
function getToken() {
  return localStorage.getItem('chat_token') || ''
}

// 通用请求
async function request(method, path, data = null) {
  const url = `${BASE_URL}${path}`
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    }
  }
  if (data && method !== 'GET') {
    options.body = JSON.stringify(data)
  }

  const res = await fetch(url, options)
  const json = await res.json()

  if (json.code !== 200) {
    throw new Error(json.msg || '请求失败')
  }
  return json.data
}

// API 方法
export default {
  // 用户
  login: (username, password) =>
    request('POST', '/user/login', { username, password }),

  register: (username, password, nickname) =>
    request('POST', '/user/register', { username, password, nickname }),

  getContacts: () =>
    request('GET', '/user/contacts'),

  searchUser: (keyword) =>
    request('GET', `/user/search?keyword=${encodeURIComponent(keyword)}`),

  // 聊天记录
  getHistory: (withUserId, page = 1, size = 50) =>
    request('GET', `/chat/history?with=${withUserId}&page=${page}&size=${size}`),

  // 文件上传（需要用 FormData，不走通用 request）
  uploadImage: async (file) => {
    const formData = new FormData()
    formData.append('file', file)
    const res = await fetch(`${BASE_URL}/upload/image`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${getToken()}` },
      body: formData
    })
    const json = await res.json()
    if (json.code !== 200) throw new Error(json.msg || '上传失败')
    return json.data.url
  },

  // ─── 好友接口 ───────────────────
  getFriendList: () =>
    request('GET', '/friend/list'),

  getFriendRequests: () =>
    request('GET', '/friend/requests'),

  sendFriendRequest: (toUserId) =>
    request('POST', '/friend/request', { toUserId }),

  acceptFriendRequest: (requestId) =>
    request('POST', `/friend/accept/${requestId}`),

  rejectFriendRequest: (requestId) =>
    request('POST', `/friend/reject/${requestId}`),

  deleteFriend: (friendUserId) =>
    request('DELETE', `/friend/${friendUserId}`),

  // ─── 管理员接口 ───────────────────
  getAdminUsers: () =>
    request('GET', '/admin/users'),

  deleteAdminUser: (username) =>
    request('DELETE', `/admin/users/${username}`),

  resetAdminPassword: (username, newPassword) =>
    request('PUT', `/admin/users/${username}/password`, { password: newPassword })
}
