import { defineStore } from 'pinia'
import { siteConfigApi } from '../api/modules'

export const useAppStore = defineStore('app', {
  state: () => ({
    config: {},
    webInfo: { webName: 'Lune', webTitle: 'Lune', notices: '[]', randomCover: '[]', footer: '', backgroundImage: '', avatar: '', historyAllCount: 0, favicon: '' },
    bgImages: {},
    ownerInfo: { nickname: 'Lune', avatar: '' },
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
          this.webInfo.favicon = data.site_logo || ''
          this.bgImages = {
            landing: data.landing_bg || '',
            homeHero: data.home_hero_bg || '',
            homeContent: data.home_content_bg || '',
            familyHero: data.family_hero_bg || '',
            familyContent: data.family_content_bg || '',
            treeholeDanmaku: data.treehole_danmaku_bg || '',
            treeholeContent: data.treehole_content_bg || '',
            essayHero: data.essay_hero_bg || '',
            essayContent: data.essay_content_bg || '',
            recordHero: data.record_hero_bg || '',
            recordContent: data.record_content_bg || ''
          }
          if (data.site_logo) {
            let link = document.querySelector("link[rel*='icon']")
            if (!link) { link = document.createElement('link'); link.rel = 'icon'; document.head.appendChild(link) }
            link.href = data.site_logo
          }
        }
        // Fetch owner (admin) profile for sidebar avatar sync
        try {
          const { userProfileApi } = await import('../api/modules')
          const profile = await userProfileApi.get()
          if (profile) this.ownerInfo = { nickname: profile.nickname, avatar: profile.avatar }
        } catch (e) { /* silently ignore if not logged in */ }
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
