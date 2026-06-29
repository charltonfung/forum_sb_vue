<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <router-link to="/" class="back-link">← 返回首頁</router-link>
      <h2 class="title">變更 Email 驗證</h2>

      <div v-if="state === 'verifying'" class="status">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>驗證中…</span>
      </div>

      <el-result v-else-if="state === 'success'" icon="success" title="Email 已變更">
        <template #sub-title>
          <p>請使用新 email 重新登入。</p>
        </template>
        <template #extra>
          <el-button type="primary" @click="$router.push('/login')">前往登入</el-button>
        </template>
      </el-result>

      <el-result v-else-if="state === 'error'" icon="error" title="驗證失敗">
        <template #sub-title>
          <p>{{ errorMsg || '驗證連結無效或已過期，請重新申請。' }}</p>
        </template>
        <template #extra>
          <el-button @click="$router.push('/login')">回登入</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { verifyEmailChangeApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()

const state = ref('verifying')  // verifying / success / error
const errorMsg = ref('')

onMounted(async () => {
  const token = route.query.token
  if (!token) {
    state.value = 'error'
    errorMsg.value = '連結缺少 token'
    return
  }
  try {
    await verifyEmailChangeApi(token)
    state.value = 'success'
    // 變更成功 → 舊 JWT 已失效，本地端先把登入狀態清掉
    auth.logout()
  } catch (e) {
    state.value = 'error'
    errorMsg.value = e?.response?.data?.message || e.message || ''
  }
})
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.auth-card {
  width: 100%;
  max-width: 480px;
}
.back-link {
  display: inline-block;
  color: #909399;
  text-decoration: none;
  font-size: 14px;
  margin-bottom: 12px;
}
.back-link:hover {
  color: #409eff;
}
.title {
  text-align: center;
  margin: 0 0 24px;
}
.status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #606266;
  padding: 24px 0;
}
</style>
