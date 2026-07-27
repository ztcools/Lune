<template>
  <div class="treehole-page">
    <!-- ====== Full-screen Danmaku Section ====== -->
    <div class="danmaku-section">
      <div class="bg-image" :style="{ backgroundImage: `url(${danmakuBg})` }"></div>
      <div class="bg-overlay"></div>

      <!-- Danmaku floating messages -->
      <div class="danmaku-container">
        <div
          v-for="msg in danmakuList"
          :key="msg.id"
          class="danmaku-item"
          :style="msg.style"
        >
          <el-avatar class="danmaku-avatar" :size="28" :src="msg.avatar">{{ (msg.nickname || '?').charAt(0) }}</el-avatar>
          <span class="danmaku-text">{{ msg.text }}</span>
        </div>
      </div>

      <!-- Centered input -->
      <div class="danmaku-input-area">
        <h2 class="danmaku-title">树洞</h2>
        <p class="danmaku-subtitle">把秘密说给树洞听</p>
        <div class="danmaku-input-row">
          <input
            class="danmaku-input"
            v-model="danmakuContent"
            placeholder="留下点什么啦~"
            maxlength="60"
            @keyup.enter="sendDanmaku"
          />
          <button
            class="danmaku-send-btn"
            @click="sendDanmaku"
          >发射</button>
        </div>
      </div>

      <!-- Scroll-down hint -->
      <div class="scroll-hint" @click="scrollToTimeline">
        <span>往下看更多</span>
        <svg viewBox="0 0 1024 1024" width="22" height="22" fill="rgba(255,255,255,0.6)">
          <path d="M512 714.666667c-8.533333 0-17.066667-4.266667-23.466667-8.533334L243.2 460.8c-12.8-12.8-12.8-32.426667 0-45.226667 12.8-12.8 32.426667-12.8 45.226667 0L512 639.146667l223.573333-223.573334c12.8-12.8 32.426667-12.8 45.226667 0 12.8 12.8 12.8 32.426667 0 45.226667l-245.333333 245.333333c-6.4 4.266667-14.933333 8.533333-23.466667 8.533334z"/>
        </svg>
      </div>
    </div>

    <!-- ====== Timeline Section ====== -->
    <div class="timeline-section" ref="timelineRef">
      <div class="bg-image" :style="{ backgroundImage: `url(${timelineBg})` }" v-if="timelineBg" />
      <div class="timeline-wrapper my-animation-hideToShow">
        <div class="tree-hole-container">
          <ol class="tree-hole-list" v-if="treeHoleList.length > 0">
            <li
              class="tree-hole-li"
              v-for="(treeHole, index) in treeHoleList"
              :key="treeHole.id"
            >
              <div
                class="tree-hole-content"
                :class="mobile
                  ? 'rightTreeHole'
                  : index % 2 === 0
                    ? 'leftTreeHole'
                    : 'rightTreeHole'"
              >
                <el-avatar
                  class="avatar-img"
                  :size="40"
                  :src="treeHole.avatar"
                >{{ (treeHole.nickname || treeHole.username || '?').charAt(0) }}</el-avatar>
                <div
                  class="tree-hole-box"
                  :style="{ background: colors[index % colors.length] }"
                >
                  <div class="tree-hole-nick">{{ treeHole.nickname || treeHole.username || '匿名' }}</div>
                  <div
                    class="box-tag"
                    :class="mobile || index % 2 !== 0 ? 'tag-left' : 'tag-right'"
                    :style="mobile || index % 2 !== 0
                      ? { 'border-color': 'transparent transparent transparent ' + colors[index % colors.length] }
                      : { 'border-color': 'transparent ' + colors[index % colors.length] + ' transparent transparent' }"
                  ></div>
                  <div class="my-content" v-html="treeHole.content"></div>
                  <div class="tree-hole-footer">
                    <span>{{ formatDate(treeHole.createTime) }}</span>
                    <span
                      v-if="userStore.isLoggedIn && userStore.user?.userId === treeHole.userId"
                      class="tree-hole-delete"
                      @click="handleDelete(treeHole.id)"
                    >
                      <svg viewBox="0 0 1024 1024" width="18" height="18" style="vertical-align: -2px;">
                        <path
                          d="M921.1392 155.392h-270.592v-48.2816c0-22.7328-18.432-41.1648-41.1648-41.1648H426.3424a41.1648 41.1648 0 0 0-41.1648 41.1648v48.2816H110.6432c-14.1312 0-25.6 11.4688-25.6 25.6s11.4688 25.6 25.6 25.6h810.496c14.1312 0 25.6-11.4688 25.6-25.6s-11.4688-25.6-25.6-25.6zM170.8032 260.0448v592.8448c0 50.8928 41.2672 92.16 92.16 92.16h500.6848c50.8928 0 92.16-41.2672 92.16-92.16V260.0448H170.8032z m249.1392 462.7968c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z m243.1488 0c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z"
                          fill="#FF623E"
                        ></path>
                      </svg>
                    </span>
                  </div>
                </div>
              </div>
            </li>
          </ol>
          <div v-else style="text-align:center; padding:80px 0;">
            <el-empty description="暂无树洞消息" />
          </div>

          <!-- Send Button (timeline area) -->
          <div class="tree-hole-go">
            <svg
              class="send-icon"
              @click="openDialog"
              viewBox="0 0 1024 1024"
              width="28"
              height="28"
            >
              <path
                d="M931.4 498.8L93.4 51.4c-15.4-7.6-34-3.2-44 10.2-4.8 6.4-6.8 14.2-6 22l70.6 334.8c1.6 7.6 6.2 14.2 13 18L301.8 524c6.6 3.8 10.8 10.8 10.8 18.4v233.6c0 24 21 44 46.6 41.4 13.6-1.4 26-8.8 34.2-19.6l96.2-124.2c5.8-7.6 14.8-12.6 24.8-12.6 2.8 0 5.6 0.4 8.4 1l272.8 72.2c15.4 4.2 28.4-2.6 33.8-13.8 3-5.8 3.6-12.4 2-18.6L940 541c-2.8-21.4-5.8-22.8-8.6-42.2z"
                fill="currentColor"
              ></path>
            </svg>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-wrap" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- Send Card -->
    <teleport to="body">
      <transition name="card-fade">
        <div v-if="dialogVisible" class="treehole-card-overlay" @click.self="dialogVisible = false">
          <transition name="card-pop">
            <div v-if="dialogVisible" class="treehole-send-card">
              <div class="card-glow" />
              <button class="card-close" @click="dialogVisible = false">✕</button>
              <div class="card-inner">
                <h3 class="card-title">🌳 微言</h3>
                <p class="card-sub">把想说的话放进树洞里</p>
                <textarea
                  v-model="content"
                  placeholder="说点什么吧..."
                  maxlength="500"
                  class="card-textarea"
                  rows="5"
                ></textarea>
                <div class="card-foot">
                  <span class="char-count">{{ content.length }}/500</span>
                  <button class="nature-btn nature-btn-primary" @click="submitWeiYan" :disabled="posting">
                    <span v-if="!posting">✨ 发布</span>
                    <span v-else>发送中...</span>
                  </button>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { treeHoleApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
import { useUserStore } from '../../stores/user'
import { useAppStore } from '../../stores/app'
import { ElMessage, ElMessageBox } from 'element-plus'
import { requireLogin } from '../../composables/useAuth'

const userStore = useUserStore()
const appStore = useAppStore()

// --- danmaku state ---
const danmakuList = ref([])
const danmakuContent = ref('')
const danmakuBg = usePageBackground('treeholeDanmaku')
const timelineBg = usePageBackground('treeholeContent')

// --- timeline state ---
const treeHoleList = ref([])
const page = ref(1)
const pageSize = 10
const total = ref(0)
const content = ref('')
const posting = ref(false)
const dialogVisible = ref(false)
const mobile = ref(false)

const timelineRef = ref(null)

// --- constants ---
const ROWS = 6
const colors = [
  '#ffecd2', '#fcb69f', '#a1c4fd', '#c2e9fb', '#d4a5ff',
  '#fbc2eb', '#a6c1ee', '#fdcbf1', '#e6dee9', '#bae1ff'
]
const defaultAvatar = '/assets/头像1.jpg'

function checkMobile() {
  mobile.value = window.innerWidth <= 600
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)

  fetchDanmaku()
  fetchTreeHoles()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// --- Danmaku ---
function makeDanmakuStyle(index) {
  const row = index % ROWS
  const top = 8 + row * 15  // 8%, 23%, 38%, 53%, 68%, 83%
  const duration = 10 + Math.random() * 14  // 10-24s
  const delay = Math.random() * -20  // negative delay = start mid-animation
  const fontSize = 13 + Math.floor(Math.random() * 3)  // 13-15px
  return {
    top: top + '%',
    animationDuration: duration + 's',
    animationDelay: delay + 's',
    fontSize: fontSize + 'px'
  }
}

async function fetchDanmaku() {
  try {
    const data = await treeHoleApi.list({ page: 1, size: 50 })
    if (data && data.records) {
      danmakuList.value = data.records.map((m, i) => ({
        id: m.id,
        text: m.content?.replace(/<[^>]+>/g, '').replace(/\n/g, ' ') || '',
        avatar: m.avatar,
        nickname: m.nickname,
        style: makeDanmakuStyle(i)
      }))
    }
  } catch (e) { /* silent */ }
}

async function sendDanmaku() {
  const text = danmakuContent.value.trim()
  if (!text) return
  try {
    const res = await treeHoleApi.create({ content: text, isPublic: true })
    danmakuList.value.push({
      id: res?.id || Date.now(),
      text: text,
      avatar: res?.avatar || '',
      nickname: res?.nickname || '匿名',
      style: makeDanmakuStyle(danmakuList.value.length)
    })
    danmakuContent.value = ''
    page.value = 1
    await fetchTreeHoles()
    ElMessage.success('发射成功！')
  } catch (e) {
    ElMessage.error('发射失败')
  }
}

function scrollToTimeline() {
  if (timelineRef.value) {
    timelineRef.value.scrollIntoView({ behavior: 'smooth' })
  }
}

// --- Timeline ---
async function fetchTreeHoles() {
  try {
    const data = await treeHoleApi.list({ page: page.value, size: pageSize })
    if (data && data.records) {
      data.records.forEach((c) => {
        if (c.content) {
          c.content = c.content.replace(/\n{2,}/g, '<div style="height:12px"></div>')
          c.content = c.content.replace(/\n/g, '<br/>')
        }
      })
      treeHoleList.value = data.records
      total.value = data.total
    }
  } catch (e) { /* silent */ }
}

async function submitWeiYan() {
  if (!content.value.trim()) return
  posting.value = true
  try {
    await treeHoleApi.create({ content: content.value, isPublic: true })
    ElMessage.success('发布成功')
    content.value = ''
    dialogVisible.value = false
    page.value = 1
    await fetchTreeHoles()
    await fetchDanmaku()
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    posting.value = false
  }
}

async function handleDelete(id) {
  if (!userStore.isLoggedIn) {
    if (!requireLogin()) return
    return
  }
  try {
    await ElMessageBox.confirm('确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
      center: true
    })
    await treeHoleApi.delete(id)
    ElMessage.success('删除成功!')
    page.value = 1
    await fetchTreeHoles()
    await fetchDanmaku()
  } catch (e) { /* cancelled */ }
}

