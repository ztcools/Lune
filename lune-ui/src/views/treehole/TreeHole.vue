<template>
  <div class="treehole-page">
    <div class="page-bg" :style="{ backgroundImage: `url(${danmakuBg})` }"></div>

    <!-- ====== Full-screen Danmaku Section ====== -->
    <div class="danmaku-section" @click="closeDanmakuCard">
      <div class="bg-overlay"></div>

      <!-- Danmaku floating messages -->
      <div class="danmaku-container">
        <div
          v-for="msg in danmakuList"
          :key="msg.id"
          class="danmaku-item"
          :class="{ 'is-paused': pausedId === msg.id, 'is-hover': hoverId === msg.id }"
          :style="msg.style"
          @mouseenter="hoverId = msg.id"
          @mouseleave="hoverId = null"
          @click.stop="openDanmakuCard(msg, $event)"
        >
          <el-avatar class="danmaku-avatar" :size="28" :src="msg.avatar">{{ (msg.nickname || '?').charAt(0) }}</el-avatar>
          <span class="danmaku-text">{{ msg.text }}</span>
        </div>
      </div>

      <!-- 弹幕发送者卡片（点击弹幕弹出，该弹幕暂停，退出恢复） -->
      <transition name="danmaku-card-pop">
        <div v-if="activeCard" class="danmaku-card" :style="cardPos" @click.stop>
          <div class="dc-head">
            <el-avatar :size="46" :src="activeCard.avatar" class="dc-avatar">{{ (activeCard.nickname || '匿').charAt(0) }}</el-avatar>
            <div class="dc-info">
              <div class="dc-name">{{ activeCard.nickname || '匿名' }}</div>
              <div class="dc-sub">{{ activeCard.createTime ? formatDate(activeCard.createTime) : '风里的悄悄话' }}</div>
            </div>
            <button class="dc-close" @click="closeDanmakuCard">×</button>
          </div>
          <div class="dc-content">{{ activeCard.text }}</div>
          <div class="dc-foot">
            <span class="dc-tip">这条已为你停住</span>
            <!-- 时间线删掉后，这里是唯一能删自己留言的地方 -->
            <button
              v-if="canDelete(activeCard)"
              class="dc-delete"
              @click="handleDelete(activeCard.id)"
            >删除</button>
          </div>
        </div>
      </transition>

      <!-- Centered input -->
      <div class="danmaku-input-area">
        <h2 class="danmaku-title">风语林</h2>
        <p class="danmaku-subtitle">说给风听，风会记得</p>
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

      <!-- 弹幕条数提示：满屏飘的是最新 50 条，说明一下免得以为丢了内容 -->
      <div class="danmaku-count" v-if="danmakuList.length">
        风里飘着 {{ danmakuList.length }} 句
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { treeHoleApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { requireLogin } from '../../composables/useAuth'

const userStore = useUserStore()

// --- danmaku state ---
const danmakuList = ref([])
const danmakuContent = ref('')
const danmakuBg = usePageBackground('treeholeDanmaku')
// 弹幕交互：悬停高亮 / 点击弹卡片并暂停该条
const hoverId = ref(null)
const pausedId = ref(null)
const activeCard = ref(null)
const cardPos = ref({ top: '50%', left: '50%', transform: 'translate(-50%,-50%)' })

function openDanmakuCard(msg, e) {
  pausedId.value = msg.id
  activeCard.value = msg
  // 卡片定位在点击附近（边界保护）
  const x = Math.min(Math.max(e.clientX, 180), window.innerWidth - 180)
  const y = Math.min(Math.max(e.clientY, 130), window.innerHeight - 160)
  cardPos.value = { top: y + 'px', left: x + 'px', transform: 'translate(-50%,-50%)' }
}
function closeDanmakuCard() {
  activeCard.value = null
  pausedId.value = null
}

// --- constants ---
const ROWS = 6

onMounted(fetchDanmaku)

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
      // userId / createTime 是给点击卡片用的（判断能不能删 + 显示日期）
      danmakuList.value = data.records.map((m, i) => ({
        id: m.id,
        userId: m.userId,
        createTime: m.createTime,
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
      userId: res?.userId ?? userStore.user?.userId,
      createTime: res?.createTime,
      text: text,
      avatar: res?.avatar || userStore.user?.avatar || '',
      nickname: res?.nickname || userStore.nickname || '匿名',
      style: makeDanmakuStyle(danmakuList.value.length)
    })
    danmakuContent.value = ''
    ElMessage.success('发射成功！')
  } catch (e) {
    ElMessage.error('发射失败')
  }
}

