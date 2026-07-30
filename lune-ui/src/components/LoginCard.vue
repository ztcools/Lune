<template>
  <teleport to="body">
    <transition name="card-fade">
      <div v-if="visible" class="card-overlay" @click.self="$emit('close')">
        <!-- 缓慢流动的靛蓝／墨青双色光雾（transform 位移，不触发重排） -->
        <div class="overlay-flow" aria-hidden="true"></div>
        <transition name="card-pop">
          <div
            v-if="visible"
            class="login-card"
            role="dialog"
            aria-modal="true"
            :aria-label="isRegister ? '注册' : '登录'"
          >
            <div class="card-glow" />

            <button class="card-close" type="button" aria-label="关闭" @click="$emit('close')">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 6l12 12M18 6L6 18" stroke-linecap="round" />
              </svg>
            </button>

            <div class="card-inner">
              <div class="card-header">
                <div class="card-logo"><LineIcon name="moon" :size="26" :stroke-width="1.6" /></div>
                <h2 class="card-title">{{ isRegister ? '加入 Lune' : '欢迎回来' }}</h2>
                <p class="card-subtitle">
                  {{ isRegister ? '创建账号，一起记录美好生活' : '登录你的账号，继续探索美好' }}
                </p>
              </div>

              <!-- 分段控件替代原先那个挤在密码框角上的「注册」小字：
                   两个入口地位相同，用户一眼就知道自己在哪一步 -->
              <div class="seg" role="tablist">
                <span class="seg-thumb" :class="{ right: isRegister }" aria-hidden="true" />
                <button
                  v-for="t in tabs" :key="t.key"
                  class="seg-btn" :class="{ on: isRegister === t.reg }"
                  type="button" role="tab" :aria-selected="isRegister === t.reg"
                  @click="switchTab(t.reg)"
                >{{ t.label }}</button>
              </div>

              <transition name="slide-fade" mode="out-in">
                <!-- ============ 登录 ============ -->
                <form v-if="!isRegister" key="login" class="card-form" @submit.prevent="handleLogin" novalidate>
                  <AuthField
                    ref="loginFirstRef"
                    v-model="loginForm.account"
                    label="用户名或邮箱"
                    name="username"
                    autocomplete="username"
                    :error="errs.account"
                    @enter="handleLogin"
                  >
                    <template #icon><IconUser /></template>
                  </AuthField>

                  <AuthField
                    v-model="loginForm.password"
                    label="密码"
                    type="password"
                    name="current-password"
                    autocomplete="current-password"
                    :error="errs.password"
                    @enter="handleLogin"
                  >
                    <template #icon><IconLock /></template>
                  </AuthField>

                  <transition name="err-fade">
                    <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
                  </transition>

                  <button class="nature-btn nature-btn-primary" type="submit" :disabled="loginLoading">
                    <span v-if="!loginLoading">登 录</span>
                    <span v-else class="btn-loading"><i class="spinner" />登录中…</span>
                  </button>

                  <p class="form-tip">
                    还没有账号？<span class="link" @click="switchTab(true)">立即注册</span>
                  </p>
                </form>

                <!-- ============ 注册 ============ -->
                <form v-else key="register" class="card-form" @submit.prevent="handleRegister" novalidate>
                  <AuthField
                    ref="regFirstRef"
                    v-model="registerForm.username"
                    label="用户名"
                    name="new-username"
                    autocomplete="username"
                    :error="errs.username"
                  >
                    <template #icon><IconUser /></template>
                  </AuthField>

                  <AuthField
                    v-model="registerForm.email"
                    label="邮箱地址"
                    name="email"
                    autocomplete="email"
                    inputmode="email"
                    :error="errs.email"
                  >
                    <template #icon><IconMail /></template>
                  </AuthField>

                  <AuthField
                    v-model="registerForm.code"
                    label="邮箱验证码"
                    name="one-time-code"
                    autocomplete="one-time-code"
                    inputmode="numeric"
                    maxlength="6"
                    :error="errs.code"
                  >
                    <template #icon><IconShield /></template>
                    <template #suffix>
                      <button
                        class="code-btn" type="button"
                        :disabled="codeSending || countdown > 0 || !emailValid"
                        :title="emailValid ? '' : '请先填写正确的邮箱'"
                        @click="sendCode"
                      >{{ countdown > 0 ? `${countdown}s` : (codeSending ? '发送中' : '发送验证码') }}</button>
                    </template>
                  </AuthField>

                  <AuthField
                    v-model="registerForm.password"
                    label="设置密码"
                    type="password"
                    name="new-password"
                    autocomplete="new-password"
                    :error="errs.regPassword"
                  >
                    <template #icon><IconLock /></template>
                  </AuthField>

                  <!-- 强度条只在开始输入后出现，避免空表单上就挂着一排灰格子 -->
                  <div v-if="registerForm.password" class="pw-meter">
                    <span v-for="i in 3" :key="i" class="pw-bar" :class="{ on: pwScore >= i, [pwLevel.cls]: pwScore >= i }" />
                    <em class="pw-text" :class="pwLevel.cls">{{ pwLevel.text }}</em>
                  </div>

                  <AuthField
                    v-model="registerForm.confirmPassword"
                    label="确认密码"
                    type="password"
                    name="confirm-password"
                    autocomplete="new-password"
                    :error="errs.confirmPassword"
                    @enter="handleRegister"
                  >
                    <template #icon><IconLock /></template>
                  </AuthField>

                  <transition name="err-fade">
                    <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
                  </transition>

                  <button class="nature-btn nature-btn-primary" type="submit" :disabled="registerLoading">
                    <span v-if="!registerLoading">注 册</span>
                    <span v-else class="btn-loading"><i class="spinner" />注册中…</span>
                  </button>

                  <p class="form-tip">
                    已有账号？<span class="link" @click="switchTab(false)">返回登录</span>
                  </p>
                </form>
              </transition>
            </div>
          </div>
        </transition>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick, onUnmounted, h } from 'vue'
