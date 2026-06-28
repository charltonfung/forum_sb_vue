<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">重設密碼</h2>

      <el-alert v-if="!form.token || !form.email" type="warning" :closable="false" style="margin-bottom: 16px">
        連結缺少 token 或 email，請從信件連結重新進入。
      </el-alert>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" readonly />
        </el-form-item>

        <el-form-item label="新密碼（至少 8 字）" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>

        <el-form-item label="再次輸入" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>

        <el-button type="primary" :loading="loading" native-type="submit" style="width: 100%" :disabled="!form.token">
          更新密碼
        </el-button>
      </el-form>

      <div class="links">
        <router-link to="/login">返回登入</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { resetPasswordApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  email: '',
  token: '',
  password: '',
  confirmPassword: ''
})

const rules = {
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

onMounted(() => {
  form.email = route.query.email || ''
  form.token = route.query.token || ''
})

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await resetPasswordApi({
      email: form.email,
      token: form.token,
      password: form.password
    })
    ElMessage.success('密碼已更新，請使用新密碼登入')
    router.push('/login')
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
  text-align: center;
  margin-top: 16px;
}
.links a {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
}
</style>
