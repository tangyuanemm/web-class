/**
 * Mock 用户认证 — 用 localStorage 模拟注册/登录
 *
 * 行为：
 * - 注册：用户名不能重复，密码 ≥ 3 位
 * - 登录：用户名必须存在且密码匹配
 * - 用户数据存储在 localStorage['mock_users']
 */

const MOCK_USERS_KEY = 'mock_users'

/** 首次加载时预置管理员账号 */
function initDefaultUsers() {
  const users = getUsers()
  if (!users['admin']) {
    users['admin'] = {
      id: 'user_admin',
      username: 'admin',
      nickname: '管理员',
      password: 'admin123'
    }
    saveUsers(users)
  }
}

/** 读取所有已注册用户 */
function getUsers() {
  try {
    return JSON.parse(localStorage.getItem(MOCK_USERS_KEY) || '{}')
  } catch {
    return {}
  }
}

/** 保存用户表 */
function saveUsers(users) {
  localStorage.setItem(MOCK_USERS_KEY, JSON.stringify(users))
}

// 模块加载时自动初始化默认用户
initDefaultUsers()

/**
 * 注册
 * @returns {{ user: object, token: string }}
 * @throws {Error} 用户名已存在 / 密码过短
 */
export function mockRegister(username, password, nickname) {
  if (!username || !username.trim()) throw new Error('用户名不能为空')
  if (!password || password.length < 3) throw new Error('密码至少 3 位')

  const users = getUsers()
  if (users[username]) throw new Error('用户名已存在')

  const user = {
    id: 'user_' + username,
    username,
    nickname: nickname || username
  }
  users[username] = { ...user, password }
  saveUsers(users)

  return {
    user,
    token: 'mock_token_' + username + '_' + Date.now()
  }
}

/**
 * 登录
 * @returns {{ user: object, token: string }}
 * @throws {Error} 用户名不存在 / 密码错误
 */
export function mockLogin(username, password) {
  if (!username || !username.trim()) throw new Error('请输入用户名')
  if (!password) throw new Error('请输入密码')

  const users = getUsers()
  const found = users[username]
  if (!found) throw new Error('用户不存在，请先注册')
  if (found.password !== password) throw new Error('密码错误')

  const user = {
    id: found.id,
    username: found.username,
    nickname: found.nickname
  }

  return {
    user,
    token: 'mock_token_' + username + '_' + Date.now()
  }
}

// ─── 管理接口 ───────────────────────────────────────────

/** 判断某个用户是否为管理员（兼容 mock 和真实模式） */
export function isAdminUser(user) {
  if (!user) return false
  // 真实模式：后端返回 role 字段
  if (import.meta.env.VITE_USE_MOCK === 'false') {
    return user.role === 'admin'
  }
  // Mock 模式：管理员用户名固定为 admin
  return user.username === 'admin'
}

/** 获取所有用户列表（不含密码） */
export function getAllUsers() {
  const users = getUsers()
  return Object.values(users).map(u => ({
    id: u.id,
    username: u.username,
    nickname: u.nickname
  }))
}

/** 删除用户（管理员不能删除自己） */
export function deleteUser(username) {
  if (username === 'admin') throw new Error('不能删除管理员账号')
  const users = getUsers()
  if (!users[username]) throw new Error('用户不存在')
  delete users[username]
  saveUsers(users)
}

/** 重置用户密码 */
export function resetUserPassword(username, newPassword) {
  if (!newPassword || newPassword.length < 3) throw new Error('密码至少 3 位')
  const users = getUsers()
  if (!users[username]) throw new Error('用户不存在')
  users[username].password = newPassword
  saveUsers(users)
}
