<template>
  <teleport to="body">
    <transition name="card-fade">
      <div v-if="visible" class="card-overlay" @click.self="$emit('close')">
        <transition name="card-pop">
          <div v-if="visible" class="login-card">
            <div class="card-glow" />
            <div class="card-inner">
              <div class="card-header">
                <div class="card-logo">
                  <span class="logo-icon">🌙</span>
                </div>
                <h2 class="card-title">{{ isRegister ? '加入 Lune' : '欢迎回来' }}</h2>
                <p class="card-subtitle">{{ isRegister ? '创建一个新账号，开始记录美好生活' : '登录你的账号，继续探索美好' }}</p>
              </div>

              <transition name="slide-fade" mode="out-in">
                <div v-if="!isRegister" key="login" class="card-form">
                  <div class="input-group">
                    <div class="input-wrapper">
                      <span class="input-icon">👤</span>
                      <input v-model="loginForm.account" type="text" placeholder="用户名或邮箱" class="nature-input" @keyup.enter="handleLogin" />
                    </div>
                  </div>
                  <div class="input-group">
                    <div class="input-wrapper">
                      <span class="input-icon">🔒</span>
                      <input v-model="loginForm.password" type="password" placeholder="密码" class="nature-input" @keyup.enter="handleLogin" />
                    </div>
                    <span class="switch-link" @click="isRegister = true">注册</span>
                  </div>
                  <div class="btn-group">
                    <button class="nature-btn nature-btn-primary" @click="handleLogin" :disabled="loginLoading">
                      <span v-if="!loginLoading">登 录</span>
                      <span v-else class="btn-loading"><i class="spinner" /> 登录中...</span>
                    </button>
                    <button class="nature-btn nature-btn-ghost" @click="$emit('close')">取消</button>
                  </div>
                </div>

                <div v-else key="register" class="card-form">
                  <div class="input-group">
                    <div class="input-wrapper">
                      <span class="input-icon">👤</span>
                      <input v-model="registerForm.username" type="text" placeholder="设置用户名" class="nature-input" />
                    </div>
                  </div>
                  <div class="input-group">
                    <div class="input-wrapper">
                      <span class="input-icon">🔒</span>
                      <input v-model="registerForm.password" type="password" placeholder="设置密码" class="nature-input" />
                    </div>
                  </div>
                  <div class="input-group">
                    <div class="input-wrapper">
                      <span class="input-icon">🔒</span>
                      <input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" class="nature-input" />
                    </div>
                  </div>
                  <div class="input-group">
                    <div class="input-wrapper">
                      <span class="input-icon">📧</span>
                      <input v-model="registerForm.email" type="email" placeholder="邮箱地址" class="nature-input" />
                    </div>
                  </div>
                  <div class="input-group">
                    <div class="input-wrapper code-wrapper">
                      <span class="input-icon">✉️</span>
                      <input v-model="registerForm.code" type="text" placeholder="验证码" class="nature-input" maxlength="6" />
                      <button class="code-btn" @click="sendCode" :disabled="codeSending || countdown > 0">
                        {{ countdown > 0 ? `${countdown}s` : '发送' }}
                      </button>
                    </div>
                  </div>
                  <div class="input-group">
                    <span class="switch-link back-link" @click="isRegister = false">← 返回登录</span>
                  </div>
                  <div class="btn-group">
                    <button class="nature-btn nature-btn-primary" @click="handleRegister" :disabled="registerLoading">
                      <span v-if="!registerLoading">注 册</span>
                      <span v-else class="btn-loading"><i class="spinner" /> 注册中...</span>
                    </button>
                    <button class="nature-btn nature-btn-ghost" @click="$emit('close')">取消</button>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </transition>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, reactive, watch, onUnmounted } from 'vue'
import { useUserStore } from '../stores/user'
import { authApi } from '../api/modules'
import { ElMessage } from 'element-plus'

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
let countdownTimer = null

const loginForm = reactive({ account: '', password: '' })
const registerForm = reactive({ username: '', password: '', confirmPassword: '', email: '', code: '' })

watch(() => props.visible, (v) => {
  if (!v) {
    isRegister.value = false
    clearCountdown()
    Object.assign(loginForm, { account: '', password: '' })
    Object.assign(registerForm, { username: '', password: '', confirmPassword: '', email: '', code: '' })
  }
})

function clearCountdown() {
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  countdown.value = 0
}

onUnmounted(clearCountdown)

async function sendCode() {
  if (!registerForm.email) { ElMessage.warning('请先输入邮箱'); return }
  codeSending.value = true
  try {
    await authApi.sendCode(registerForm.email)
    ElMessage.success('验证码已发送')
    clearCountdown()
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearCountdown()
    }, 1000)
  } catch (e) {
    ElMessage.error(e?.message || '发送失败')
  } finally {
    codeSending.value = false
  }
}

