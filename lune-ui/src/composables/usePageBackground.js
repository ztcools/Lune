import { computed } from 'vue'
import { useAppStore } from '../stores/app'

/**
 * 获取页面背景图
 * 优先使用后台配置的图片数组（随机选取），无配置时返回空字符串
 */
export function usePageBackground(key) {
  const appStore = useAppStore()
  return computed(() => {
    const images = appStore.bgImages[key]
    if (Array.isArray(images) && images.length > 0) {
      return images[Math.floor(Math.random() * images.length)]
    }
    return ''
  })
}
