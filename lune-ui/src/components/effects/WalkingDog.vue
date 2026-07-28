<template>
  <div class="walking-dog" aria-hidden="true" :style="{ bottom: bottom + 'px' }">
    <div class="dog-body">
      <span class="dog-emoji">{{ dogEmoji }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 一只偶尔从屏幕边缘走过的可爱小狗，为页面增添生气
const props = defineProps({
  bottom: { type: Number, default: 8 },
  // 每隔多少秒走一次
  interval: { type: Number, default: 24 }
})

const dogEmoji = ref('🐕')
let timer = null

onMounted(() => {
  // 随机小狗表情
  const dogs = ['🐕', '🐩', '🦮', '🐕‍🦺']
  dogEmoji.value = dogs[Math.floor(Math.random() * dogs.length)]
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.walking-dog {
  position: fixed;
  left: 0;
  z-index: 2;
  pointer-events: none;
  animation: dog-walk 18s linear infinite;
  animation-delay: 4s;
}
.dog-body {
  font-size: 34px;
  transform: scaleX(-1);
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.12));
  animation: dog-bob 0.6s ease-in-out infinite;
}
@keyframes dog-walk {
  0% { transform: translateX(-10vw); opacity: 0; }
  3% { opacity: 1; }
  96% { opacity: 1; }
  100% { transform: translateX(112vw); opacity: 0; }
}
@keyframes dog-bob {
  0%, 100% { transform: scaleX(-1) translateY(0); }
  50% { transform: scaleX(-1) translateY(-5px); }
}
</style>
