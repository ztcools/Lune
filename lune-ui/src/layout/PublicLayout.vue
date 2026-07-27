<template>
  <div class="public-layout">
    <transition name="el-fade-in-linear">
      <div
        v-show="appStore.toolbar.visible && !isLanding"
        :class="{ enter: appStore.toolbar.enter }"
        class="toolbar-content myBetween"
      >
        <div class="toolbar-title">
          <h2 @click="goHome" class="site-title-link">{{ appStore.webInfo.webName || 'Lune' }}</h2>
        </div>

        <div v-if="appStore.mobile" class="toolbar-mobile-menu"
          :class="{ enter: appStore.toolbar.enter }"
          @click="toolbarDrawer = !toolbarDrawer">
          <i class="el-icon-s-operation"></i>
        </div>

        <div v-else>
          <ul class="scroll-menu">
            <li @click="$router.push({ path: '/home' })" :class="{ active: route.path === '/home' }">
              <div class="my-menu">🏠 <span>首页</span></div>
            </li>
            <li @click="$router.push({ path: '/family' })" :class="{ active: route.path === '/family' }">
              <div class="my-menu">❤️ <span>家</span></div>
            </li>
            <li @click="$router.push({ path: '/treehole' })" :class="{ active: route.path === '/treehole' }">
              <div class="my-menu">🌳 <span>树洞</span></div>
            </li>
            <li @click="$router.push({ path: '/essay' })" :class="{ active: route.path === '/essay' }">
              <div class="my-menu">🏖️ <span>随笔</span></div>
            </li>
            <li @click="$router.push({ path: '/record' })" :class="{ active: route.path === '/record' }">
              <div class="my-menu">📒 <span>记录</span></div>
            </li>
            <li v-if="userStore.isAdmin" @click="goAdmin()">
              <div class="my-menu">💻️ <span>后台</span></div>
            </li>

            <li class="avatar-menu-item">
              <template v-if="userStore.isLoggedIn">
                <div class="avatar-hover-zone" ref="avatarZoneRef">
                  <el-avatar class="user-avatar" :size="36" style="margin-top:12px"
                    :src="userStore.user?.avatar"
                    @click.stop="toggleDropdown">
                    {{ (userStore.nickname || '?').charAt(0) }}
                  </el-avatar>
                  <div class="avatar-menu-drop" :class="{ pinned: dropdownPinned, show: dropdownHover }"
                    @mouseenter="dropdownHover = true" @mouseleave="dropdownHover = false">
                    <div class="drop-item" @click="showProfile = true; closeDropdown()">👤 个人信息</div>
                    <div class="drop-item" @click="handleLogout()">🚪 退出</div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="login-btn-nav" @click="showLogin = true">
                  <span class="login-btn-icon">👤</span>
                  <span>登录</span>
                </div>
              </template>
            </li>
          </ul>
        </div>
      </div>
    </transition>

    <main class="main-container">
      <router-view />
    </main>

    <footer v-if="!isLanding" class="site-footer">
      <div class="footer-inner">
        <p>{{ appStore.webInfo.footer || '© 2024 Lune. All Rights Reserved.' }}</p>
      </div>
    </footer>

    <div class="toolButton">
      <div v-if="showBackTop" class="backTop" @click="scrollToTop()">
        <svg viewBox="0 0 1024 1024" width="50" height="50">
          <path d="M696.741825 447.714002c2.717387-214.485615-173.757803-312.227566-187.33574-320.371729-10.857551 5.430775-190.050127 103.168727-187.33274 320.371729-35.297037 24.435488-73.306463 65.1623-67.875688 135.752376 5.430775 70.589076 76.018851 119.460051 103.168726 116.745664 27.152875-2.716387 19.004713-21.7221 19.004713-21.7221l8.148162-38.011425s40.721814 59.732525 51.583363 59.732525h146.609927c13.574938 0 51.585363-59.732525 51.585363-59.732525l8.147162 38.011425s-8.147162 19.005713 19.004713 21.7221c27.148876 2.714388 97.738951-46.156588 103.168727-116.745664s-32.57965-111.316888-67.876688-135.752376z" fill="var(--fontColor)"/>
          <path d="M423.602441 746.060699c6.47054-6.297579 12.823107-7.017417 21.629121-2.784372 34.520213 16.582259 70.232157 19.645568 107.031855 9.116944 8.118169-2.323476 15.974396-5.475765 23.598677-9.22392 13.712907-6.73648 26.003134 0.8878 26.080116 16.13936 0.109975 22.574907-0.024994 45.142816 0.080982 67.709725 0.031993 7.464316-2.277486 13.322995-9.44387 16.608254-7.277358 3.333248-13.765895 1.961558-19.526595-3.264264-3.653176-3.313253-7.063407-6.897444-10.634601-10.304675-6.563519-6.259588-6.676494-6.25259-10.625603 1.603638-8.437097 16.80121-16.821205 33.623415-25.257302 50.423625-2.489438 4.953882-5.706713 9.196925-11.411426 10.775569-8.355115 2.315478-15.772442-1.070758-20.272427-9.867774-8.774021-17.15313-17.269104-34.453228-25.918153-51.669344-3.750154-7.469315-3.9891-7.479313-10.141712-1.514658-3.715162 3.602187-7.31435 7.326347-11.142486 10.800563-5.571743 5.060858-11.934308 6.269586-18.936728 3.207277-6.82746-2.984327-9.869774-8.483086-9.892769-15.685462-0.070984-23.506697-0.041991-47.018393-0.020995-70.532089 0.007998-4.679944 1.46467-8.785018 4.803916-11.538397z" fill="var(--fontColor)"/>
        </svg>
      </div>
    </div>

    <el-drawer v-model="toolbarDrawer" :show-close="false" size="65%" custom-class="toolbarDrawer" title="欢迎光临" direction="ltr">
      <div>
        <ul class="small-menu">
          <li @click="smallMenu('/home')"><div>🏠 <span>首页</span></div></li>
          <li @click="smallMenu('/family')"><div>❤️ <span>家</span></div></li>
          <li @click="smallMenu('/treehole')"><div>🌳 <span>树洞</span></div></li>
          <li @click="smallMenu('/essay')"><div>🏖️ <span>随笔</span></div></li>
          <li @click="smallMenu('/record')"><div>📒 <span>记录</span></div></li>
          <li v-if="userStore.isAdmin" @click="goAdmin()"><div>💻️ <span>后台</span></div></li>
          <template v-if="!userStore.isLoggedIn">
            <li @click="smallMenuLogin()"><div><i class="fa fa-sign-in" aria-hidden="true"></i><span>&nbsp;登录</span></div></li>
          </template>
          <template v-else>
            <li @click="showProfile = true; toolbarDrawer = false"><div><i class="fa fa-user-circle" aria-hidden="true"></i><span>&nbsp;个人信息</span></div></li>
            <li @click="smallMenuLogout()"><div><i class="fa fa-sign-out" aria-hidden="true"></i><span>&nbsp;退出</span></div></li>
          </template>
        </ul>
      </div>
    </el-drawer>

    <LoginCard :visible="showLogin" @close="showLogin = false" @logged-in="onLoggedIn" />
    <ProfileCard :visible="showProfile" @close="showProfile = false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'
