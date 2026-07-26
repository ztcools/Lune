import { ElNotification } from 'element-plus'
import { useUserStore } from '../stores/user'

let showLoginCard = null

export function setLoginCardTrigger(fn) {
  showLoginCard = fn
}

export function requireLogin() {
  const userStore = useUserStore()
  if (userStore.isLoggedIn) return true
  ElNotification({
    title: '需要登录',
    message: '请先登录后再执行此操作',
    type: 'warning',
    duration: 4000,
    customClass: 'login-required-notify',
    onClick() {
      if (showLoginCard) showLoginCard()
    }
  })
  return false
}
