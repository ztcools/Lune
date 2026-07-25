<template>
  <div class="public-layout">
    <!-- el过渡动画 -->
    <transition name="el-fade-in-linear">
      <!-- 导航栏 -->
      <div
        v-show="appStore.toolbar.visible"
        :class="{ enter: appStore.toolbar.enter }"
        class="toolbar-content myBetween"
      >
        <!-- 网站名称 -->
        <div class="toolbar-title">
          <h2 @click="goHome" class="site-title-link">{{ appStore.webInfo.webName || 'Lune' }}</h2>
        </div>

        <!-- 手机导航按钮（窄屏） -->
        <div
          v-if="appStore.mobile"
          class="toolbar-mobile-menu"
          :class="{ enter: appStore.toolbar.enter }"
          @click="toolbarDrawer = !toolbarDrawer"
        >
          <i class="el-icon-s-operation"></i>
        </div>

        <!-- 导航列表 -->
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

            <!-- 个人中心 / 用户头像 -->
            <li>
              <el-dropdown placement="bottom">
                <el-avatar
                  class="user-avatar"
                  :size="36"
                  style="margin-top: 12px"
                  :src="userStore.user?.avatar || appStore.webInfo.avatar"
                />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="userStore.isLoggedIn" @click="$router.push({ path: '/user' })">
                      <i class="fa fa-user-circle" aria-hidden="true"></i> <span>个人中心</span>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="userStore.isLoggedIn" @click="handleLogout()">
                      <i class="fa fa-sign-out" aria-hidden="true"></i> <span>退出</span>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="!userStore.isLoggedIn" @click="showLogin = true">
                      <i class="fa fa-sign-in" aria-hidden="true"></i> <span>登陆</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </li>
          </ul>
        </div>
      </div>
    </transition>

    <!-- 主内容区 -->
    <main class="main-container">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="footer-inner">
        <p>{{ appStore.webInfo.footer || '© 2024 Lune. All Rights Reserved.' }}</p>
      </div>
    </footer>

    <!-- 浮动工具按钮 -->
    <div class="toolButton">
      <!-- 回到顶部按钮 -->
      <div v-if="showBackTop" class="backTop" @click="scrollToTop()">
        <svg viewBox="0 0 1024 1024" width="50" height="50">
          <path
            d="M696.741825 447.714002c2.717387-214.485615-173.757803-312.227566-187.33574-320.371729-10.857551 5.430775-190.050127 103.168727-187.33274 320.371729-35.297037 24.435488-73.306463 65.1623-67.875688 135.752376 5.430775 70.589076 76.018851 119.460051 103.168726 116.745664 27.152875-2.716387 19.004713-21.7221 19.004713-21.7221l8.148162-38.011425s40.721814 59.732525 51.583363 59.732525h146.609927c13.574938 0 51.585363-59.732525 51.585363-59.732525l8.147162 38.011425s-8.147162 19.005713 19.004713 21.7221c27.148876 2.714388 97.738951-46.156588 103.168727-116.745664s-32.57965-111.316888-67.876688-135.752376z m-187.33574-2.713388c-5.426776 0-70.589076-2.717387-78.733239-78.737238 2.713388-73.306463 73.306463-78.733239 78.733239-81.450626 5.430775 0 76.02385 8.144163 78.736238 81.450626-8.143163 76.019851-73.305463 78.737238-78.736238 78.737238z m0 0"
            fill="var(--fontColor)"></path>
          <path
            d="M423.602441 746.060699c6.47054-6.297579 12.823107-7.017417 21.629121-2.784372 34.520213 16.582259 70.232157 19.645568 107.031855 9.116944 8.118169-2.323476 15.974396-5.475765 23.598677-9.22392 13.712907-6.73648 26.003134 0.8878 26.080116 16.13936 0.109975 22.574907-0.024994 45.142816 0.080982 67.709725 0.031993 7.464316-2.277486 13.322995-9.44387 16.608254-7.277358 3.333248-13.765895 1.961558-19.526595-3.264264-3.653176-3.313253-7.063407-6.897444-10.634601-10.304675-6.563519-6.259588-6.676494-6.25259-10.625603 1.603638-8.437097 16.80121-16.821205 33.623415-25.257302 50.423625-2.489438 4.953882-5.706713 9.196925-11.411426 10.775569-8.355115 2.315478-15.772442-1.070758-20.272427-9.867774-8.774021-17.15313-17.269104-34.453228-25.918153-51.669344-3.750154-7.469315-3.9891-7.479313-10.141712-1.514658-3.715162 3.602187-7.31435 7.326347-11.142486 10.800563-5.571743 5.060858-11.934308 6.269586-18.936728 3.207277-6.82746-2.984327-9.869774-8.483086-9.892769-15.685462-0.070984-23.506697-0.041991-47.018393-0.020995-70.532089 0.007998-4.679944 1.46467-8.785018 4.803916-11.538397z"
            fill="var(--fontColor)"></path>
        </svg>
      </div>

    </div>

    <!-- 手机端抽屉导航 -->
    <el-drawer
      v-model="toolbarDrawer"
      :show-close="false"
      size="65%"
      custom-class="toolbarDrawer"
      title="欢迎光临"
      direction="ltr"
    >
      <div>
        <ul class="small-menu">
          <li @click="smallMenu('/home')">
            <div>🏠 <span>首页</span></div>
          </li>
          <li @click="smallMenu('/family')">
            <div>❤️ <span>家</span></div>
          </li>
          <li @click="smallMenu('/treehole')">
            <div>🌳 <span>树洞</span></div>
          </li>
          <li @click="smallMenu('/essay')">
            <div>🏖️ <span>随笔</span></div>
          </li>
          <li @click="smallMenu('/record')">
            <div>📒 <span>记录</span></div>
          </li>
          <li v-if="userStore.isAdmin" @click="goAdmin()">
            <div>💻️ <span>后台</span></div>
          </li>
          <template v-if="!userStore.isLoggedIn">
            <li @click="smallMenuLogin()">
              <div>
                <i class="fa fa-sign-in" aria-hidden="true"></i>
                <span>&nbsp;登录</span>
              </div>
            </li>
          </template>
          <template v-else>
            <li @click="smallMenu('/user')">
              <div>
                <i class="fa fa-user-circle" aria-hidden="true"></i>
                <span>&nbsp;个人中心</span>
              </div>
            </li>
            <li @click="smallMenuLogout()">
              <div>
                <i class="fa fa-sign-out" aria-hidden="true"></i>
                <span>&nbsp;退出</span>
              </div>
            </li>
          </template>
        </ul>
      </div>
    </el-drawer>

    <!-- 登录弹窗 -->
    <el-dialog v-model="showLogin" title="欢迎登录 Lune" width="400px" :close-on-click-modal="false" center>
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLogin = false">取消</el-button>
        <el-button type="primary" @click="handleLogin">登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

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
const showBackTop = ref(false)
const toolbarDrawer = ref(false)
const loginForm = ref({ username: '', password: '' })

