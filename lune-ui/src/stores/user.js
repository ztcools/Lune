import { defineStore } from 'pinia'
import { authApi, userProfileApi } from '../api/modules'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN',
    username: (state) => state.user?.username || '',
    nickname: (state) => state.user?.nickname || state.user?.username || ''
  },
  actions: {
    async login(account, password) {
      const data = await authApi.login({ account, password })
      this.token = data.token
      this.user = {
        userId: data.userId, username: data.username, nickname: data.nickname,
        email: data.email, avatar: data.avatar, gender: data.gender,
        signature: data.signature, role: data.role
      }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return data
    },
    async register(form) {
      const data = await authApi.register(form)
      this.token = data.token
      this.user = {
        userId: data.userId, username: data.username, nickname: data.nickname,
        email: data.email, avatar: data.avatar, gender: data.gender,
        signature: data.signature, role: data.role
      }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return data
    },
    async refreshProfile() {
      const data = await userProfileApi.get()
      this.user = {
        ...this.user,
        userId: data.id, username: data.username, nickname: data.nickname,
        email: data.email, avatar: data.avatar, gender: data.gender,
        birthday: data.birthday, signature: data.signature, role: data.role
      }
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    async updateProfile(form) {
      const data = await userProfileApi.update(form)
      this.user = {
        ...this.user,
        nickname: data.nickname, avatar: data.avatar, gender: data.gender,
        birthday: data.birthday, signature: data.signature
      }
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    async logout() {
      try { await authApi.logout() } catch (e) {}
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
