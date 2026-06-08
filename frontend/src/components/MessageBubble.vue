<template>
  <div :class="['bubble', message.direction]">
    <!-- 文本消息 -->
    <div v-if="message.contentType === 'text'" class="bubble-text">
      {{ message.content }}
    </div>

    <!-- 表情消息 -->
    <div v-else-if="message.contentType === 'emoji'" class="bubble-emoji">
      {{ message.content }}
    </div>

    <!-- 图片消息 -->
    <div v-else-if="message.contentType === 'image'" class="bubble-image">
      <img :src="message.content" alt="图片" @click="previewImage" />
    </div>

    <!-- 时间 -->
    <div class="bubble-time">
      {{ formatTime(message.timestamp) }}
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  message: { type: Object, required: true }
})

function formatTime(ts) {
  const d = new Date(ts)
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  const h = d.getHours().toString().padStart(2, '0')
  const m = d.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${h}:${m}`
}

function previewImage() {
  window.open(props.message.content, '_blank')
}
</script>

<style scoped>
.bubble {
  max-width: 65%;
  position: relative;
}

.bubble.in .bubble-text,
.bubble.in .bubble-emoji {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px 16px 16px 16px;
}

.bubble.out .bubble-text,
.bubble.out .bubble-emoji {
  background: #667eea;
  color: #fff;
  border-radius: 16px 4px 16px 16px;
}

.bubble-text {
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.bubble-emoji {
  padding: 6px 10px;
  font-size: 32px;
  line-height: 1.2;
}

.bubble-image img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
}

.bubble-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  text-align: right;
}

.bubble.out .bubble-time {
  color: #999;
}
</style>
