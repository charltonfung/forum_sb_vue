<template>
  <div v-loading="loading">
    <el-button text @click="$router.push('/')">← 返回列表</el-button>

    <el-card v-if="article" class="article" shadow="never">
      <h1 class="title">{{ article.title }}</h1>
      <p class="meta">
        <el-icon><User /></el-icon>
        {{ article.authorName || '未知作者' }} · {{ formatDate(article.createdAt) }}
      </p>
      <article class="content">{{ article.content }}</article>

      <div class="actions">
        <LikeButton
          :liked="article.likedByMe"
          :count="article.likeCount"
          :disabled="!auth.isLoggedIn"
          size="default"
          @toggle="onToggleLike"
        />

        <template v-if="auth.currentUserId === article.userId">
          <el-button @click="$router.push(`/articles/${article.id}/edit`)">編輯</el-button>
          <el-popconfirm title="確定刪除？" @confirm="onDelete">
            <template #reference>
              <el-button type="danger">刪除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </div>

      <!-- 留言區 -->
      <CommentSection :article-id="article.id" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticle, deleteArticle } from '@/api/article'
import { likeArticle, unlikeArticle } from '@/api/like'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import CommentSection from '@/components/CommentSection.vue'
import LikeButton from '@/components/LikeButton.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const article = ref(null)

async function fetch() {
  loading.value = true
  try {
    article.value = await getArticle(route.params.id)
  } finally {
    loading.value = false
  }
}

async function onDelete() {
  await deleteArticle(article.value.id)
  ElMessage.success('文章已刪除')
  router.push('/')
}

async function onToggleLike() {
  if (!auth.isLoggedIn) {
    ElMessage.warning('請先登入')
    return
  }
  const a = article.value
  const wasLiked = a.likedByMe
  a.likedByMe = !wasLiked
  a.likeCount += wasLiked ? -1 : 1
  try {
    await (wasLiked ? unlikeArticle(a.id) : likeArticle(a.id))
  } catch {
    a.likedByMe = wasLiked
    a.likeCount += wasLiked ? 1 : -1
  }
}

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleString('zh-TW', { hour12: false }) : ''
}

onMounted(fetch)
</script>

<style scoped>
.article {
  margin-top: 12px;
}
.title {
  margin: 0 0 8px;
  font-size: 28px;
}
.meta {
  color: #909399;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 20px;
}
.content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
  font-size: 16px;
}
.actions {
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