import { useUserStore } from '../stores/user'
import { authApi } from '../api/modules'
import AuthField from './auth/AuthField.vue'
import LineIcon from './LineIcon.vue'
import { ElMessage } from 'element-plus'

/**
 * 前台登录/注册弹窗。
 *
 * 表单观感统一走 AuthField（与后台登录页共用），这里只负责流程：
 * 分段切换、验证码倒计时、行内校验、Esc 关闭与滚动锁。
 */

// 图标用内联 SVG 而不是 emoji：emoji 在各平台渲染差异大、还会跟着系统字体
// 变粗变彩，是原先这张卡片显得「廉价」的主要来源。
const stroke = { fill: 'none', stroke: 'currentColor', 'stroke-width': 1.8, 'stroke-linecap': 'round' }
const svg = (...paths) => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, ...stroke }, paths)
const IconUser = () => svg(
  h('circle', { cx: 12, cy: 8.2, r: 3.6 }),
  h('path', { d: 'M4.8 20c.9-3.6 3.8-5.6 7.2-5.6s6.3 2 7.2 5.6' })
)
const IconLock = () => svg(
  h('rect', { x: 4.5, y: 10.5, width: 15, height: 9.5, rx: 2.6 }),
  h('path', { d: 'M8.2 10.5V7.8a3.8 3.8 0 0 1 7.6 0v2.7' })
)
const IconMail = () => svg(
  h('rect', { x: 3, y: 5.5, width: 18, height: 13, rx: 2.6 }),
  h('path', { d: 'M4 7l8 6 8-6' })
)
const IconShield = () => svg(
  h('path', { d: 'M12 3.5l7 2.6v5.2c0 4.2-2.9 7.4-7 9.2-4.1-1.8-7-5-7-9.2V6.1l7-2.6Z' }),
  h('path', { d: 'M9.2 12.2l2 2 3.6-3.9' })
)

const props = defineProps({
  visible: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'logged-in'])

const userStore = useUserStore()
const isRegister = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const formError = ref('')
let countdownTimer = null

const tabs = [
  { key: 'login', label: '登录', reg: false },
  { key: 'register', label: '注册', reg: true }
]

const loginForm = reactive({ account: '', password: '' })
const registerForm = reactive({ username: '', password: '', confirmPassword: '', email: '', code: '' })
const errs = reactive({ account: '', password: '', username: '', email: '', code: '', regPassword: '', confirmPassword: '' })

const loginFirstRef = ref(null)
const regFirstRef = ref(null)

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
const emailValid = computed(() => EMAIL_RE.test(registerForm.email))

