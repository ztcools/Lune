<template>
  <div class="smart-image" :style="containerStyle">
    <!-- 模糊占位（加载前显示） -->
    <div
      v-if="!loaded && placeholder"
      class="smart-placeholder"
      :style="{ backgroundImage: `url(${placeholder})` }"
    ></div>
    <div v-if="!loaded" class="smart-skeleton"></div>

    <img
      ref="imgRef"
      :src="finalSrc"
      :srcset="finalSrcset"
      :sizes="sizes"
      :alt="alt"
      :loading="lazy ? 'lazy' : 'eager'"
      :decoding="decoding"
      :fetchpriority="fetchpriority"
      class="smart-img"
      :class="{ loaded }"
      @load="onLoad"
      @error="onError"
    />

    <div v-if="error" class="smart-error">
      <span>📷</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useNetworkQuality } from '../composables/useNetworkQuality'

/**
 * 智能图片组件
 * - 原生懒加载
 * - 模糊占位（LQIP）→ 高清淡入
 * - 响应式 srcset 自动选尺寸
 * - 弱网检测强制小图
 * - WebP 自动降级
 */
const props = defineProps({
  src: { type: String, required: true },
  // srcset 配置：{ 400: 'url-w400', 800: 'url-w800', 1600: 'url-w1600' }
  srcset: { type: Object, default: null },
  // sizes 提示，默认按视口宽度
  sizes: { type: String, default: '(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw' },
  alt: { type: String, default: '' },
  // 宽高比（防止 CLS），如 '16/9' '1/1' '4/3'
  ratio: { type: String, default: '' },
  // 是否懒加载
  lazy: { type: Boolean, default: true },
  // 低质量占位图
  placeholder: { type: String, default: '' },
  // 优先级（首屏关键图设为 high）
  fetchpriority: { type: String, default: 'auto' },
  // 强制质量 'high' | 'medium' | 'low' | 'auto'
  quality: { type: String, default: 'auto' },
  // 加载失败 fallback
  fallback: { type: String, default: '' }
})

const emit = defineEmits(['load', 'error'])

const imgRef = ref(null)
const loaded = ref(false)
const error = ref(false)

const { quality: netQuality } = useNetworkQuality()

// 实际生效的质量
const effectiveQuality = computed(() => {
  if (props.quality !== 'auto') return props.quality
  return netQuality.value // 'high' | 'medium' | 'low'
})

// 根据质量选择 src
const finalSrc = computed(() => {
  if (error.value && props.fallback) return props.fallback
  if (!props.srcset) return props.src
  const map = { low: 400, medium: 800, high: 1600 }
  const targetW = map[effectiveQuality.value] || 800
  // 找到最接近的尺寸
  const keys = Object.keys(props.srcset).map(Number).sort((a, b) => a - b)
  let pickKey = keys[0]
  for (const k of keys) {
    if (k <= targetW) pickKey = k
  }
  // 若目标比最小的还小，用最小；若比最大还大，用最大
  if (targetW >= keys[keys.length - 1]) pickKey = keys[keys.length - 1]
  return props.srcset[pickKey] || props.src
})

// srcset 字符串
const finalSrcset = computed(() => {
  if (!props.srcset) return undefined
  return Object.entries(props.srcset)
    .map(([w, url]) => `${url} ${w}w`)
    .join(', ')
})

const containerStyle = computed(() => {
  if (!props.ratio) return {}
  return { aspectRatio: props.ratio }
})

const decoding = computed(() => (props.lazy ? 'async' : 'auto'))

function onLoad() {
  loaded.value = true
  emit('load')
}

function onError() {
  error.value = true
  loaded.value = true
  emit('error')
}
</script>

<style scoped>
.smart-image {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: #f0f0f2;
}
.smart-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  opacity: 0;
  transition: opacity 0.4s ease;
}
.smart-img.loaded { opacity: 1; }

.smart-placeholder {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: blur(20px) saturate(1.2);
  transform: scale(1.1);
}

.smart-skeleton {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgba(0, 0, 0, 0.04) 25%,
    rgba(0, 0, 0, 0.08) 50%,
    rgba(0, 0, 0, 0.04) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.smart-error {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #c7c7cc;
  background: #f5f5f7;
}
</style>
