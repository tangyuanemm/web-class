/**
 * 本地消息存储
 * 使用 localStorage 缓存聊天记录，支持离线查看
 */

const PREFIX = 'chat_history_'

/**
 * 保存消息列表到本地
 * @param {string} contactId - 聊天对象的 userId
 * @param {Array} messages - 消息列表
 * @param {number} maxCount - 最多缓存条数，默认 500
 */
export function saveMessage(contactId, messages, maxCount = 500) {
  try {
    const key = PREFIX + contactId
    // 只保留最近 N 条
    const trimmed = messages.slice(-maxCount)
    localStorage.setItem(key, JSON.stringify(trimmed))
  } catch (e) {
    console.warn('消息缓存失败:', e)
  }
}

/**
 * 加载本地历史消息
 * @param {string} contactId
 * @returns {Array}
 */
export function loadHistory(contactId) {
  try {
    const key = PREFIX + contactId
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : []
  } catch (e) {
    console.warn('读取历史消息失败:', e)
    return []
  }
}

/**
 * 清除某个联系人的本地消息
 * @param {string} contactId
 */
export function clearHistory(contactId) {
  localStorage.removeItem(PREFIX + contactId)
}

/**
 * 清除所有本地消息
 */
export function clearAllHistory() {
  const keys = Object.keys(localStorage).filter(k => k.startsWith(PREFIX))
  keys.forEach(k => localStorage.removeItem(k))
}
