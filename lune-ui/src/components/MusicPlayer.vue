<template>
  <div class="music-root">
    <!-- bar 变体（移动端顶栏）：紧凑一行，点一下开完整面板 -->
    <div v-if="isBar" class="bar-row" @click="panelOpen = true">
      <div class="bar-disc" :class="{ spinning: musicPlaying }">
        <img v-if="currentSong?.cover" :src="currentSong.cover" class="disc-img" alt="" />
        <LineIcon v-else name="music" :size="13" />
      </div>
      <div class="bar-text">
        <span class="bar-name">{{ currentSong?.name || '暂无音乐' }}</span>
        <span class="bar-sub" v-if="hint">{{ hint }}</span>
      </div>
      <button class="bar-play" @click.stop="toggleMusic" :title="musicPlaying ? '暂停' : '播放'" :aria-label="musicPlaying ? '暂停音乐' : '播放音乐'">
        <svg v-if="!musicPlaying" viewBox="0 0 24 24" width="15" height="15"><path d="M8 5v14l11-7z" fill="currentColor"/></svg>
        <svg v-else viewBox="0 0 24 24" width="15" height="15"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" fill="currentColor"/></svg>
      </button>
    </div>

    <!-- 完整播放器。bar 变体下 teleport 到 body 居中弹出（顶栏有 backdrop-filter，
         留在里面会被新的包含块 + 溢出裁掉）；card 变体下 teleport 关掉，就地渲染。 -->
    <teleport to="body" :disabled="!isBar">
      <transition name="mp-pop">
        <div v-if="!isBar || panelOpen" class="mp-mask" :class="{ inline: !isBar }" @click.self="panelOpen = false">
          <div class="mp-panel glass-card shadow-box">
            <div class="mp-head">
              <span class="music-title"><LineIcon name="music" :size="15" />来听首小曲</span>
              <button v-if="isBar" class="mp-close" @click="panelOpen = false">×</button>
            </div>
            <div class="music-player">
              <!-- 封面 -->
              <div class="music-disc-wrap" v-if="currentSong?.cover">
                <div class="music-disc" :class="{ spinning: musicPlaying }">
                  <img :src="currentSong.cover" class="disc-img" alt="" />
                </div>
              </div>

              <!-- 歌词 -->
              <div class="music-lyrics" v-if="displayLyric">
                <div class="lyric-line active">{{ displayLyric }}</div>
              </div>

              <div class="music-song-name">{{ currentSong?.name || '暂无音乐' }}</div>

              <!-- 控制 -->
              <div class="music-controls">
                <button class="music-ctl-btn" @click="prevSong" title="上一首" aria-label="上一首">
                  <svg viewBox="0 0 24 24" width="16" height="16"><path d="M6 6h2v12H6zm3.5 6l8.5 6V6z" fill="currentColor"/></svg>
                </button>
                <button class="music-play-btn" @click="toggleMusic">
                  <svg v-if="!musicPlaying" viewBox="0 0 24 24" width="20" height="20"><path d="M8 5v14l11-7z" fill="currentColor"/></svg>
                  <svg v-else viewBox="0 0 24 24" width="20" height="20"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" fill="currentColor"/></svg>
                </button>
                <button class="music-ctl-btn" @click="nextSong" title="下一首" aria-label="下一首">
                  <svg viewBox="0 0 24 24" width="16" height="16"><path d="M16 6h2v12h-2zM6 18l8.5-6L6 6z" fill="currentColor"/></svg>
                </button>
                <div class="music-progress" @click="seekMusic" role="slider" :aria-valuenow="musicProgress" aria-valuemin="0" aria-valuemax="100" aria-label="播放进度">
                  <div class="music-progress-fill" :style="{ width: musicProgress + '%' }" />
                </div>
              </div>
              <div class="music-song-artist">{{ currentSong?.artist || '在后台添加音乐吧' }}</div>
              <div class="music-time" v-if="duration > 0">{{ fmtTime(currentTime) }} / {{ fmtTime(duration) }}</div>
              <!-- 播放失败/受阻提示：原先失败是完全静默的，与「暂停」无法区分 -->
              <div class="music-hint" v-if="hint">{{ hint }}</div>
              <!-- CC BY 曲目要求可见署名，见 public/media/CREDITS.md -->
              <div class="music-license" v-if="currentSong?.license">{{ currentSong.license }}</div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- 隐藏的音频元素（始终留在组件里，不随面板开关重挂载） -->
    <audio
      ref="audioRef"
      :src="currentSong?.url"
      @timeupdate="onTimeUpdate"
      @ended="nextSong"
      @loadedmetadata="onLoaded"
      @error="onError"
      @stalled="onStalled"
      preload="metadata"
    ></audio>
  </div>
