<template>
  <div>
    <el-button text @click="$router.back()">← 返回</el-button>

    <el-card class="form-card">
      <h2 class="title">發表文章</h2>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="標題" prop="title">
          <el-input v-model="form.title" maxlength="255" show-word-limit />
        </el-form-item>

        <el-form-item label="內容（至少 10 字）" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="12" />
        </el-form-item>

        <el-button type="primary" :loading="loading" native-type="submit">發表</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createArticle } from '@/api/article'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ title: '', content: '' })

const rules = {
  title: [{ required: true, message: '標題必填', trigger: 'blur' }],
  content: [
    { required: true, message: '內容必填', trigger: 'blur' },
    { min: 10, message: '至少 10 字', trigger: 'blur' }
  ]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const article = await createArticle(form)
    ElMessage.success('文章發表成功')
    router.push(`/articles/${article.id}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-card {
  margin-top: 12px;
}
.title {
  margin: 0 0 20px;
  font-size: 22px;
}
</style>
