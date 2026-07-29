import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 网络质量检测（响应式）
 * 返回 quality: 'high' | 'medium' | 'low'
 *
 * 规则：
 * - saveData=true → low
 * - effectiveType: 'slow-2g' | '2g' → low
 * - effectiveType: '3g' → medium
 * - effectiveType: '4g' | undefined → high
 * - downlink < 1 Mbps → low
 * - downlink < 3 Mbps → medium
 */
export function useNetworkQuality() {
  const quality = ref('high')
  const connection = ref(null)

  function detect() {
    if (typeof navigator === 'undefined') {
      quality.value = 'high'
      return
    }
    const conn = navigator.connection || navigator.mozConnection || navigator.webkitConnection
    if (!conn) {
      quality.value = 'high'
      return
    }
    connection.value = conn

    // 用户开启省流模式
    if (conn.saveData) {
      quality.value = 'low'
      return
    }

    const type = conn.effectiveType
    if (type === 'slow-2g' || type === '2g') {
      quality.value = 'low'
      return
    }
    if (type === '3g') {
      quality.value = 'medium'
      return
    }

    // 用 downlink 兜底（部分浏览器 effectiveType 不准）
    const dl = conn.downlink // Mbps
    if (typeof dl === 'number') {
      if (dl < 1) { quality.value = 'low'; return }
      if (dl < 3) { quality.value = 'medium'; return }
    }
    quality.value = 'high'
  }

  function onChange() { detect() }

  onMounted(() => {
    detect()
    const conn = connection.value
    if (conn && conn.addEventListener) {
      conn.addEventListener('change', onChange)
    }
  })

  onUnmounted(() => {
    const conn = connection.value
    if (conn && conn.removeEventListener) {
      conn.removeEventListener('change', onChange)
    }
  })

  return { quality }
}

/**
 * 根据当前网络质量，从 URL 集合中挑选合适的图
 * @param {Object} srcset - { 400: url, 800: url, 1600: url }
 * @param {string} fallback - 没找到时的兜底 URL
 */
export function pickImageByNetwork(srcset, fallback = '') {
  if (!srcset || typeof srcset !== 'object') return fallback
  const { quality } = useNetworkQuality()
  const map = { low: 400, medium: 800, high: 1600 }
  const targetW = map[quality.value] || 800
  const keys = Object.keys(srcset).map(Number).sort((a, b) => a - b)
  let pickKey = keys[0]
  for (const k of keys) {
    if (k <= targetW) pickKey = k
  }
  if (targetW >= keys[keys.length - 1]) pickKey = keys[keys.length - 1]
  return srcset[pickKey] || fallback
}
