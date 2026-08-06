<template>
  <div class="tool-card">
    <!-- Article preview - EXACT Home.vue card style -->
    <template v-if="isArticle && result.preview">
      <div class="article-card shadow-box" @click="$emit('view')" title="点击查看全文">
        <div class="article-cover-wrap" v-if="result.preview.cover">
          <img class="article-cover-img" :src="result.preview.cover" :alt="result.preview.title">
        </div>
        <div class="article-cover-wrap" v-else>
          <div class="image-slot article-cover-error">{{ result.preview.title }}</div>
        </div>
        <div class="article-body">
          <h3 class="article-title">{{ result.preview.title }}</h3>
          <p class="article-summary">{{ result.preview.summary || stripHtml(result.preview.content || '').substring(0, 120) }}</p>
          <div class="article-meta">
            <span v-if="result.preview.createTime">📅 {{ result.preview.createTime?.substring(0, 10) }}</span>
            <span v-else>草稿</span>
          </div>
        </div>
      </div>
      <div class="card-actions" v-if="isCreateArticle && result.success">
        <button class="action-btn primary" @click="$emit('publish')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
          发布
        </button>
        <button class="action-btn danger" @click="$emit('discard')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6"/></svg>
          放弃
        </button>
      </div>
    </template>

    <!-- Generic result (non-preview) -->
    <template v-else>
      <div class="generic-result" :class="{ success: result.success, fail: !result.success }">
        <span>{{ result.success ? '✅' : '❌' }} {{ result.message || '完成' }}</span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  toolName: String,
  result: Object
})

defineEmits(['publish', 'discard', 'view'])

const isCreateArticle = computed(() => props.toolName === 'create_article')
const isArticle = computed(() => props.toolName?.includes('article'))

function stripHtml(html) {
  return (html || '').replace(/<[^>]+>/g, '').replace(/\s+/g, ' ')
}
</script>

<style scoped>
/* Exact copy of Home.vue article-card styles */
.article-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e0e8e0;
  display: flex;
  flex-direction: column;
  max-width: 340px;
  cursor: pointer;
  transition: all .2s;
}
.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,.08);
}
.article-cover-wrap {
  width: 100%;
  height: 180px;
  flex-shrink: 0;
  overflow: hidden;
}
.article-cover-img { width: 100%; height: 100%; object-fit: cover; }
.article-cover-error {
  background: #f0f5f0; color: #999; text-align: center;
  padding: 20px; height: 100%; display: flex; align-items: center; justify-content: center;
  font-size: 14px;
}
.article-body { padding: 14px 16px 16px; display: flex; flex-direction: column; flex: 1; }
.article-title {
  font-size: 17px; margin: 0 0 6px; color: #2a3a2a;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 700;
  font-family: var(--trendy-font, 'Fredoka', sans-serif); letter-spacing: 0.3px;
}
.article-summary {
  color: #6a7a6a; font-size: 13px; flex: 1; margin: 0;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; line-height: 1.6;
}
.article-meta {
  display: flex; gap: 14px; color: #aaa; font-size: 11px; margin-top: 8px;
  font-family: var(--trendy-font, 'Fredoka', sans-serif);
}

/* Actions */
.card-actions { display: flex; gap: 8px; margin-top: 10px; }
.action-btn {
  display: flex; align-items: center; gap: 4px;
  padding: 7px 16px; border-radius: 8px; border: none;
  font-size: 13px; cursor: pointer; transition: all .2s;
}
.action-btn.primary { background: #e8f5e9; color: #43a047; }
.action-btn.primary:hover { background: #43a047; color: #fff; }
.action-btn.danger { background: #fff0f0; color: #e74c3c; }
.action-btn.danger:hover { background: #e74c3c; color: #fff; }

/* Generic */
.generic-result {
  padding: 10px 14px; border-radius: 10px; font-size: 13px;
}
.generic-result.success { background: #f0faf0; color: #2e7d32; }
.generic-result.fail { background: #fef0f0; color: #c62828; }
</style>
