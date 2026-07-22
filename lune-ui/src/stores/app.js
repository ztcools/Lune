import { defineStore } from 'pinia'
import { siteConfigApi } from '../api/modules'

export const useAppStore = defineStore('app', {
  state: () => ({
    config: {},
    webInfo: { webName: 'Lune', webTitle: 'Lune', notices: '[]', randomCover: '[]', footer: '', backgroundImage: '', avatar: '', historyAllCount: 0 },
    sortInfo: [],
    toolbar: { visible: true, enter: false },
    darkMode: false,
    mobile: false
  }),
  actions: {
    async fetchConfig() {
      try {
        const data = await siteConfigApi.getPublic()
        if (data) {
          this.config = data
          this.webInfo.webName = data.site_name || 'Lune'
          this.webInfo.webTitle = data.site_title || 'Lune'
          this.webInfo.footer = data.site_footer || ''
          this.webInfo.backgroundImage = data.site_background || ''
          this.webInfo.avatar = data.site_avatar || ''
          this.webInfo.notices = data.notices || '[]'
          this.webInfo.randomCover = data.random_cover || '[]'
        }
      } catch (e) { console.error('Failed to fetch site config') }
    },
    toggleDark() {
      this.darkMode = !this.darkMode
      document.documentElement.setAttribute('data-theme', this.darkMode ? 'dark' : '')
      localStorage.setItem('darkMode', this.darkMode ? '1' : '0')
    },
    initDarkMode() {
      const saved = localStorage.getItem('darkMode')
      if (saved === '1') {
        this.darkMode = true
        document.documentElement.setAttribute('data-theme', 'dark')
      }
    },
    changeToolbarStatus(status) {
      this.toolbar = { ...this.toolbar, ...status }
    }
  }
})
