<!--
  通用愛心按鈕（給文章 / 留言共用）
  ============================================================
  Props:
    - liked      Boolean  當前是否已讚（決定愛心是實心還是空心）
    - count      Number   讚數
    - disabled   Boolean  禁用點擊（例如未登入）
    - size       String   'default' | 'small'，視覺尺寸
  Event:
    - toggle     觸發 like / unlike 的決定權交給 parent（這層只負責 UI）
                 parent 收到 toggle 後決定打哪個 API、收到結果後再傳新的 liked/count 進來。

  為什麼不在這個元件內直接打 API？
    1. 通用化：article / comment 用不同 API endpoint，元件不該知道
    2. 樂觀更新：parent 才知道整個資料結構，自己控制 UI 更新時機比較靈活
-->
<template>
  <el-button
    :type="liked ? 'danger' : 'default'"
    :size="size"
    :disabled="disabled"
    :plain="!liked"
    @click.stop="$emit('toggle')"
  >
    <el-icon><StarFilled v-if="liked" /><Star v-else /></el-icon>
    <span class="count">{{ count }}</span>
  </el-button>
</template>

<script setup>
import { Star, StarFilled } from '@element-plus/icons-vue'

defineProps({
  liked: { type: Boolean, default: false },
  count: { type: Number, default: 0 },
  disabled: { type: Boolean, default: false },
  size: { type: String, default: 'small' }
})
defineEmits(['toggle'])
</script>

<style scoped>
.count {
  margin-left: 4px;
  font-variant-numeric: tabular-nums;
}
</style>
