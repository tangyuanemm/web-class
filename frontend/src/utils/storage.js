/**
 * 本地消息存储
 * 使用 localStorage 缓存聊天记录，支持离线查看
 * key 格式: chat_history_{userId}_{contactId}，防止切换账号后消息泄露
 */

const PREFIX = 'chat_history_'

function makeKey(userId, contactId) {
  return PREFIX + userId + '_' + contactId
}

/**
 * 保存消息列表到本地
 * @param {string} userId - 当前登录用户 ID
 * @param {string} contactId - 聊天对象的 userId
 * @param {Array} messages - 消息列表
 * @param {number} maxCount - 最多缓存条数，默认 500
 */
export function saveMessage(userId, contactId, messages, maxCount = 500) {
  try {
    const key = makeKey(userId, contactId)
    const trimmed = messages.slice(-maxCount)
    localStorage.setItem(key, JSON.stringify(trimmed))
  } catch (e) {
    console.warn('消息缓存失败:', e)
  }
}

/**
 * 加载本地历史消息
 * @param {string} userId - 当前登录用户 ID
 * @param {string} contactId
 * @returns {Array}
 */
export function loadHistory(userId, contactId) {
  try {
    const key = makeKey(userId, contactId)
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : []
  } catch (e) {
    console.warn('读取历史消息失败:', e)
    return []
  }
}

/**
 * 清除某个联系人的本地消息
 * @param {string} userId - 当前登录用户 ID
 * @param {string} contactId
 */
export function clearHistory(userId, contactId) {
  localStorage.removeItem(makeKey(userId, contactId))
}

/**
 * 清除当前用户的所有本地消息
 * @param {string} userId - 当前登录用户 ID
 */
export function clearAllHistory(userId) {
  const prefix = PREFIX + userId + '_'
  const keys = Object.keys(localStorage).filter(k => k.startsWith(prefix))
  keys.forEach(k => localStorage.removeItem(k))
}
