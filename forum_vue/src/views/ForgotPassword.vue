<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <router-link to="/" class="back-link">← 返回首頁</router-link>
      <h2 class="title">忘記密碼</h2>
      <p class="hint">輸入註冊時的 email，我們會寄重設連結給你。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="you@example.com" />
        </el-form-item>

        <el-button type="primary" :loading="loading" native-type="submit" style="width: 100%">
          寄送重設信件
        </el-button>
      </el-form>

      <div class="links">
        <router-link to="/login">返回登入</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { forgotPasswordApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const formRef = ref()
const loading = ref(false)
const form = reactive({ email: '' })

const rules = {
  email: [
    { required: true, message: 'Email 必填', trigger: 'blur' },
    { type: 'email', message: 'Email 格式錯誤', trigger: 'blur' }
  ]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await forgotPasswordApi(form.email)
    ElMessage.success('若信箱存在，重設信件已寄出，請至信箱查收（開發環境用 Mailpit: http://localhost:8026）')
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
  margin: 0 0 8px;
}
.hint {
  color: #909399;
  font-size: 13px;
  text-align: center;
  margin: 0 0 20px;
}
.links {
  text-align: center;
  margin-top: 16px;
}
.links a {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
}
</style>
