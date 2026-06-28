<template>
  <div>
    <h1 class="page-title">個人資料</h1>

    <el-card v-loading="loading">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ auth.user?.id }}</el-descriptions-item>
        <el-descriptions-item label="顯示名稱">{{ auth.user?.name }}</el-descriptions-item>
        <el-descriptions-item label="Email">{{ auth.user?.email }}</el-descriptions-item>
        <el-descriptions-item label="加入時間">{{ formatDate(auth.user?.createdAt) }}</el-descriptions-item>
      </el-descriptions>

      <el-alert
        type="info"
        :closable="false"
        title="MVP 階段尚未開放修改個人資料 / 變更密碼，後續會加入"
        style="margin-top: 20px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)

onMounted(async () => {
  if (!auth.user) {
    loading.value = true
    try {
      await auth.fetchMe()
    } finally {
      loading.value = false
    }
  }
})

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleString('zh-TW', { hour12: false }) : ''
}
</script>

<style scoped>
.page-title {
  margin: 0 0 16px;
  font-size: 22px;
}
</style>
