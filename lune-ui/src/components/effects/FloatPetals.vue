<template>
  <div class="petals-layer" aria-hidden="true">
    <span
      v-for="p in petals"
      :key="p.id"
      class="petal"
      :style="p.style"
    >{{ p.char }}</span>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 轻盈飘落装饰 —— 花瓣/叶子/雪花
 * 纯 CSS 动画，性能开销极小；移动端自动减半数量
 */
const props = defineProps({
  count: { type: Number, default: 14 },
  // petal | leaf | snow
  type: { type: String, default: 'petal' }
})

const CHARS = {
  petal: ['🌸', '🌺', '💮', '🏵️'],
  leaf: ['🍃', '🍀', '🌿', '☘️'],
  snow: ['❄️', '❅', '❆', '✻']
}

const petals = ref([])
let idSeq = 0

function makePetal() {
  const chars = CHARS[props.type] || CHARS.petal
  const size = 12 + Math.random() * 14
  const left = Math.random() * 100
  const duration = 9 + Math.random() * 10
  const delay = -Math.random() * 12
  const drift = (Math.random() * 60 - 30).toFixed(0)
  return {
    id: idSeq++,
    char: chars[Math.floor(Math.random() * chars.length)],
    style: {
      left: left + 'vw',
      fontSize: size + 'px',
      animationDuration: duration + 's',
      animationDelay: delay + 's',
      '--drift': drift + 'px',
      opacity: (0.5 + Math.random() * 0.5).toFixed(2)
    }
  }
}

onMounted(() => {
  const isMobile = window.innerWidth < 768
  const n = isMobile ? Math.ceil(props.count / 2) : props.count
  petals.value = Array.from({ length: n }, makePetal)
})
onUnmounted(() => { petals.value = [] })
</script>

<style scoped>
.petals-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 5;
}
.petal {
  position: absolute;
  top: -8%;
  animation: petal-fall linear infinite;
  will-change: transform;
  filter: drop-shadow(0 2px 3px rgba(0,0,0,0.08));
}
@keyframes petal-fall {
  0% { transform: translateY(-8vh) translateX(0) rotate(0deg); }
  100% { transform: translateY(112vh) translateX(var(--drift)) rotate(360deg); }
}
</style>