import LoginCard from '../components/LoginCard.vue'
import ProfileCard from '../components/ProfileCard.vue'
import { setLoginCardTrigger } from '../composables/useAuth'

const router = useRouter()
const route = useRoute()

const isLanding = computed(() => route.name === 'Landing')

function goHome() {
  if (route.path === '/') return
  const el = document.querySelector('.toolbar-title h2')
  el?.classList.add('site-clicked')
  setTimeout(() => el?.classList.remove('site-clicked'), 600)
  router.push('/')
}

const appStore = useAppStore()
const userStore = useUserStore()

const showLogin = ref(false)
const showProfile = ref(false)
const showBackTop = ref(false)
const toolbarDrawer = ref(false)
const dropdownPinned = ref(false)
const dropdownHover = ref(false)

function toggleDropdown() { dropdownPinned.value = !dropdownPinned.value }
function closeDropdown() { dropdownPinned.value = false; dropdownHover.value = false }
function handleClickOutside(e) {
  if (dropdownPinned.value && !e.target.closest('.avatar-hover-zone')) {
    dropdownPinned.value = false
  }
}

provide('showLoginCard', () => { showLogin.value = true })

let scrollTop = 0
let oldScrollTop = 0

function onScrollPage() {
  oldScrollTop = scrollTop
  scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const enter = scrollTop > window.innerHeight / 2
  const isShow = scrollTop - window.innerHeight > 30
  showBackTop.value = isShow
  appStore.changeToolbarStatus({ enter: enter, visible: true })
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goAdmin() {
  window.open(router.resolve('/admin').href)
}

function smallMenu(path) {
  router.push(path)
  toolbarDrawer.value = false
}

function smallMenuLogin() {
  showLogin.value = true
  toolbarDrawer.value = false
}

function smallMenuLogout() {
  handleLogout()
  toolbarDrawer.value = false
}

function onLoggedIn() {}

async function handleLogout() {
  await userStore.logout()
  router.push({ path: '/' })
}

const checkMobile = () => { appStore.mobile = document.body.clientWidth < 1100 }

onMounted(() => {
  appStore.initDarkMode()
  appStore.fetchConfig()
  window.addEventListener('scroll', onScrollPage)
  document.addEventListener('click', handleClickOutside)
  checkMobile()
  window.addEventListener('resize', checkMobile)
  appStore.changeToolbarStatus({ enter: false, visible: true })
  setLoginCardTrigger(() => { showLogin.value = true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScrollPage)
  window.removeEventListener('resize', checkMobile)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.public-layout {
  min-height: 100vh; display: flex; flex-direction: column;
}
.toolbar-content {
  width: 100%; height: 60px; color: var(--white);
  position: fixed; z-index: 100; user-select: none;
  transition: all 0.3s ease-in-out;
}
.toolbar-content.enter {
  background: var(--toolbarBackground); color: var(--toolbarFont);
  box-shadow: 0 1px 3px 0 rgba(0,34,77,0.05);
}
.toolbar-title { margin-left: 30px; cursor: pointer; }
.toolbar-title h2 {
  font-size: 22px; white-space: nowrap; cursor: pointer;
  font-family: 'ZCOOL XiaoWei', cursive;
  letter-spacing: 3px; color: var(--themeBackground);
  transition: all 0.3s ease;
}
.toolbar-title h2:hover {
  transform: scale(1.12);
  text-shadow: 0 0 12px rgba(255,165,0,0.5), 0 0 24px rgba(255,165,0,0.3);
  filter: brightness(1.2);
}
.site-clicked { animation: sitePop 0.6s ease; }
@keyframes sitePop {
  0% { transform: scale(1); }
  30% { transform: scale(1.25); color: #ff4757; }
  100% { transform: scale(1); }
}
.toolbar-mobile-menu { font-size: 30px; margin-right: 15px; cursor: pointer; }

.scroll-menu {
  margin: 0 25px 0 0; display: flex; justify-content: flex-end; padding: 0; gap: 6px;
}
.scroll-menu li {
  list-style: none; margin: 0 10px; font-size: 17px;
  height: 60px; line-height: 60px; position: relative;
  cursor: pointer; display: flex; flex-direction: column; align-items: center;
}
.scroll-menu li:hover .my-menu span { color: var(--themeBackground); }
.scroll-menu li .my-menu { height: 52px; line-height: 52px; }
.scroll-menu li.active .my-menu span {
  color: var(--themeBackground); font-weight: 700;
  text-shadow: 0 0 12px rgba(255,165,0,0.6), 0 0 24px rgba(255,165,0,0.3);
}
.scroll-menu li .my-menu:after {
  content: ''; display: block; position: absolute; bottom: 0; height: 6px;
  background-color: var(--themeBackground); width: 100%;
  max-width: 0; transition: max-width 0.25s ease-in-out;
}
.scroll-menu li:hover .my-menu:after { max-width: 100%; }

.avatar-menu-item { position: relative; }
.avatar-hover-zone { position: relative; }

.avatar-menu-drop {
  position: absolute; top: 62px; right: 50%; transform: translateX(50%);
  background: rgba(255,255,255,0.95); backdrop-filter: blur(16px);
  border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  border: 1px solid rgba(0,0,0,0.06); overflow: hidden; z-index: 20;
  min-width: 150px;
  opacity: 0; pointer-events: none;
  transition: all 0.25s ease;
  transform-origin: top center;
}
.avatar-menu-drop.show, .avatar-menu-drop.pinned {
  opacity: 1; pointer-events: auto;
}
.drop-item {
  padding: 11px 20px; font-size: 14px; font-weight: 500;
  color: #333; cursor: pointer;
  font-family: var(--trendy-font);
  display: flex; align-items: center; gap: 8px;
  transition: all 0.2s;
}
.drop-item:hover { background: #e8f5e9; color: #2e7d32; padding-left: 24px; }

.login-btn-nav {
  display: flex; align-items: center; gap: 6px;
  height: 60px; line-height: 60px; cursor: pointer;
  font-family: var(--trendy-font);
  font-size: 14px; font-weight: 600;
  color: var(--nature-green); padding: 0 16px;
  border-radius: 14px; margin-left: 8px;
  transition: all 0.3s ease;
  background: rgba(76,175,80,0.08);
}
.login-btn-nav:hover {
  background: var(--nature-gradient);
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(76,175,80,0.3);
}
.login-btn-icon { font-size: 16px; }

.user-avatar { cursor: pointer; transition: all 0.3s; }
.user-avatar:hover { transform: rotate(360deg); }

.el-dropdown { font-size: unset; color: unset; }
.el-popper[x-placement^='bottom'] { margin-top: -8px; }
.el-dropdown-menu { padding: 5px 0; }
.el-dropdown-menu__item:hover { background-color: var(--white); color: var(--themeBackground); }

.main-container { flex: 1; }

.site-footer {
  background: var(--gradientBG); background-size: 400% 400%;
  animation: gradientBG 10s ease infinite;
  padding: 20px; text-align: center; color: var(--white); font-size: 14px;
}
.footer-inner { max-width: 1200px; margin: 0 auto; }

.toolButton {
  position: fixed; right: 3vh; bottom: 3vh;
  animation: slide-bottom 0.5s ease-in-out both; z-index: 99;
  cursor: pointer; font-size: 25px; width: 30px;
}
.backTop { transition: all 0.3s ease-in; position: relative; top: 0; left: -13px; margin-bottom: 2px; }
.backTop:hover { top: -10px; }

.small-menu li { padding: 10px 0; cursor: pointer; font-size: 17px; }
.small-menu li:hover { color: var(--themeBackground); }

@media screen and (max-width: 400px) { .toolButton { right: 0.5vh; } }
</style>
