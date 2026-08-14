<template>
  <div ref="pageRoot" class="agent-page" @dragover.prevent="dragOver = true" @dragleave="onDragLeave" @drop.prevent="onDrop">
    <div v-if="dragOver" class="drop-overlay"><div class="drop-hint">松开以上传图片</div></div>
    <!-- Header -->
    <div class="agent-header">
      <div class="agent-header-left">
        <div class="agent-avatar">🤖</div>
        <span class="agent-name">Luna</span>
      </div>
      <div class="agent-header-right">
        <button class="agent-pref-btn" @click="showPref = true" title="偏好设置">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20V10M18 20V4M6 20v-4"/></svg>
        </button>
        <button class="agent-setting-btn" @click="showConfig = true" title="Agent 配置">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
        </button>
      </div>
    </div>

    <!-- Messages -->
    <div class="agent-messages" ref="msgContainer">
      <div v-if="visibleMessages.length === 0 && !loading" class="agent-empty">
        <div class="empty-icon">💬</div>
        <p class="empty-title">和 Luna 聊聊吧</p>
      </div>

      <div v-for="(msg, i) in visibleMessages" :key="msg._k || i" class="msg-wrapper">
        <!-- User -->
        <div v-if="msg.role === 'user'" class="msg-row msg-user">
          <div class="msg-bubble msg-bubble-user">{{ msg.content }}</div>
        </div>
        <!-- Assistant -->
        <div v-else-if="msg.role === 'assistant'" class="msg-row msg-assistant">
          <div class="msg-bubble msg-bubble-assistant" v-html="renderMd(msg.content)"></div>
        </div>
        <!-- Tool result card -->
        <div v-else-if="msg.role === 'tool'" class="msg-tool-card">
          <ToolResultCard
            :toolName="msg.toolName"
            :result="msg.toolResult"
            @publish="handlePublish(msg.toolResult)"
            @discard="handleDiscard(msg.toolName, msg.toolResult)"
            @view="openArticle(msg.toolResult.preview)"
          />
        </div>
      </div>

      <!-- Typing -->
      <div v-if="streaming" class="msg-row msg-assistant">
        <div class="msg-bubble msg-bubble-assistant typing-bubble">
          <span class="typing-dot"></span><span class="typing-dot"></span><span class="typing-dot"></span>
        </div>
      </div>
    </div>

    <!-- Input -->
    <div class="agent-input-area">
      <AgentImageQueue :images="pendingImages" @remove="removeImage" />
      <div class="agent-toolbar">
        <div class="toolbar-left">
          <label class="upload-btn" title="上传图片">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/></svg>
            <input type="file" accept="image/*" multiple hidden @change="onFileSelect" />
          </label>
          <button v-for="s in shortcuts" :key="s.key"
            :class="['shortcut-btn', { active: activeDomain === s.key }]"
            @click="toggleDomain(s.key)" :title="s.title">{{ s.label }}</button>
        </div>
        <div class="toolbar-actions">
          <label class="toolbar-toggle" :class="{ active: contextEnabled }" @click="toggleContext">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
            <span>记忆</span>
          </label>
          <button class="toolbar-btn" @click="clearHistory" title="清空对话">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
          </button>
        </div>
      </div>
      <div class="input-row">
        <textarea ref="inputRef" v-model="inputText" class="agent-input"
          :placeholder="placeholderText" rows="1"
          @keydown.enter.exact.prevent="handleSend"
          @input="autoResize"
        ></textarea>
        <button class="send-btn" :class="{ active: inputText.trim() && !loading, stop: loading }"
          @click="loading ? cancelRequest() : handleSend()" :disabled="!inputText.trim() && !loading">
          <svg v-if="loading" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><rect x="4" y="4" width="16" height="16" rx="2"/></svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
        </button>
      </div>
    </div>

    <!-- Config Dialog -->
    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="showConfig" class="config-overlay" @click.self="showConfig = false">
          <div class="config-dialog">
            <AgentConfigDialog @close="showConfig = false" @saved="showConfig = false" />
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Preferences Dialog -->
    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="showPref" class="config-overlay" @click.self="showPref = false">
          <div class="config-dialog">
            <div class="config-dialog-header">
              <h3>写作偏好</h3>
              <button class="config-close" @click="showPref = false">×</button>
            </div>
            <AgentPreferenceDialog />
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Article preview modal -->
    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="previewArticle" class="config-overlay" @click.self="previewArticle = null">
          <div class="preview-modal">
            <div class="preview-modal-header">
              <h2>{{ previewArticle.title }}</h2>
              <button @click="previewArticle = null" class="config-close">×</button>
            </div>
            <div class="preview-modal-body" v-html="sanitizeHtml(previewArticle.content)"></div>
            <div class="preview-modal-footer">
              <span v-if="previewArticle.createTime">📅 {{ previewArticle.createTime?.substring(0, 10) }}</span>
              <span>状态：{{ previewArticle.status === 0 ? '草稿' : '已发布' }}</span>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { agentApi, articleApi } from '../api/modules'