</template>

<script setup>
/**
 * 音乐播放器（行业成熟方案）
 * - 后台 site_config.home_music_list 配置真实音频 [{name,artist,url,cover,lrc}]
 * - HTML5 Audio 播放，支持进度/歌词/上下首/旋转唱片
 * - 无配置时不渲染播放（提示去后台添加）
 *
 * 两种形态：
 * - variant="card"（默认）PC 首页侧栏的完整卡片
 * - variant="bar"        移动端顶栏里的一行，点开是居中面板。
 *   之前移动端是 position:fixed 吊在屏幕底部压着 TabBar，观感差，见 PublicLayout。
 */
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { siteConfigApi } from '../api/modules'
import LineIcon from './LineIcon.vue'

const props = defineProps({
  // 兜底纯音乐列表（Web Audio 合成，无配置时用）
  fallback: { type: Array, default: () => [] },
  variant: { type: String, default: 'card' }
})

const isBar = computed(() => props.variant === 'bar')
const panelOpen = ref(false)

const audioRef = ref(null)
const musicPlaying = ref(false)
const musicProgress = ref(0)
const currentTime = ref(0)
const duration = ref(0)
const songList = ref([])
const songIdx = ref(0)
const hint = ref('')
// 用户的播放意图，与 musicPlaying（实际播放状态）分开：
// 坏链接会让 musicPlaying 变 false，但意图仍是「想听」，自动跳转后应继续播放
const wantPlay = ref(false)
// 加载失败的曲目下标：切歌时跳过，避免反复卡在同一个坏链接上
const brokenIdx = ref(new Set())
let hintTimer = null

const currentSong = computed(() => songList.value[songIdx.value] || null)
const displayLyric = computed(() => currentSong.value?.lrc || '')

function setHint(msg, autoClearMs = 0) {
  hint.value = msg
  if (hintTimer) { clearTimeout(hintTimer); hintTimer = null }
  if (msg && autoClearMs > 0) {
    hintTimer = setTimeout(() => { hint.value = ''; hintTimer = null }, autoClearMs)
  }
}

onMounted(async () => {
  try {
    const cfg = await siteConfigApi.getPublic()
    const raw = cfg?.home_music_list
    let list = []
    if (raw) { try { const a = JSON.parse(raw); if (Array.isArray(a)) list = a.filter(s => s && s.url) } catch {} }
    songList.value = list.length ? list : props.fallback
  } catch { songList.value = props.fallback }
  if (!songList.value.length) setHint('暂无音乐，请在后台添加歌单')
})

function toggleMusic() {
  if (!currentSong.value) {
    setHint('暂无可播放的音乐，请在后台添加', 4000)
    return
  }
  if (musicPlaying.value) pause()
  else play()
}

function play() {
  const el = audioRef.value
  if (!el || !currentSong.value?.url) return
  setHint('')
  wantPlay.value = true
  el.play()
    .then(() => { musicPlaying.value = true })
    .catch((err) => {
      musicPlaying.value = false
      // 浏览器自动播放策略拦截 ≠ 资源加载失败，两者提示不同
      if (err?.name === 'NotAllowedError') setHint('请点击播放按钮开始（浏览器限制自动播放）', 4000)
      else if (err?.name === 'NotSupportedError') markBrokenAndSkip('音频格式不支持')
      else setHint('播放失败，请稍后重试', 4000)
    })
}
function pause() { audioRef.value?.pause(); musicPlaying.value = false; wantPlay.value = false }

/** 全部曲目都坏掉时返回 true —— 用于终止跳转，避免无限递归 */
function allBroken() {
  return songList.value.length > 0 && brokenIdx.value.size >= songList.value.length
}

