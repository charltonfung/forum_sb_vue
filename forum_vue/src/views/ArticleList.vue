<template>
  <div>
    <div class="header-row">
      <h1 class="page-title">最新文章</h1>
      <el-button v-if="auth.isLoggedIn" type="primary" @click="$router.push('/articles/create')">
        發表文章
      </el-button>
    </div>

    <el-empty v-if="!loading && articles.length === 0" description="還沒有文章，搶第一篇吧！" />

    <div v-loading="loading" class="list">
      <el-card v-for="a in articles" :key="a.id" class="article-card" shadow="hover">
        <div class="article-head">
          <router-link :to="`/articles/${a.id}`" class="article-title">{{ a.title }}</router-link>
          <span class="meta">
            <el-icon><User /></el-icon>
            {{ a.authorName || '未知作者' }} · {{ formatDate(a.createdAt) }}
          </span>
        </div>

        <p class="excerpt">{{ excerpt(a.content) }}</p>

        <div class="actions">
          <LikeButton
            :liked="a.likedByMe"
            :count="a.likeCount"
            :disabled="!auth.isLoggedIn"
            @toggle="onToggleLike(a)"
          />

          <template v-if="auth.currentUserId === a.userId">
            <el-button size="small" @click="$router.push(`/articles/${a.id}/edit`)">編輯</el-button>
            <el-popconfirm title="確定刪除？" @confirm="onDelete(a.id)">
              <template #reference>
                <el-button size="small" type="danger">刪除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </div>
      </el-card>
    </div>

    <!-- 分頁（永遠顯示，含「共 N 篇」+ 上下頁按鈕） -->
    <div class="pagination-wrap">
      <span class="pagination-info">
        共 {{ total }} 篇，第 {{ page }} / {{ Math.max(Math.ceil(total / pageSize), 1) }} 頁
      </span>
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        :hide-on-single-page="false"
        @current-change="fetch"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listArticles, deleteArticle } from '@/api/article'
import { likeArticle, unlikeArticle } from '@/api/like'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import LikeButton from '@/components/LikeButton.vue'

const auth = useAuthStore()

const loading = ref(false)
const articles = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

async function fetch() {
  loading.value = true
  try {
    const data = await listArticles(page.value, pageSize.value)
    articles.value = data.items
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function onDelete(id) {
  await deleteArticle(id)
  ElMessage.success('文章已刪除')
  fetch()
}

// 樂觀更新：先翻轉 UI 再打 API；失敗就翻回去
async function onToggleLike(article) {
  if (!auth.isLoggedIn) {
    ElMessage.warning('請先登入')
    return
  }
  const wasLiked = article.likedByMe
  article.likedByMe = !wasLiked
  article.likeCount += wasLiked ? -1 : 1
  try {
    await (wasLiked ? unlikeArticle(article.id) : likeArticle(article.id))
  } catch {
    // 還原
    article.likedByMe = wasLiked
    article.likeCount += wasLiked ? 1 : -1
  }
}

function formatDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleString('zh-TW', { hour12: false })
}

function excerpt(content) {
  if (!content) return ''
  return content.length > 120 ? content.slice(0, 120) + '...' : content
}

onMounted(fetch)
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
  font-size: 22px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}
.article-card {
  cursor: default;
}
.article-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}
.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  text-decoration: none;
}
.article-title:hover {
  color: #409eff;
}
.meta {
  font-size: 13px;
  color: #909399;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.excerpt {
  color: #606266;
  margin: 0 0 12px;
  white-space: pre-wrap;
  line-height: 1.6;
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 4px;
}
.pagination-info {
  font-size: 13px;
  color: #909399;
}
</style>