// 三档强度：够长 / 有字母+数字 / 再加符号或更长
const pwScore = computed(() => {
  const p = registerForm.password
  if (!p) return 0
  let s = 0
  if (p.length >= 8) s++
  if (/[a-zA-Z]/.test(p) && /\d/.test(p)) s++
  if (/[^a-zA-Z0-9]/.test(p) || p.length >= 12) s++
  return s
})
const pwLevel = computed(() => (
  [{ cls: 'weak', text: '偏弱' }, { cls: 'weak', text: '偏弱' }, { cls: 'mid', text: '一般' }, { cls: 'good', text: '很强' }][pwScore.value]
))

function clearErrs() {
  formError.value = ''
  Object.keys(errs).forEach(k => { errs[k] = '' })
}

function switchTab(reg) {
  if (isRegister.value === reg) return
  isRegister.value = reg
  clearErrs()
  focusFirst()
}

function focusFirst() {
  nextTick(() => {
    (isRegister.value ? regFirstRef.value : loginFirstRef.value)?.focus()
  })
}

function onEsc(e) { if (e.key === 'Escape') emit('close') }

watch(() => props.visible, (v) => {
  if (v) {
    // 打开时锁背景滚动 + 焦点落到第一个输入框，键盘用户不用先 Tab 一圈
    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', onEsc)
    focusFirst()
  } else {
    document.body.style.overflow = ''
    window.removeEventListener('keydown', onEsc)
    isRegister.value = false
    clearErrs()
    clearCountdown()
    Object.assign(loginForm, { account: '', password: '' })
    Object.assign(registerForm, { username: '', password: '', confirmPassword: '', email: '', code: '' })
  }
})

function clearCountdown() {
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  countdown.value = 0
}

onUnmounted(() => {
  clearCountdown()
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onEsc)
})

async function sendCode() {
  if (!emailValid.value) { errs.email = '请填写正确的邮箱地址'; return }
  errs.email = ''
  codeSending.value = true
  try {
    await authApi.sendCode(registerForm.email)
    ElMessage.success('验证码已发送，请查收邮箱')
    clearCountdown()
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearCountdown()
    }, 1000)
  } catch (e) {
    formError.value = e?.message || '验证码发送失败，请稍后再试'
  } finally {
    codeSending.value = false
  }
}

