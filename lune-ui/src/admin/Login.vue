<template>
  <div class="myCenter verify-container">
    <div class="verify-content">
      <div><el-avatar :size="50" src="/assets/logo.jpg" /></div>
      <div><el-input v-model="account"><template #prepend>账号</template></el-input></div>
      <div><el-input v-model="password" type="password" @keyup.enter="login"><template #prepend>密码</template></el-input></div>
      <div><el-button type="primary" style="width:100%" @click="login" :loading="loading">提交</el-button></div>
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
  if (!account.value || !password.value) { ElMessage.error('请输入账号和密码'); return }
  localStorage.removeItem('token'); localStorage.removeItem('user')
  loading.value = true
  try {
    const data = await userStore.login(account.value, password.value)
    if (data.role !== 'ADMIN') { ElMessage.error('无管理员权限'); return }
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/admin')
  } catch(e) { ElMessage.error('登录失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.verify-container{height:100vh;background:url('/assets/背景1.jpg') center center / cover no-repeat}
.verify-content{background:var(--maxWhiteMask);padding:30px 40px 5px;position:relative}
.verify-content>div:first-child{position:absolute;left:50%;transform:translate(-50%);top:-25px}
.verify-content>div:not(:first-child){margin:25px 0}
</style>
