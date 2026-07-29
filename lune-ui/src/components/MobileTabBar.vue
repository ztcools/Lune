<template>
  <nav v-if="!isLanding" class="mobile-tabbar" :class="{ hidden: !visible }">
    <div
      v-for="tab in mainTabs"
      :key="tab.path"
      class="tabbar-item"
      :class="{ active: isActive(tab.path) }"
      @click="goTab(tab.path)"
      @touchstart.passive="onTouchStart"
    >
      <span class="tabbar-icon">{{ tab.icon }}</span>
      <span class="tabbar-label">{{ tab.label }}</span>
      <span class="tabbar-dot"></span>
    </div>

    <!-- 我的（弹出 ActionSheet） -->
    <div
      class="tabbar-item"
      :class="{ active: isUserActive }"
      @click="showUserSheet = true"
      @touchstart.passive="onTouchStart"
    >
      <span class="tabbar-icon">👤</span>
      <span class="tabbar-label">我的</span>
      <span class="tabbar-dot"></span>
    </div>

    <!-- 用户面板（ActionSheet） -->
    <teleport to="body">
      <transition name="sheet">
        <div v-if="showUserSheet" class="sheet-mask" @click.self="showUserSheet = false">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <div
              class="sheet-header"
              :class="{ clickable: !userStore.isLoggedIn }"
              @click="!userStore.isLoggedIn && onLogin()"
            >
              <el-avatar :size="44" :src="userStore.user?.avatar" class="sheet-avatar">
                {{ (userStore.nickname || '游').charAt(0) }}
              </el-avatar>
              <div class="sheet-user">
                <div class="sheet-name">{{ userStore.isLoggedIn ? (userStore.nickname || '游客') : '未登录' }}</div>
                <div class="sheet-tip" :class="{ 'login-tip': !userStore.isLoggedIn }">
                  {{ userStore.isLoggedIn ? '欢迎回来～' : '👆 点击登录 / 注册' }}
                </div>
              </div>
              <div v-if="!userStore.isLoggedIn" class="sheet-login-cta">
                立即登录 →
              </div>
            </div>
            <div class="sheet-grid">
              <div class="sheet-item" @click="goPage('/record')">
                <span class="sheet-icon">📒</span>
                <span class="sheet-label">记录</span>
              </div>
              <div class="sheet-item" @click="goPage('/wish')">
                <span class="sheet-icon">🌠</span>
                <span class="sheet-label">许愿池</span>
              </div>
              <div class="sheet-item" @click="goPage('/resume')">
                <span class="sheet-icon">🌿</span>
                <span class="sheet-label">简历</span>
              </div>
              <div v-if="userStore.isAdmin" class="sheet-item" @click="goAdmin">
                <span class="sheet-icon">💻</span>
                <span class="sheet-label">后台</span>
              </div>
              <div v-if="userStore.isLoggedIn" class="sheet-item" @click="showProfile = true; showUserSheet = false">
                <span class="sheet-icon">⚙️</span>
                <span class="sheet-label">设置</span>
              </div>
              <div v-if="!userStore.isLoggedIn" class="sheet-item" @click="onLogin">
                <span class="sheet-icon">🔐</span>
                <span class="sheet-label">登录</span>
              </div>
              <div v-else class="sheet-item danger" @click="onLogout">
                <span class="sheet-icon">🚪</span>
                <span class="sheet-label">退出</span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <LoginCard :visible="showLogin" @close="showLogin = false" @logged-in="showLogin = false" />
    <ProfileCard :visible="showProfile" @close="showProfile = false" />
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import LoginCard from './LoginCard.vue'
import ProfileCard from './ProfileCard.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const showUserSheet = ref(false)
const showLogin = ref(false)
const showProfile = ref(false)
const visible = ref(true)

const isLanding = computed(() => route.name === 'Landing')

const mainTabs = [
  { path: '/home', icon: '🏠', label: '首页' },
  { path: '/family', icon: '💕', label: '家' },
  { path: '/treehole', icon: '🌳', label: '树洞' },
  { path: '/essay', icon: '✏️', label: '随笔' }
]

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

const isUserActive = computed(() => {
  const userPaths = ['/record', '/wish', '/resume']
  return userPaths.some(p => isActive(p)) || showUserSheet.value
})

let lastScrollTop = 0
function onScroll() {
  const st = document.documentElement.scrollTop || document.body.scrollTop
  // 下滑隐藏、上滑显示
  if (st > lastScrollTop && st > 100) {
    visible.value = false
  } else {
    visible.value = true
  }
  lastScrollTop = st
}

function goTab(path) {
  if (route.path === path) return
  vibrate()
  router.push(path)
}

