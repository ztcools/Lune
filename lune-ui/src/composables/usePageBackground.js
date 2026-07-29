import { ref, watch } from 'vue'
import { useAppStore } from '../stores/app'

/**
 * 获取页面背景图（自动按设备选 PC/移动）
 * 从后台配置的图片数组中随机选取，仅在配置变更时重新随机
 *
 * @param {string} key - 背景 key（camelCase，如 'homeHero' 'essayContent'）
 * @returns {Ref<string>} 当前背景图 URL
 *
 * 后台 site_config 命名约定：
 *   snake_case      → PC 端（如 home_hero_bg）
 *   snake_case + _mobile → 移动端专用（如 home_hero_bg_mobile）
 * 映射由 store.app.bgImages 完成（camelCase + Mobile 后缀）
 */
export function usePageBackground(key) {
  const appStore = useAppStore()
  const current = ref('')

  function pick() {
    // 移动端优先用 mobile 后缀的 key，没有则 fallback 到主 key
    const mobileKey = key + 'Mobile'
    const useKey = (appStore.mobile && Array.isArray(appStore.bgImages[mobileKey]) && appStore.bgImages[mobileKey].length > 0)
      ? mobileKey
      : key
    const images = appStore.bgImages[useKey]
    if (Array.isArray(images) && images.length > 0) {
      current.value = images[Math.floor(Math.random() * images.length)]
    } else {
      current.value = ''
    }
  }

  // 初始选取
  pick()

  // 配置变更 / 设备切换 时重新随机
  watch(() => appStore.bgImages[key], () => pick())
  watch(() => appStore.bgImages[key + 'Mobile'], () => pick())
  watch(() => appStore.mobile, () => pick())

  return current
}
