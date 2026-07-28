import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function handleAuthFail(message) {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  // 处于后台管理页时跳回登录
  if (window.location.pathname.startsWith('/admin') && !window.location.pathname.startsWith('/admin/login')) {
    ElMessage.error(message || '登录已过期，请重新登录')
    window.location.href = '/admin/login'
  }
}

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 200) {
      return data.data
    }
    if (data.code === 401 || data.code === 403) {
      handleAuthFail(data.message)
      return Promise.reject(new Error(data.message))
    }
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message))
  },
  error => {
    const status = error?.response?.status
    const msg = error?.response?.data?.message
    if (status === 401 || status === 403) {
      handleAuthFail(msg)
    } else {
      ElMessage.error(msg || '网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