function handlePageChange(p) {
  page.value = p
  if (timelineRef.value) {
    timelineRef.value.scrollIntoView({ behavior: 'smooth' })
  }
  fetchTreeHoles()
}

function openDialog() {
  dialogVisible.value = true
}

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
</script>

<style scoped>
/* ====== Full-screen Danmaku Section ====== */
.danmaku-section {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
.danmaku-section::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(to bottom, transparent, var(--background));
  z-index: 5;
  pointer-events: none;
}
.bg-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
}
.bg-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
}

/* ====== Danmaku Container ====== */
.danmaku-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
  overflow: hidden;
  pointer-events: none;
}

.danmaku-item {
  position: absolute;
  left: 100%;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 24px;
  padding: 6px 14px 6px 6px;
  animation: danmaku-drift linear infinite;
  user-select: none;
}

.danmaku-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  flex-shrink: 0; border: 1.5px solid rgba(255,255,255,0.35);
  font-size: 11px;
}

.danmaku-text {
  color: rgba(255, 255, 255, 0.92);
  font-weight: 500;
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  letter-spacing: 0.5px;
}

@keyframes danmaku-drift {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(calc(-100vw - 100%));
  }
}

/* ====== Centered Input ====== */
.danmaku-input-area {
  position: absolute;
  left: 50%;
  top: 42%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 10;
  animation: hideToShow 2s;
}

