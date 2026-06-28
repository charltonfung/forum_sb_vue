<!--
  留言區元件
  ============================================================
  傳入 articleId，自動載入該文章的所有留言。
  - 未登入：只看得到留言列表 + 提示「登入後可留言」
  - 已登入：可發新留言、刪除自己的留言
-->
<template>
  <section class="comments">
    <h3 class="title">
      留言
      <span class="count">（{{ comments.length }}）</span>
    </h3>

    <!-- 留言列表 -->
    <div v-loading="loading" class="list">
      <el-empty v-if="!loading && comments.length === 0" description="還沒有留言，搶頭香！" :image-size="80" />

      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-head">
          <span class="author">
            <el-icon><UserFilled /></el-icon>
            {{ c.authorName || '未知使用者' }}
          </span>
          <span class="time">{{ formatDate(c.createdAt) }}</span>

          <el-popconfirm
            v-if="auth.currentUserId === c.userId"
            title="確定刪除這則留言？"
            @confirm="onDelete(c.id)"
          >
            <template #reference>
              <el-button size="small" text type="danger">刪除</el-button>
            </template>
          </el-popconfirm>
        </div>
        <p class="content">{{ c.content }}</p>

        <div class="comment-foot">
          <LikeButton
            :liked="c.likedByMe"
            :count="c.likeCount"
            :disabled="!auth.isLoggedIn"
            @toggle="onToggleLike(c)"
          />
        </div>
      </div>
    </div>

    <!-- 發新留言 -->
    <div class="composer">
      <template v-if="auth.isLoggedIn">
        <el-input
          v-model="newContent"
          type="textarea"
          :rows="3"
          maxlength="2000"
          show-word-limit
          placeholder="寫下你的留言…"
        />
        <div class="composer-actions">
          <el-button type="primary" :loading="submitting" :disabled="!newContent.trim()" @click="onSubmit">
            送出留言
          </el-button>
        </div>
      </template>

      <el-alert v-else type="info" :closable="false">
        <router-link to="/login">登入</router-link>
        後即可留言
      </el-alert>
    </div>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { listComments, createComment, deleteComment } from '@/api/comment'
import { likeComment, unlikeComment } from '@/api/like'
import LikeButton from '@/components/LikeButton.vue'

const props = defineProps({
  articleId: { type: [String, Number], required: true }
})

const auth = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const comments = ref([])
const newContent = ref('')

async function fetch() {
  if (!props.articleId) return
  loading.value = true
  try {
    comments.value = await listComments(props.articleId)
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  const content = newContent.value.trim()
  if (!content) return
  submitting.value = true
  try {
    const created = await createComment(props.articleId, content)
    comments.value.push(created)         // 樂觀更新：直接 append，不必重打整串列表
    newContent.value = ''
    ElMessage.success('留言成功')
  } finally {
    submitting.value = false
  }
}

async function onDelete(commentId) {
  await deleteComment(commentId)
  comments.value = comments.value.filter(c => c.id !== commentId)
  ElMessage.success('留言已刪除')
}

async function onToggleLike(comment) {
  if (!auth.isLoggedIn) {
    ElMessage.warning('請先登入')
    return
  }
  const wasLiked = comment.likedByMe
  comment.likedByMe = !wasLiked
  comment.likeCount += wasLiked ? -1 : 1
  try {
    await (wasLiked ? unlikeComment(comment.id) : likeComment(comment.id))
  } catch {
    comment.likedByMe = wasLiked
    comment.likeCount += wasLiked ? 1 : -1
  }
}

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleString('zh-TW', { hour12: false }) : ''
}

// articleId 變了就重新撈（雖然這頁通常不會切，保險寫法）
watch(() => props.articleId, fetch, { immediate: true })
</script>

<style scoped>
.comments {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
.title {
  font-size: 18px;
  margin: 0 0 16px;
}
.count {
  color: #909399;
  font-size: 14px;
  font-weight: normal;
}
.list {
  min-height: 80px;
  margin-bottom: 24px;
}
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f2f4f8;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  font-size: 13px;
}
.author {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  color: #303133;
}
.time {
  color: #909399;
  flex: 1;
}
.content {
  margin: 0 0 8px;
  white-space: pre-wrap;
  line-height: 1.6;
  color: #606266;
}
.comment-foot {
  display: flex;
  align-items: center;
}
.composer-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
