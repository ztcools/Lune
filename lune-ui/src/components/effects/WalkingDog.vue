<template>
  <div class="walking-dog" aria-hidden="true" :style="{ bottom: bottom + 'px' }">
    <div class="dog-body">
      <span class="dog-emoji">{{ dogEmoji }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 一只偶尔从屏幕边缘走过的可爱小狗，为页面增添生气。
// 走动节奏由 CSS 的 dog-walk 动画控制（18s 一趟），没有定时器：
// 原来这里留着一个 interval prop 和 timer 变量，谁都没赋值过，onUnmounted 里
// 还在 clear 一个永远是 null 的 timer —— 一并清掉。
const props = defineProps({
  bottom: { type: Number, default: 8 }
})

const dogEmoji = ref('🐕')

onMounted(() => {
  // 随机小狗表情
  const dogs = ['🐕', '🐩', '🦮', '🐕‍🦺']
  dogEmoji.value = dogs[Math.floor(Math.random() * dogs.length)]
})
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

/* 这只狗的全部内容就是「走过去」，减少动态效果时没有静态形态可留 */
@media (prefers-reduced-motion: reduce) {
  .walking-dog { display: none; }
}
</style>