.danmaku-title {
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  font-size: 48px;
  font-weight: 700;
  font-style: italic;
  color: #fff;
  margin: 0 0 6px;
  letter-spacing: 8px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  user-select: none;
}

.danmaku-subtitle {
  font-size: 16px;
  font-weight: 500;
  font-style: italic;
  color: rgba(255, 255, 255, 0.75);
  margin: 0 0 28px;
  letter-spacing: 2px;
}

.danmaku-input-row {
  display: flex;
  gap: 10px;
  justify-content: center;
  align-items: center;
}

.danmaku-input {
  width: 320px;
  padding: 12px 18px;
  border-radius: 24px;
  border: 1.5px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(6px);
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  outline: none;
  transition: all 0.3s;
}
.danmaku-input::placeholder {
  color: rgba(255, 255, 255, 0.55);
}
.danmaku-input:focus {
  border-color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.2);
}

.danmaku-send-btn {
  padding: 12px 22px;
  border-radius: 24px;
  border: none;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(6px);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  cursor: pointer;
  transition: all 0.25s;
  letter-spacing: 2px;
}
.danmaku-send-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: scale(1.05);
}

/* ====== Scroll Hint ====== */
.scroll-hint {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  animation: float-up-down 2s ease-in-out infinite;
}
.scroll-hint span {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: 2px;
}