import AgentConfigDialog from '../components/agent/AgentConfigDialog.vue'
import AgentPreferenceDialog from '../components/agent/AgentPreferenceDialog.vue'
import AgentImageQueue from '../components/agent/AgentImageQueue.vue'
import ToolResultCard from '../components/agent/ToolResultCard.vue'

let _kid = 0
let _imgId = 0
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const streaming = ref(false)
const contextEnabled = ref(true)
const showConfig = ref(false)
const showPref = ref(false)
const previewArticle = ref(null)
const msgContainer = ref(null)
const pageRoot = ref(null)
const inputRef = ref(null)
let abortController = null

const visibleMessages = computed(() =>
  messages.value.filter(m => {
    if (m.role === 'user' && !m.content) return false
    if (m.role === 'assistant' && !m.content) return false
    if (m.role === 'tool' && !m.toolResult) return false
    return true
  })
)

const shortcuts = [
  { key: 'article', label: '@文章', title: '发文/更新文章', placeholder: '输入文章内容...' },
  { key: 'essay', label: '@随笔', title: '发随笔/朋友圈', placeholder: '记录此刻的想法...' },
  { key: 'record', label: '@记录', title: '新建记录', placeholder: '打卡/收藏/记录...' },
  { key: 'work', label: '@工作', title: '添加工作经历', placeholder: '公司+职位+开始日期...' },
  { key: 'project', label: '@项目', title: '添加项目经历', placeholder: '项目名+简介...' },
]

const activeDomain = ref(null)
const pendingImages = ref([])
const dragOver = ref(false)

const placeholderText = computed(() => {
  const s = shortcuts.find(s => s.key === activeDomain.value)
  return s ? s.placeholder : '输入消息...'
})

function toggleDomain(key) {
  activeDomain.value = activeDomain.value === key ? null : key
  inputRef.value?.focus()
}

function removeImage(idx) {
  pendingImages.value.splice(idx, 1)
}

/** 图片上传通用方法：blob → base64 → upload → queue */
async function queueImage(blob, name) {
  if (pendingImages.value.length >= 9) {
    ElMessage.warning('最多上传9张图片')
    return
  }
  if (blob.size > 50 * 1024 * 1024) { ElMessage.error('图片过大（最多50MB）'); return }
  const tempUrl = URL.createObjectURL(blob)
  const id = ++_imgId
  pendingImages.value.push({ id, url: tempUrl, name, uploading: true })

  const reader = new FileReader()
  reader.onload = async () => {
    try {
      const resp = await fetch('/api/admin/resources/upload-base64', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') },
        body: JSON.stringify({ base64: reader.result, filename: name || ('img-' + Date.now() + '.jpg') })
      })
      const json = await resp.json()
      // 用稳定 id 定位，避免上传期间移除其它图片导致索引错位
      const i = pendingImages.value.findIndex(x => x.id === id)
      if (i === -1) return
      if (resp.ok && json.code === 200 && json.data) {
        pendingImages.value[i] = { id, url: json.data.path, name: json.data.filename || name, uploading: false }
      } else {
        pendingImages.value.splice(i, 1)
        ElMessage.error('上传失败')
      }
    } catch (err) {
      const i = pendingImages.value.findIndex(x => x.id === id)
      if (i !== -1) pendingImages.value.splice(i, 1)
      ElMessage.error('上传失败: ' + (err.message || ''))
    } finally {
      URL.revokeObjectURL(tempUrl)
    }
  }
  reader.onerror = () => {
    const i = pendingImages.value.findIndex(x => x.id === id)
    if (i !== -1) pendingImages.value.splice(i, 1)
    URL.revokeObjectURL(tempUrl)
  }
  reader.readAsDataURL(blob)
}

