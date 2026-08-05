<!--
  文章点赞/评论按钮组，被 ArticleReader 桌面侧栏、桌面下方栏、移动端水平栏共用。
-->
<template>
  <div class="side-line" />
  <button class="side-btn" :class="{ liked }" @click.stop="$emit('toggle-like')">
    <svg viewBox="0 0 24 24" width="22" height="22" class="side-icon">
      <path v-if="!liked" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="none" stroke="currentColor" stroke-width="1.5"/>
      <path v-else d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#ff4757" stroke="#ff4757" stroke-width="1.5"/>
    </svg>
    <span class="side-count">{{ likeCount }}</span>
  </button>
  <div class="side-line" />
  <button class="side-btn" @click.stop="$emit('toggle-comments')">
    <svg viewBox="0 0 24 24" width="22" height="22" class="side-icon">
      <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" fill="none" stroke="currentColor" stroke-width="1.5"/>
    </svg>
    <span class="side-count">{{ commentTotal }}</span>
  </button>
  <div class="side-line" />
</template>

<script setup>
defineProps({
  liked: { type: Boolean, default: false },
  likeCount: { type: Number, default: 0 },
  commentTotal: { type: Number, default: 0 }
})
defineEmits(['toggle-like', 'toggle-comments'])
</script>

<style scoped>
.side-line {
  width: 20px; height: 1.5px;
  background: rgba(255,255,255,0.5); border-radius: 1px;
}
.side-btn {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2);
  color: rgba(255,255,255,0.9); cursor: pointer;
  padding: 10px 10px; border-radius: 14px;
  transition: all 0.2s; backdrop-filter: blur(4px);
}
.side-btn:hover {
  color: #fff; background: rgba(255,255,255,0.22);
  transform: scale(1.08); border-color: rgba(255,255,255,0.35);
}
.side-btn.liked .side-icon { color: #ff4757; }
.side-count { font-size: 12px; font-weight: 600; min-width: 18px; text-align: center; }
</style>
