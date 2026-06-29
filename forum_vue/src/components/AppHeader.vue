<!--
  頂部導覽列
  ============================================================
  - 左邊 Logo（點擊回首頁）
  - 右邊：未登入 → 登入 / 註冊；已登入 → 使用者下拉選單（發表文章按鈕在首頁列表上）
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
          <el-dropdown @command="handleCommand">
            <span class="user-trigger">
              {{ auth.user?.name || auth.user?.email }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="my-articles">我的文章</el-dropdown-item>
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
import { ArrowDown } from '@element-plus/icons-vue'

const auth = useAuthStore()
const router = useRouter()

function handleCommand(cmd) {
  if (cmd === 'my-articles') {
    // 走首頁 + ?userId= 過濾，ArticleList.vue 看到 query 會自動切「我的文章」模式
    router.push({ path: '/', query: { userId: auth.currentUserId } })
  } else if (cmd === 'profile') {
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
