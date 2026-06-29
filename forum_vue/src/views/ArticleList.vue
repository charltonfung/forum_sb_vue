<template>
  <div>
    <div class="header-row">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <el-button v-if="auth.isLoggedIn" type="primary" @click="$router.push('/articles/create')">
        發表文章
      </el-button>
    </div>

    <!-- 搜尋列：輸入後 debounce 300ms 才打 API，避免每按一鍵都打 -->
    <div class="search-row">
      <el-input
        v-model="searchInput"
        placeholder="搜尋文章標題…"
        clearable
        :prefix-icon="Search"
      />
    </div>

    <el-empty
      v-if="!loading && articles.length === 0"
      :description="keyword ? `沒有符合「${keyword}」的文章` : '還沒有文章，搶第一篇吧！'"
    />

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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listArticles, deleteArticle } from '@/api/article'
import { likeArticle, unlikeArticle } from '@/api/like'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Search } from '@element-plus/icons-vue'
import LikeButton from '@/components/LikeButton.vue'

const auth = useAuthStore()
const route = useRoute()

const loading = ref(false)
const articles = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

// route.query.userId 存在 → 「我的文章」模式（或「某使用者的文章」）
// AppHeader 下拉的「我的文章」會帶 ?userId=<當前使用者 id>
const filterUserId = computed(() => {
  const v = route.query.userId
  return v ? Number(v) : null
})

const pageTitle = computed(() => {
  if (filterUserId.value && filterUserId.value === auth.currentUserId) return '我的文章'
  if (filterUserId.value) return '使用者文章'
  return '最新文章'
})

// searchInput = 輸入框即時值（每按鍵都變）
// keyword     = 真正送到 API 的值（debounce 後才同步）
const searchInput = ref('')
const keyword = ref('')

// debounce：使用者停止輸入 300ms 後才更新 keyword → 觸發 fetch
let searchTimer = null
watch(searchInput, (val) => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    keyword.value = val.trim()
    page.value = 1            // 換關鍵字一定要回第一頁
    fetch()
  }, 300)
})

async function fetch() {
  loading.value = true
  try {
    const data = await listArticles(page.value, pageSize.value, keyword.value, filterUserId.value)
    articles.value = data.items
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// URL 上的 ?userId= 變了（例如點 nav bar 的「我的文章」/「首頁」切換）→ 回第 1 頁重撈
watch(filterUserId, () => {
  page.value = 1
  searchInput.value = ''   // 切模式時清掉搜尋字串，避免「我的文章」還掛著首頁搜過的關鍵字
  keyword.value = ''
  fetch()
})

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
  return content || ''
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
.search-row {
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
  line-height: 1.6;
  /* 保留原始換行（pre-wrap）+ 限制顯示 3 行，超過用 ... 截斷。
     文章裡 \n\n 的空行也算 1 行，所以 3 行可能很快用完 — 這是預期行為。 */
  white-space: pre-wrap;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
