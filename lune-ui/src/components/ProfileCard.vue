<template>
  <teleport to="body">
    <transition name="card-fade">
      <div v-if="visible" class="profile-overlay" @click.self="$emit('close')">
        <transition name="card-pop">
          <div v-if="visible" class="profile-card">
            <button class="close-btn" @click="$emit('close')">✕</button>
            <div class="card-glow" />

            <div class="profile-inner">
              <div class="profile-avatar-section">
                <div class="avatar-wrap" @click="triggerAvatarUpload">
                  <el-avatar :size="90" :src="form.avatar" class="profile-avatar">
                    {{ form.nickname?.charAt(0) || '?' }}
                  </el-avatar>
                  <div class="avatar-overlay"><span>📷</span></div>
                </div>
                <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
                <h3 class="profile-nickname">{{ form.nickname || '未设置' }}</h3>
              </div>

              <div class="profile-fields">
                <div class="field-row">
                  <label class="field-label">昵称</label>
                  <input v-model="form.nickname" class="field-input" placeholder="你的昵称" @input="markDirty" />
                </div>
                <div class="field-row">
                  <label class="field-label">性别</label>
                  <div class="gender-toggles">
                    <button :class="['gender-btn', { active: form.gender === '男' }]" @click="form.gender = '男'; markDirty()">♂ 男</button>
                    <button :class="['gender-btn', { active: form.gender === '女' }]" @click="form.gender = '女'; markDirty()">♀ 女</button>
                    <button :class="['gender-btn', { active: form.gender === '保密' }]" @click="form.gender = '保密'; markDirty()">保密</button>
                  </div>
                </div>
                <div class="field-row">
                  <label class="field-label">账号</label>
                  <input :value="form.username" class="field-input" readonly disabled />
                </div>
                <div class="field-row">
                  <label class="field-label">密码</label>
                  <div class="password-section">
                    <div class="password-input-wrap">
                      <input :type="showPassword ? 'text' : 'password'" v-model="passwordForm.oldPassword" class="field-input" placeholder="旧密码" />
                    </div>
                    <div class="password-input-wrap">
                      <input :type="showPassword ? 'text' : 'password'" v-model="passwordForm.newPassword" class="field-input" placeholder="新密码（留空不修改）" @input="markDirty" />
                      <span class="password-toggle" @click="showPassword = !showPassword">{{ showPassword ? '🙈' : '👁️' }}</span>
                    </div>
                  </div>
                </div>
                <div class="field-row">
                  <label class="field-label">邮箱</label>
                  <input :value="form.email" class="field-input" readonly disabled />
                </div>
                <div class="field-row">
                  <label class="field-label">生日</label>
                  <input type="date" v-model="form.birthday" class="field-input" @change="markDirty" />
                </div>
                <div class="field-row">
                  <label class="field-label">签名</label>
                  <textarea v-model="form.signature" class="field-textarea" placeholder="写一句个性签名..." maxlength="200" @input="markDirty" />
                </div>
              </div>

              <transition name="slide-up">
                <div v-if="isDirty" class="confirm-bar">
                  <button class="nature-btn nature-btn-primary" @click="handleSave" :disabled="saving">
                    {{ saving ? '保存中...' : '确认更改' }}
                  </button>
                </div>
              </transition>

              <div class="danger-zone">
                <div class="danger-divider"><span>危险操作</span></div>
                <div v-if="!showDeleteConfirm" class="danger-trigger" @click="showDeleteConfirm = true">
                  <span>注销账号</span>
                  <span class="arrow">→</span>
                </div>
                <div v-else class="delete-confirm">
                  <p class="delete-warning">注销后所有数据将无法恢复</p>
                  <div class="delete-code-row">
                    <input v-model="deleteCode" class="field-input" placeholder="输入邮箱验证码" maxlength="6" />
                    <button class="code-btn" @click="sendDeleteCode" :disabled="deleteCodeSending || deleteCountdown > 0">
                      {{ deleteCountdown > 0 ? `${deleteCountdown}s` : '获取' }}
                    </button>
                  </div>
                  <div class="delete-btns">
                    <button class="nature-btn nature-btn-danger" @click="handleDelete" :disabled="deleting">确认注销</button>
                    <button class="nature-btn nature-btn-ghost" @click="showDeleteConfirm = false">取消</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import { userProfileApi, resourceApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false }
})
const emit = defineEmits(['close'])

const userStore = useUserStore()
const isDirty = ref(false)
const saving = ref(false)
const showPassword = ref(false)
const showDeleteConfirm = ref(false)
const deleteCode = ref('')
const deleteCodeSending = ref(false)
const deleteCountdown = ref(0)
const deleting = ref(false)
const avatarInput = ref(null)
let deleteTimer = null

const form = reactive({
  username: '', nickname: '', email: '', avatar: '', gender: '保密', birthday: '', signature: ''
})
const passwordForm = reactive({ oldPassword: '', newPassword: '' })

