import { defineStore } from 'pinia'
import { siteConfigApi } from '../api/modules'

export const useAppStore = defineStore('app', {
  state: () => ({
    config: {},
    webInfo: { webName: 'Lune', webTitle: 'Lune', notices: '[]', footer: '', favicon: '' },
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
          this.webInfo.notices = data.notices || '[]'
          this.webInfo.favicon = data.site_logo || ''

          // 背景图：每个 key 存储 JSON 数组，随机选取
          // PC端 + 移动端双套配置，前端按设备自动选择
          this.bgImages = {
            landing: this.parseJsonArray(data.landing_bg),
            homeHero: this.parseJsonArray(data.home_hero_bg),
            homeContent: this.parseJsonArray(data.home_content_bg),
            familyHero: this.parseJsonArray(data.family_hero_bg),
            familyContent: this.parseJsonArray(data.family_content_bg),
            treeholeDanmaku: this.parseJsonArray(data.treehole_danmaku_bg),
            treeholeContent: this.parseJsonArray(data.treehole_content_bg),
            essayHero: this.parseJsonArray(data.essay_hero_bg),
            essayContent: this.parseJsonArray(data.essay_content_bg),
            recordHero: this.parseJsonArray(data.record_hero_bg),
            recordContent: this.parseJsonArray(data.record_content_bg),
            wishHero: this.parseJsonArray(data.wish_hero_bg),
            wishContent: this.parseJsonArray(data.wish_content_bg),
            resumeHero: this.parseJsonArray(data.resume_hero_bg),
            // 移动端专用
            landingMobile: this.parseJsonArray(data.landing_bg_mobile),
            homeHeroMobile: this.parseJsonArray(data.home_hero_bg_mobile),
            homeContentMobile: this.parseJsonArray(data.home_content_bg_mobile),
            familyHeroMobile: this.parseJsonArray(data.family_hero_bg_mobile),
            familyContentMobile: this.parseJsonArray(data.family_content_bg_mobile),
            treeholeDanmakuMobile: this.parseJsonArray(data.treehole_danmaku_bg_mobile),
            treeholeContentMobile: this.parseJsonArray(data.treehole_content_bg_mobile),
            essayHeroMobile: this.parseJsonArray(data.essay_hero_bg_mobile),
            essayContentMobile: this.parseJsonArray(data.essay_content_bg_mobile),
            recordHeroMobile: this.parseJsonArray(data.record_hero_bg_mobile),
            recordContentMobile: this.parseJsonArray(data.record_content_bg_mobile),
            wishHeroMobile: this.parseJsonArray(data.wish_hero_bg_mobile),
            wishContentMobile: this.parseJsonArray(data.wish_content_bg_mobile),
            resumeHeroMobile: this.parseJsonArray(data.resume_hero_bg_mobile)
          }

          if (data.site_logo) {
            let link = document.querySelector("link[rel*='icon']")
            if (!link) { link = document.createElement('link'); link.rel = 'icon'; document.head.appendChild(link) }
            link.href = data.site_logo
          }
        }
        // 获取管理员公开信息（头像/昵称）
        try {
          const { userProfileApi } = await import('../api/modules')
          const profile = await userProfileApi.getPublic(1)
          if (profile) this.ownerInfo = { nickname: profile.nickname, avatar: profile.avatar }
        } catch (e) { /* 忽略获取失败 */ }
      } catch (e) { console.error('Failed to fetch site config') }
    },

    /** 解析 JSON 数组字符串为数组 */
    parseJsonArray(val) {
      if (!val || val === '[]') return []
      try {
        const arr = JSON.parse(val)
        return Array.isArray(arr) ? arr : []
      } catch { return [] }
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
