<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">登入 Forum</h2>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="you@example.com" />
        </el-form-item>

        <el-form-item label="密碼" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>

        <el-button type="primary" :loading="loading" native-type="submit" style="width: 100%">
          登入
        </el-button>
      </el-form>

      <div class="links">
        <router-link to="/forgot-password">忘記密碼？</router-link>
        <router-link to="/register">建立新帳號</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ email: '', password: '' })

const rules = {
  email: [
    { required: true, message: 'Email 必填', trigger: 'blur' },
    { type: 'email', message: 'Email 格式錯誤', trigger: 'blur' }
  ],
  password: [{ required: true, message: '密碼必填', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登入成功')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
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
  max-width: 400px;
}
.title {
  text-align: center;
  margin: 0 0 24px;
}
.links {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 14px;
}
.links a {
  color: #409eff;
  text-decoration: none;
}
.links a:hover {
  text-decoration: underline;
}
</style>