watch(() => props.visible, async (v) => {
  if (v) {
    try {
      const data = await userProfileApi.get()
      Object.assign(form, {
        username: data.username || '',
        nickname: data.nickname || '',
        email: data.email || '',
        avatar: data.avatar || '',
        gender: data.gender || '保密',
        birthday: data.birthday || '',
        signature: data.signature || ''
      })
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      isDirty.value = false
      showDeleteConfirm.value = false
      deleteCode.value = ''
    } catch (e) { ElMessage.error('加载个人信息失败') }
  }
})

function markDirty() { isDirty.value = true }

function triggerAvatarUpload() { avatarInput.value?.click() }

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const data = await resourceApi.upload(file)
    form.avatar = data.path || data.url || ''
    markDirty()
  } catch (e) { ElMessage.error('头像上传失败') }
}

async function handleSave() {
  saving.value = true
  try {
    const updateData = {
      nickname: form.nickname,
      gender: form.gender,
      birthday: form.birthday,
      signature: form.signature,
      avatar: form.avatar
    }
    await userStore.updateProfile(updateData)
    if (passwordForm.newPassword) {
      if (!passwordForm.oldPassword) { ElMessage.warning('请输入旧密码'); saving.value = false; return }
      await userProfileApi.changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      ElMessage.success('密码已更新')
    }
    isDirty.value = false
    ElMessage.success('信息已更新')
  } catch (e) { ElMessage.error(e?.message || '更新失败') }
  finally { saving.value = false }
}

async function sendDeleteCode() {
  deleteCodeSending.value = true
  try {
    await userProfileApi.sendDeleteCode()
    ElMessage.success('验证码已发送')
    deleteCountdown.value = 60
    deleteTimer = setInterval(() => {
      deleteCountdown.value--
      if (deleteCountdown.value <= 0) clearInterval(deleteTimer)
    }, 1000)
  } catch (e) { ElMessage.error(e?.message || '发送失败') }
  finally { deleteCodeSending.value = false }
}

async function handleDelete() {
  if (!deleteCode.value) { ElMessage.warning('请输入验证码'); return }
  try {
    await ElMessageBox.confirm('确认注销账号？此操作不可恢复！', '最后确认', {
      confirmButtonText: '确认注销', cancelButtonText: '取消', type: 'error', center: true
    })
    deleting.value = true
    await userProfileApi.deleteAccount(deleteCode.value)
    await userStore.logout()
    ElMessage.success('账号已注销')
    emit('close')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '注销失败')
  } finally { deleting.value = false }
}
</script>

<style scoped>
.profile-overlay {
  position: fixed; inset: 0; z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.35);
  backdrop-filter: blur(8px);
}

.profile-card {
  position: relative;
  width: 420px; max-width: 92vw; max-height: 90vh; overflow-y: auto;
  background: linear-gradient(160deg, rgba(255,255,255,0.88) 0%, rgba(232,245,233,0.92) 50%, rgba(200,230,201,0.88) 100%);
  backdrop-filter: blur(24px);
  border-radius: 32px;
  border: 1.5px solid rgba(255,255,255,0.5);
  box-shadow: 0 8px 40px rgba(56,142,60,0.15), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.6);
  font-family: var(--trendy-font);
}
.profile-card::-webkit-scrollbar { width: 4px; }
.profile-card::-webkit-scrollbar-thumb { background: rgba(76,175,80,0.3); border-radius: 2px; }

.card-glow {
  position: absolute; top: -30%; left: -30%;
  width: 160%; height: 160%;
  background: radial-gradient(circle at 30% 20%, rgba(129,199,132,0.15) 0%, transparent 50%),
              radial-gradient(circle at 70% 60%, rgba(255,183,77,0.1) 0%, transparent 50%);
  pointer-events: none;
}