function onFileSelect(e) {
  const files = e.target.files
  if (!files?.length) return
  Array.from(files).forEach(f => queueImage(f, f.name))
  e.target.value = ''
}

function onDrop(e) {
  dragOver.value = false
  const files = e.dataTransfer?.files
  if (!files?.length) return
  Array.from(files).filter(f => f.type.startsWith('image/')).forEach(f => queueImage(f, f.name))
}

/** 仅在真正离开容器时隐藏遮罩，避免经过子元素误触发 dragleave 导致闪烁。 */
function onDragLeave(e) {
  if (e.currentTarget.contains(e.relatedTarget)) return
  dragOver.value = false
}

async function handlePaste(e) {
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      e.preventDefault()
      queueImage(item.getAsFile(), 'clipboard-' + Date.now() + '.jpg')
    }
  }
}

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}

const mdCache = new Map()
function renderMd(text) {
  if (!text) return ''
  const hit = mdCache.get(text)
  if (hit !== undefined) return hit
  const html = text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.+?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
  // 防止流式期间每个 token 累积出无界中间态；超阈值清空重来
  if (mdCache.size > 2000) mdCache.clear()
  mdCache.set(text, html)
  return html
}

/** 富文本轻量消毒（纵深防御）：剥离 script/iframe/事件属性/javascript: 链接。 */
function sanitizeHtml(html) {
  if (!html) return ''
  return html
    .replace(/<\s*script[^>]*>[\s\S]*?<\s*\/\s*script\s*>/gi, '')
    .replace(/<\s*\/?\s*(iframe|object|embed|link|style|meta)\b[^>]*>/gi, '')
    .replace(/\son\w+\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi, '')
    .replace(/(href|src)\s*=\s*["']?\s*javascript:[^"'\s>]*["']?/gi, '')
}

function addMsg(msg) {
  msg._k = ++_kid
  messages.value.push(msg)
}

async function handleSend() {
  let msg = inputText.value.trim()
  // 跳过仅空白+图片的情况
  if (!msg && pendingImages.value.filter(i => !i.uploading).length === 0) return
  if (loading.value) return

  // @域名前缀
  if (activeDomain.value) {
    const s = shortcuts.find(s => s.key === activeDomain.value)
    msg = (s ? s.label + ' ' : '') + msg
  }

  // 拼接图片到消息
  const ready = pendingImages.value.filter(i => !i.uploading)
  if (ready.length > 0) {
    msg += '\n' + ready.map((img, idx) => '[图片' + (idx + 1) + '] ' + img.url).join('\n')
  }

  inputText.value = ''
  // 保留仍在上传中的图片，只清空已完成的
  pendingImages.value = pendingImages.value.filter(i => i.uploading)
  activeDomain.value = null
  if (inputRef.value) inputRef.value.style.height = 'auto'

  addMsg({ role: 'user', content: msg })
  scrollToBottom()

  loading.value = true
  streaming.value = true

  abortController = new AbortController()
  try {
    const response = await agentApi.chatStream(msg, abortController.signal)
    if (!response.ok) {
      const txt = await response.text()
      addMsg({ role: 'assistant', content: '请求失败: HTTP ' + response.status + (txt ? ' ' + txt.substring(0, 200) : '') })
      loading.value = false; streaming.value = false; return
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      buffer = parseSSE(buffer)
    }
    // 收尾：flush 剩余 buffer + finalize 多字节字符（避免尾部事件/字符丢失）
    buffer += decoder.decode()
    parseSSE(buffer)
  } catch (e) {
    if (e.name !== 'AbortError') {
      ElMessage.error('连接失败: ' + e.message)
      addMsg({ role: 'assistant', content: '连接失败，请检查 API Key 配置或网络' })
    }
  } finally {
    loading.value = false
    streaming.value = false
    abortController = null
    scrollToBottom()
  }
}

function cancelRequest() {
  if (abortController) {
    abortController.abort()
    abortController = null
    loading.value = false
    streaming.value = false
    addMsg({ role: 'assistant', content: '⏹ 已取消' })
    scrollToBottom()
  }
}

