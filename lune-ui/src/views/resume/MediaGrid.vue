<template>
  <div class="media-grid">
    <!-- 图片：preview-teleported 是关键。
         没有它时 el-image 的大图查看器渲染在组件内部，而祖先 .detail-overlay 带
         backdrop-filter，会给 position:fixed 的后代重新建立包含块，再叠上
         .detail-card 的 overflow-y:auto —— 查看器就被裁在卡片里，等于点了没反应。 -->
    <el-image
      v-for="(m, i) in images"
      :key="'i' + i"
      :src="m.url"
      fit="cover"
      class="media-tile"
      :preview-src-list="imageUrls"
      :initial-index="i"
      preview-teleported
      :z-index="3000"
      hide-on-click-modal
    />

    <!-- 视频：格子里只显首帧（#t 让浏览器抓 0.1s 那一帧当封面），点击才进播放层 -->
    <div v-for="(m, i) in videos" :key="'v' + i" class="media-tile video-tile" @click="$emit('play', m.url)">
      <video :src="thumbSrc(m.url)" class="video-thumb" preload="metadata" muted playsinline></video>
      <span class="video-badge"><LineIcon name="play" :size="20" /></span>
    </div>
  </div>
</template>

<script setup>
/**
 * 简历页统一媒体网格：图片点开看大图（可左右切换），视频点开在播放层里播。
 * 原来图片是裸 el-image（大图看不见）、视频是裸 <video controls>（格子里一条黑边），
 * 两种媒体尺寸也不一致，这里统一成同一套正方格子。
 */
import { computed } from 'vue'
import LineIcon from '../../components/LineIcon.vue'

const props = defineProps({
  images: { type: Array, default: () => [] },
  videos: { type: Array, default: () => [] }
})

defineEmits(['play'])

const imageUrls = computed(() => props.images.map(m => m.url))

function thumbSrc(url) {
  return url.includes('#') ? url : `${url}#t=0.1`
}
</script>

<style scoped>
.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(104px, 1fr));
  gap: 10px;
}

.media-tile {
  position: relative;
  aspect-ratio: 1 / 1;
  width: 100%;
  border-radius: 10px;
  overflow: hidden;
  background: #f2f4f4;
  cursor: pointer;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.media-tile:hover { transform: translateY(-3px); box-shadow: 0 8px 18px rgba(15, 40, 40, 0.14); }

.video-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  background: #1a2222;
  pointer-events: none;
}

.video-badge {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(18, 32, 32, 0.32);
  transition: background 0.3s;
}
.video-tile:hover .video-badge { background: rgba(18, 32, 32, 0.14); }

@media (prefers-reduced-motion: reduce) {
  .media-tile:hover { transform: none; }
}
</style>
