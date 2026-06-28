<!--
  主畫面 layout：上方導覽列 + 下方內容區
-->
<template>
  <div class="layout">
    <AppHeader />
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.content {
  max-width: 960px;
  width: 100%;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}
</style>