.close-btn {
  position: absolute; top: 16px; right: 16px; z-index: 10;
  width: 36px; height: 36px; border-radius: 50%;
  border: none; background: rgba(255,255,255,0.7);
  color: #888; font-size: 16px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(8px);
}
.close-btn:hover { background: #ff5252; color: #fff; transform: rotate(90deg); }

.profile-inner { position: relative; padding: 40px 32px 28px; z-index: 1; }

.profile-avatar-section {
  display: flex; flex-direction: column; align-items: center; margin-bottom: 24px;
}
.avatar-wrap {
  position: relative; cursor: pointer; border-radius: 50%;
  transition: transform 0.3s ease;
}
.avatar-wrap:hover { transform: scale(1.06); }
.avatar-wrap:hover .avatar-overlay { opacity: 1; }
.profile-avatar { border: 3px solid #81c784; box-shadow: 0 4px 16px rgba(76,175,80,0.25); }
.avatar-overlay {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,0.35);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.3s ease;
  font-size: 28px;
}
.profile-nickname {
  margin: 12px 0 0; font-size: 22px; font-weight: 700; color: #2e7d32;
}

.profile-fields { display: flex; flex-direction: column; gap: 14px; margin-bottom: 16px; }

.field-row { display: flex; align-items: center; gap: 12px; }
.field-label {
  width: 48px; flex-shrink: 0;
  font-size: 13px; font-weight: 600; color: #689f63;
  text-align: right;
}
.field-input {
  flex: 1; border: 1.5px solid rgba(129,199,132,0.3);
  background: rgba(255,255,255,0.6);
  border-radius: 14px; padding: 10px 14px;
  font-size: 14px; color: #333; outline: none;
  font-family: var(--trendy-font); font-weight: 500;
  transition: all 0.3s ease;
}
.field-input:focus { border-color: #66bb6a; box-shadow: 0 0 0 3px rgba(76,175,80,0.1); background: #fff; }
.field-input:disabled, .field-input[readonly] { background: rgba(0,0,0,0.03); color: #999; cursor: not-allowed; }

.field-textarea {
  flex: 1; border: 1.5px solid rgba(129,199,132,0.3);
  background: rgba(255,255,255,0.6);
  border-radius: 14px; padding: 10px 14px;
  font-size: 14px; color: #333; outline: none;
  font-family: var(--trendy-font); font-weight: 500;
  resize: none; height: 60px;
  transition: all 0.3s ease;
}
.field-textarea:focus { border-color: #66bb6a; box-shadow: 0 0 0 3px rgba(76,175,80,0.1); background: #fff; }

.gender-toggles { display: flex; gap: 6px; flex: 1; }
.gender-btn {
  flex: 1; border: 1.5px solid rgba(129,199,132,0.3);
  background: rgba(255,255,255,0.5);
  border-radius: 14px; padding: 8px 8px;
  font-size: 13px; font-weight: 600; color: #888; cursor: pointer;
  font-family: var(--trendy-font);
  transition: all 0.25s ease;
}
.gender-btn:hover { border-color: #81c784; color: #66bb6a; }
.gender-btn.active {
  background: var(--nature-gradient-soft);
  border-color: #66bb6a; color: #2e7d32;
  box-shadow: 0 2px 8px rgba(76,175,80,0.15);
}

.password-section { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.password-input-wrap { position: relative; flex: 1; }
.password-input-wrap .field-input { width: 100%; padding-right: 40px; }
.password-toggle {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  cursor: pointer; font-size: 16px; user-select: none;
  transition: transform 0.2s;
}
.password-toggle:hover { transform: translateY(-50%) scale(1.2); }

.confirm-bar {
  margin: 16px 0; text-align: center;
}
.nature-btn {
  border: none; outline: none;
  font-size: 15px; font-weight: 600;
  padding: 12px 28px; border-radius: 18px; cursor: pointer;
  font-family: var(--trendy-font);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.nature-btn-primary {
  background: var(--nature-gradient);
  color: #fff;
  box-shadow: 0 4px 16px rgba(76,175,80,0.35);
}
.nature-btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(76,175,80,0.4);
}
.nature-btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }
.nature-btn-ghost {
  background: rgba(255,255,255,0.5); color: #689f63;
  border: 1.5px solid rgba(129,199,132,0.4);
  padding: 10px 20px;
}
.nature-btn-ghost:hover { background: rgba(255,255,255,0.9); border-color: #81c784; }
.nature-btn-danger {
  background: linear-gradient(135deg, #ef5350, #e53935);
  color: #fff; border: none;
  box-shadow: 0 4px 16px rgba(229,57,53,0.3);
}
.nature-btn-danger:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(229,57,53,0.4);
}
.nature-btn-danger:disabled { opacity: 0.7; cursor: not-allowed; }

.danger-zone { margin-top: 24px; padding-top: 8px; }
.danger-divider {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 12px;
}
.danger-divider::before, .danger-divider::after {
  content: ''; flex: 1; height: 1px;
  background: rgba(0,0,0,0.08);
}
.danger-divider span { font-size: 12px; color: #ccc; font-weight: 500; }
.danger-trigger {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255,82,82,0.06);
  border: 1.5px solid rgba(255,82,82,0.15);
  border-radius: 14px; cursor: pointer;
  font-size: 14px; color: #ef5350; font-weight: 600;
  transition: all 0.3s ease;
}
.danger-trigger:hover { background: rgba(255,82,82,0.12); border-color: rgba(255,82,82,0.3); }
.danger-trigger .arrow { font-size: 16px; transition: transform 0.3s; }
.danger-trigger:hover .arrow { transform: translateX(4px); }

.delete-confirm { padding: 4px 0; }
.delete-warning { font-size: 13px; color: #ef5350; margin: 0 0 10px; }
.delete-code-row { display: flex; gap: 8px; margin-bottom: 10px; }
.delete-code-row .field-input { flex: 1; }
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
.delete-btns { display: flex; gap: 8px; }

.card-fade-enter-active, .card-fade-leave-active { transition: opacity 0.3s ease; }
.card-fade-enter-from, .card-fade-leave-to { opacity: 0; }
.card-pop-enter-active { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.card-pop-leave-active { transition: all 0.2s ease-in; }
.card-pop-enter-from { opacity: 0; transform: scale(0.85) translateY(20px); }
.card-pop-leave-to { opacity: 0; transform: scale(0.9) translateY(10px); }
.slide-up-enter-active { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.slide-up-leave-active { transition: all 0.2s ease-in; }
.slide-up-enter-from { opacity: 0; transform: translateY(16px); }
.slide-up-leave-to { opacity: 0; transform: translateY(8px); }
</style>
