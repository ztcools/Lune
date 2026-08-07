/**
 * useNow — 高效时间戳 composable
 *
 * 替代每秒 setInterval 的计时器方案：
 * - 使用 requestAnimationFrame，只在屏幕可见时驱动更新
 * - 不触发不必要的 Vue 响应式重渲染
 * - 自动在组件销毁时清理
 *
 * 用法:
 * const now = useNow()
 * const togetherDays = computed(() =>
 *   family.value.timing
 *     ? Math.max(0, Math.floor((now.value - new Date(family.value.timing).getTime()) / 86400000))
 *     : 0
 * )
 */
import { ref, onMounted, onUnmounted } from 'vue'

export function useNow() {
  const now = ref(Date.now())
  let rafId = null

  function tick() {
    now.value = Date.now()
    rafId = requestAnimationFrame(tick)
  }

  onMounted(() => {
    rafId = requestAnimationFrame(tick)
  })

  onUnmounted(() => {
    if (rafId) cancelAnimationFrame(rafId)
  })

  return now
}