/** 解析 SSE buffer：处理完整事件（以双换行分隔），返回未消费的尾部 buffer。 */
function parseSSE(buffer) {
  const parts = buffer.split(/\r?\n\r?\n/)
  const rest = parts.pop() || ''
  for (const block of parts) {
    if (!block.trim()) continue
    for (const line of block.split(/\r?\n/)) {
      const trimmed = line.trim()
      if (!trimmed.startsWith('data:')) continue
      // 兼容 "data:{json}" 与 "data: {json}"
      const payload = trimmed[5] === ' ' ? trimmed.slice(6) : trimmed.slice(5)
      if (!payload) continue
      try { handleSSE(JSON.parse(payload)) } catch (e) { /* skip malformed */ }
    }
  }
  return rest
}

function handleSSE(data) {
  switch (data.type) {
    case 'text': {
      if (!data.data?.content) break
      const msgs = messages.value
      const last = msgs[msgs.length - 1]
      if (last && last.role === 'assistant') {
        last.content += data.data.content
      } else {
        addMsg({ role: 'assistant', content: data.data.content })
      }
      streaming.value = false
      scrollToBottom()
      break
    }
    case 'tool_call':
      streaming.value = false
      break
    case 'tool_result':
      addMsg({ role: 'tool', toolName: data.data._toolName || data.data.toolName, toolResult: data.data })
      scrollToBottom()
      break
    case 'done':
      streaming.value = false
      break
    case 'error': {
      streaming.value = false
      const msgs = messages.value
      const last = msgs[msgs.length - 1]
      if (last && last.role === 'assistant' && !last.content) msgs.pop()
      const err = typeof data.data === 'string' ? data.data : (data.data?.message || '未知错误')
      addMsg({ role: 'assistant', content: '⚠️ ' + err })
      break
    }
  }
}

function openArticle(article) {
  if (article) previewArticle.value = article
}

async function handlePublish(toolResult) {
  const article = toolResult?.preview
  if (!article?.id) return
  try {
    await articleApi.update(article.id, {
      title: article.title, content: article.content,
      summary: article.summary, cover: article.cover,
      categoryId: article.categoryId, status: 1
    })
    // Update the preview status in-place so the card reflects it
    if (toolResult.preview) toolResult.preview.status = 1
    ElMessage.success('已发布')
    addMsg({ role: 'assistant', content: '✅《' + article.title + '》已发布' })
    scrollToBottom()
  } catch (e) { ElMessage.error('发布失败: ' + (e.message || '')) }
}

async function handleDiscard(toolName, toolResult) {
  const id = toolResult?.preview?.id
  if (!id || !toolName) return
  // 目前只有文章支持从工具卡片直接删除；其它类型不误报删除结果
  if (!toolName.includes('article')) {
    ElMessage.info('该类型暂不支持在聊天中删除')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除吗？', '确认', { type: 'warning' })
    await articleApi.delete(id)
    addMsg({ role: 'assistant', content: '已删除' })
    scrollToBottom()
  } catch (e) { /* cancelled or error */ }
}

async function toggleContext() {
  contextEnabled.value = !contextEnabled.value
  try { await agentApi.setContext(contextEnabled.value) } catch (e) { /* ignore */ }
}

async function clearHistory() {
  // 先中止当前请求
  if (abortController) {
    abortController.abort()
    abortController = null
    loading.value = false
    streaming.value = false
  }
  try {
    await agentApi.clearHistory()
    messages.value = []
    ElMessage.success('已清空')
  } catch (e) { /* ignore */ }
}

onMounted(async () => {
  pageRoot.value?.addEventListener('paste', handlePaste)
  try {
    // 读回服务端真实的记忆开关状态，而不是无条件覆盖为 true
    try {
      const ctx = await agentApi.getContext()
      contextEnabled.value = ctx !== undefined ? ctx : true
    } catch (e) { contextEnabled.value = true }

    const res = await agentApi.getHistory()
    // getHistory returns empty array when no history or server error — that's fine
    if (Array.isArray(res) && res.length > 0) {
      for (const m of res) m._k = ++_kid
      messages.value = res
      await nextTick()
      scrollToBottom()
    }
  } catch (e) {
    console.warn('加载历史记录失败:', e)
    contextEnabled.value = true // default ON even on error
  }
  nextTick(() => inputRef.value?.focus())
})

onUnmounted(() => {
  if (abortController) abortController.abort()
  pageRoot.value?.removeEventListener('paste', handlePaste)
})
</script>

