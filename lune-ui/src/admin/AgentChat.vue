<template>
  <div class="agent-page">
    <!-- Header -->
    <div class="agent-header">
      <div class="agent-header-left">
        <div class="agent-avatar">🤖</div>
        <span class="agent-name">Luna</span>
      </div>
      <button class="agent-setting-btn" @click="showConfig = true" title="Agent 配置">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
      </button>
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
      <div class="agent-toolbar">
        <label class="toolbar-toggle" :class="{ active: contextEnabled }" @click="toggleContext">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
          <span>记忆</span>
        </label>
        <button class="toolbar-btn" @click="clearHistory" title="清空对话">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
        </button>
      </div>
      <div class="input-row">
        <textarea ref="inputRef" v-model="inputText" class="agent-input"
          placeholder="输入消息..." rows="1"
          @keydown.enter.exact.prevent="handleSend"
          @input="autoResize"
        ></textarea>
        <button class="send-btn" :class="{ active: inputText.trim() }"
          @click="handleSend" :disabled="!inputText.trim() || loading">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
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

    <!-- Article preview modal -->
    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="previewArticle" class="config-overlay" @click.self="previewArticle = null">
          <div class="preview-modal">
            <div class="preview-modal-header">
              <h2>{{ previewArticle.title }}</h2>
              <button @click="previewArticle = null" class="config-close">×</button>
            </div>
            <div class="preview-modal-body" v-html="previewArticle.content"></div>
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
import ToolResultCard from '../components/agent/ToolResultCard.vue'

let _kid = 0
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const streaming = ref(false)
const contextEnabled = ref(true)
const showConfig = ref(false)
const previewArticle = ref(null)
const msgContainer = ref(null)
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

function renderMd(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.+?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

function addMsg(msg) {
  msg._k = ++_kid
  messages.value.push(msg)
}

async function handleSend() {
  const msg = inputText.value.trim()
  if (!msg || loading.value) return
  inputText.value = ''
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
      // Split SSE events by double newline
      const parts = buffer.split('\n\n')
      buffer = parts.pop() || ''
      for (const block of parts) {
        if (!block.trim()) continue
        // Extract data: payload — SseEmitter sends "data:{json}" (no space after colon)
        for (const line of block.split('\n')) {
          const trimmed = line.trim()
          if (!trimmed.startsWith('data:')) continue
          // Handle both "data:{...}" and "data: {...}"
          const payload = trimmed[5] === ' ' ? trimmed.slice(6) : trimmed.slice(5)
          if (!payload) continue
          try { handleSSE(JSON.parse(payload)) } catch (e) { /* skip malformed */ }
        }
      }
    }
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
  try {
    await ElMessageBox.confirm('确定要删除吗？', '确认', { type: 'warning' })
    if (toolName.includes('article')) await articleApi.delete(id)
    addMsg({ role: 'assistant', content: '已删除' })
    scrollToBottom()
  } catch (e) { /* cancelled or error */ }
}

async function toggleContext() {
  contextEnabled.value = !contextEnabled.value
  try { await agentApi.setContext(contextEnabled.value) } catch (e) { /* ignore */ }
}

async function clearHistory() {
  try {
    await agentApi.clearHistory()
    messages.value = []
    ElMessage.success('已清空')
  } catch (e) { /* ignore */ }
}

onMounted(async () => {
  try {
    const history = await agentApi.getHistory()
    if (history && history.length > 0) {
      for (const m of history) m._k = ++_kid
      messages.value = history
      await nextTick()
      scrollToBottom()
    }
  } catch (e) { /* no history */ }
  nextTick(() => inputRef.value?.focus())
})

onUnmounted(() => {
  if (abortController) abortController.abort()
})
</script>

<style scoped>
.agent-page {
  display: flex; flex-direction: column;
  height: calc(100vh - 130px); max-width: 860px; margin: 0 auto;
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 20px rgba(0,0,0,.06);
}
.agent-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 20px; background: linear-gradient(135deg, #43a047, #66bb6a);
  color: #fff; flex-shrink: 0;
}
.agent-header-left { display: flex; align-items: center; gap: 10px; }
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
  flex: 1; overflow-y: auto; padding: 16px 20px;
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
.agent-toolbar { display: flex; gap: 8px; margin-bottom: 8px; padding: 0 2px; }
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

.config-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.3);
  display: flex; align-items: center; justify-content: center;
  z-index: 2000; backdrop-filter: blur(4px);
}
.config-dialog {
  background: #fff; border-radius: 16px;
  width: 420px; max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,.15);
}
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
  .agent-page { height: calc(100vh - 84px); border-radius: 0; max-width: 100%; }
  .msg-row { max-width: 88%; }
  .agent-messages { padding: 12px; }
  .msg-tool-card { max-width: 95%; }
}
</style>
