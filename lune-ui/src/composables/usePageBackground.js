import { ref, watch } from 'vue'
import { useAppStore } from '../stores/app'

/**
 * 获取页面背景图
 * 从后台配置的图片数组中随机选取，仅在配置变更时重新随机
 */
export function usePageBackground(key) {
  const appStore = useAppStore()
  const current = ref('')

  function pick() {
    const images = appStore.bgImages[key]
    if (Array.isArray(images) && images.length > 0) {
      current.value = images[Math.floor(Math.random() * images.length)]
    } else {
      current.value = ''
    }
  }

  // 初始选取
  pick()

  // 配置变更时重新随机
  watch(() => appStore.bgImages[key], () => pick())

  return current
}
