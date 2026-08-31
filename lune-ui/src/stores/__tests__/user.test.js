import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user'
import { authApi } from '../../api/modules'

// 隔离网络层，只测 store 状态流转与 localStorage 同步
vi.mock('../../api/modules', () => ({
  authApi: { login: vi.fn(), register: vi.fn(), logout: vi.fn() },
  userProfileApi: { get: vi.fn(), update: vi.fn() }
}))

describe('user store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts logged out', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.token).toBe('')
  })

  it('login stores token and user in localStorage', async () => {
    authApi.login.mockResolvedValue({
      token: 'jwt-token', userId: 1, username: 'admin', nickname: '站长',
      email: '', avatar: '', gender: '', signature: '', role: 'ADMIN'
    })

    const store = useUserStore()
    await store.login('admin', 'pass')

    expect(store.token).toBe('jwt-token')
    expect(store.isLoggedIn).toBe(true)
    expect(store.isAdmin).toBe(true)
    expect(store.nickname).toBe('站长')
    expect(localStorage.getItem('token')).toBe('jwt-token')
    expect(JSON.parse(localStorage.getItem('user')).username).toBe('admin')
  })

  it('logout clears token and user', async () => {
    localStorage.setItem('token', 'jwt-token')
    localStorage.setItem('user', JSON.stringify({ username: 'admin', role: 'ADMIN' }))
    authApi.logout.mockResolvedValue(undefined)

    const store = useUserStore()
    expect(store.isLoggedIn).toBe(true)

    await store.logout()

    expect(store.token).toBe('')
    expect(store.user).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('user')).toBeNull()
  })
})
