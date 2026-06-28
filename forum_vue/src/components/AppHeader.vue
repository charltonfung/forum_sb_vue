<!--
  頂部導覽列
  ============================================================
  - 左邊 Logo（點擊回首頁）
  - 右邊：未登入 → 登入 / 註冊；已登入 → 發表文章 / 使用者下拉選單
-->
<template>
  <header class="header">
    <div class="inner">
      <router-link to="/" class="logo">📰 Forum</router-link>

      <div class="actions">
        <template v-if="!auth.isLoggedIn">
          <el-button text @click="$router.push('/login')">登入</el-button>
          <el-button type="primary" @click="$router.push('/register')">註冊</el-button>
        </template>

        <template v-else>
          <el-button type="primary" plain @click="$router.push('/articles/create')">
            <el-icon><EditPen /></el-icon>
            <span style="margin-left: 4px">發表文章</span>
          </el-button>

          <el-dropdown @command="handleCommand">
            <span class="user-trigger">
              {{ auth.user?.name || auth.user?.email }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">個人資料</el-dropdown-item>
                <el-dropdown-item divided command="logout">登出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { ArrowDown, EditPen } from '@element-plus/icons-vue'

const auth = useAuthStore()
const router = useRouter()

function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    auth.logout()
    ElMessage.success('已登出')
    router.push('/')
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
.inner {
  max-width: 960px;
  margin: 0 auto;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  text-decoration: none;
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
}
</style>