@keyframes float-up-down {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(8px); }
}

@keyframes hideToShow {
  0% { opacity: 0; transform: translate(-50%, -50%) translateY(20px); }
  100% { opacity: 1; transform: translate(-50%, -50%) translateY(0); }
}

/* ====== Timeline Section ====== */
.timeline-section {
  position: relative; overflow: hidden;
  min-height: 100vh;
}

.timeline-wrapper {
  background: var(--background);
  padding: 20px;
}

/* ====== Tree Hole Container ====== */
.tree-hole-container {
  padding: 20px;
  margin: 0 auto;
}

.tree-hole-list {
  padding: 100px 0 20px;
  margin: 0;
  position: relative;
  list-style: none;
}

/* Center line */
.tree-hole-list::before {
  content: '';
  width: 4px;
  border-radius: 50%;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  background-color: var(--themeBackground);
}

/* Pulsing red circle at top */
.tree-hole-list::after {
  content: '';
  width: 12px;
  height: 12px;
  border: 4px solid var(--maxLightRed);
  border-radius: 50%;
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  background-color: var(--white);
  animation: weiYanShadowFlashing 1.5s linear infinite;
}

.tree-hole-li {
  margin: 5px auto;
}

.tree-hole-content {
  position: relative;
  width: 50%;
}

.leftTreeHole {
  text-align: right;
}

.rightTreeHole {
  margin-left: 50%;
}

