<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// 全局 App 组件，只负责渲染路由视图
</script>

<style>
#app {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  color: #333;
}
</style>
