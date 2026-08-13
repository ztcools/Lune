<template>
  <div>
    <div class="config-header">
      <h3>⚙️ Agent 配置</h3>
      <button class="config-close" @click="$emit('close')">×</button>
    </div>
    <div class="config-body">
      <div class="config-field">
        <label>API 地址</label>
        <el-input v-model="form.baseUrl" placeholder="https://aigw.phigent.cn" />
        <p class="field-hint">自定义网关地址，不含 /v1 后缀</p>
      </div>
      <div class="config-field">
        <label>模型名称</label>
        <el-input v-model="form.model" placeholder="deepseek/deepseek-v4-flash" />
      </div>
      <div class="config-field">
        <label>API Key</label>
        <el-input
          v-model="form.apiKey"
          :type="showKey ? 'text' : 'password'"
          placeholder="sk-..."
          show-password
        />
      </div>
      <div class="config-actions">
        <el-button @click="testConnection" :loading="testing" plain>测试连接</el-button>
        <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
      </div>
      <p v-if="testResult" :class="['test-result', testResult.success ? 'success' : 'fail']">
        {{ testResult.success ? '✅ 连接成功' : '❌ ' + testResult.message }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { agentApi } from '../../api/modules'

const emit = defineEmits(['close', 'saved'])

const form = reactive({
  baseUrl: 'https://aigw.phigent.cn',
  model: 'deepseek/deepseek-v4-flash',
  apiKey: ''
})

const showKey = ref(false)
const saving = ref(false)
const testing = ref(false)
const testResult = ref(null)

onMounted(async () => {
  try {
    const config = await agentApi.getConfig()
    if (config) {
      if (config.baseUrl) form.baseUrl = config.baseUrl
      if (config.model) form.model = config.model
      // 后端返回的是脱敏占位符（sk-****xxxx），不回填，避免误用/误存
      if (config.apiKey && !config.apiKey.startsWith('sk-****')) form.apiKey = config.apiKey
    }
  } catch (e) { /* use defaults */ }
})

async function testConnection() {
  if (!form.apiKey) {
    testResult.value = { success: false, message: '请先输入 API Key' }
    return
  }
  testing.value = true
  testResult.value = null
  try {
    const url = form.baseUrl.replace(/\/+$/, '') + '/v1/chat/completions'
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + form.apiKey },
      body: JSON.stringify({
        model: form.model,
        messages: [{ role: 'user', content: 'hi' }],
        max_tokens: 5,
        stream: false
      })
    })
    if (resp.ok) {
      testResult.value = { success: true }
    } else {
      const txt = await resp.text()
      testResult.value = { success: false, message: resp.status + ': ' + txt.substring(0, 100) }
    }
  } catch (e) {
    testResult.value = { success: false, message: e.message }
  } finally {
    testing.value = false
  }
}

async function saveConfig() {
  saving.value = true
  try {
    await agentApi.saveConfig({
      baseUrl: form.baseUrl.replace(/\/+$/, ''),
      model: form.model,
      apiKey: form.apiKey
    })
    ElMessage.success('配置已保存')
    emit('saved')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.config-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 20px; border-bottom: 1px solid #eee;
}
.config-header h3 { margin: 0; font-size: 16px; font-weight: 600; }
.config-close {
  width: 30px; height: 30px; border-radius: 8px;
  border: none; background: #f5f5f5; font-size: 16px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
}
.config-close:hover { background: #e8e8e8; }
.config-body { padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.config-field { display: flex; flex-direction: column; gap: 6px; }
.config-field label { font-size: 13px; font-weight: 600; color: #555; }
.field-hint { margin: 0; font-size: 11px; color: #aaa; }
.config-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 4px; }
.test-result { font-size: 13px; margin: 0; text-align: center; padding: 8px; border-radius: 8px; }
.test-result.success { color: #43a047; background: #f0faf0; }
.test-result.fail { color: #e74c3c; background: #fef0f0; }
</style>