<style scoped>
.agent-page {
  display: flex; flex-direction: column;
  position: absolute; inset: 0;
  max-width: 860px; margin: 0 auto;
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 20px rgba(0,0,0,.06);
}
.agent-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 20px; background: linear-gradient(135deg, #43a047, #66bb6a);
  color: #fff; flex-shrink: 0;
}
.agent-header-left { display: flex; align-items: center; gap: 10px; }
.agent-header-right { display: flex; align-items: center; gap: 6px; }
.agent-pref-btn {
  background: rgba(255,255,255,.15); border: none; color: #fff;
  width: 30px; height: 30px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all .2s;
}
.agent-pref-btn:hover { background: rgba(255,255,255,.3); }
.agent-avatar {
  width: 34px; height: 34px; border-radius: 10px;
  background: rgba(255,255,255,.2); display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}
.agent-name { font-weight: 700; font-size: 15px; font-family: var(--trendy-font); }
.agent-setting-btn {
  background: rgba(255,255,255,.15); border: none; color: #fff;
  width: 34px; height: 34px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all .2s;
}
.agent-setting-btn:hover { background: rgba(255,255,255,.3); }

.agent-messages {
  flex: 1; overflow-y: auto; padding: 16px 20px; min-height: 0;
  background: #f0f4f0; display: flex; flex-direction: column; gap: 10px;
}
.agent-messages::-webkit-scrollbar { width: 5px; }
.agent-messages::-webkit-scrollbar-thumb { background: #c8d6c8; border-radius: 3px; }

.agent-empty { text-align: center; padding: 64px 20px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-title { font-size: 16px; font-weight: 600; color: #333; }

.msg-wrapper { display: contents; }

.msg-row { display: flex; max-width: 80%; }
.msg-user { align-self: flex-end; }
.msg-assistant { align-self: flex-start; }
.msg-bubble {
  padding: 10px 14px; border-radius: 18px;
  font-size: 14px; line-height: 1.6; word-break: break-word;
}
.msg-bubble-user {
  background: linear-gradient(135deg, #66bb6a, #43a047);
  color: #fff; border-bottom-right-radius: 6px;
}
.msg-bubble-assistant {
  background: #fff; color: #333;
  border-bottom-left-radius: 6px;
  box-shadow: 0 1px 2px rgba(0,0,0,.06);
}
.msg-bubble-assistant :deep(code) {
  background: #f0f4f0; padding: 2px 6px; border-radius: 4px;
  font-size: 13px; color: #e74c3c;
}

.typing-bubble { display: flex; gap: 4px; align-items: center; padding: 14px 18px; }
.typing-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #b0c0b0;
  animation: typingBounce 1.4s ease-in-out infinite;
}
.typing-dot:nth-child(2) { animation-delay: .2s; }
.typing-dot:nth-child(3) { animation-delay: .4s; }
@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: .4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

.msg-tool-card { max-width: 85%; align-self: flex-start; }

.agent-input-area {
  flex-shrink: 0; background: #fff;
  border-top: 1px solid #e8efe8; padding: 10px 16px 14px;
}
.agent-toolbar { display: flex; gap: 8px; margin-bottom: 8px; padding: 0 2px; justify-content: space-between; align-items: center; }
.toolbar-left { display: flex; gap: 4px; align-items: center; }
.shortcut-btn {
  padding: 4px 10px; border: 1px solid #dce4dc; border-radius: 14px;
  background: #f5f8f5; color: #5a7a5a; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; font-family: var(--trendy-font); white-space: nowrap;
}
.shortcut-btn:hover { background: #e0efe0; border-color: #81c784; color: #2e5a2e; }
.shortcut-btn.active {
  background: var(--nature-green); color: #fff; border-color: var(--nature-green);
  box-shadow: 0 2px 8px rgba(76,175,80,0.3);
}
.upload-btn {
  display: flex; align-items: center; justify-content: center;
  width: 30px; height: 30px; border-radius: 8px; cursor: pointer;
  color: #999; transition: all .2s; margin-right: 2px;
}
.upload-btn:hover { color: #43a047; background: #f0faf0; }
.upload-btn input { display: none; }
.toolbar-actions { display: flex; gap: 6px; align-items: center; flex-shrink: 0; }
.toolbar-toggle {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: #999; cursor: pointer;
  padding: 4px 10px; border-radius: 12px;
  border: 1px solid #e0e6e0; transition: all .2s; user-select: none;
}
.toolbar-toggle.active { color: #43a047; border-color: #66bb6a; background: #f0faf0; }
.toolbar-btn {
  display: flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 8px;
  border: 1px solid transparent; background: none;
  color: #999; cursor: pointer; transition: all .2s;
}
.toolbar-btn:hover { background: #f5f5f5; color: #666; }

.input-row {
  display: flex; align-items: flex-end; gap: 10px;
  background: #f5f7f5; border-radius: 24px;
  padding: 8px 8px 8px 18px;
  border: 2px solid transparent; transition: border-color .2s;
}
.input-row:focus-within { border-color: #66bb6a; }
.agent-input {
  flex: 1; border: none; background: none;
  font-size: 14px; line-height: 1.5; resize: none; outline: none;
  max-height: 120px; font-family: inherit; color: #333;
}
.agent-input::placeholder { color: #bbb; }
.send-btn {
  width: 38px; height: 38px; border-radius: 50%;
  border: none; background: #e0e6e0; color: #999;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all .2s; flex-shrink: 0;
}
.send-btn.active { background: linear-gradient(135deg, #66bb6a, #43a047); color: #fff; }
.send-btn.active:hover { transform: scale(1.05); }
.send-btn.stop { background: #e74c3c; color: #fff; cursor: pointer; }
.send-btn.stop:hover { background: #c0392b; transform: scale(1.05); }

.config-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.3);
  display: flex; align-items: center; justify-content: center;
  z-index: 2000; backdrop-filter: blur(4px);
}
.config-dialog {
  background: #fff; border-radius: 16px;
  width: 420px; max-width: 90vw; max-height: 85vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,.15); padding: 20px;
}
.config-dialog-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.config-dialog-header h3 { margin: 0; font-size: 16px; font-weight: 700; color: #3a4a3a; }
.dialog-enter-active, .dialog-leave-active { transition: opacity .2s; }
.dialog-enter-from, .dialog-leave-to { opacity: 0; }

/* Article preview modal */
.preview-modal {
  background: #fff; border-radius: 16px;
  width: 720px; max-width: 92vw; max-height: 85vh;
  display: flex; flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,.15);
  overflow: hidden;
}
.preview-modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #eee;
  flex-shrink: 0;
}
.preview-modal-header h2 { margin: 0; font-size: 17px; font-weight: 700; }
.preview-modal-body {
  flex: 1; overflow-y: auto; padding: 20px 24px;
  font-size: 15px; line-height: 1.8; color: #333;
}
.preview-modal-body :deep(h2) { margin: 20px 0 10px; font-size: 18px; }
.preview-modal-body :deep(h3) { margin: 16px 0 8px; font-size: 16px; }
.preview-modal-body :deep(p) { margin: 8px 0; }
.preview-modal-body :deep(ul), .preview-modal-body :deep(ol) { padding-left: 20px; margin: 8px 0; }
.preview-modal-body :deep(li) { margin: 4px 0; }
.preview-modal-body :deep(code) {
  background: #f5f5f5; padding: 2px 6px; border-radius: 4px; font-size: 13px;
}
.preview-modal-footer {
  display: flex; gap: 16px; padding: 12px 20px;
  border-top: 1px solid #eee; font-size: 13px; color: #999;
  flex-shrink: 0;
}
.config-close {
  width: 30px; height: 30px; border-radius: 8px;
  border: none; background: #f5f5f5; font-size: 16px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
}
.config-close:hover { background: #e8e8e8; }

@media (max-width: 768px) {
  .agent-page { border-radius: 0; max-width: 100%; }
  .msg-row { max-width: 88%; }
  .agent-messages { padding: 12px; }
  .msg-tool-card { max-width: 95%; }
}

/* Drag-drop overlay */
.drop-overlay {
  position: absolute; inset: 0; z-index: 100;
  background: rgba(67,160,71,0.12); border: 3px dashed #66bb6a;
  border-radius: 16px; display: flex; align-items: center; justify-content: center;
  pointer-events: none;
}
.drop-hint {
  background: rgba(67,160,71,0.85); color: #fff;
  padding: 14px 28px; border-radius: 12px; font-size: 15px; font-weight: 700;
  font-family: var(--trendy-font);
}
</style>