/* Circle markers on center line for each item */
.tree-hole-content::before {
  content: '';
  width: 12px;
  height: 12px;
  border: 4px solid var(--blue);
  border-radius: 50%;
  position: absolute;
  top: 10px;
  background-color: var(--white);
}

.leftTreeHole::before {
  right: 0;
  transform: translateX(10px);
}

.rightTreeHole::before {
  left: 0;
  transform: translateX(-10px);
}

/* Avatar */
.avatar-img {
  position: absolute;
  top: 0;
  transition: all 0.3s ease-in-out;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  object-fit: cover;
}

.leftTreeHole .avatar-img {
  right: 25px;
}

.rightTreeHole .avatar-img {
  left: 25px;
}

/* Message Bubble */
.tree-hole-box {
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  font-size: 17px;
  padding: 14px;
  width: 380px;
  border-radius: 8px;
  position: relative;
  letter-spacing: 0.05em;
  font-weight: 500;
  transition: all 0.3s ease-in-out;
  color: var(--black);
  text-align: left;
  display: inline-block;
}

.leftTreeHole .tree-hole-box {
  margin-right: 90px;
}

.rightTreeHole .tree-hole-box {
  margin-left: 90px;
}

.tree-hole-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 16px 3px var(--miniMask);
}

.avatar-img:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 16px 3px var(--miniMask);
}

/* Triangular Tail */
.box-tag {
  content: '';
  position: absolute;
  border-style: solid;
}

.leftTreeHole .box-tag {
  right: -10px;
  border-width: 15px 0 5px 10px;
}

.rightTreeHole .box-tag {
  left: -10px;
  border-width: 15px 10px 5px 0;
}

/* Content area */
.tree-hole-nick {
  font-size: 13px; font-weight: 600; color: rgba(0,0,0,0.5);
  padding: 0 10px 4px; font-family: var(--trendy-font);
}
.my-content {
  margin: 0 10px 10px;
  line-height: 34px;
  font-weight: 500;
  font-style: italic;
}

/* Footer inside bubble */
.tree-hole-footer {
  color: var(--greyFont);
  padding: 12px 10px 0;
  border-top: 1px dashed var(--white);
  font-size: 15px;
  font-weight: 500;
  display: flex;
  justify-content: space-between;
}

.tree-hole-delete {
  font-size: 14px;
  cursor: pointer;
}

/* Send Button */
.tree-hole-go {
  color: var(--blue);
  font-weight: 700;
  font-size: 25px;
  margin: 20px auto;
  text-align: center;
}

.send-icon {
  cursor: pointer;
  transition: all 0.3s;
}

.send-icon:hover {
  animation: scale 1s linear infinite;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-bottom: 40px;
}

