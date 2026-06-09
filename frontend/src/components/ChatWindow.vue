<template>
  <div class="chat-window">
    <!-- 消息列表 -->
    <div class="message-list" ref="msgListRef">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message-row', msg.direction]"
      >
        <MessageBubble :message="msg" :current-user-id="currentUserId" />
      </div>
      <div v-if="messages.length === 0" class="empty-hint">
        暂无消息，发送一条消息开始聊天吧～
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="toolbar">
        <button class="tool-btn" @click="showEmoji = !showEmoji" title="表情">😊</button>
        <label class="tool-btn" title="图片">
          📷
          <input type="file" accept="image/*" hidden @change="handleImageUpload" />
        </label>
      </div>

      <EmojiPicker v-if="showEmoji" @select="insertEmoji" />

      <div class="input-row">
        <textarea
          v-model="inputText"
          class="msg-input"
          placeholder="输入消息，Enter 发送，Shift+Enter 换行"
          rows="3"
          @keydown.enter.exact.prevent="handleSendText"
        ></textarea>
        <button class="btn-send" @click="handleSendText">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import MessageBubble from '@/components/MessageBubble.vue'
import EmojiPicker from '@/components/EmojiPicker.vue'
import api from '@/utils/api'

const props = defineProps({
  messages: { type: Array, default: () => [] },
  currentUserId: { type: String, default: '' }
})

const emit = defineEmits(['send'])

const inputText = ref('')
const showEmoji = ref(false)
const msgListRef = ref(null)

// 新消息时自动滚动到底部
watch(() => props.messages.length, () => {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
})

// 发送文本消息
function handleSendText() {
  const text = inputText.value.trim()
  if (!text) return
  emit('send', { content: text, contentType: 'text' })
  inputText.value = ''
  showEmoji.value = false
}

// 插入表情
function insertEmoji(emoji) {
  inputText.value += emoji
  showEmoji.value = false
}

// 发送图片
async function handleImageUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  try {
    // Mock 模式：直接用 DataURL
    if (import.meta.env.VITE_USE_MOCK !== 'false') {
      const reader = new FileReader()
      reader.onload = () => {
        emit('send', { content: reader.result, contentType: 'image' })
      }
      reader.readAsDataURL(file)
      return
    }

    // 真实上传
    const url = await api.uploadImage(file)
    emit('send', { content: url, contentType: 'image' })
  } catch (e) {
    alert('图片发送失败: ' + e.message)
  } finally {
    e.target.value = ''
  }
}
</script>

<style scoped>
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ===== 消息列表 ===== */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafafa;
}

.message-row {
  display: flex;
  margin-bottom: 16px;
}

.message-row.in {
  justify-content: flex-start;
}

.message-row.out {
  justify-content: flex-end;
}

.empty-hint {
  text-align: center;
  color: #ccc;
  margin-top: 120px;
}

/* ===== 输入区 ===== */
.input-area {
  border-top: 1px solid #e0e0e0;
  background: #fff;
}

.toolbar {
  display: flex;
  gap: 4px;
  padding: 8px 16px 0;
}

.tool-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-btn:hover {
  background: #f0f0f0;
}

.input-row {
  display: flex;
  gap: 10px;
  padding: 8px 16px 12px;
  align-items: flex-end;
}

.msg-input {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-size: 14px;
  line-height: 1.6;
  font-family: inherit;
}

.btn-send {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  background: #667eea;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.btn-send:hover {
  background: #5a6fd6;
}
</style>
