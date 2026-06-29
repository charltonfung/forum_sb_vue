<template>
  <div>
    <h1 class="page-title">個人資料</h1>

    <el-card v-loading="loading" class="card">
      <!-- 基本資料：名稱可編、email 走變更流程 -->
      <h2 class="section-title">基本資料</h2>
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-position="left"
        label-width="100px"
      >
        <el-form-item label="ID">
          <span class="readonly">{{ auth.user?.id }}</span>
        </el-form-item>

        <el-form-item label="顯示名稱" prop="name">
          <el-input v-model="profileForm.name" maxlength="60" show-word-limit style="max-width: 400px" />
        </el-form-item>

        <el-form-item label="Email">
          <div class="email-row">
            <span class="readonly">{{ auth.user?.email }}</span>
            <el-button size="small" @click="emailDialogVisible = true">變更 email</el-button>
          </div>
        </el-form-item>

        <el-form-item label="加入時間">
          <span class="readonly">{{ formatDate(auth.user?.createdAt) }}</span>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="savingProfile"
            :disabled="!isProfileDirty"
            @click="onSaveProfile"
          >
            儲存名稱
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <!-- 變更密碼 -->
      <h2 class="section-title">變更密碼</h2>
      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-position="left"
        label-width="100px"
      >
        <el-form-item label="目前密碼" prop="currentPassword">
          <el-input v-model="pwdForm.currentPassword" type="password" show-password style="max-width: 400px" />
        </el-form-item>
        <el-form-item label="新密碼" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password style="max-width: 400px" />
        </el-form-item>
        <el-form-item label="再次輸入" prop="confirmNewPassword">
          <el-input v-model="pwdForm.confirmNewPassword" type="password" show-password style="max-width: 400px" />
        </el-form-item>

        <el-alert type="warning" :closable="false" style="margin-bottom: 12px">
          變更密碼成功後將強制重新登入。
        </el-alert>

        <el-form-item>
          <el-button type="primary" :loading="savingPwd" @click="onChangePassword">變更密碼</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 變更 email 對話框 -->
    <el-dialog v-model="emailDialogVisible" title="變更 Email" width="500px" @close="resetEmailDialog">
      <el-form
        ref="emailFormRef"
        :model="emailForm"
        :rules="emailRules"
        label-position="top"
      >
        <el-alert type="info" :closable="false" style="margin-bottom: 16px">
          我們會寄一封驗證信到「新 email」，點連結後變更才會生效。<br>
          變更成功後，舊登入狀態會失效，需用新 email 重新登入。
        </el-alert>

        <el-form-item label="新 Email" prop="newEmail">
          <el-input v-model="emailForm.newEmail" placeholder="new@example.com" />
        </el-form-item>
        <el-form-item label="目前密碼（驗證身份用）" prop="currentPassword">
          <el-input v-model="emailForm.currentPassword" type="password" show-password />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="emailDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingEmail" @click="onRequestEmailChange">寄送驗證信</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { updateProfileApi, changePasswordApi, requestEmailChangeApi } from '@/api/user'

const auth = useAuthStore()
const router = useRouter()

const loading = ref(false)

// ---------- 基本資料 ----------
const profileFormRef = ref()
const profileForm = reactive({ name: '' })
const savingProfile = ref(false)
const profileRules = {
  name: [
    { required: true, message: '名稱必填', trigger: 'blur' },
    { max: 60, message: '名稱過長', trigger: 'blur' }
  ]
}
const isProfileDirty = computed(() => profileForm.name && profileForm.name !== auth.user?.name)

async function onSaveProfile() {
  await profileFormRef.value.validate()
  savingProfile.value = true
  try {
    const updated = await updateProfileApi({ name: profileForm.name })
    auth.user = { ...auth.user, ...updated }
    ElMessage.success('名稱已更新')
  } finally {
    savingProfile.value = false
  }
}

// ---------- 變更密碼 ----------
const pwdFormRef = ref()
const pwdForm = reactive({ currentPassword: '', newPassword: '', confirmNewPassword: '' })
const savingPwd = ref(false)
const pwdRules = {
  currentPassword: [{ required: true, message: '目前密碼必填', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '新密碼必填', trigger: 'blur' },
    { min: 8, message: '至少 8 字', trigger: 'blur' }
  ],
  confirmNewPassword: [
    {
      validator: (_r, v, cb) =>
        v === pwdForm.newPassword ? cb() : cb(new Error('兩次新密碼不一致')),
      trigger: 'blur'
    }
  ]
}

async function onChangePassword() {
  await pwdFormRef.value.validate()
  savingPwd.value = true
  try {
    await changePasswordApi({
      currentPassword: pwdForm.currentPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密碼已更新，請使用新密碼重新登入')
    // 主動登出 → 跳登入頁
    auth.logout()
    router.push({ name: 'login' })
  } finally {
    savingPwd.value = false
  }
}

// ---------- 變更 Email ----------
const emailDialogVisible = ref(false)
const emailFormRef = ref()
const emailForm = reactive({ newEmail: '', currentPassword: '' })
const savingEmail = ref(false)
const emailRules = {
  newEmail: [
    { required: true, message: '新 email 必填', trigger: 'blur' },
    { type: 'email', message: 'email 格式錯誤', trigger: 'blur' }
  ],
  currentPassword: [{ required: true, message: '目前密碼必填', trigger: 'blur' }]
}

function resetEmailDialog() {
  emailForm.newEmail = ''
  emailForm.currentPassword = ''
  emailFormRef.value?.clearValidate()
}

async function onRequestEmailChange() {
  await emailFormRef.value.validate()
  savingEmail.value = true
  try {
    await requestEmailChangeApi({
      newEmail: emailForm.newEmail,
      currentPassword: emailForm.currentPassword
    })
    ElMessage.success('驗證信已寄到新 email，請點連結完成變更（dev 環境用 Mailpit: http://localhost:8026）')
    emailDialogVisible.value = false
  } finally {
    savingEmail.value = false
  }
}

// ---------- 初始化 ----------
onMounted(async () => {
  if (!auth.user) {
    loading.value = true
    try {
      await auth.fetchMe()
    } finally {
      loading.value = false
    }
  }
  profileForm.name = auth.user?.name || ''
})

// auth.user 更新後（例如 fetchMe 完成）同步到表單
watch(() => auth.user?.name, (name) => {
  if (name && !profileForm.name) profileForm.name = name
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
.card {
  max-width: 720px;
}
.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  color: #303133;
}
.readonly {
  color: #606266;
}
.email-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