// 只能删自己的：与后端权限一致，避免给出点了才报错的按钮
function canDelete(msg) {
  return !!msg && userStore.isLoggedIn && userStore.user?.userId === msg.userId
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
    closeDanmakuCard()
    await fetchDanmaku()
  } catch (e) { /* cancelled */ }
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
.page-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  z-index: -1;
}
.danmaku-section {
  position: relative;
  width: 100%;
  /* 手机上 100vh 是「地址栏收起后」的高度，首屏就比可视区高出 60~100px，
     底部的条数提示正好被顶出屏幕。dvh 跟随可视区变化，旧浏览器回落到 vh。 */
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
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
  cursor: pointer;
  transition: transform 0.25s ease, background 0.25s ease, box-shadow 0.25s ease;
}

/* 悬停：高亮 + 轻微放大 + 该条减速（通过暂停实现更直观） */
.danmaku-item.is-hover {
  background: rgba(67, 160, 71, 0.55);
  transform: scale(1.1);
  box-shadow: 0 4px 18px rgba(102, 187, 106, 0.5);
  z-index: 5;
}

/* 点击后该条暂停（其他弹幕不受影响） */
.danmaku-item.is-paused {
  animation-play-state: paused;
  background: rgba(67, 160, 71, 0.6);
  box-shadow: 0 0 0 2px rgba(129, 199, 132, 0.7), 0 6px 22px rgba(102, 187, 106, 0.6);
  z-index: 6;
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

/* ====== 弹幕发送者卡片 ====== */
.danmaku-card {
  position: fixed;
  z-index: 60;
  width: 300px;
  background: linear-gradient(135deg, rgba(255,255,255,0.96), rgba(232,245,233,0.96));
  backdrop-filter: blur(18px);
  border-radius: 22px;
  box-shadow: 0 16px 48px rgba(0,0,0,0.28);
  border: 1.5px solid rgba(255,255,255,0.7);
  padding: 18px;
}
.dc-head { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.dc-avatar { border: 2px solid #fff; box-shadow: 0 3px 10px rgba(0,0,0,0.15); }
.dc-info { flex: 1; min-width: 0; }
.dc-name { font-size: 16px; font-weight: 700; color: #2e5a2e; font-family: var(--trendy-font); }
.dc-sub { font-size: 11px; color: #8aa88a; margin-top: 2px; }
.dc-close { width: 30px; height: 30px; border-radius: 50%; border: none; background: var(--nature-green-pale); color: var(--nature-green-dark); font-size: 20px; cursor: pointer; line-height: 1; transition: all 0.3s; flex-shrink: 0; }
.dc-close:hover { background: var(--nature-green-light); color: #fff; transform: rotate(90deg); }
.dc-content { font-size: 14px; color: #4a5a4a; line-height: 1.7; background: rgba(232,245,233,0.6); border-radius: 14px; padding: 12px 14px; word-break: break-word; white-space: pre-wrap; }
.dc-foot { margin-top: 12px; display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.dc-tip { font-size: 12px; color: var(--nature-green-dark); font-family: var(--handwriting-font); letter-spacing: 1px; }
.dc-delete {
  border: none; background: rgba(255, 98, 62, 0.1); color: #e04f2c;
  font-size: 12px; font-weight: 600; padding: 5px 14px; border-radius: 14px;
  cursor: pointer; transition: all 0.25s; flex-shrink: 0;
}
.dc-delete:hover { background: #e04f2c; color: #fff; }

.danmaku-card-pop-enter-active { transition: all 0.35s cubic-bezier(0.34,1.56,0.64,1); }
.danmaku-card-pop-leave-active { transition: all 0.2s ease-in; }
.danmaku-card-pop-enter-from { opacity: 0; transform: translate(-50%,-50%) scale(0.7); }
.danmaku-card-pop-leave-to { opacity: 0; transform: translate(-50%,-50%) scale(0.85); }

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

/* ====== 弹幕条数提示 ====== */
.danmaku-count {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  font-size: 12.5px;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.5);
  font-family: var(--handwriting-font);
  user-select: none;
}

@keyframes hideToShow {
  0% { opacity: 0; transform: translate(-50%, -50%) translateY(20px); }
  100% { opacity: 1; transform: translate(-50%, -50%) translateY(0); }
}

/* ====== Responsive ====== */
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
  .danmaku-card { width: 268px; }
}

</style>
