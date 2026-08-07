<template>
  <div class="public-layout">
    <!-- 全局装饰：柔和聚光灯 + 飘落花瓣 + 路过的小狗（移动端隐藏以省性能） -->
    <template v-if="!isLanding && !appStore.mobile">
      <Spotlight />
      <FloatPetals type="leaf" :count="10" />
      <WalkingDog />
    </template>

    <transition name="el-fade-in-linear">
      <div
        v-show="appStore.toolbar.visible && !isLanding"
        :class="{ enter: appStore.toolbar.enter }"
        class="toolbar-content myBetween"
      >
        <div class="toolbar-title">
          <h2 @click="goHome" class="site-title-link">{{ appStore.webInfo.webName || 'Lune' }}</h2>
        </div>

        <!-- 移动端音乐条：原来吊在屏幕底部压着 TabBar，挪到站名右边这片留白里。
             放在常驻布局里还有个好处 —— 跨页切换不会重挂载，音乐不断。 -->
        <MusicPlayer v-if="appStore.mobile" variant="bar" class="toolbar-music" />

        <!-- PC端导航菜单 -->
        <div v-if="!appStore.mobile">
          <ul class="scroll-menu">
            <li
              v-for="item in navItems"
              :key="item.path"
              @click="$router.push({ path: item.path })"
              :class="{ active: route.path === item.path }"
              :aria-label="'前往' + item.label"
              role="menuitem"
            >
              <div class="my-menu"><LineIcon :name="item.icon" :size="16" /><span>{{ item.label }}</span></div>
            </li>
            <li v-if="userStore.isAdmin" @click="goAdmin()">
              <div class="my-menu"><LineIcon name="grid" :size="16" /><span>后台</span></div>
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
                    <div class="drop-item" @click="showProfile = true; closeDropdown()"><LineIcon name="user" :size="15" />个人信息</div>
                    <div class="drop-item" @click="handleLogout()"><LineIcon name="arrow-right" :size="15" />退出</div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="login-btn-nav" @click="showLogin = true">
                  <LineIcon name="user" :size="15" />
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

    <!-- 非 Landing 页：可爱的"到底啦"提示，无版权页脚 -->
    <div v-if="!isLanding" class="cute-footer">
      <span class="cute-footer-line"></span>
      <span class="cute-footer-text">{{ cuteFooterText }}</span>
      <span class="cute-footer-line"></span>
    </div>

    <div class="toolButton">
      <div v-if="showBackTop" class="backTop" @click="scrollToTop()" role="button" aria-label="回到顶部">
        <svg viewBox="0 0 1024 1024" width="50" height="50">
          <path d="M696.741825 447.714002c2.717387-214.485615-173.757803-312.227566-187.33574-320.371729-10.857551 5.430775-190.050127 103.168727-187.33274 320.371729-35.297037 24.435488-73.306463 65.1623-67.875688 135.752376 5.430775 70.589076 76.018851 119.460051 103.168726 116.745664 27.152875-2.716387 19.004713-21.7221 19.004713-21.7221l8.148162-38.011425s40.721814 59.732525 51.583363 59.732525h146.609927c13.574938 0 51.585363-59.732525 51.585363-59.732525l8.147162 38.011425s-8.147162 19.005713 19.004713 21.7221c27.148876 2.714388 97.738951-46.156588 103.168727-116.745664s-32.57965-111.316888-67.876688-135.752376z" fill="var(--fontColor)"/>
          <path d="M423.602441 746.060699c6.47054-6.297579 12.823107-7.017417 21.629121-2.784372 34.520213 16.582259 70.232157 19.645568 107.031855 9.116944 8.118169-2.323476 15.974396-5.475765 23.598677-9.22392 13.712907-6.73648 26.003134 0.8878 26.080116 16.13936 0.109975 22.574907-0.024994 45.142816 0.080982 67.709725 0.031993 7.464316-2.277486 13.322995-9.44387 16.608254-7.277358 3.333248-13.765895 1.961558-19.526595-3.264264-3.653176-3.313253-7.063407-6.897444-10.634601-10.304675-6.563519-6.259588-6.676494-6.25259-10.625603 1.603638-8.437097 16.80121-16.821205 33.623415-25.257302 50.423625-2.489438 4.953882-5.706713 9.196925-11.411426 10.775569-8.355115 2.315478-15.772442-1.070758-20.272427-9.867774-8.774021-17.15313-17.269104-34.453228-25.918153-51.669344-3.750154-7.469315-3.9891-7.479313-10.141712-1.514658-3.715162 3.602187-7.31435 7.326347-11.142486 10.800563-5.571743 5.060858-11.934308 6.269586-18.936728 3.207277-6.82746-2.984327-9.869774-8.483086-9.892769-15.685462-0.070984-23.506697-0.041991-47.018393-0.020995-70.532089 0.007998-4.679944 1.46467-8.785018 4.803916-11.538397z" fill="var(--fontColor)"/>
        </svg>
      </div>
    </div>

    <!-- 移动端底部 TabBar -->
    <MobileTabBar v-if="appStore.mobile" />

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
import MobileTabBar from '../components/MobileTabBar.vue'
import MusicPlayer from '../components/MusicPlayer.vue'
import LineIcon from '../components/LineIcon.vue'
import Spotlight from '../components/effects/Spotlight.vue'
import FloatPetals from '../components/effects/FloatPetals.vue'
import WalkingDog from '../components/effects/WalkingDog.vue'
import { setLoginCardTrigger } from '../composables/useAuth'