let scrollTop = 0
let oldScrollTop = 0

function onScrollPage() {
  oldScrollTop = scrollTop
  scrollTop = document.documentElement.scrollTop || document.body.scrollTop

  // 如果滑动距离超过屏幕高度一半视为进入页面，背景改为白色
  const enter = scrollTop > window.innerHeight / 2
  // 向上滚动时显示导航栏，向下滚动时隐藏
  const top = scrollTop - oldScrollTop < 0
  const isShow = scrollTop - window.innerHeight > 30
  showBackTop.value = isShow

  // 导航栏显示与颜色
  const toolbarStatus = {
    enter: enter,
    visible: top,
  }
  appStore.changeToolbarStatus(toolbarStatus)
}

function scrollToTop() {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  })
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

async function handleLogin() {
  try {
    await userStore.login(loginForm.value.username, loginForm.value.password)
    showLogin.value = false
    ElMessage.success('登录成功')
  } catch (e) {
    ElMessage.error('登录失败')
  }
}

async function handleLogout() {
  await userStore.logout()
  router.push({ path: '/' })
}

onMounted(async () => {
  appStore.initDarkMode()
  await appStore.fetchConfig()
  window.addEventListener('scroll', onScrollPage)

  // 检测窄屏
  const checkMobile = () => {
    appStore.mobile = document.body.clientWidth < 1100
  }
  checkMobile()
  window.addEventListener('resize', checkMobile)

  // 初始化工具栏状态
  appStore.changeToolbarStatus({ enter: false, visible: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScrollPage)
})
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ===== 导航栏 ===== */
.toolbar-content {
  width: 100%;
  height: 60px;
  color: var(--white);
  /* 固定位置，不随滚动条滚动 */
  position: fixed;
  z-index: 100;
  /* 禁止选中文字 */
  user-select: none;
  transition: all 0.3s ease-in-out;
}

.toolbar-content.enter {
  background: var(--toolbarBackground);
  color: var(--toolbarFont);
  box-shadow: 0 1px 3px 0 rgba(0, 34, 77, 0.05);
}

.toolbar-title {
  margin-left: 30px;
  cursor: pointer;
}
.toolbar-title h2 {
  font-size: 22px; white-space: nowrap; cursor: pointer;
  font-family: 'Ma Shan Zheng', 'Liu Jian Mao Cao', 'KaiTi', cursive;
  letter-spacing: 3px; color: var(--themeBackground);
  transition: all 0.3s ease;
  position: relative;
}
.toolbar-title h2:hover {
  transform: scale(1.12);
  text-shadow: 0 0 12px rgba(255,165,0,0.5), 0 0 24px rgba(255,165,0,0.3);
  filter: brightness(1.2);
}
.site-clicked {
  animation: sitePop 0.6s ease;
}
@keyframes sitePop {
  0% { transform: scale(1); }
  30% { transform: scale(1.25); color: #ff4757; }
  100% { transform: scale(1); }
}

.toolbar-mobile-menu {
  font-size: 30px;
  margin-right: 15px;
  cursor: pointer;
}

/* ===== 导航菜单 ===== */
.scroll-menu {
  margin: 0 25px 0 0;
  display: flex;
  justify-content: flex-end;
  padding: 0;
  gap: 6px;
}
.scroll-menu li {
  list-style: none; margin: 0 10px; font-size: 17px;
  height: 60px; line-height: 60px; position: relative;
  cursor: pointer; display: flex; flex-direction: column;
  align-items: center;
}
.scroll-menu li:hover .my-menu span { color: var(--themeBackground); }
.scroll-menu li .my-menu { height: 52px; line-height: 52px; }
.scroll-menu li.active .my-menu span {
  color: var(--themeBackground); font-weight: 700;
  text-shadow: 0 0 12px rgba(255,165,0,0.6), 0 0 24px rgba(255,165,0,0.3);
}

.scroll-menu li .my-menu:after {
  content: '';
  display: block;
  position: absolute;
  bottom: 0;
  height: 6px;
  background-color: var(--themeBackground);
  width: 100%;
  max-width: 0;
  transition: max-width 0.25s ease-in-out;
}

.scroll-menu li:hover .my-menu:after {
  max-width: 100%;
}

.el-dropdown {
  font-size: unset;
  color: unset;
}

.el-popper[x-placement^='bottom'] {
  margin-top: -8px;
}

.el-dropdown-menu {
  padding: 5px 0;
}

.el-dropdown-menu__item {
  font-size: unset;
}

.el-dropdown-menu__item:hover {
  background-color: var(--white);
  color: var(--themeBackground);
}

/* ===== 主内容区 ===== */
.main-container {
  flex: 1;
}

/* ===== 页脚 ===== */
.site-footer {
  background: var(--gradientBG);
  background-size: 400% 400%;
  animation: gradientBG 10s ease infinite;
  padding: 20px;
  text-align: center;
  color: var(--white);
  font-size: 14px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
}

/* ===== 浮动工具按钮 ===== */
.toolButton {
  position: fixed;
  right: 3vh;
  bottom: 3vh;
  animation: slide-bottom 0.5s ease-in-out both;
  z-index: 99;
  cursor: pointer;
  font-size: 25px;
  width: 30px;
}

.backTop {
  transition: all 0.3s ease-in;
  position: relative;
  top: 0;
  left: -13px;
  margin-bottom: 2px;
}

.backTop:hover {
  top: -10px;
}

.my-setting {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;
  cursor: pointer;
  font-size: 20px;
}

.my-setting i {
  padding: 5px;
}

.my-setting i:hover {
  color: var(--themeBackground);
}

/* ===== 手机端抽屉 ===== */
.small-menu li {
  padding: 10px 0;
  cursor: pointer;
  font-size: 17px;
}

.small-menu li:hover {
  color: var(--themeBackground);
}

/* ===== 响应式 ===== */
@media screen and (max-width: 400px) {
  .toolButton {
    right: 0.5vh;
  }
}
</style>
