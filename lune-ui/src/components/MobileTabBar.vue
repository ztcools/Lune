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
      <LineIcon class="tabbar-icon" :name="tab.icon" :size="20" />
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
      <LineIcon class="tabbar-icon" name="user" :size="20" />
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
                  {{ userStore.isLoggedIn ? '欢迎回来～' : '点击登录 / 注册' }}
                </div>
              </div>
              <div v-if="!userStore.isLoggedIn" class="sheet-login-cta">
                立即登录
                <LineIcon name="arrow-right" :size="13" />
              </div>
            </div>
            <div class="sheet-grid">
              <div class="sheet-item" @click="goPage('/record')">
                <LineIcon class="sheet-icon" name="book" :size="22" />
                <span class="sheet-label">光阴集</span>
              </div>
              <div class="sheet-item" @click="goPage('/wish')">
                <LineIcon class="sheet-icon" name="star" :size="22" />
                <span class="sheet-label">星愿池</span>
              </div>
              <div class="sheet-item" @click="goPage('/resume')">
                <LineIcon class="sheet-icon" name="route" :size="22" />
                <span class="sheet-label">山海志</span>
              </div>
              <div v-if="userStore.isAdmin" class="sheet-item" @click="goAdmin">
                <LineIcon class="sheet-icon" name="grid" :size="22" />
                <span class="sheet-label">后台</span>
              </div>
              <div v-if="userStore.isLoggedIn" class="sheet-item" @click="showProfile = true; showUserSheet = false">
                <LineIcon class="sheet-icon" name="sliders" :size="22" />
                <span class="sheet-label">设置</span>
              </div>
              <div v-if="!userStore.isLoggedIn" class="sheet-item" @click="onLogin">
                <LineIcon class="sheet-icon" name="user" :size="22" />
                <span class="sheet-label">登录</span>
              </div>
              <div v-else class="sheet-item danger" @click="onLogout">
                <LineIcon class="sheet-icon" name="log-out" :size="22" />
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
import LineIcon from './LineIcon.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const showUserSheet = ref(false)
const showLogin = ref(false)
const showProfile = ref(false)
const visible = ref(true)

const isLanding = computed(() => route.name === 'Landing')

const mainTabs = [
  { path: '/home', icon: 'home', label: '云栖阁' },
  { path: '/family', icon: 'heart', label: '长相守' },
  { path: '/treehole', icon: 'leaf', label: '风语林' },
  { path: '/essay', icon: 'brush', label: '浮生记' }
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
/* 悬浮胶囊：不再是贴底的整条白板，左右留白 + 大圆角 */
.mobile-tabbar {
  position: fixed;
  left: 12px; right: 12px;
  bottom: calc(10px + env(safe-area-inset-bottom, 0px));
  height: 54px;
  display: flex;
  justify-content: space-around;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 22px;
  box-shadow: 0 6px 26px rgba(20, 40, 28, 0.1), 0 1px 3px rgba(20, 40, 28, 0.06);
  z-index: 1000;
  transition: transform 0.32s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.28s;
  user-select: none;
  -webkit-user-select: none;
  -webkit-tap-highlight-color: transparent;
  overflow: hidden;
}
.mobile-tabbar.hidden {
  transform: translateY(calc(100% + 14px));
  opacity: 0;
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  position: relative;
  color: #9a9a9e;
  transition: color 0.25s;
}
.tabbar-item.active { color: var(--nature-green-dark, #2e7d32); }
.tabbar-item:active { opacity: 0.65; }

.tabbar-icon {
  display: block;
  transition: transform 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.tabbar-item.active .tabbar-icon {
  transform: translateY(-2px);
}

.tabbar-label {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 1px;
  line-height: 1;
  font-family: var(--calligraphy-font, sans-serif);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.tabbar-item.active .tabbar-label {
  transform: translateY(-2px);
}

.tabbar-dot {
  position: absolute;
  bottom: 5px;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0;
  transform: scale(0);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.tabbar-item.active .tabbar-dot {
  opacity: 1;
  transform: scale(1);
}

@media (prefers-reduced-motion: reduce) {
  .tabbar-icon, .tabbar-label, .tabbar-dot, .mobile-tabbar { transition: none; }
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
  background: linear-gradient(135deg, rgba(47, 111, 106, 0.08), rgba(63, 79, 143, 0.08));
  border: 1.5px dashed rgba(47, 111, 106, 0.3);
  margin: -4px -4px 12px;
}
.sheet-header.clickable:active {
  background: linear-gradient(135deg, rgba(47, 111, 106, 0.15), rgba(63, 79, 143, 0.15));
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
  color: #2f6f6a;
  font-weight: 600;
  animation: tipPulse 2s ease-in-out infinite;
}
@keyframes tipPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
.sheet-login-cta {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #2f6f6a, #3f4f8f);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  border-radius: 20px;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px rgba(47, 111, 106, 0.35);
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
.sheet-icon { color: var(--nature-green-dark, #2e7d32); }
.sheet-label {
  font-size: 12px;
  color: #3a3a3c;
  font-weight: 500;
  letter-spacing: 0.5px;
  font-family: var(--calligraphy-font, sans-serif);
}
.sheet-item.danger .sheet-icon,
.sheet-item.danger .sheet-label { color: #e5544b; }

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

@media (prefers-reduced-motion: reduce) {
  .sheet-tip.login-tip, .sheet-login-cta { animation: none; }
  .sheet-enter-active, .sheet-leave-active,
  .sheet-enter-active .sheet-panel, .sheet-leave-active .sheet-panel { transition: none; }
}
</style>
