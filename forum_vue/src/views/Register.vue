<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <router-link to="/" class="back-link">← 返回首頁</router-link>
      <h2 class="title">註冊 Forum</h2>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="顯示名稱" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>

        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="you@example.com" />
        </el-form-item>

        <el-form-item label="密碼（至少 8 字）" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>

        <el-form-item label="再次輸入密碼" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>

        <el-button type="primary" :loading="loading" native-type="submit" style="width: 100%">
          建立帳號
        </el-button>
      </el-form>

      <div class="links">
        <span>已經有帳號？</span>
        <router-link to="/login">登入</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ name: '', email: '', password: '', confirmPassword: '' })

const rules = {
  name: [{ required: true, message: '名稱必填', trigger: 'blur' }],
  email: [
    { required: true, message: 'Email 必填', trigger: 'blur' },
    { type: 'email', message: 'Email 格式錯誤', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密碼必填', trigger: 'blur' },
    { min: 8, message: '至少 8 字', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_r, v, cb) =>
        v === form.password ? cb() : cb(new Error('兩次密碼不一致')),
      trigger: 'blur'
    }
  ]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.register({ name: form.name, email: form.email, password: form.password })
    ElMessage.success('註冊成功')
    router.push('/')
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
.links {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
}
.links a {
  color: #409eff;
  margin-left: 4px;
  text-decoration: none;
}
</style>