async function handleLogin() {
  if (!loginForm.account || !loginForm.password) { ElMessage.warning('请填写账号和密码'); return }
  loginLoading.value = true
  try {
    await userStore.login(loginForm.account, loginForm.password)
    ElMessage.success('登录成功')
    emit('logged-in')
    emit('close')
  } catch (e) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.username || !registerForm.password || !registerForm.confirmPassword || !registerForm.email || !registerForm.code) {
    ElMessage.warning('请填写所有字段'); return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次密码不一致'); return
  }
  registerLoading.value = true
  try {
    await userStore.register(registerForm)
    ElMessage.success('注册成功，欢迎加入！')
    emit('logged-in')
    emit('close')
  } catch (e) {
    ElMessage.error(e?.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.card-overlay {
  position: fixed; inset: 0; z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.login-card {
  position: relative;
  width: 420px; max-width: 92vw;
  background: linear-gradient(145deg, rgba(255,255,255,0.85) 0%, rgba(232,245,233,0.9) 40%, rgba(200,230,201,0.85) 100%);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 32px;
  border: 1.5px solid rgba(255,255,255,0.5);
  box-shadow:
    0 8px 40px rgba(56, 142, 60, 0.15),
    0 2px 8px rgba(0,0,0,0.06),
    inset 0 1px 0 rgba(255,255,255,0.6);
  overflow: hidden;
  font-family: var(--trendy-font);
}

.card-glow {
  position: absolute; top: -50%; left: -50%;
  width: 200%; height: 200%;
  background: radial-gradient(circle at 30% 20%, rgba(129,199,132,0.2) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(255,183,77,0.15) 0%, transparent 50%);
  pointer-events: none;
  animation: glowFloat 6s ease-in-out infinite;
}
@keyframes glowFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(10px, -10px); }
}

.card-inner { position: relative; padding: 40px 36px 32px; z-index: 1; }

.card-header { text-align: center; margin-bottom: 28px; }
.card-logo {
  width: 72px; height: 72px; margin: 0 auto 16px;
  background: var(--nature-gradient);
  border-radius: 22px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 20px rgba(76,175,80,0.35);
  animation: logoBounce 3s ease-in-out infinite;
}
@keyframes logoBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}
.logo-icon { font-size: 36px; }

.card-title {
  font-size: 26px; font-weight: 700; color: #2e7d32;
  margin: 0 0 6px; letter-spacing: 1px;
}
.card-subtitle {
  font-size: 14px; color: #689f63; margin: 0;
}

.card-form { padding: 0 8px; }

.input-group { margin-bottom: 16px; position: relative; }
.input-wrapper {
  display: flex; align-items: center;
  background: rgba(255,255,255,0.7);
  border: 1.5px solid rgba(129,199,132,0.3);
  border-radius: 18px;
  padding: 2px; padding-left: 14px;
  transition: all 0.3s ease;
  overflow: hidden;
}
.input-wrapper:focus-within {
  border-color: #66bb6a;
  box-shadow: 0 0 0 4px rgba(76,175,80,0.12), 0 4px 12px rgba(76,175,80,0.1);
  background: rgba(255,255,255,0.95);
  transform: translateY(-1px);
}
.input-icon { font-size: 16px; margin-right: 8px; flex-shrink: 0; }
.nature-input {
  flex: 1; border: none; outline: none; background: transparent;
  font-size: 15px; padding: 12px 8px; color: #333;
  font-family: var(--trendy-font); font-weight: 500;
}
.nature-input::placeholder { color: #a5c8a0; }

.code-wrapper { padding-right: 6px; }
.code-btn {
  flex-shrink: 0; border: none; outline: none;
  background: var(--nature-gradient);
  color: #fff; font-size: 13px; font-weight: 600;
  padding: 8px 14px; border-radius: 14px; cursor: pointer;
  font-family: var(--trendy-font);
  transition: all 0.3s ease;
  white-space: nowrap;
}
.code-btn:hover:not(:disabled) { transform: scale(1.05); filter: brightness(1.1); }
.code-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.switch-link {
  position: absolute; right: 4px; bottom: -20px;
  font-size: 13px; color: #66bb6a; cursor: pointer;
  font-weight: 600; transition: all 0.2s;
  user-select: none;
}
.switch-link:hover { color: #388e3c; text-decoration: underline; }
.switch-link.back-link { position: static; display: inline-block; }

.btn-group { display: flex; gap: 12px; margin-top: 24px; }

.nature-btn {
  flex: 1; border: none; outline: none;
  font-size: 16px; font-weight: 600;
  padding: 14px 20px; border-radius: 18px; cursor: pointer;
  font-family: var(--trendy-font);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 2px;
}
.nature-btn-primary {
  background: var(--nature-gradient);
  color: #fff;
  box-shadow: 0 4px 16px rgba(76,175,80,0.35);
}
.nature-btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(76,175,80,0.45);
  filter: brightness(1.08);
}
.nature-btn-primary:active:not(:disabled) { transform: translateY(0) scale(0.97); }
.nature-btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }

.nature-btn-ghost {
  background: rgba(255,255,255,0.5);
  color: #689f63;
  border: 1.5px solid rgba(129,199,132,0.4);
}
.nature-btn-ghost:hover {
  background: rgba(255,255,255,0.9);
  border-color: #81c784;
  transform: translateY(-1px);
}

.btn-loading { display: flex; align-items: center; justify-content: center; gap: 6px; }
.spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.card-fade-enter-active, .card-fade-leave-active { transition: opacity 0.3s ease; }
.card-fade-enter-from, .card-fade-leave-to { opacity: 0; }

.card-pop-enter-active { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.card-pop-leave-active { transition: all 0.2s ease-in; }
.card-pop-enter-from { opacity: 0; transform: scale(0.85) translateY(20px); }
.card-pop-leave-to { opacity: 0; transform: scale(0.9) translateY(10px); }

.slide-fade-enter-active { transition: all 0.35s ease-out; }
.slide-fade-leave-active { transition: all 0.2s ease-in; }
.slide-fade-enter-from { opacity: 0; transform: translateX(25px); }
.slide-fade-leave-to { opacity: 0; transform: translateX(-25px); }
</style>