function goPage(path) {
  showUserSheet.value = false
  vibrate()
  router.push(path)
}

function goAdmin() {
  showUserSheet.value = false
  window.open(router.resolve('/admin').href)
}

function onLogin() {
  showUserSheet.value = false
  showLogin.value = true
}

async function onLogout() {
  showUserSheet.value = false
  await userStore.logout()
  router.push('/')
}

function vibrate() {
  if (navigator.vibrate) navigator.vibrate(8)
}

function onTouchStart() {
  // iOS 触摸反馈
}

import { onMounted, onUnmounted } from 'vue'
onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.mobile-tabbar {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  height: calc(56px + env(safe-area-inset-bottom, 0px));
  padding-bottom: env(safe-area-inset-bottom, 0px);
  display: flex;
  justify-content: space-around;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.04);
  z-index: 1000;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  -webkit-user-select: none;
  -webkit-tap-highlight-color: transparent;
}
.mobile-tabbar.hidden {
  transform: translateY(100%);
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  position: relative;
  color: #8a8a8e;
  transition: color 0.25s;
}
.tabbar-item:active {
  transform: scale(0.92);
}

.tabbar-icon {
  font-size: 22px;
  line-height: 1;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  filter: grayscale(0.4);
}
.tabbar-item.active .tabbar-icon {
  transform: scale(1.18) translateY(-2px);
  filter: grayscale(0) drop-shadow(0 2px 8px rgba(102, 187, 106, 0.4));
}

.tabbar-label {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.5px;
  font-family: var(--trendy-font, sans-serif);
  transition: all 0.25s;
}
.tabbar-item.active .tabbar-label {
  color: var(--nature-green, #66bb6a);
  font-weight: 700;
}

.tabbar-dot {
  position: absolute;
  bottom: 6px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--nature-green, #66bb6a);
  opacity: 0;
  transform: scale(0);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.tabbar-item.active .tabbar-dot {
  opacity: 1;
  transform: scale(1);
}

/* ============ ActionSheet ============ */
.sheet-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 2000;
  display: flex;
  align-items: flex-end;
  backdrop-filter: blur(2px);
}
.sheet-panel {
  width: 100%;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  border-radius: 20px 20px 0 0;
  padding: 12px 20px calc(20px + env(safe-area-inset-bottom, 0px));
  box-shadow: 0 -4px 32px rgba(0, 0, 0, 0.1);
  max-height: 70vh;
  overflow-y: auto;
}
.sheet-handle {
  width: 40px;
  height: 4px;
  background: #d1d1d6;
  border-radius: 2px;
  margin: 0 auto 16px;
}
.sheet-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 4px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
  border-radius: 12px;
  transition: all 0.2s;
}
.sheet-header.clickable {
  cursor: pointer;
  padding: 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  border: 1.5px dashed rgba(102, 126, 234, 0.3);
  margin: -4px -4px 12px;
}
.sheet-header.clickable:active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15), rgba(118, 75, 162, 0.15));
  transform: scale(0.98);
}
.sheet-user { flex: 1; min-width: 0; }
.sheet-name {
  font-size: 17px;
  font-weight: 700;
  color: #1c1c1e;
  font-family: var(--trendy-font, sans-serif);
}
.sheet-tip {
  font-size: 13px;
  color: #8e8e93;
  margin-top: 2px;
}
.sheet-tip.login-tip {
  color: #667eea;
  font-weight: 600;
  animation: tipPulse 2s ease-in-out infinite;
}
@keyframes tipPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
.sheet-login-cta {
  flex-shrink: 0;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  border-radius: 20px;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
  animation: ctaBounce 2s ease-in-out infinite;
}
@keyframes ctaBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-2px); }
}
.sheet-avatar { flex-shrink: 0; }
.sheet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.sheet-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 4px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;
}
.sheet-item:active {
  background: rgba(102, 187, 106, 0.1);
  transform: scale(0.95);
}
.sheet-item.danger:active {
  background: rgba(255, 59, 48, 0.08);
}
.sheet-icon { font-size: 24px; }
.sheet-label {
  font-size: 12px;
  color: #3a3a3c;
  font-weight: 500;
}
.sheet-item.danger .sheet-label { color: #ff3b30; }

/* 动画 */
.sheet-enter-active, .sheet-leave-active {
  transition: opacity 0.3s ease;
}
.sheet-enter-active .sheet-panel, .sheet-leave-active .sheet-panel {
  transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1);
}
.sheet-enter-from, .sheet-leave-to { opacity: 0; }
.sheet-enter-from .sheet-panel, .sheet-leave-to .sheet-panel {
  transform: translateY(100%);
}
</style>
