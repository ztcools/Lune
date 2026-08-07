/**
 * 图片URL万象CI处理参数拼接工具
 *
 * 腾讯云数据万象（CI）通过 URL 参数实现实时图片处理：
 *   存一张原图 → 不同场景拼不同参数 → CDN自动缓存处理结果
 *
 * 开发环境（/upload/ /media/ 路径）原样返回，不拼参数。
 */

const PROFILES = {
  thumb:      'imageMogr2/thumbnail/400x/format/webp/rquality/75/ignore-error/1',
  content:    'imageMogr2/thumbnail/800x/format/webp/rquality/80/ignore-error/1',
  cover:      'imageMogr2/thumbnail/1200x/format/webp/rquality/80/ignore-error/1',
  hero:       'imageMogr2/thumbnail/1920x/format/webp/rquality/75/ignore-error/1',
  heroMobile: 'imageMogr2/thumbnail/750x/format/webp/rquality/70/ignore-error/1',
  avatar:     'imageMogr2/thumbnail/200x200/format/webp/ignore-error/1',
  bgDesktop:  'imageMogr2/thumbnail/1920x/format/webp/rquality/75/ignore-error/1',
  bgMobile:   'imageMogr2/thumbnail/750x/format/webp/rquality/70/ignore-error/1',
}

/** 本地路径前缀——这些路径不需要万象CI处理 */
const LOCAL_PREFIXES = ['/upload/', '/media/', 'data:image/', 'blob:']

/**
 * 根据使用场景拼接万象CI处理参数
 * @param {string} url - 原始图片URL
 * @param {string} profile - 使用场景: thumb|content|cover|hero|heroMobile|avatar|bgDesktop|bgMobile
 * @returns {string} 带处理参数的URL
 */
export function processImage(url, profile = 'content') {
  if (!url) return ''
  for (const prefix of LOCAL_PREFIXES) {
    if (url.startsWith(prefix)) return url
  }
  const params = PROFILES[profile] || PROFILES.content
  const sep = url.includes('?') ? '&' : '?'
  return url + sep + params
}

/**
 * 根据设备类型获取合适的 profile 名
 * @param {boolean} isMobile - 是否为移动端
 * @param {string} desktopProfile - PC端 profile 名
 * @param {string} mobileProfile - 移动端 profile 名（可选，默认加 Mobile 后缀）
 * @returns {string}
 */
export function responsiveProfile(isMobile, desktopProfile, mobileProfile) {
  if (!isMobile) return desktopProfile
  if (mobileProfile) return mobileProfile
  return desktopProfile + 'Mobile'
}

export { PROFILES }
