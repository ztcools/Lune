/**
 * 日期时间格式化工具
 *
 * 统一项目中的日期格式化逻辑，避免多个视图中的重复实现。
 */

/**
 * 相对时间格式化
 * @param {string|Date} d - 日期字符串或 Date 对象
 * @returns {string} 如 "刚刚"、"3分钟前"、"2小时前"、"5天前"、"3个月前"、"1年前"
 */
export function formatRelative(d) {
  if (!d) return ''
  const diff = Date.now() - new Date(d).getTime()
  const s = Math.floor(diff / 1000)
  const m = Math.floor(s / 60)
  const h = Math.floor(m / 60)
  const days = Math.floor(h / 24)
  if (s < 60) return '刚刚'
  if (m < 60) return `${m}分钟前`
  if (h < 24) return `${h}小时前`
  if (days < 30) return `${days}天前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}

/**
 * 格式化日期为短格式（如 2024-08-08）
 * @param {string|Date} d
 * @returns {string}
 */
export function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/**
 * 格式化日期为中文长格式（如 2024年8月8日）
 * @param {string|Date} d
 * @returns {string}
 */
export function formatFullDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

/**
 * 格式化日期为 locale 短格式
 * @param {string|Date} d
 * @returns {string}
 */
export function formatLocaleDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}
