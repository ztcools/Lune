/**
 * 媒体解析工具
 *
 * 统一项目中 JSON 媒体字段的解析逻辑。
 * 后端存储格式: JSON 字符串，如 '["url1","url2"]' 或 '[{"type":"image","url":"..."}]'
 */

/**
 * 解析后端返回的 media JSON 字符串为结构化数组
 * @param {string|null} json - 后端返回的 JSON 字符串
 * @returns {Array<{type: string, url: string}>}
 */
export function parseMedia(json) {
  if (!json) return []
  try {
    let a = JSON.parse(json)
    if (!Array.isArray(a)) return []
    return a
      .map((m) => {
        if (typeof m === 'string') return { type: 'image', url: m }
        if (m && m.url) return { type: m.type || 'image', url: m.url }
        return null
      })
      .filter(Boolean)
  } catch {
    return []
  }
}

/**
 * 从媒体列表中提取图片 URL
 * @param {Array} mediaList
 * @returns {string[]}
 */
export function extractImageUrls(mediaList) {
  if (!Array.isArray(mediaList)) return []
  return mediaList.filter((m) => m.type === 'image').map((m) => m.url)
}
