import { computed } from 'vue'
import { useAppStore } from '../stores/app'

const FALLBACKS = [
  '/assets/背景1.jpg', '/assets/背景2.jpg', '/assets/背景3.jpg',
  '/assets/背景4.jpg', '/assets/背景5.jpg', '/assets/背景6.jpg'
]

export function usePageBackground(key) {
  const appStore = useAppStore()
  return computed(() => {
    const configured = appStore.bgImages[key]
    if (configured) return configured
    return FALLBACKS[Math.floor(Math.random() * FALLBACKS.length)]
  })
}
