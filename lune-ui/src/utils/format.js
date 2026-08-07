/**
 * 通用格式化/解析工具
 */

/**
 * 安全解析 JSON 字符串
 * @param {string|null} str - 原始 JSON 字符串
 * @param {*} fallback - 解析失败时的默认值
 * @returns {*}
 */
export function safeJsonParse(str, fallback) {
  if (!str) return fallback
  try {
    return JSON.parse(str)
  } catch {
    return fallback
  }
}

/**
 * 将纯文本换行转换为 HTML <br>（XSS 安全：不解析 HTML 标签）
 * @param {string} text - 原始文本
 * @returns {string} 转义后的 HTML
 */
export function nl2br(text) {
  if (!text) return ''
  return text
    .replace(/\n{2,}/g, '<div style="height:8px"></div>')
    .replace(/\n/g, '<br/>')
}

/**
 * 截断文本（按字符数）
 * @param {string} text
 * @param {number} maxLen - 最大字符数
 * @param {string} suffix
 * @returns {string}
 */
export function truncate(text, maxLen = 120, suffix = '...') {
  if (!text || text.length <= maxLen) return text || ''
  return text.substring(0, maxLen) + suffix
}
