/**
 * 中文书法体的延迟加载。
 *
 * 站点用到三款中文书法/手写体（见 variables.css 的 --calligraphy-font /
 * --cursive-font / --handwriting-font），系统里没有等价字体，只能走 Web Font。
 * 但它们全都是装饰性用途（标题、格言、日记签名），不该拖慢首屏：
 *
 * - 不能自托管：Google 把中文字体按 unicode-range 切片，这三款合计 276 个分片，
 *   往仓库里塞近三百个二进制文件不划算。
 * - 不能直连 fonts.googleapis.com：大陆不可访问，而 <link rel="stylesheet">
 *   是渲染阻塞资源，会让唯一的访问来源白屏数秒。
 * - 因此走国内可达镜像 fonts.loli.net，且在首屏渲染完成后才注入。
 *
 * 动态插入的样式表不阻塞首屏（首屏此时已经画完了），也不需要
 * media="print" + 内联 onload 那套规避手法 —— 那种写法会被本站
 * CSP 的 script-src 'self' 拦掉，样式表会永远停在 media="print"。
 */

const CJK_FONT_CSS =
  'https://fonts.loli.net/css2' +
  '?family=Ma+Shan+Zheng' +
  '&family=Zhi+Mang+Xing' +
  '&family=Long+Cang' +
  '&display=swap'

/**
 * 注入书法体样式表。失败（镜像挂了/被拦截）只记一条 warn：
 * 标题会回落到 variables.css 里的兜底字体，属于观感降级，不是故障。
 */
export function loadCjkDisplayFonts() {
  if (document.querySelector(`link[href="${CJK_FONT_CSS}"]`)) return

  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = CJK_FONT_CSS
  link.crossOrigin = 'anonymous'
  link.onerror = () => {
    console.warn('[fonts] 书法体镜像加载失败，已回落到系统字体')
  }
  document.head.appendChild(link)
}

/**
 * 等首屏真正画完再注入。requestIdleCallback 在 Safari 上仍不可用，
 * 故保留 setTimeout 兜底；两条路径都在 load 之后，不会抢首屏带宽。
 */
export function scheduleCjkDisplayFonts() {
  const run = () => {
    if ('requestIdleCallback' in window) {
      window.requestIdleCallback(loadCjkDisplayFonts, { timeout: 3000 })
    } else {
      setTimeout(loadCjkDisplayFonts, 300)
    }
  }
  if (document.readyState === 'complete') run()
  else window.addEventListener('load', run, { once: true })
}