/* ====== Send Card ====== */
.treehole-card-overlay {
  position: fixed; inset: 0; z-index: 10000;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.35); backdrop-filter: blur(8px);
}
.treehole-send-card {
  position: relative; width: 420px; max-width: 92vw;
  background: linear-gradient(155deg, rgba(255,255,255,0.9) 0%, rgba(232,245,233,0.93) 50%, rgba(200,230,201,0.9) 100%);
  backdrop-filter: blur(24px);
  border-radius: 32px;
  border: 1.5px solid rgba(255,255,255,0.5);
  box-shadow: 0 8px 40px rgba(56,142,60,0.18), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.6);
  overflow: hidden;
  font-family: var(--trendy-font);
}
.treehole-send-card .card-glow {
  position: absolute; top: -30%; left: -30%; width: 160%; height: 160%;
  background: radial-gradient(circle at 30% 20%, rgba(129,199,132,0.15) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(255,183,77,0.1) 0%, transparent 50%);
  pointer-events: none;
}
.treehole-send-card .card-close {
  position: absolute; top: 14px; right: 14px; z-index: 2;
  width: 32px; height: 32px; border-radius: 50%; border: none;
  background: rgba(0,0,0,0.06); color: #999; font-size: 14px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.3s ease;
}
.treehole-send-card .card-close:hover { background: #ff5252; color: #fff; transform: rotate(90deg); }
.treehole-send-card .card-inner { position: relative; z-index: 1; padding: 32px 28px 24px; }
.treehole-send-card .card-title {
  font-size: 22px; font-weight: 700; color: #2e7d32; margin: 0 0 4px; text-align: center;
}
.treehole-send-card .card-sub {
  font-size: 13px; color: #689f63; margin: 0 0 20px; text-align: center;
}
.treehole-send-card .card-textarea {
  width: 100%; border: 1.5px solid rgba(129,199,132,0.4); border-radius: 18px;
  padding: 14px; font-size: 15px; resize: vertical; outline: none;
  background: rgba(255,255,255,0.7); color: #333;
  font-family: var(--trendy-font); font-weight: 500;
  transition: all 0.3s ease; box-sizing: border-box;
}
.treehole-send-card .card-textarea:focus {
  border-color: #66bb6a; box-shadow: 0 0 0 4px rgba(76,175,80,0.1);
  background: #fff;
}
.treehole-send-card .card-foot {
  display: flex; justify-content: space-between; align-items: center; margin-top: 12px;
}
.treehole-send-card .char-count { font-size: 12px; color: #aaa; }
.treehole-send-card .nature-btn {
  border: none; outline: none; font-size: 15px; font-weight: 600;
  padding: 10px 24px; border-radius: 16px; cursor: pointer;
  font-family: var(--trendy-font);
  transition: all 0.3s cubic-bezier(0.4,0,0.2,1);
}
.treehole-send-card .nature-btn-primary {
  background: var(--nature-gradient); color: #fff;
  box-shadow: 0 4px 16px rgba(76,175,80,0.35);
}
.treehole-send-card .nature-btn-primary:hover:not(:disabled) {
  transform: translateY(-2px); box-shadow: 0 6px 20px rgba(76,175,80,0.45); filter: brightness(1.08);
}
.treehole-send-card .nature-btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }

.card-fade-enter-active, .card-fade-leave-active { transition: opacity 0.3s ease; }
.card-fade-enter-from, .card-fade-leave-to { opacity: 0; }
.card-pop-enter-active { transition: all 0.4s cubic-bezier(0.34,1.56,0.64,1); }
.card-pop-leave-active { transition: all 0.2s ease-in; }
.card-pop-enter-from { opacity: 0; transform: scale(0.85) translateY(20px); }
.card-pop-leave-to { opacity: 0; transform: scale(0.9) translateY(10px); }

/* ====== Responsive ====== */
@media screen and (max-width: 1000px) {
  .tree-hole-box {
    width: calc(100% - 90px);
  }
}

@media screen and (max-width: 600px) {
  .danmaku-title {
    font-size: 34px;
    letter-spacing: 6px;
  }
  .danmaku-subtitle {
    font-size: 14px;
  }
  .danmaku-input {
    width: 220px;
    font-size: 14px;
    padding: 10px 14px;
  }
  .danmaku-send-btn {
    padding: 10px 16px;
    font-size: 14px;
  }

  .tree-hole-content {
    margin-bottom: 50px;
  }

  .tree-hole-list::after {
    left: 0;
  }

  .tree-hole-list::before {
    left: 0;
  }

  .tree-hole-content {
    width: 100%;
  }

  .rightTreeHole {
    margin-left: unset;
  }

  .tree-hole-content::before {
    left: 0 !important;
    right: auto !important;
    transform: translateX(-10px) !important;
  }

  .leftTreeHole,
  .rightTreeHole {
    text-align: left;
  }

  .avatar-img {
    left: 25px !important;
    right: auto !important;
  }

  .tree-hole-box {
    margin-left: 90px !important;
    margin-right: 0 !important;
  }

  .box-tag {
    left: -10px !important;
    right: auto !important;
    border-width: 15px 10px 5px 0 !important;
  }
}
.bg-image.content-bg { position: absolute; inset: 0; background-size: cover; background-position: center; z-index: 0; opacity: 0.12; }
</style>