const router = useRouter()
const route = useRoute()

const isLanding = computed(() => route.name === 'Landing')

// 前台栏目名走三字雅称（后台菜单保留功能名）；简历原来只能从 Landing 进，
// 现在 Landing 只留一道门，所以「山海志」必须在这里有入口。
const navItems = [
  { path: '/home', label: '云栖阁', icon: 'home' },
  { path: '/family', label: '长相守', icon: 'heart' },
  { path: '/treehole', label: '风语林', icon: 'leaf' },
  { path: '/essay', label: '浮生记', icon: 'brush' },
  { path: '/record', label: '光阴集', icon: 'book' },
  { path: '/wish', label: '星愿池', icon: 'star' },
  { path: '/resume', label: '山海志', icon: 'route' }
]

// 可爱的"到底啦"提示（随机一条）
const cuteTexts = ['～ 到底啦，去别处逛逛吧 ～', '🌸 被你发现啦，这里是最底部 🌸', '～ 到底啦，喝口水休息下 ～', '🍃 到底啦，风把秘密都吹走啦 🍃']
const cuteFooterText = cuteTexts[Math.floor(Math.random() * cuteTexts.length)]

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

function onLoggedIn() {}

async function handleLogout() {
  await userStore.logout()
  router.push({ path: '/' })
}

