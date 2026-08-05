<!--
  通用评论列表+输入框。
  被 ArticleReader 的桌面内联面板和移动底部弹出层共用，避免重复代码。
-->
<template>
  <div class="comment-section">
    <div class="comment-panel-header">
      <h3>评论 ({{ total }})</h3>
      <button class="comment-panel-close" @click="$emit('close')">&times;</button>
    </div>

    <div class="comment-list" ref="listRef">
      <div v-if="loading" class="myCenter" style="padding:40px">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      </div>
      <div v-else-if="items.length === 0" style="text-align:center;padding:40px;color:#999">
        暂无评论，来说点什么吧
      </div>
      <div v-else v-for="item in items" :key="item.id" class="comment-item">
        <el-avatar :size="36" :src="item.avatar" class="comment-avatar" @click.stop="$emit('show-mini', item, $event)">
          {{ (item.nickname || item.username || '匿').charAt(0) }}
        </el-avatar>
        <div class="comment-body">
          <div class="comment-top">
            <span class="comment-nick">{{ item.nickname || item.username || '匿名' }}</span>
            <span class="comment-time">{{ timeAgo(item.createTime) }}</span>
          </div>
          <div class="comment-text">{{ item.content }}</div>
          <div class="comment-actions-row">
            <button class="c-action" @click="$emit('start-reply', item)">
              <svg viewBox="0 0 24 24" width="14" height="14"><path d="M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z" fill="currentColor"/></svg>
              回复
            </button>
            <button class="c-action" @click="$emit('like-comment', item)">
              <svg viewBox="0 0 24 24" width="14" height="14"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="currentColor"/></svg>
              {{ item._likes || 0 }}
            </button>
          </div>
          <div v-if="item.children && item.children.length" class="replies-wrap">
            <div v-for="reply in item.children" :key="reply.id" class="reply-item">
              <span class="reply-nick">{{ reply.nickname || reply.username || '匿名' }}</span>
              <span v-if="reply.replyToUsername" class="reply-to"> 回复 @{{ reply.replyToUsername }}</span>
              <span class="reply-colon">：</span>
              <span class="reply-text">{{ reply.content }}</span>
              <span class="reply-time-inline">{{ timeAgo(reply.createTime) }}</span>
              <button class="reply-reply-btn" @click="$emit('start-reply', item, reply)">回复</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="comment-input-bar">
      <el-input
        :model-value="inputText"
        :placeholder="replyTarget ? '回复 @' + (replyTarget?.username || '') + '...' : '说点什么...'"
        size="large"
        class="comment-input"
        @update:model-value="$emit('update:inputText', $event)"
        @keyup.enter="$emit('submit')"
      >
        <template #suffix>
          <el-button
            type="primary"
            :disabled="!inputText?.trim()"
            :loading="submitting"
            @click="$emit('submit')"
            size="small"
            round
          >
            发送
          </el-button>
        </template>
      </el-input>
      <span v-if="replyTarget" class="reply-cancel" @click="$emit('cancel-reply')">取消回复</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'

defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  inputText: { type: String, default: '' },
  replyTarget: { type: Object, default: null },
  submitting: { type: Boolean, default: false }
})

defineEmits(['close', 'show-mini', 'start-reply', 'like-comment', 'submit', 'cancel-reply', 'update:inputText'])

const listRef = ref(null)

function timeAgo(d) {
  if (!d) return ''
  const diff = Date.now() - new Date(d).getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
</script>

<style scoped>
.comment-panel-header { display: flex; justify-content: space-between; align-items: center; padding: 18px 20px; border-bottom: 1px solid #f0f0f0; flex-shrink: 0; }
.comment-panel-header h3 { margin: 0; font-size: 17px; font-weight: 600; color: #333; }
.comment-panel-close { background: none; border: none; font-size: 24px; color: #999; cursor: pointer; padding: 0 4px; line-height: 1; }
.comment-panel-close:hover { color: #333; }
.comment-list { flex: 1; overflow-y: auto; padding: 12px 16px; }
.comment-list::-webkit-scrollbar { width: 4px }
.comment-list::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 2px }
.comment-item { display: flex; gap: 10px; padding: 14px 0; border-bottom: 1px solid #f5f5f5; }
.comment-avatar { flex-shrink: 0; cursor: pointer; transition: all 0.2s ease; }
.comment-avatar:hover { transform: scale(1.12); box-shadow: 0 0 0 3px rgba(76,175,80,0.25); }
.comment-body { flex: 1; min-width: 0; }
.comment-top { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.comment-nick { font-size: 13px; font-weight: 600; color: #555; }
.comment-time { font-size: 11px; color: #bbb; }
.comment-text { font-size: 15px; line-height: 1.6; color: #333; word-break: break-word; }
.comment-actions-row { display: flex; gap: 16px; margin-top: 6px; }
.c-action { display: flex; align-items: center; gap: 3px; background: none; border: none; color: #999; font-size: 12px; cursor: pointer; padding: 2px 0; transition: color 0.2s; }
.c-action:hover { color: #666; }
.replies-wrap { margin-top: 8px; padding: 8px 12px; background: #f9f9f9; border-radius: 8px; }
.reply-item { padding: 5px 0; font-size: 14px; line-height: 1.6; }
.reply-nick { font-weight: 600; color: #666; }
.reply-to { color: #999; }
.reply-colon { color: #999; }
.reply-text { color: #333; }
.reply-time-inline { font-size: 11px; color: #bbb; margin-left: 6px; }
.reply-reply-btn { background: none; border: none; color: #999; font-size: 11px; cursor: pointer; padding: 0; margin-left: 6px; }
.reply-reply-btn:hover { color: #666; }
.comment-input-bar { flex-shrink: 0; padding: 12px 16px; border-top: 1px solid #f0f0f0; background: #fafafa; }
.comment-input :deep(.el-input__wrapper) { border-radius: 24px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.reply-cancel { display: block; text-align: center; margin-top: 6px; font-size: 12px; color: #999; cursor: pointer; }
.reply-cancel:hover { color: #666; }
</style>
