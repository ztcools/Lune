<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <div class="card-glow" />
      <div class="card-inner">
        <div class="card-logo">
          <span>🌙</span>
        </div>
        <h2 class="card-title">Lune 后台</h2>
        <p class="card-subtitle">管理员登录</p>
        <div class="input-group">
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input v-model="account" type="text" placeholder="管理员账号" class="nature-input" @keyup.enter="login" />
          </div>
        </div>
        <div class="input-group">
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input v-model="password" type="password" placeholder="密码" class="nature-input" @keyup.enter="login" />
          </div>
        </div>
        <div class="btn-group">
          <button class="nature-btn nature-btn-primary" @click="login" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <span v-else class="btn-loading"><i class="spinner" /> 验证中...</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const account = ref('')
const password = ref('')
const loading = ref(false)

async function login() {
  if (!account.value || !password.value) { ElMessage.warning('请输入账号和密码'); return }
  localStorage.removeItem('token'); localStorage.removeItem('user')
  loading.value = true
  try {
    const data = await userStore.login(account.value, password.value)
    if (data.role !== 'ADMIN') { ElMessage.error('无管理员权限'); loading.value = false; return }
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/admin')
  } catch(e) { ElMessage.error('登录失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.admin-login-page {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 30%, #fff8e1 70%, #f1f8e9 100%);
  background-size: 300% 300%;
  animation: bgShift 12s ease infinite;
}
@keyframes bgShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.admin-login-card {
  position: relative; width: 400px; max-width: 92vw;
  background: linear-gradient(145deg, rgba(255,255,255,0.88) 0%, rgba(232,245,233,0.92) 50%, rgba(200,230,201,0.88) 100%);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1.5px solid rgba(255,255,255,0.5);
  box-shadow: 0 8px 40px rgba(56,142,60,0.15), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.6);
  overflow: hidden;
  font-family: var(--trendy-font);
}

.card-glow {
  position: absolute; top: -50%; left: -50%;
  width: 200%; height: 200%;
  background: radial-gradient(circle at 30% 20%, rgba(129,199,132,0.2) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(255,183,77,0.12) 0%, transparent 50%);
  pointer-events: none;
  animation: glowFloat 6s ease-in-out infinite;
}
@keyframes glowFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(10px, -10px); }
}

.card-inner { position: relative; padding: 44px 36px 36px; z-index: 1; text-align: center; }

.card-logo {
  width: 72px; height: 72px; margin: 0 auto 14px;
  background: var(--nature-gradient);
  border-radius: 22px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 20px rgba(76,175,80,0.35);
  animation: logoBounce 3s ease-in-out infinite;
}
.card-logo span { font-size: 36px; }
@keyframes logoBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.card-title { font-size: 26px; font-weight: 700; color: #2e7d32; margin: 0 0 4px; }
.card-subtitle { font-size: 14px; color: #689f63; margin: 0 0 28px; }

.input-group { margin-bottom: 18px; }
.input-wrapper {
  display: flex; align-items: center;
  background: rgba(255,255,255,0.7);
  border: 1.5px solid rgba(129,199,132,0.3);
  border-radius: 18px; padding: 2px 2px 2px 14px;
  transition: all 0.3s ease;
}
.input-wrapper:focus-within {
  border-color: #66bb6a;
  box-shadow: 0 0 0 4px rgba(76,175,80,0.12), 0 4px 12px rgba(76,175,80,0.1);
  background: rgba(255,255,255,0.95);
  transform: translateY(-1px);
}
.input-icon { font-size: 16px; margin-right: 8px; }
.nature-input {
  flex: 1; border: none; outline: none; background: transparent;
  font-size: 15px; padding: 12px 8px; color: #333;
  font-family: var(--trendy-font); font-weight: 500;
}
.nature-input::placeholder { color: #a5c8a0; }

.btn-group { display: flex; gap: 12px; margin-top: 8px; }
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

.btn-loading { display: flex; align-items: center; justify-content: center; gap: 6px; }
.spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
