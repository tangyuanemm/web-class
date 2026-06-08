<template>
  <div class="contact-list">
    <div class="search-box">
      <input v-model="keyword" type="text" placeholder="搜索联系人..." />
    </div>
    <ul class="contact-items">
      <li
        v-for="c in filteredContacts"
        :key="c.id"
        :class="['contact-item', { active: c.id === activeId }]"
        @click="$emit('select', c)"
      >
        <div class="avatar">
          <span :class="['status-dot', c.online ? 'online' : 'offline']"></span>
          {{ c.nickname.charAt(0) }}
        </div>
        <div class="info">
          <div class="name">{{ c.nickname }}</div>
          <div class="status-text">{{ c.online ? '在线' : '离线' }}</div>
        </div>
      </li>
      <li v-if="filteredContacts.length === 0" class="empty">暂无联系人</li>
    </ul>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  contacts: { type: Array, default: () => [] },
  activeId: { type: String, default: '' }
})

defineEmits(['select'])

const keyword = ref('')

const filteredContacts = computed(() => {
  if (!keyword.value) return props.contacts
  return props.contacts.filter(c =>
    c.nickname.includes(keyword.value)
  )
})
</script>

<style scoped>
.contact-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-box {
  padding: 12px;
}

.search-box input {
  width: 100%;
  height: 36px;
  padding: 0 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  background: #fff;
}

.contact-items {
  flex: 1;
  overflow-y: auto;
  list-style: none;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.contact-item:hover {
  background: #e8e8e8;
}

.contact-item.active {
  background: #d6d6e8;
}

.avatar {
  position: relative;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #667eea;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.status-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #f0f0f0;
}

.status-dot.online { background: #67c23a; }
.status-dot.offline { background: #c0c4cc; }

.name {
  font-weight: 500;
  font-size: 14px;
}

.status-text {
  font-size: 12px;
  color: #999;
}

.empty {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>
