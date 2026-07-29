import { ElNotification } from 'element-plus'
import { h } from 'vue'
import { useUserStore } from '../stores/user'

let showLoginCard = null

export function setLoginCardTrigger(fn) {
  showLoginCard = fn
}

/**
 * 需要登录的拦截提醒
 * 使用渐变色卡片 + 明确的"点击登录"CTA按钮，让用户一眼知道可以点
 */
export function requireLogin() {
  const userStore = useUserStore()
  if (userStore.isLoggedIn) return true

  ElNotification({
    title: '',
    message: h('div', { class: 'login-notify-content' }, [
      h('div', { class: 'login-notify-icon' }, '🔐'),
      h('div', { class: 'login-notify-text' }, [
        h('div', { class: 'login-notify-title' }, '需要登录'),
        h('div', { class: 'login-notify-desc' }, '登录后即可继续操作')
      ]),
      h('div', { class: 'login-notify-btn' }, '立即登录 →')
    ]),
    type: 'info',
    duration: 5000,
    position: 'top-right',
    customClass: 'login-required-notify',
    showClose: true,
    onClick() {
      if (showLoginCard) showLoginCard()
    }
  })
  return false
}