function markBrokenAndSkip(reason) {
  if (!songList.value.length) return
  brokenIdx.value.add(songIdx.value)
  musicPlaying.value = false
  if (allBroken()) {
    wantPlay.value = false
    setHint(`音乐资源加载失败（${reason}）`)
    return
  }
  setHint('已跳过无法播放的曲目', 3000)
  step(1)
}

function onError() {
  // <audio> 加载失败（404 / 网络错误 / 解码失败）。
  // 修复前这里没有任何处理，坏链接和「暂停」在界面上完全一样 —— 正是本次 bug 的表象。
  // src 为空时部分浏览器也会触发 error，此时不应把曲目标记为损坏
  if (!currentSong.value?.url) return
  const code = audioRef.value?.error?.code
  const reason = code === 4 ? '资源不存在或格式不支持' : code === 2 ? '网络错误' : '加载失败'
  markBrokenAndSkip(reason)
}

function onStalled() {
  if (musicPlaying.value) setHint('缓冲中…', 3000)
}

/** 按 dir 方向找到下一个未损坏的曲目 */
function step(dir) {
  const n = songList.value.length
  if (!n) return
  if (allBroken()) { setHint('音乐资源加载失败'); return }
  let i = songIdx.value
  for (let tried = 0; tried < n; tried++) {
    i = (i + dir + n) % n
    if (!brokenIdx.value.has(i)) break
  }
  songIdx.value = i
  resetAndPlay()
}

function nextSong() { step(1) }
function prevSong() { step(-1) }

function resetAndPlay() {
  musicProgress.value = 0; currentTime.value = 0; duration.value = 0
  if (!wantPlay.value) return
  // 等 :src 绑定更新到 DOM 后再 load/play（原实现用 setTimeout(50) 猜时序，不可靠）
  nextTick(() => {
    const el = audioRef.value
    if (!el) return
    el.load()
    play()
  })
}

function onTimeUpdate() {
  const el = audioRef.value
  if (!el) return
  currentTime.value = el.currentTime
  if (el.duration) musicProgress.value = (el.currentTime / el.duration) * 100
}
function onLoaded() { duration.value = audioRef.value?.duration || 0 }

function seekMusic(e) {
  const el = audioRef.value
  if (!el || !el.duration) return
  const rect = e.currentTarget.getBoundingClientRect()
  const pct = (e.clientX - rect.left) / rect.width
  el.currentTime = pct * el.duration
}

function fmtTime(s) {
  if (!s || isNaN(s)) return '0:00'
  const m = Math.floor(s / 60), sec = Math.floor(s % 60)
  return `${m}:${String(sec).padStart(2, '0')}`
}

onUnmounted(() => {
  pause()
  if (hintTimer) { clearTimeout(hintTimer); hintTimer = null }
})
</script>

<style scoped>
.mp-panel { padding: 18px; }
.mp-head { display: flex; align-items: center; justify-content: center; margin-bottom: 12px; }
.music-title { display: inline-flex; align-items: center; gap: 6px; color: var(--nature-green-dark); font-size: 16px; font-weight: 800; letter-spacing: 1.5px; font-family: var(--trendy-font); }
.music-player { display: flex; flex-direction: column; align-items: center; gap: 8px; }

.music-disc-wrap { margin-bottom: 4px; }
.music-disc { width: 90px; height: 90px; border-radius: 50%; overflow: hidden; border: 4px solid rgba(255,255,255,0.8); box-shadow: 0 6px 20px rgba(0,0,0,0.18); }
.music-disc.spinning { animation: discSpin 8s linear infinite; }
@keyframes discSpin { to { transform: rotate(360deg); } }
.disc-img { width: 100%; height: 100%; object-fit: cover; }

