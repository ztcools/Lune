<template>
  <div class="login-shell">
    <!-- ===== 左：品牌视觉面 ===== -->
    <aside class="visual" :class="{ 'has-img': !!coverImage }">
      <div v-if="coverImage" class="visual-img" :style="{ backgroundImage: `url(${coverImage})` }" />
      <div class="visual-scrim" />

      <div class="visual-body">
        <div class="brand">
          <span class="brand-mark">🌙</span>
          <span class="brand-name">Lune</span>
        </div>

        <h2 class="visual-title">记录美好生活<br />的后台</h2>
        <p class="visual-sub">文章、随笔、树洞、许愿池 —— 站点的一切都从这里开始。</p>

        <ul class="visual-points">
          <li v-for="p in points" :key="p">{{ p }}</li>
        </ul>
      </div>

      <p class="visual-foot">© {{ year }} Lune · 仅限管理员访问</p>
    </aside>

    <!-- ===== 右：表单面 ===== -->
    <main class="panel">
      <div class="panel-inner">
        <div class="brand brand-mobile">
          <span class="brand-mark">🌙</span>
          <span class="brand-name">Lune</span>
        </div>

        <header class="panel-head">
          <h1 class="panel-title">欢迎回来</h1>
          <p class="panel-sub">请使用管理员账号登录后台</p>
        </header>

        <!-- 用真表单：浏览器/密码管理器只在 submit 时才提示保存凭据 -->
        <form class="panel-form" @submit.prevent="login" novalidate>
          <AuthField
            ref="accountRef"
            v-model="account"
            label="管理员账号"
            name="username"
            autocomplete="username"
            :error="errors.account"
            @enter="login"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
                <circle cx="12" cy="8.2" r="3.6" />
                <path d="M4.8 20c.9-3.6 3.8-5.6 7.2-5.6s6.3 2 7.2 5.6" stroke-linecap="round" />
              </svg>
            </template>
          </AuthField>

          <AuthField
            v-model="password"
            label="密码"
            type="password"
            name="current-password"
            autocomplete="current-password"
            :error="errors.password"
            @enter="login"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="4.5" y="10.5" width="15" height="9.5" rx="2.6" />
                <path d="M8.2 10.5V7.8a3.8 3.8 0 0 1 7.6 0v2.7" stroke-linecap="round" />
              </svg>
            </template>
          </AuthField>

          <p v-if="capsOn" class="caps-hint">⇪ Caps Lock 已开启</p>

          <label class="remember">
            <input type="checkbox" v-model="remember" />
            <span class="remember-box" aria-hidden="true"></span>
            <span>记住账号</span>
          </label>

          <transition name="err-fade">
            <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
          </transition>

          <button class="submit-btn" type="submit" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <span v-else class="btn-loading"><i class="spinner" />验证中…</span>
          </button>
        </form>

        <p class="panel-foot">
          忘记密码？请在服务器上通过环境变量
          <code>ADMIN_DEFAULT_PASSWORD</code> 重置。
        </p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useAppStore } from '../stores/app'
import { usePageBackground } from '../composables/usePageBackground'
import AuthField from '../components/auth/AuthField.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// 视觉面复用站长在后台配的 Landing 背景（未配则退化成纯渐变，不留空洞）
const coverImage = usePageBackground('landing')

const REMEMBER_KEY = 'lune_admin_account'
const account = ref(localStorage.getItem(REMEMBER_KEY) || '')
const password = ref('')
const remember = ref(!!localStorage.getItem(REMEMBER_KEY))
const loading = ref(false)
const capsOn = ref(false)
const formError = ref('')
const errors = reactive({ account: '', password: '' })
const accountRef = ref(null)

const year = new Date().getFullYear()
const points = [
  '📊 访问统计 · 地区分布与趋势',
  '📝 内容管理 · 文章 / 随笔 / 记录',
  '🎨 站点装扮 · 背景图与音乐歌单'
]

const onKey = (e) => {
  if (typeof e.getModifierState === 'function') capsOn.value = e.getModifierState('CapsLock')
}

onMounted(() => {
  appStore.fetchConfig()
  window.addEventListener('keydown', onKey)
  window.addEventListener('keyup', onKey)
  if (!account.value) accountRef.value?.focus()
})
onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
  window.removeEventListener('keyup', onKey)
})