async function handleLogin() {
  clearErrs()
  errs.account = loginForm.account ? '' : '请输入用户名或邮箱'
  errs.password = loginForm.password ? '' : '请输入密码'
  if (errs.account || errs.password) return
  loginLoading.value = true
  try {
    await userStore.login(loginForm.account, loginForm.password)
    ElMessage.success('登录成功')
    emit('logged-in')
    emit('close')
  } catch (e) {
    // 后端的「剩余尝试次数」「账号已锁定」提示要原样传给用户
    formError.value = e?.message || '登录失败，请稍后再试'
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  clearErrs()
  errs.username = registerForm.username ? '' : '请设置用户名'
  errs.email = registerForm.email ? (emailValid.value ? '' : '邮箱格式不正确') : '请输入邮箱'
  errs.code = registerForm.code ? '' : '请输入邮箱验证码'
  errs.regPassword = registerForm.password
    ? (registerForm.password.length >= 6 ? '' : '密码至少 6 位')
    : '请设置密码'
  errs.confirmPassword = registerForm.confirmPassword
    ? (registerForm.confirmPassword === registerForm.password ? '' : '两次输入的密码不一致')
    : '请再次输入密码'
  if (Object.values(errs).some(Boolean)) return

  registerLoading.value = true
  try {
    await userStore.register(registerForm)
    ElMessage.success('注册成功，欢迎加入！')
    emit('logged-in')
    emit('close')
  } catch (e) {
    formError.value = e?.message || '注册失败，请稍后再试'
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
/* 局部墨青／靛蓝调色板：只作用在这张卡片上，不动全站绿系令牌 */
.card-overlay {
  --ink: #2f6f6a;
  --ink-deep: #1f4f4b;
  --indigo: #3f4f8f;
  --ink-soft: #6d8a88;
  --ink-pale: #eef4f4;
  /* 喂给 AuthField（描边/图标/浮动标签） */
  --af-line: rgba(47, 111, 106, 0.16);
  --af-line-on: rgba(47, 111, 106, 0.55);
  --af-ring: rgba(47, 111, 106, 0.12);
  --af-muted: #9bafae;
  --af-accent: #1f4f4b;

  position: fixed; inset: 0; z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  padding: 20px;
  overflow: hidden;
  background: rgba(16, 28, 34, 0.42);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* 背景光雾：两团渐变缓慢绕行，比原来的静态绿底「高级」一点 */
.overlay-flow {
  position: absolute; inset: -35%;
  pointer-events: none;
  background:
    radial-gradient(circle at 28% 30%, rgba(63, 79, 143, 0.5) 0%, transparent 46%),
    radial-gradient(circle at 72% 68%, rgba(47, 111, 106, 0.45) 0%, transparent 48%);
  filter: blur(10px);
  will-change: transform;
  animation: overlayFlow 20s ease-in-out infinite;
}
@keyframes overlayFlow {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  33% { transform: translate3d(4%, -3%, 0) scale(1.08); }
  66% { transform: translate3d(-3%, 4%, 0) scale(1.04); }
}

.login-card {
  position: relative;
  width: 400px; max-width: 100%;
  max-height: calc(100vh - 40px);
  max-height: calc(100dvh - 40px);
  overflow-y: auto;
  background: #fff;
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 24px 60px rgba(15, 40, 40, 0.14), 0 2px 6px rgba(15, 40, 40, 0.06);
  font-family: var(--trendy-font);
  scrollbar-width: none;
}
.login-card::-webkit-scrollbar { display: none; }

/* 白卡上只留一层几乎看不见的冷色晕，用来托住顶部 logo */
.card-glow {
  position: absolute; inset: 0;
  border-radius: inherit;
  background: radial-gradient(120% 60% at 50% -10%, rgba(47, 111, 106, 0.07) 0%, transparent 70%);
  pointer-events: none;
}

.card-close {
  position: absolute; top: 14px; right: 14px; z-index: 2;
  width: 30px; height: 30px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  border: none; cursor: pointer;
  color: var(--ink-soft); background: var(--ink-pale);
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}
.card-close:hover { color: var(--ink-deep); background: #e3eeed; transform: rotate(90deg); }

.card-inner { position: relative; padding: 32px 30px 26px; z-index: 1; }

.card-header { text-align: center; margin-bottom: 20px; }
.card-logo {
  width: 54px; height: 54px; margin: 0 auto 14px;
  background: linear-gradient(135deg, var(--ink) 0%, var(--indigo) 100%);
  border-radius: 17px;
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 20px rgba(47, 111, 106, 0.28);
}

.card-title {
  font-size: 25px; font-weight: 700; color: #1c3a3a; margin: 0 0 5px;
  letter-spacing: 2px;
  font-family: var(--calligraphy-font, var(--trendy-font));
}
.card-subtitle { font-size: 13px; color: var(--ink-soft); margin: 0; }

/* ============ 分段控件 ============ */
.seg {
  position: relative;
  display: grid; grid-template-columns: 1fr 1fr;
  padding: 4px; margin-bottom: 22px;
  background: var(--ink-pale);
  border-radius: 14px;
}
.seg-thumb {
  position: absolute; top: 4px; left: 4px;
  width: calc(50% - 4px); height: calc(100% - 8px);
  background: #fff; border-radius: 11px;
  box-shadow: 0 2px 8px rgba(15, 40, 40, 0.12);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.seg-thumb.right { transform: translateX(100%); }
.seg-btn {
  position: relative; z-index: 1;
  border: none; background: transparent; cursor: pointer;
  padding: 9px 0; font-family: inherit;
  font-size: 14px; font-weight: 600; letter-spacing: 1px;
  color: var(--ink-soft);
  transition: color 0.25s ease;
}
.seg-btn.on { color: var(--ink-deep); }

.card-form { display: grid; gap: 15px; }

/* ============ 验证码按钮 ============ */
.code-btn {
  flex-shrink: 0; border: none; outline: none;
  background: var(--ink-pale);
  color: var(--ink-deep);
  font-family: inherit; font-size: 12.5px; font-weight: 700;
  padding: 7px 11px; border-radius: 10px; cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s ease, color 0.2s ease, opacity 0.2s ease;
}
.code-btn:hover:not(:disabled) { background: linear-gradient(135deg, var(--ink), var(--indigo)); color: #fff; }
.code-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ============ 密码强度 ============ */
.pw-meter { display: flex; align-items: center; gap: 5px; margin-top: -6px; padding: 0 2px; }
.pw-bar {
  flex: 1; height: 4px; border-radius: 3px;
  background: rgba(47, 111, 106, 0.14);
  transition: background 0.25s ease;
}
.pw-bar.on.weak { background: #d9a05b; }
.pw-bar.on.mid { background: #5f9b94; }
.pw-bar.on.good { background: var(--ink-deep); }
.pw-text { font-size: 11.5px; font-style: normal; margin-left: 4px; min-width: 26px; }
.pw-text.weak { color: #c08540; }
.pw-text.mid { color: #4d8681; }
.pw-text.good { color: var(--ink-deep); }

/* ============ 错误 / 提示 / 按钮 ============ */
.form-error {
  margin: 0; padding: 10px 13px;
  font-size: 12.5px; line-height: 1.6; color: #b5453f;
  background: rgba(229, 115, 115, 0.1);
  border: 1px solid rgba(229, 115, 115, 0.26);
  border-radius: 12px;
}
.err-fade-enter-active, .err-fade-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.err-fade-enter-from, .err-fade-leave-to { opacity: 0; transform: translateY(-4px); }

.form-tip { margin: 2px 0 0; text-align: center; font-size: 12.5px; color: var(--ink-soft); }
.form-tip .link { color: var(--ink-deep); font-weight: 700; cursor: pointer; }
.form-tip .link:hover { text-decoration: underline; }

.nature-btn {
  border: none; outline: none; cursor: pointer;
  font-family: inherit; font-size: 15.5px; font-weight: 700;
  padding: 13px 20px; border-radius: 14px;
  letter-spacing: 3px;
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease;
}
.nature-btn-primary {
  margin-top: 3px;
  background: linear-gradient(135deg, var(--ink-deep) 0%, var(--indigo) 100%);
  color: #fff;
  box-shadow: 0 6px 18px rgba(31, 79, 75, 0.26);
}
.nature-btn-primary:hover:not(:disabled) { transform: translateY(-1.5px); box-shadow: 0 10px 24px rgba(31, 79, 75, 0.32); filter: brightness(1.08); }
.nature-btn-primary:active:not(:disabled) { transform: translateY(0) scale(0.99); }
.nature-btn-primary:disabled { opacity: 0.68; cursor: not-allowed; }
.nature-btn:focus-visible { box-shadow: 0 0 0 4px rgba(47, 111, 106, 0.3); }

.btn-loading { display: inline-flex; align-items: center; justify-content: center; gap: 8px; letter-spacing: 1px; }
.spinner {
  width: 15px; height: 15px; border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff;
  animation: spin 0.65s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ============ 过渡 ============ */
.card-fade-enter-active, .card-fade-leave-active { transition: opacity 0.28s ease; }
.card-fade-enter-from, .card-fade-leave-to { opacity: 0; }

.card-pop-enter-active { transition: all 0.38s cubic-bezier(0.34, 1.4, 0.64, 1); }
.card-pop-leave-active { transition: all 0.2s ease-in; }
.card-pop-enter-from { opacity: 0; transform: scale(0.9) translateY(16px); }
.card-pop-leave-to { opacity: 0; transform: scale(0.94) translateY(8px); }

.slide-fade-enter-active { transition: all 0.3s ease-out; }
.slide-fade-leave-active { transition: all 0.18s ease-in; }
.slide-fade-enter-from { opacity: 0; transform: translateX(18px); }
.slide-fade-leave-to { opacity: 0; transform: translateX(-18px); }

@media (max-width: 480px) {
  .card-inner { padding: 26px 20px 22px; }
  .card-title { font-size: 21px; }
}

@media (prefers-reduced-motion: reduce) {
  .overlay-flow { animation: none; }
  .card-pop-enter-active, .card-pop-leave-active,
  .slide-fade-enter-active, .slide-fade-leave-active { transition: opacity 0.15s ease; }
  .card-pop-enter-from, .card-pop-leave-to,
  .slide-fade-enter-from, .slide-fade-leave-to { transform: none; }
}
</style>