onMounted(() => {
  appStore.initDarkMode()
  appStore.fetchConfig()
  appStore.pingVisit()
  window.addEventListener('scroll', onScrollPage)
  document.addEventListener('click', handleClickOutside)
  // 移动端断点（768px）改由 store 的 matchMedia 统一维护，见 App.vue
  appStore.initViewport()
  appStore.changeToolbarStatus({ enter: false, visible: true })
  setLoginCardTrigger(() => { showLogin.value = true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScrollPage)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.public-layout {
  min-height: 100vh; display: flex; flex-direction: column;
}
.toolbar-content {
  width: 100%; height: 60px; color: #fff;
  position: fixed; z-index: 100; user-select: none;
  transition: all 0.35s ease-in-out;
}
/* 顶部（透明）：文字深色描边阴影，确保白/亮背景下可读 */
.toolbar-content:not(.enter) .scroll-menu li .my-menu span {
  text-shadow: 0 1px 6px rgba(0,0,0,0.55), 0 0 2px rgba(0,0,0,0.4);
}
/* 滚动后：毛玻璃白底，文字深色 */
.toolbar-content.enter {
  background: var(--toolbarBackground);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  color: var(--toolbarFont);
  box-shadow: 0 2px 16px rgba(67,160,71,0.1);
}
.toolbar-title { margin-left: 30px; cursor: pointer; }
.toolbar-title h2 {
  font-size: 22px; white-space: nowrap; cursor: pointer;
  font-family: var(--calligraphy-font);
  letter-spacing: 3px; color: var(--nature-green);
  transition: all 0.3s ease;
  text-shadow: 0 1px 6px rgba(0,0,0,0.25);
}
.toolbar-content.enter .toolbar-title h2 { text-shadow: none; }
.toolbar-title h2:hover {
  transform: scale(1.12);
  text-shadow: 0 0 14px rgba(102,187,106,0.7), 0 0 28px rgba(102,187,106,0.4);
  filter: brightness(1.15);
}
.site-clicked { animation: sitePop 0.6s ease; }
@keyframes sitePop {
  0% { transform: scale(1); }
  30% { transform: scale(1.25); color: #ff4757; }
  100% { transform: scale(1); }
}

.scroll-menu {
  margin: 0 25px 0 0; display: flex; justify-content: flex-end; padding: 0; gap: 2px;
}
.scroll-menu li {
  list-style: none; margin: 0 7px; font-size: 17px;
  height: 60px; line-height: 60px; position: relative;
  cursor: pointer; display: flex; flex-direction: column; align-items: center;
}
.scroll-menu li:hover .my-menu span { color: var(--nature-green-light); }
.toolbar-content.enter .scroll-menu li:hover .my-menu span { color: var(--nature-green); }
.scroll-menu li .my-menu {
  height: 52px; font-weight: 600;
  display: flex; align-items: center; gap: 5px;
}
/* 三字雅称用书法体，图标跟着文字颜色走 */
.scroll-menu li .my-menu span {
  font-family: var(--calligraphy-font); letter-spacing: 2px; white-space: nowrap;
}
.scroll-menu li .my-menu :deep(.line-icon) { opacity: 0.75; transition: opacity 0.25s; }
.scroll-menu li:hover .my-menu :deep(.line-icon),
.scroll-menu li.active .my-menu :deep(.line-icon) { opacity: 1; }
.scroll-menu li.active .my-menu :deep(.line-icon) { color: var(--nature-green); }
.scroll-menu li.active .my-menu span {
  color: var(--nature-green); font-weight: 700;
  text-shadow: 0 0 12px rgba(102,187,106,0.5);
}
.scroll-menu li .my-menu:after {
  content: ''; display: block; position: absolute; bottom: 0; height: 5px;
  background: var(--nature-gradient); width: 100%; border-radius: 4px;
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

/* 加了「山海志」后 PC 端一共 8 项，窄屏笔记本（769~1050）收紧一点免得挤到站名 */
@media screen and (min-width: 769px) and (max-width: 1050px) {
  .scroll-menu { margin-right: 12px; }
  .scroll-menu li { margin: 0 3px; font-size: 15px; }
  .scroll-menu li .my-menu { gap: 3px; }
  .scroll-menu li .my-menu span { letter-spacing: 1px; }
  .login-btn-nav { padding: 0 12px; margin-left: 4px; }
}

/* 移动端音乐条：站名右边那片留白 */
.toolbar-music { flex: 1; min-width: 0; margin-left: 12px; }

/* 移动端：主内容区底部预留 TabBar 空间 */
@media screen and (max-width: 768px) {
  .main-container {
    /* TabBar 是悬浮胶囊（54px 高 + 底部 10px 间距），再留一点呼吸位 */
    padding-bottom: calc(78px + env(safe-area-inset-bottom, 0px));
  }
  .toolbar-content {
    height: 50px;
  }
  .toolbar-title { margin-left: 16px; }
  .toolbar-title h2 { font-size: 19px; letter-spacing: 2px; }
  /* 移动端 toolbar 始终毛玻璃化（因为顶部没有菜单需要展示了） */
  .toolbar-content:not(.enter) {
    background: var(--toolbarBackground);
    backdrop-filter: blur(18px);
    -webkit-backdrop-filter: blur(18px);
    color: var(--toolbarFont);
  }
  /* 隐藏回到顶部按钮（TabBar已占用右下角） */
  .toolButton { display: none; }
  .cute-footer { padding: 24px 16px 18px; }
}

/* 可爱的"到底啦"提示 */
.cute-footer {
  display: flex; align-items: center; justify-content: center; gap: 18px;
  padding: 40px 20px 30px; user-select: none;
}
.cute-footer-line {
  flex: 0 0 80px; height: 2px; border-radius: 2px;
  background: linear-gradient(90deg, transparent, var(--nature-green-light), transparent);
}
.cute-footer-text {
  font-family: var(--handwriting-font); font-size: 17px; color: var(--nature-green);
  letter-spacing: 2px; white-space: nowrap;
  animation: cuteBounce 3s ease-in-out infinite;
}
@keyframes cuteBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.toolButton {
  position: fixed; right: 3vh; bottom: 3vh;
  animation: slide-bottom 0.5s ease-in-out both; z-index: 99;
  cursor: pointer; font-size: 25px; width: 30px;
}
.backTop { transition: all 0.3s ease-in; position: relative; top: 0; left: -13px; margin-bottom: 2px; }
.backTop:hover { top: -10px; }

@media screen and (max-width: 400px) { .toolButton { right: 0.5vh; } }
</style>
