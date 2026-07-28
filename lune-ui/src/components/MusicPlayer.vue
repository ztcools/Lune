<template>
  <div class="music-card glass-card shadow-box">
    <div class="music-title">🎵 来听首小曲</div>
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
        <button class="music-ctl-btn" @click="prevSong" title="上一首">
          <svg viewBox="0 0 24 24" width="16" height="16"><path d="M6 6h2v12H6zm3.5 6l8.5 6V6z" fill="currentColor"/></svg>
        </button>
        <button class="music-play-btn" @click="toggleMusic">
          <svg v-if="!musicPlaying" viewBox="0 0 24 24" width="20" height="20"><path d="M8 5v14l11-7z" fill="currentColor"/></svg>
          <svg v-else viewBox="0 0 24 24" width="20" height="20"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" fill="currentColor"/></svg>
        </button>
        <button class="music-ctl-btn" @click="nextSong" title="下一首">
          <svg viewBox="0 0 24 24" width="16" height="16"><path d="M16 6h2v12h-2zM6 18l8.5-6L6 6z" fill="currentColor"/></svg>
        </button>
        <div class="music-progress" @click="seekMusic">
          <div class="music-progress-fill" :style="{ width: musicProgress + '%' }" />
        </div>
      </div>
      <div class="music-song-artist">{{ currentSong?.artist || '在后台添加音乐吧' }}</div>
      <div class="music-time" v-if="duration > 0">{{ fmtTime(currentTime) }} / {{ fmtTime(duration) }}</div>
    </div>

    <!-- 隐藏的音频元素 -->
    <audio ref="audioRef" :src="currentSong?.url" @timeupdate="onTimeUpdate" @ended="nextSong" @loadedmetadata="onLoaded" preload="metadata"></audio>
  </div>
</template>

<script setup>
/**
 * 首页音乐播放器（行业成熟方案）
 * - 后台 site_config.home_music_list 配置真实音频 [{name,artist,url,cover,lrc}]
 * - HTML5 Audio 播放，支持进度/歌词/上下首/旋转唱片
 * - 无配置时不渲染播放（提示去后台添加）
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { siteConfigApi } from '../api/modules'

const props = defineProps({
  // 兜底纯音乐列表（Web Audio 合成，无配置时用）
  fallback: { type: Array, default: () => [] }
})

const audioRef = ref(null)
const musicPlaying = ref(false)
const musicProgress = ref(0)
const currentTime = ref(0)
const duration = ref(0)
const songList = ref([])
const songIdx = ref(0)

const currentSong = computed(() => songList.value[songIdx.value] || null)
const displayLyric = computed(() => currentSong.value?.lrc || '')

onMounted(async () => {
  try {
    const cfg = await siteConfigApi.getPublic()
    const raw = cfg?.home_music_list
    let list = []
    if (raw) { try { const a = JSON.parse(raw); if (Array.isArray(a)) list = a.filter(s => s && s.url) } catch {} }
    songList.value = list.length ? list : props.fallback
  } catch { songList.value = props.fallback }
})

function toggleMusic() {
  if (!currentSong.value) return
  if (musicPlaying.value) pause()
  else play()
}

function play() {
  const el = audioRef.value
  if (!el || !currentSong.value?.url) return
  el.play().then(() => { musicPlaying.value = true }).catch(() => { musicPlaying.value = false })
}
function pause() { audioRef.value?.pause(); musicPlaying.value = false }

function nextSong() {
  if (!songList.value.length) return
  songIdx.value = (songIdx.value + 1) % songList.value.length
  resetAndPlay()
}
function prevSong() {
  if (!songList.value.length) return
  songIdx.value = (songIdx.value - 1 + songList.value.length) % songList.value.length
  resetAndPlay()
}
function resetAndPlay() {
  musicProgress.value = 0; currentTime.value = 0
  if (musicPlaying.value) {
    // 等 audio src 更新后播放
    setTimeout(() => play(), 50)
  }
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

onUnmounted(() => pause())
</script>

<style scoped>
.music-card { padding: 18px; }
.music-title { color: var(--nature-green-dark); font-size: 16px; font-weight: 800; margin-bottom: 12px; letter-spacing: 1.5px; font-family: var(--trendy-font); text-align: center; }
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
</style>
