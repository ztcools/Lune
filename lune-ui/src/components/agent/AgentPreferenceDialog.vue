<template>
  <div class="pref-panel">
    <div class="pref-tabs">
      <button v-for="tab in tabs" :key="tab.key" :class="['pref-tab', { active: activeTab === tab.key }]" @click="activeTab = tab.key">
        {{ tab.label }}
      </button>
    </div>

    <div class="pref-body" v-if="activeTab === 'article'">
      <div class="pref-field">
        <label>写作风格</label>
        <textarea v-model="prefs.article_style" placeholder="例如：简洁技术笔记、深度长文..." class="pref-textarea" rows="2"></textarea>
      </div>
      <div class="pref-field">
        <label>自动拟标题</label>
        <select v-model="prefs.article_auto_title" class="pref-select">
          <option value="true">开启</option>
          <option value="false">关闭</option>
        </select>
      </div>
    </div>

    <div class="pref-body" v-if="activeTab === 'essay'">
      <div class="pref-field">
        <label>常驻城市</label>
        <input v-model="prefs.essay_default_location" placeholder="广州" class="pref-input" />
      </div>
      <div class="pref-field">
        <label>写作风格</label>
        <textarea v-model="prefs.essay_style" placeholder="例如：朋友圈式，轻松口语" class="pref-textarea" rows="2"></textarea>
      </div>
    </div>

    <div class="pref-body" v-if="activeTab === 'record'">
      <div class="pref-field">
        <label>写作风格</label>
        <textarea v-model="prefs.record_style" placeholder="例如：短评，100字内" class="pref-textarea" rows="2"></textarea>
      </div>
    </div>

    <div class="pref-body" v-if="activeTab === 'work'">
      <div class="pref-field">
        <label>写作模板</label>
        <select v-model="prefs.work_template" class="pref-select">
          <option value="STAR法则">STAR法则（情境/任务/行动/结果）</option>
          <option value="时间线">时间线</option>
          <option value="项目驱动">项目驱动</option>
        </select>
      </div>
    </div>

    <div class="pref-body" v-if="activeTab === 'project'">
      <div class="pref-field">
        <label>写作模板</label>
        <select v-model="prefs.project_template" class="pref-select">
          <option value="README式">README式（名/简介/技术栈/亮点）</option>
          <option value="案例式">案例式（背景/方案/结果）</option>
          <option value="技术文档">技术文档</option>
        </select>
      </div>
    </div>

    <div class="pref-footer">
      <button class="pref-save-btn" :class="{ saving }" :disabled="saving" @click="handleSave">
        <span v-if="saving">保存中...</span>
        <span v-else>保存</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { agentApi } from '../../api/modules'
import { ElMessage } from 'element-plus'

const tabs = [
  { key: 'article', label: '文章' },
  { key: 'essay', label: '随笔' },
  { key: 'record', label: '记录' },
  { key: 'work', label: '工作' },
  { key: 'project', label: '项目' },
]

const activeTab = ref('article')
const saving = ref(false)
const prefs = ref({
  article_style: '',
  article_auto_title: 'true',
  essay_default_location: '广州',
  essay_style: '',
  record_style: '',
  work_template: 'STAR法则',
  project_template: 'README式',
})

onMounted(async () => {
  try {
    const res = await agentApi.getPreferences()
    if (res) {
      for (const [k, v] of Object.entries(res)) {
        // 仅对 null/undefined/空串保持默认，保留合法的 false/'0' 等假值语义
        if (v !== null && v !== undefined && v !== '') prefs.value[k] = v
      }
    }
  } catch (e) { /* 使用默认值 */ }
})

async function handleSave() {
  saving.value = true
  try {
    await agentApi.savePreferences(prefs.value)
    ElMessage.success('偏好已保存')
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.message || ''))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.pref-panel { display: flex; flex-direction: column; min-height: 260px; }
.pref-tabs { display: flex; gap: 6px; margin-bottom: 18px; border-bottom: 1px solid #e8efe8; padding-bottom: 10px; }
.pref-tab {
  padding: 6px 16px; border: none; border-radius: 18px;
  background: #f2f5f2; color: #7a8a7a; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; font-family: var(--trendy-font);
}
.pref-tab:hover { background: #e0ece0; }
.pref-tab.active { background: var(--nature-green); color: #fff; }
.pref-body { flex: 1; }
.pref-field { margin-bottom: 14px; }
.pref-field label { display: block; font-size: 13px; font-weight: 600; color: #4a5a4a; margin-bottom: 4px; }
.pref-input, .pref-select, .pref-textarea {
  width: 100%; padding: 8px 12px; border: 1px solid #dce4dc; border-radius: 10px;
  font-size: 14px; font-family: inherit; background: #fafcfa; color: #3a4a3a;
  box-sizing: border-box; outline: none; transition: border-color 0.2s;
}
.pref-input:focus, .pref-select:focus, .pref-textarea:focus { border-color: var(--nature-green-light); }
.pref-textarea { resize: vertical; }
.pref-footer { display: flex; justify-content: flex-end; padding-top: 12px; border-top: 1px solid #e8efe8; margin-top: 8px; }
.pref-save-btn {
  padding: 9px 28px; border: none; border-radius: 22px;
  background: var(--nature-gradient); color: #fff; font-size: 14px; font-weight: 700;
  font-family: var(--trendy-font); cursor: pointer; transition: all 0.3s;
}
.pref-save-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(76,175,80,0.4); }
.pref-save-btn.saving { opacity: 0.6; cursor: not-allowed; }
</style>
