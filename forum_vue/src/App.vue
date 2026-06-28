<!--
  App 根元件
  ============================================================
  只負責「擺一個 router-view 的位置」，實際畫面由路由切換決定。
-->
<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

// App 啟動時：若 localStorage 有 token，自動載入使用者資料
// 這樣重新整理頁面後使用者狀態不會消失
const auth = useAuthStore()
onMounted(() => {
  if (auth.token) {
    auth.fetchMe().catch(() => auth.logout())
  }
})
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang TC',
               'Microsoft JhengHei', sans-serif;
  background: #f5f7fa;
}
</style>