.music-lyrics { text-align: center; min-height: 22px; }
.lyric-line { font-size: 13px; color: var(--nature-green-dark); font-weight: 600; }
.music-song-name { font-size: 15px; font-weight: 700; color: #2e5a2e; font-family: var(--trendy-font); }

.music-controls { display: flex; align-items: center; gap: 10px; width: 100%; }
.music-play-btn { width: 40px; height: 40px; border-radius: 50%; border: none; background: var(--nature-gradient); color: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 4px 14px rgba(76,175,80,0.4); transition: all 0.3s; }
.music-play-btn:hover { transform: scale(1.1); }
.music-ctl-btn { width: 30px; height: 30px; border-radius: 50%; border: none; background: var(--nature-green-pale); color: var(--nature-green-dark); cursor: pointer; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: all 0.3s; }
.music-ctl-btn:hover { background: var(--nature-green-light); color: #fff; }
.music-progress { flex: 1; height: 6px; background: rgba(129,199,132,0.25); border-radius: 3px; cursor: pointer; overflow: hidden; }
.music-progress-fill { height: 100%; background: var(--nature-gradient); border-radius: 3px; transition: width 0.2s linear; }
.music-song-artist { font-size: 12px; color: #8aa88a; }
.music-time { font-size: 11px; color: #a0b0a0; font-family: var(--trendy-font); }
.music-hint {
  font-size: 11px; color: #c98a5a;
  background: rgba(255, 183, 77, 0.14);
  padding: 3px 10px; border-radius: 10px;
  text-align: center; line-height: 1.5;
}
.music-license { font-size: 10px; color: #b3c4b3; letter-spacing: 0.2px; }

/* ============ bar 变体：顶栏里的一行 ============ */
.music-root { min-width: 0; }

.bar-row {
  display: flex;
  align-items: center;
  gap: 7px;
  height: 30px;
  padding: 0 4px 0 3px;
  border-radius: 15px;
  background: rgba(76, 175, 80, 0.09);
  cursor: pointer;
  max-width: 210px;
  transition: background 0.25s;
}
.bar-row:active { background: rgba(76, 175, 80, 0.18); }

.bar-disc {
  width: 22px; height: 22px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--nature-green-pale, #eef6ee);
  color: var(--nature-green-dark);
  display: flex; align-items: center; justify-content: center;
  border: 1.5px solid rgba(255, 255, 255, 0.9);
}
.bar-disc.spinning { animation: discSpin 6s linear infinite; }

.bar-text { flex: 1; min-width: 0; display: flex; align-items: baseline; gap: 6px; }
.bar-name {
  font-size: 12px; font-weight: 600; color: var(--nature-green-dark);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  font-family: var(--trendy-font);
}
.bar-sub { font-size: 10px; color: #c98a5a; white-space: nowrap; }

.bar-play {
  width: 24px; height: 24px;
  border-radius: 50%; border: none;
  background: var(--nature-gradient, linear-gradient(135deg, #66bb6a, #43a047));
  color: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  transition: transform 0.2s;
}
.bar-play:active { transform: scale(0.9); }

/* ============ 完整面板 ============ */
/* card 变体（PC 侧栏）：teleport 关掉，遮罩层退化成普通块 */
.mp-mask.inline { position: static; display: block; background: none; padding: 0; backdrop-filter: none; }
.mp-mask.inline .mp-panel { width: auto; }

.mp-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(20, 34, 24, 0.42);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.mp-mask:not(.inline) .mp-panel { width: 300px; max-width: 90vw; }

.mp-close {
  position: absolute;
  top: 12px; right: 12px;
  width: 28px; height: 28px;
  border-radius: 50%; border: none;
  background: var(--nature-green-pale, #eef6ee);
  color: var(--nature-green-dark);
  font-size: 18px; line-height: 1; cursor: pointer;
  transition: background 0.25s;
}
.mp-close:active { background: var(--nature-green-light); color: #fff; }
.mp-mask:not(.inline) .mp-panel { position: relative; }

.mp-pop-enter-active { transition: opacity 0.28s ease; }
.mp-pop-leave-active { transition: opacity 0.2s ease; }
.mp-pop-enter-from, .mp-pop-leave-to { opacity: 0; }
.mp-pop-enter-active .mp-panel { transition: transform 0.32s cubic-bezier(0.34, 1.4, 0.64, 1); }
.mp-pop-enter-from .mp-panel { transform: scale(0.9); }

@media (prefers-reduced-motion: reduce) {
  .music-disc.spinning, .bar-disc.spinning { animation: none; }
  .mp-pop-enter-active, .mp-pop-leave-active, .mp-pop-enter-active .mp-panel { transition: none; }
}
</style>