const filled = computed(() => !!account.value && !!password.value)

function validate() {
  errors.account = account.value ? '' : '请输入管理员账号'
  errors.password = password.value ? '' : '请输入密码'
  return filled.value
}

async function login() {
  formError.value = ''
  if (!validate()) return
  // 换账号登录时，先清掉上一位用户残留的凭据
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  loading.value = true
  try {
    const data = await userStore.login(account.value, password.value)
    if (data.role !== 'ADMIN') {
      // 原来只是 return，token 已经写进 localStorage —— 等于给了非管理员
      // 一个可用会话，只是这个页面不让进。这里必须把会话撤掉。
      await userStore.logout().catch(() => {})
      formError.value = '该账号没有后台权限'
      return
    }
    if (remember.value) localStorage.setItem(REMEMBER_KEY, account.value)
    else localStorage.removeItem(REMEMBER_KEY)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/admin')
  } catch (e) {
    // 后端会返回「账号或密码错误（剩余尝试次数：3）」「账号已锁定…」这类
    // 关键提示，原先一律吞成「登录失败」，锁定了都不知道为什么。
    formError.value = e?.message || '登录失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-shell {
  display: flex;
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--nature-gradient-soft);
  font-family: var(--globalFont);
}

/* ============ 左：视觉面 ============ */
.visual {
  position: relative;
  flex: 1 1 58%;
  display: flex; flex-direction: column; justify-content: space-between;
  padding: 46px 52px;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(150deg, #2e7d32 0%, #43a047 45%, #81c784 100%);
}
.visual-img {
  position: absolute; inset: 0;
  background-size: cover; background-position: center;
  transform: scale(1.04);
  animation: kenBurns 26s ease-in-out infinite alternate;
}
@keyframes kenBurns {
  from { transform: scale(1.04) translate(0, 0); }
  to { transform: scale(1.12) translate(-1.2%, -1%); }
}
/* 压暗到文字可读，同时保留图本身的色彩（不做成灰蒙蒙的一层） */
.visual-scrim {
  position: absolute; inset: 0;
  background:
    linear-gradient(115deg, rgba(27, 66, 32, 0.72) 0%, rgba(38, 92, 44, 0.42) 48%, rgba(38, 92, 44, 0.12) 100%),
    radial-gradient(120% 90% at 12% 88%, rgba(20, 52, 25, 0.5), transparent 60%);
}
.visual.has-img { background: #2b5c31; }

.visual-body { position: relative; z-index: 1; margin-top: auto; max-width: 460px; }
.visual-foot { position: relative; z-index: 1; margin: 0; font-size: 12.5px; color: rgba(255,255,255,0.66); }

.brand { display: flex; align-items: center; gap: 10px; }
.brand-mark {
  width: 40px; height: 40px; border-radius: 13px;
  display: flex; align-items: center; justify-content: center;
  font-size: 21px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(6px);
}
.brand-name { font-size: 21px; font-weight: 700; letter-spacing: 2px; }

.visual-title {
  margin: 26px 0 12px;
  font-size: 40px; line-height: 1.24; font-weight: 700;
  letter-spacing: 1px;
  text-shadow: 0 2px 18px rgba(0, 0, 0, 0.22);
}
.visual-sub {
  margin: 0 0 26px;
  font-size: 15px; line-height: 1.8;
  color: rgba(255, 255, 255, 0.86);
}
.visual-points { margin: 0; padding: 0; list-style: none; display: grid; gap: 10px; }
.visual-points li {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.92);
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 12px;
  padding: 9px 14px;
  backdrop-filter: blur(4px);
  width: fit-content;
}

/* ============ 右：表单面 ============ */
.panel {
  flex: 0 0 clamp(380px, 34%, 480px);
  display: flex; align-items: center; justify-content: center;
  padding: 40px 30px;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow: -18px 0 48px rgba(46, 92, 49, 0.08);
}
.panel-inner { width: 100%; max-width: 340px; }

.brand-mobile { display: none; margin-bottom: 22px; color: var(--nature-green-dark); }
.brand-mobile .brand-mark {
  background: var(--nature-gradient);
  border-color: transparent;
  box-shadow: 0 5px 16px rgba(76, 175, 80, 0.3);
}

.panel-head { margin-bottom: 26px; }
.panel-title {
  margin: 0 0 6px;
  font-size: 27px; font-weight: 700; letter-spacing: 0.5px;
  color: #24402a;
}
.panel-sub { margin: 0; font-size: 13.5px; color: var(--articleGreyFontColor); }

.panel-form { display: grid; gap: 16px; }

.caps-hint { margin: -6px 0 0; font-size: 12px; color: #c98a3a; }

.remember { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--maxGreyFont); cursor: pointer; user-select: none; }
.remember input { position: absolute; opacity: 0; width: 0; height: 0; }
.remember-box {
  width: 16px; height: 16px; border-radius: 5px;
  border: 1.5px solid rgba(67, 160, 71, 0.4);
  background: #fff;
  transition: all 0.2s ease;
  position: relative; flex-shrink: 0;
}
.remember input:checked + .remember-box {
  background: var(--nature-gradient); border-color: transparent;
}
.remember input:checked + .remember-box::after {
  content: ''; position: absolute; left: 4.6px; top: 1.6px;
  width: 4px; height: 8px;
  border: solid #fff; border-width: 0 2px 2px 0;
  transform: rotate(42deg);
}
.remember input:focus-visible + .remember-box { box-shadow: 0 0 0 4px rgba(102, 187, 106, 0.2); }

.form-error {
  margin: 0; padding: 10px 13px;
  font-size: 13px; line-height: 1.6; color: #b5453f;
  background: rgba(229, 115, 115, 0.1);
  border: 1px solid rgba(229, 115, 115, 0.28);
  border-radius: 12px;
}
.err-fade-enter-active, .err-fade-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.err-fade-enter-from, .err-fade-leave-to { opacity: 0; transform: translateY(-4px); }

.submit-btn {
  margin-top: 4px;
  border: none; outline: none; cursor: pointer;
  padding: 14px 20px; border-radius: 14px;
  font-family: inherit; font-size: 15.5px; font-weight: 700; letter-spacing: 3px;
  color: #fff;
  background: linear-gradient(135deg, #43a047, #66bb6a);
  box-shadow: 0 6px 18px rgba(67, 160, 71, 0.28);
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}
.submit-btn:hover:not(:disabled) { transform: translateY(-1.5px); box-shadow: 0 10px 24px rgba(67, 160, 71, 0.34); filter: brightness(1.04); }
.submit-btn:active:not(:disabled) { transform: translateY(0) scale(0.99); }
.submit-btn:disabled { opacity: 0.68; cursor: not-allowed; }
.submit-btn:focus-visible { box-shadow: 0 0 0 4px rgba(102, 187, 106, 0.35); }

.btn-loading { display: inline-flex; align-items: center; gap: 8px; letter-spacing: 1px; }
.spinner {
  width: 15px; height: 15px; border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  animation: spin 0.65s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.panel-foot { margin: 22px 0 0; font-size: 12px; line-height: 1.7; color: var(--greyFont); }
.panel-foot code {
  font-size: 11.5px; padding: 1px 5px; border-radius: 5px;
  background: rgba(67, 160, 71, 0.1); color: var(--nature-green-dark);
}

/* ============ 响应式：窄屏折叠成单栏 ============ */
@media (max-width: 900px) {
  .login-shell { flex-direction: column; }
  .visual {
    flex: none;
    min-height: 210px;
    padding: 26px 24px 22px;
    justify-content: flex-end;
  }
  .visual-body { margin-top: 0; }
  .visual-title { font-size: 26px; margin: 14px 0 8px; }
  .visual-sub { margin-bottom: 0; font-size: 13.5px; }
  .visual-points, .visual-foot { display: none; }

  .panel {
    flex: 1;
    align-items: flex-start;
    padding: 30px 22px 40px;
    box-shadow: 0 -14px 32px rgba(46, 92, 49, 0.1);
    border-radius: 26px 26px 0 0;
    margin-top: -22px;
    position: relative; z-index: 2;
    background: rgba(255, 255, 255, 0.94);
  }
  .panel-inner { max-width: 420px; margin: 0 auto; }
  .panel-title { font-size: 23px; }
}

@media (prefers-reduced-motion: reduce) {
  .visual-img { animation: none; }
}
</style>
