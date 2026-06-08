<template>
  <div class="emoji-picker">
    <span
      v-for="e in emojis"
      :key="e"
      class="emoji-item"
      @click="$emit('select', e)"
      :title="e"
    >{{ e }}</span>
  </div>
</template>

<script setup>
defineEmits(['select'])

const emojis = [
  '😊', '😂', '🤣', '😍', '🥰', '😘', '😜', '😎',
  '😢', '😭', '😡', '🤬', '👍', '👎', '👏', '🙏',
  '💪', '🤝', '❤️', '💔', '🔥', '⭐', '🎉', '🎂',
  '🍔', '🍕', '☕', '🍺', '🐶', '🐱', '🌸', '🌞',
  '✅', '❌', '❓', '💯', '💰', '💻', '📱', '🎵'
]
</script>

<style scoped>
.emoji-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px 16px;
  background: #fff;
  border-top: 1px solid #eee;
  max-height: 160px;
  overflow-y: auto;
}

.emoji-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s;
}

.emoji-item:hover {
  background: #f0f0f0;
}
</style>
