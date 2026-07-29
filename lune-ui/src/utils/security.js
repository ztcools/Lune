/**
 * 前端安全加固 —— 仅生产环境生效。
 *
 * 说明（企业级诚实边界）：前端代码运行在用户浏览器，本质上是公开的，
 * 无法做到"绝对防查看"。本模块做的是"合理提高调试与抓包门槛"：
 *   1. 生产环境禁用右键 / 常用调试快捷键，提升随意查看的成本；
 *   2. 检测 DevTools 打开并做出轻量响应（清空控制台 + 提示）；
 *   3. 生产构建配合关闭 sourcemap（见 vite.config.js），使源码不可直接还原。
 *
 * 这些措施不拦截有决心的攻击者，真正的数据安全依赖：
 * 服务端鉴权、接口限流、敏感数据不下发。请勿在本层存放任何密钥。
 */

const isProd = import.meta.env.PROD

/** 禁用右键菜单与常见调试快捷键 */
function disableContextAndShortcuts() {
  // 右键
  document.addEventListener('contextmenu', (e) => {
    e.preventDefault()
  })
  // 调试快捷键：F12 / Ctrl+Shift+I / Ctrl+Shift+J / Ctrl+U / Ctrl+S
  document.addEventListener('keydown', (e) => {
    const key = e.key
    const blocked =
      key === 'F12' ||
      (e.ctrlKey && e.shiftKey && ['I', 'i', 'J', 'j', 'C', 'c'].includes(key)) ||
      (e.ctrlKey && ['U', 'u', 'S', 's'].includes(key))
    if (blocked) {
      e.preventDefault()
      e.stopPropagation()
    }
  }, true)
}

/** 检测 DevTools 打开（基于窗口尺寸差），做出轻量响应 */
function watchDevTools() {
  const threshold = 160
  let warned = false
  const check = () => {
    const widthDiff = window.outerWidth - window.innerWidth > threshold
    const heightDiff = window.outerHeight - window.innerHeight > threshold
    if ((widthDiff || heightDiff) && !warned) {
      warned = true
      // 清空控制台，给一条友好提示（不清空页面，避免影响正常用户）
      // eslint-disable-next-line no-console
      console.clear()
      // eslint-disable-next-line no-console
      console.log('%c🔒 本站内容受版权保护，请勿用于商业用途。', 'color:#888;font-size:12px')
    } else if (!widthDiff && !heightDiff) {
      warned = false
    }
  }
  window.addEventListener('resize', check)
  setInterval(check, 1500)
}

/** 初始化（只在生产调用） */
export function setupSecurity() {
  if (!isProd) return
  try {
    disableContextAndShortcuts()
    watchDevTools()
  } catch (_) {
    // 任何异常都不应影响站点正常使用
  }
}
