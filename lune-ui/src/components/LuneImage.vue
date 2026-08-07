<template>
  <img
    :src="processedSrc"
    :alt="alt"
    :width="imgWidth"
    :height="imgHeight"
    :loading="loadingAttr"
    :decoding="decodingAttr"
    :fetchpriority="fetchPriorityAttr"
    :class="['lune-image', { 'lune-image-lcp': lcp }]"
    :style="{ objectFit: fit, aspectRatio: aspectRatio || undefined }"
    @click="handleClick"
  />
</template>

<script setup>
/**
 * LuneImage — 统一响应式图片组件
 *
 * 替代 <el-image> 和裸 <img>：
 * - 自动拼接万象CI参数（生产），本地路径原样（开发）
 * - LCP 首屏图片：fetchpriority="high" + loading="eager"
 * - 非首屏图片：loading="lazy" + decoding="async"
 * - 显式 width/height 消除 CLS
 * - 点击大图预览（透传 previewSrcList）
 */
import { computed } from 'vue'
import { processImage, responsiveProfile } from '../utils/imageUrl'

const SIZE_MAP = {
  thumb:     { w: 400, h: 0,   ratio: '4/3' },
  content:   { w: 800, h: 0,   ratio: undefined },
  cover:     { w: 1200, h: 630, ratio: '1200/630' },
  hero:      { w: 1920, h: 0,   ratio: undefined },
  heroMobile:{ w: 750, h: 0,   ratio: undefined },
  avatar:    { w: 200, h: 200,  ratio: '1/1' },
  bgDesktop: { w: 1920, h: 0,   ratio: undefined },
  bgMobile:  { w: 750, h: 0,   ratio: undefined },
}

const props = defineProps({
  src:        { type: String, default: '' },
  variant:    { type: String, default: 'content' }, // thumb|content|cover|hero|avatar|bg
  lcp:        { type: Boolean, default: false },
  alt:        { type: String, default: '' },
  fit:        { type: String, default: 'cover' },
  width:      { type: [Number, String], default: undefined },
  height:     { type: [Number, String], default: undefined },
  previewSrcList: { type: Array, default: undefined }, // 保留但本文以简单实现为主
  mobile:     { type: Boolean, default: undefined }, // 可选：强制指定设备
})

const emit = defineEmits(['click'])

const processedSrc = computed(() => {
  if (!props.src) return ''
  // 如果是 bg 类 variant，根据设备选 profile
  if (props.variant === 'bg' || props.variant.startsWith('bg')) {
    const isMobile = props.mobile !== undefined ? props.mobile : false
    const profile = responsiveProfile(isMobile, 'bgDesktop', 'bgMobile')
    return processImage(props.src, profile)
  }
  if (props.variant === 'hero') {
    const isMobile = props.mobile !== undefined ? props.mobile : false
    const profile = responsiveProfile(isMobile, 'hero', 'heroMobile')
    return processImage(props.src, profile)
  }
  return processImage(props.src, props.variant)
})

const sizeInfo = computed(() => SIZE_MAP[props.variant] || SIZE_MAP.content)

const imgWidth = computed(() => {
  if (props.width) return props.width
  if (sizeInfo.value.w) return sizeInfo.value.w
  return undefined
})

const imgHeight = computed(() => {
  if (props.height) return props.height
  if (sizeInfo.value.h) return sizeInfo.value.h
  return undefined
})

const aspectRatio = computed(() => sizeInfo.value.ratio)
const loadingAttr = computed(() => props.lcp ? 'eager' : 'lazy')
const decodingAttr = computed(() => props.lcp ? 'sync' : 'async')
const fetchPriorityAttr = computed(() => props.lcp ? 'high' : 'auto')

function handleClick() {
  emit('click')
}
</script>

<style scoped>
.lune-image {
  display: block;
  max-width: 100%;
  height: auto;
  background: var(--el-fill-color-light, #f0f0f0);
  transition: opacity 0.3s ease;
}

.lune-image[loading="lazy"] {
  /* 加载中保持占位，避免CLS——width/height属性已在HTML处理 */
}
</style>
