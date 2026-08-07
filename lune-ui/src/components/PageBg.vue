<template>
  <div class="page-bg" :class="variant" aria-hidden="true">
    <!-- 极淡的可选背景图（QQ空间透明感） -->
    <div v-if="bgUrl" class="page-bg-img" :style="{ backgroundImage: `url(${bgUrl})` }"></div>
    <!-- 柔和渐变装饰光斑 -->
    <div class="page-bg-blob blob-1"></div>
    <div class="page-bg-blob blob-2"></div>
    <div class="page-bg-blob blob-3"></div>
  </div>
</template>

<script setup>
/**
 * 统一的内容区透明背景（QQ空间风）
 * - 背景图极淡固定，不干扰内容
 * - 叠加柔和的动态渐变光斑，避免空洞留白
 * - 生产环境自动拼接万象CI参数（移动端用 bgMobile profile）
 * variant: green（默认）/ pink / blue / plain（无光斑，仅背景图）
 */
import { computed } from 'vue'
import { useAppStore } from '../stores/app'
import { processImage } from '../utils/imageUrl'

const props = defineProps({
  image: { type: [String, Object], default: '' },
  variant: { type: String, default: 'green' }
})

const appStore = useAppStore()

const bgUrl = computed(() => {
  const v = props.image
  const raw = (v && typeof v === 'object' && 'value' in v) ? v.value : v
  if (!raw) return ''
  const profile = appStore.mobile ? 'bgMobile' : 'bgDesktop'
  return processImage(raw, profile)
})
</script>

<style scoped>
.page-bg { position: absolute; inset: 0; overflow: hidden; z-index: 0; pointer-events: none; }

/* 背景图：极淡 + 固定 + 全覆盖，类似 QQ 空间透明背景 */
.page-bg-img {
  position: absolute; inset: 0;
  background-size: cover; background-position: center;
  background-attachment: fixed;
  opacity: 0.06;
}

/* 渐变光斑：柔和动态，填充留白 */
.page-bg-blob { position: absolute; border-radius: 50%; filter: blur(80px); will-change: transform; }
.page-bg.green .blob-1 { width: 45vw; height: 45vw; background: radial-gradient(circle, rgba(129,199,132,0.18), transparent 70%); top: -12%; left: -10%; animation: blobFloat1 24s ease-in-out infinite; }
.page-bg.green .blob-2 { width: 38vw; height: 38vw; background: radial-gradient(circle, rgba(255,183,77,0.14), transparent 70%); bottom: -14%; right: -8%; animation: blobFloat2 28s ease-in-out infinite; }
.page-bg.green .blob-3 { width: 28vw; height: 28vw; background: radial-gradient(circle, rgba(79,195,247,0.12), transparent 70%); top: 45%; left: 55%; animation: blobFloat1 32s ease-in-out infinite reverse; }

.page-bg.pink .blob-1 { width: 45vw; height: 45vw; background: radial-gradient(circle, rgba(255,182,193,0.2), transparent 70%); top: -12%; left: -10%; animation: blobFloat1 24s ease-in-out infinite; }
.page-bg.pink .blob-2 { width: 38vw; height: 38vw; background: radial-gradient(circle, rgba(224,170,255,0.16), transparent 70%); bottom: -14%; right: -8%; animation: blobFloat2 28s ease-in-out infinite; }
.page-bg.pink .blob-3 { width: 28vw; height: 28vw; background: radial-gradient(circle, rgba(255,214,224,0.14), transparent 70%); top: 45%; left: 55%; animation: blobFloat1 32s ease-in-out infinite reverse; }

.page-bg.blue .blob-1 { width: 45vw; height: 45vw; background: radial-gradient(circle, rgba(79,195,247,0.18), transparent 70%); top: -12%; left: -10%; animation: blobFloat1 24s ease-in-out infinite; }
.page-bg.blue .blob-2 { width: 38vw; height: 38vw; background: radial-gradient(circle, rgba(129,199,132,0.14), transparent 70%); bottom: -14%; right: -8%; animation: blobFloat2 28s ease-in-out infinite; }
.page-bg.blue .blob-3 { width: 28vw; height: 28vw; background: radial-gradient(circle, rgba(255,183,77,0.12), transparent 70%); top: 45%; left: 55%; animation: blobFloat1 32s ease-in-out infinite reverse; }

.page-bg.plain .page-bg-blob { display: none; }

@keyframes blobFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(5vw, 7vh) scale(1.12); }
}
@keyframes blobFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-6vw, -5vh) scale(1.08); }
}

@media (max-width: 768px) {
  .page-bg-blob { filter: blur(50px); }
  /* iOS Safari 不支持 background-attachment: fixed —— 它会退化成把图钉在视口上
     重绘，滚动时每帧都要合成一张全屏图，长页面直接掉帧。移动端改回 scroll。 */
  .page-bg-img { background-attachment: scroll; }
  /* 三层 45vw 的高斯模糊在手机 GPU 上并不便宜，砍掉最不起眼的第三层 */
  .page-bg .blob-3 { display: none; }
}

/* 用户在系统里开了「减少动效」：留渐变、去漂移 */
@media (prefers-reduced-motion: reduce) {
  .page-bg-blob { animation: none !important; }
}
</style>
