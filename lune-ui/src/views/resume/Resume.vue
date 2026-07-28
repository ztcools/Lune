<template>
  <div class="resume-page">
    <!-- ===== Hero ===== -->
    <div class="hero-banner">
      <div class="bg-image" :style="{ backgroundImage: `url(${heroBg})` }"></div>
      <div class="bg-overlay"></div>
      <div class="hero-info">
        <h1 class="hero-title">我的旅程</h1>
        <p class="hero-subtitle">在路上，永远热爱，永远向前 🌿</p>
      </div>
      <div class="hero-wave"></div>
    </div>

    <!-- ===== 导航 ===== -->
    <div class="nav-card-wrap">
      <div class="nav-card">
        <div v-for="tab in tabs" :key="tab.key" class="nav-item" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
          <span class="nav-icon">{{ tab.icon }}</span>
          <span class="nav-label">{{ tab.label }}</span>
        </div>
      </div>
    </div>

    <!-- ===== 内容 ===== -->
    <div class="resume-content">
      <transition name="tab-fade" mode="out-in">
        <!-- 个人介绍 -->
        <div v-if="activeTab === 'about'" key="about" class="tab-pane">
          <div class="profile-card deco-card">
            <div class="profile-avatar-wrap">
              <el-avatar :size="110" :src="profile.avatar" class="profile-avatar">{{ (profile.nickname || 'L').charAt(0) }}</el-avatar>
              <div class="avatar-ring"></div>
            </div>
            <h2 class="profile-name">{{ profile.nickname || 'Lune' }}</h2>
            <p class="profile-motto">「 {{ profile.motto }} 」</p>
            <div class="profile-tags">
              <span v-for="t in profile.tags" :key="t" class="p-tag">{{ t }}</span>
            </div>
            <div class="profile-grid">
              <div class="p-cell"><span class="p-cell-icon">🎂</span><span class="p-cell-label">出生</span><span class="p-cell-value">{{ profile.birthday }}</span></div>
              <div class="p-cell"><span class="p-cell-icon">💻</span><span class="p-cell-label">技术栈</span><span class="p-cell-value">{{ profile.skills }}</span></div>
              <div class="p-cell"><span class="p-cell-icon">🎨</span><span class="p-cell-label">爱好</span><span class="p-cell-value">{{ profile.hobbies }}</span></div>
              <div class="p-cell"><span class="p-cell-icon">✉️</span><span class="p-cell-label">邮箱</span><span class="p-cell-value">{{ profile.email }}</span></div>
            </div>
            <div class="profile-links">
              <a v-if="profile.github" :href="profile.github" target="_blank" class="p-link github">
                <svg viewBox="0 0 16 16" width="18" height="18" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8z"/></svg>
                GitHub
              </a>
              <a v-if="profile.email" :href="`mailto:${profile.email}`" class="p-link email">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>
                邮箱
              </a>
            </div>
          </div>
        </div>

        <!-- 工作经历（垂直时间线，点击展开详情） -->
        <div v-else-if="activeTab === 'work'" key="work" class="tab-pane">
          <div class="wtimeline" v-if="workList.length">
            <div class="wtl-track"></div>
            <div v-for="(w, i) in workList" :key="w.id" class="wtl-item" :style="{ '--delay': i * 0.15 + 's' }" @click="openWork(w)">
              <div class="wtl-node">
                <span class="wtl-node-dot"></span>
                <span class="wtl-node-ring"></span>
              </div>
              <div class="wtl-card deco-card">
                <div class="wtl-period">{{ fmtRange(w.startDate, w.endDate, w.isCurrent) }}</div>
                <h3 class="wtl-company">{{ w.company }}</h3>
                <div class="wtl-role">{{ w.position }}<span v-if="w.location" class="wtl-loc">📍 {{ w.location }}</span></div>
                <div class="wtl-more">查看详情 →</div>
                <span v-if="w.isCurrent" class="current-badge">在职</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无工作经历" />
        </div>

        <!-- 项目经历（预览卡片，点击展开详情） -->
        <div v-else key="project" class="tab-pane">
          <div class="preview-grid" v-if="projectList.length">
            <div v-for="(p, i) in projectList" :key="p.id" class="preview-card deco-card project-preview" :style="{ '--delay': i * 0.08 + 's' }" @click="openProject(p)">
              <div class="preview-badge proj-badge">🚀</div>
              <div class="preview-date" v-if="p.devPeriod">{{ p.devPeriod }}</div>
              <h3 class="preview-title">{{ p.name }}</h3>
              <div class="preview-sub">{{ p.summary }}</div>
              <div class="preview-tech" v-if="parseTech(p.techStack).length">
                <span v-for="t in parseTech(p.techStack).slice(0, 4)" :key="t" class="mini-tech">{{ t }}</span>
              </div>
              <div class="preview-more">点击查看详情 →</div>
            </div>
          </div>
          <el-empty v-else description="暂无项目经历" />
        </div>
      </transition>
    </div>

    <!-- ===== 工作详情弹窗 ===== -->
    <transition name="detail-pop">
      <div v-if="detailWork" class="detail-overlay" @click.self="detailWork = null">
        <div class="detail-card work-detail">
          <button class="detail-close" @click="detailWork = null">×</button>
          <div class="detail-head">
            <span class="detail-badge work-badge">💼</span>
            <div>
              <h2 class="detail-title">{{ detailWork.company }}</h2>
              <div class="detail-sub">{{ detailWork.position }}<span v-if="detailWork.location"> · 📍{{ detailWork.location }}</span></div>
              <div class="detail-date">{{ fmtRange(detailWork.startDate, detailWork.endDate, detailWork.isCurrent) }}<span v-if="detailWork.isCurrent" class="current-badge">在职</span></div>
            </div>
          </div>
          <div class="detail-body">
            <div class="detail-section" v-if="detailWork.description">
              <div class="detail-sec-title">📋 工作内容</div>
              <p class="detail-text">{{ detailWork.description }}</p>
            </div>
            <div class="detail-section" v-if="detailWork.responsibilities">
              <div class="detail-sec-title">🎯 核心职责</div>
              <p class="detail-text">{{ detailWork.responsibilities }}</p>
            </div>
            <div class="detail-section" v-if="parseMedia(detailWork.media).length">
              <div class="detail-sec-title">🖼️ 工作记录</div>
              <div class="detail-media">
                <el-image v-for="(m, mi) in parseMedia(detailWork.media).filter(x => x.type === 'image')" :key="mi" :src="m.url" fit="cover" class="detail-media-img" :preview-src-list="parseMedia(detailWork.media).filter(x => x.type === 'image').map(x => x.url)" :initial-index="mi" />
                <video v-for="(m, mi) in parseMedia(detailWork.media).filter(x => x.type === 'video')" :key="'v'+mi" :src="m.url" controls class="detail-media-video" preload="metadata"></video>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- ===== 项目详情弹窗 ===== -->
    <transition name="detail-pop">
      <div v-if="detailProject" class="detail-overlay" @click.self="detailProject = null">
        <div class="detail-card proj-detail">
          <button class="detail-close" @click="detailProject = null">×</button>
          <div class="detail-cover" v-if="detailProject.cover"><el-image :src="detailProject.cover" fit="cover" class="detail-cover-img" /></div>
          <div class="detail-head">
            <span class="detail-badge proj-badge">🚀</span>
            <div>
              <h2 class="detail-title">{{ detailProject.name }}</h2>
              <div class="detail-sub">{{ detailProject.summary }}</div>
              <div class="detail-date" v-if="detailProject.devPeriod">🕐 {{ detailProject.devPeriod }}<span v-if="detailProject.role"> · 👤 {{ detailProject.role }}</span></div>
            </div>
          </div>
          <div class="detail-body">
            <div class="detail-section" v-if="detailProject.description">
              <div class="detail-sec-title">📖 项目背景</div>
              <p class="detail-text">{{ detailProject.description }}</p>
            </div>
            <div class="detail-section" v-if="parseTech(detailProject.techStack).length">
              <div class="detail-sec-title">🛠️ 技术栈</div>
              <div class="detail-tech"><span v-for="t in parseTech(detailProject.techStack)" :key="t" class="tech-chip">{{ t }}</span></div>
            </div>
            <div class="detail-section" v-if="parseMedia(detailProject.media).length">
              <div class="detail-sec-title">🖼️ 效果展示</div>
              <div class="detail-media">
                <el-image v-for="(m, mi) in parseMedia(detailProject.media).filter(x => x.type === 'image')" :key="mi" :src="m.url" fit="cover" class="detail-media-img" :preview-src-list="parseMedia(detailProject.media).filter(x => x.type === 'image').map(x => x.url)" :initial-index="mi" />
                <video v-for="(m, mi) in parseMedia(detailProject.media).filter(x => x.type === 'video')" :key="'v'+mi" :src="m.url" controls class="detail-media-video" preload="metadata"></video>
              </div>
            </div>
            <div class="detail-links">
              <a v-if="detailProject.projectUrl" :href="detailProject.projectUrl" target="_blank" class="d-link">🔗 在线预览</a>
              <a v-if="detailProject.repoUrl" :href="detailProject.repoUrl" target="_blank" class="d-link">💻 源码仓库</a>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { resumeApi, userProfileApi, siteConfigApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'

const heroBg = usePageBackground('resumeHero')

const tabs = [
  { key: 'about', label: '个人介绍', icon: '🌱' },
  { key: 'work', label: '工作经历', icon: '💼' },
  { key: 'project', label: '项目经历', icon: '🚀' }
]
const activeTab = ref('about')

const profile = reactive({
  nickname: 'Lune', avatar: '', motto: '时刻保持思考，永远热爱生活',
  birthday: '2000-01-01', skills: 'Vue / Spring Boot / 全栈开发',
  hobbies: '编程 / 摄影 / 旅行 / 音乐', email: '', github: 'https://github.com/ztcools',
  tags: ['全栈开发', '热爱开源', '持续学习', '生活记录者']
})

const workList = ref([])
const projectList = ref([])
const detailWork = ref(null)
const detailProject = ref(null)

onMounted(async () => {
  try {
    const data = await resumeApi.getPublic()
    workList.value = data?.workExperiences || []
    projectList.value = data?.projects || []
  } catch (e) { /* silent */ }
  try {
    const p = await userProfileApi.getPublic(1)
    if (p) {
      profile.nickname = p.nickname || profile.nickname
      profile.avatar = p.avatar || ''
      profile.email = p.email || ''
      if (p.birthday) profile.birthday = p.birthday
    }
  } catch (e) { /* silent */ }
  try {
    const cfg = await siteConfigApi.getPublic()
    if (cfg) {
      if (cfg.resume_motto) profile.motto = cfg.resume_motto
      if (cfg.resume_skills) profile.skills = cfg.resume_skills
      if (cfg.resume_hobbies) profile.hobbies = cfg.resume_hobbies
      if (cfg.resume_github) profile.github = cfg.resume_github
      if (cfg.resume_tags) { try { const t = JSON.parse(cfg.resume_tags); if (Array.isArray(t) && t.length) profile.tags = t } catch {} }
    }
  } catch (e) { /* silent */ }
})

function openWork(w) { detailProject.value = null; detailWork.value = w }
function openProject(p) { detailWork.value = null; detailProject.value = p }

function parseMedia(json) { if (!json) return []; try { const a = JSON.parse(json); return Array.isArray(a) ? a.filter(m => m && m.url) : [] } catch { return [] } }
function parseTech(json) { if (!json) return []; try { const a = JSON.parse(json); return Array.isArray(a) ? a : [] } catch { return [] } }
function fmt(d) { if (!d) return ''; const dt = new Date(d); return `${dt.getFullYear()}.${String(dt.getMonth() + 1).padStart(2, '0')}` }
function fmtRange(s, e, cur) { return `${fmt(s)} — ${cur ? '至今' : fmt(e)}` }
</script>

<style scoped>
/* ===== Hero ===== */
.hero-banner { position: relative; height: 40vh; min-height: 280px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.bg-image { position: absolute; inset: 0; background-size: cover; background-position: center; }
.bg-overlay { position: absolute; inset: 0; background: linear-gradient(135deg, rgba(67,160,71,0.45), rgba(255,183,77,0.25)); }
.hero-info { position: relative; z-index: 2; text-align: center; color: #fff; }
.hero-title { font-family: var(--calligraphy-font); font-size: 48px; font-weight: 700; letter-spacing: 8px; margin: 0 0 12px; text-shadow: 0 4px 24px rgba(0,0,0,0.3); }
.hero-subtitle { font-size: 17px; opacity: 0.95; font-weight: 500; letter-spacing: 2px; }
.hero-wave { position: absolute; bottom: -2px; left: 0; width: 100%; height: 56px; background: var(--background); border-radius: 50% 50% 0 0 / 100% 100% 0 0; z-index: 3; }

/* ===== 导航 ===== */
.nav-card-wrap { display: flex; justify-content: center; margin: -32px auto 0; position: relative; z-index: 10; padding: 0 16px; }
.nav-card { display: flex; gap: 8px; background: rgba(255,255,255,0.78); backdrop-filter: blur(20px); border-radius: 40px; padding: 9px; box-shadow: var(--card-shadow-hover); border: var(--card-border); }
.nav-item { position: relative; display: flex; align-items: center; gap: 8px; padding: 11px 24px; border-radius: 30px; cursor: pointer; transition: all 0.35s cubic-bezier(0.34,1.56,0.64,1); user-select: none; }
.nav-icon { font-size: 19px; transition: transform 0.35s; }
.nav-label { font-family: var(--trendy-font); font-size: 16px; font-weight: 600; color: #5a7a5a; }
.nav-item:hover { transform: translateY(-3px); background: rgba(129,199,132,0.16); }
.nav-item:hover .nav-icon { transform: scale(1.25) rotate(-8deg); }
.nav-item.active { background: var(--nature-gradient); box-shadow: 0 6px 20px rgba(76,175,80,0.4); }
.nav-item.active .nav-label { color: #fff; }

/* ===== 内容 ===== */
.resume-content { max-width: 1060px; margin: 0 auto; padding: 36px 20px 70px; }
.tab-fade-enter-active, .tab-fade-leave-active { transition: all 0.32s ease; }
.tab-fade-enter-from { opacity: 0; transform: translateY(18px); }
.tab-fade-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 装饰卡片（动态渐变背景，去空洞留白） ===== */
.deco-card { position: relative; overflow: hidden; background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(232,245,233,0.85)); backdrop-filter: blur(16px); border-radius: var(--card-radius); box-shadow: var(--card-shadow); border: var(--card-border); }
.deco-card::before { content: ''; position: absolute; top: -40%; right: -20%; width: 70%; height: 120%; background: radial-gradient(circle, rgba(129,199,132,0.16), transparent 70%); pointer-events: none; }
.deco-card::after { content: ''; position: absolute; bottom: -40%; left: -15%; width: 60%; height: 110%; background: radial-gradient(circle, rgba(255,183,77,0.12), transparent 70%); pointer-events: none; }

/* ===== 个人卡片 ===== */
.profile-card { padding: 46px 38px; text-align: center; }
.profile-avatar-wrap { position: relative; display: inline-block; margin-bottom: 16px; z-index: 1; }
.profile-avatar { border: 4px solid #fff; box-shadow: 0 8px 24px rgba(76,175,80,0.3); }
.avatar-ring { position: absolute; inset: -10px; border-radius: 50%; border: 3px dashed var(--nature-green-light); animation: spin 14s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.profile-name { font-family: var(--calligraphy-font); font-size: 34px; font-weight: 700; color: #2e5a2e; margin: 0 0 6px; position: relative; z-index: 1; }
.profile-motto { font-size: 16px; color: var(--nature-green-dark); font-style: italic; margin: 0 0 18px; position: relative; z-index: 1; font-family: var(--handwriting-font); }
.profile-tags { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; margin-bottom: 28px; position: relative; z-index: 1; }
.p-tag { padding: 6px 16px; border-radius: 20px; background: var(--nature-green-pale); color: var(--nature-green-dark); font-size: 13px; font-weight: 600; border: 1px solid rgba(129,199,132,0.4); transition: all 0.3s; }
.p-tag:hover { transform: translateY(-3px) scale(1.05); background: var(--nature-green-light); color: #fff; box-shadow: 0 4px 12px rgba(76,175,80,0.3); }
.profile-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); gap: 14px; margin-bottom: 28px; position: relative; z-index: 1; }
.p-cell { display: flex; flex-direction: column; gap: 6px; padding: 18px; border-radius: var(--card-radius-sm); background: linear-gradient(135deg, rgba(232,245,233,0.7), rgba(255,248,225,0.7)); transition: all 0.3s; }
.p-cell:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(76,175,80,0.15); }
.p-cell-icon { font-size: 24px; }
.p-cell-label { font-size: 12px; color: #8aa88a; font-weight: 600; letter-spacing: 1px; }
.p-cell-value { font-size: 15px; color: #3a5a3a; font-weight: 600; }
.profile-links { display: flex; gap: 14px; justify-content: center; position: relative; z-index: 1; }
.p-link { display: flex; align-items: center; gap: 8px; padding: 12px 26px; border-radius: 30px; font-weight: 600; font-size: 15px; transition: all 0.3s; }
.p-link.github { background: #24292e; color: #fff; }
.p-link.email { background: var(--nature-gradient); color: #fff; }
.p-link:hover { transform: translateY(-3px) scale(1.04); box-shadow: 0 8px 20px rgba(0,0,0,0.2); }

/* ===== 预览卡片网格 ===== */
.preview-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 22px; }
.preview-card { padding: 26px; cursor: pointer; transition: all 0.4s cubic-bezier(0.34,1.4,0.64,1); animation: cardIn 0.5s ease backwards; animation-delay: var(--delay, 0s); }
@keyframes cardIn { from { opacity: 0; transform: translateY(24px) scale(0.96); } to { opacity: 1; transform: translateY(0) scale(1); } }
.preview-card:hover { transform: translateY(-8px) scale(1.02); box-shadow: var(--card-shadow-hover); }
.preview-badge { width: 52px; height: 52px; border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 26px; margin-bottom: 14px; position: relative; z-index: 1; }
.work-badge { background: linear-gradient(135deg, #e3f2fd, #bbdefb); }
.proj-badge { background: linear-gradient(135deg, #fff3e0, #ffe0b2); }
.preview-date { font-size: 13px; color: var(--nature-green-dark); font-weight: 700; margin-bottom: 8px; position: relative; z-index: 1; }
.preview-title { font-family: var(--trendy-font); font-size: 21px; font-weight: 700; color: #2e5a2e; margin: 0 0 6px; position: relative; z-index: 1; }
.preview-sub { font-size: 14px; color: #5a7a5a; font-weight: 600; margin-bottom: 12px; position: relative; z-index: 1; }
.preview-loc { color: #9ab89a; }
.preview-tech { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; position: relative; z-index: 1; }
.mini-tech { padding: 3px 10px; border-radius: 12px; background: var(--nature-green-pale); color: var(--nature-green-dark); font-size: 11px; font-weight: 600; }
.preview-more { font-size: 13px; color: var(--nature-green); font-weight: 600; opacity: 0; transform: translateX(-6px); transition: all 0.3s; position: relative; z-index: 1; }
.preview-card:hover .preview-more { opacity: 1; transform: translateX(0); }
.current-badge { position: absolute; top: 20px; right: 20px; padding: 3px 12px; border-radius: 14px; background: var(--nature-gradient); color: #fff; font-size: 11px; font-weight: 600; z-index: 1; }

/* ===== 工作时间线（垂直蜿蜒渐变轨道） ===== */
.wtimeline { position: relative; max-width: 720px; margin: 0 auto; padding: 20px 0 20px 8px; }
.wtl-track { position: absolute; left: 27px; top: 0; bottom: 0; width: 5px; border-radius: 5px; background: linear-gradient(180deg, #81c784, #ffb74d, #4fc3f7, #81c784); background-size: 100% 300%; animation: trackFlow 6s linear infinite; box-shadow: 0 0 12px rgba(129,199,132,0.4); }
@keyframes trackFlow { 0% { background-position: 0 0; } 100% { background-position: 0 300%; } }

.wtl-item { position: relative; padding: 0 0 34px 70px; cursor: pointer; animation: wtlIn 0.6s cubic-bezier(0.22,1,0.36,1) backwards; animation-delay: var(--delay, 0s); }
@keyframes wtlIn { from { opacity: 0; transform: translateX(-30px); } to { opacity: 1; transform: translateX(0); } }

.wtl-node { position: absolute; left: 16px; top: 26px; width: 28px; height: 28px; z-index: 2; }
.wtl-node-dot { position: absolute; inset: 7px; border-radius: 50%; background: var(--nature-gradient); box-shadow: 0 0 0 4px #fff, 0 0 14px rgba(76,175,80,0.6); transition: all 0.3s; }
.wtl-node-ring { position: absolute; inset: 0; border-radius: 50%; border: 2px solid var(--nature-green-light); opacity: 0; }
.wtl-item:hover .wtl-node-dot { transform: scale(1.3); }
.wtl-item:hover .wtl-node-ring { animation: nodePing 0.8s ease-out; }
@keyframes nodePing { 0% { transform: scale(0.6); opacity: 1; } 100% { transform: scale(1.8); opacity: 0; } }

.wtl-card { position: relative; padding: 22px 26px; transition: all 0.4s cubic-bezier(0.34,1.4,0.64,1); }
.wtl-card::before { content: ''; position: absolute; left: -8px; top: 32px; width: 16px; height: 16px; background: inherit; transform: rotate(45deg); border-radius: 3px; box-shadow: -3px 3px 6px rgba(0,0,0,0.04); }
.wtl-item:hover .wtl-card { transform: translateX(8px) scale(1.02); box-shadow: var(--card-shadow-hover); }
.wtl-period { display: inline-block; font-size: 13px; font-weight: 700; color: #fff; background: var(--nature-gradient); padding: 4px 14px; border-radius: 16px; margin-bottom: 12px; letter-spacing: 0.5px; }
.wtl-company { font-family: var(--trendy-font); font-size: 22px; font-weight: 700; color: #2e5a2e; margin: 0 0 6px; position: relative; z-index: 1; }
.wtl-role { font-size: 15px; color: var(--nature-green-dark); font-weight: 600; position: relative; z-index: 1; }
.wtl-loc { margin-left: 12px; font-size: 13px; color: #9ab89a; font-weight: 500; }
.wtl-more { font-size: 13px; color: var(--nature-green); font-weight: 600; margin-top: 12px; opacity: 0; transform: translateX(-6px); transition: all 0.3s; position: relative; z-index: 1; }
.wtl-item:hover .wtl-more { opacity: 1; transform: translateX(0); }
.wtl-card .current-badge { position: absolute; top: 20px; right: 20px; }

@media screen and (max-width: 768px) {
  .wtimeline { padding-left: 0; }
  .wtl-track { left: 18px; }
  .wtl-item { padding-left: 52px; }
  .wtl-node { left: 7px; }
  .wtl-company { font-size: 19px; }
}

/* ===== 详情弹窗 ===== */
.detail-overlay { position: fixed; inset: 0; z-index: 1000; background: rgba(30,50,30,0.4); backdrop-filter: blur(8px); display: flex; align-items: center; justify-content: center; padding: 24px; }
.detail-card { position: relative; width: 720px; max-width: 94vw; max-height: 86vh; overflow-y: auto; background: linear-gradient(135deg, #ffffff, #f2f9f2); border-radius: 30px; box-shadow: 0 24px 80px rgba(0,0,0,0.25); padding: 34px 36px; }
.detail-pop-enter-active { transition: all 0.4s cubic-bezier(0.34,1.56,0.64,1); }
.detail-pop-leave-active { transition: all 0.22s ease-in; }
.detail-pop-enter-from { opacity: 0; transform: scale(0.85) translateY(30px); }
.detail-pop-leave-to { opacity: 0; transform: scale(0.92) translateY(14px); }
.detail-close { position: absolute; top: 18px; right: 18px; width: 40px; height: 40px; border-radius: 50%; border: none; background: var(--nature-green-pale); color: var(--nature-green-dark); font-size: 24px; cursor: pointer; transition: all 0.3s; z-index: 5; line-height: 1; }
.detail-close:hover { background: var(--nature-green-light); color: #fff; transform: rotate(90deg); }
.detail-cover { margin: -34px -36px 20px; height: 200px; overflow: hidden; border-radius: 30px 30px 0 0; }
.detail-cover-img { width: 100%; height: 100%; }
.detail-head { display: flex; gap: 18px; align-items: flex-start; margin-bottom: 22px; }
.detail-badge { width: 64px; height: 64px; border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 32px; flex-shrink: 0; }
.detail-title { font-family: var(--trendy-font); font-size: 26px; font-weight: 700; color: #2e5a2e; margin: 0 0 6px; }
.detail-sub { font-size: 15px; color: #5a7a5a; font-weight: 600; margin-bottom: 6px; }
.detail-date { font-size: 13px; color: var(--nature-green-dark); font-weight: 700; display: flex; align-items: center; gap: 8px; }
.detail-date .current-badge { position: static; }
.detail-section { margin-bottom: 22px; }
.detail-sec-title { font-size: 16px; font-weight: 700; color: var(--nature-green-dark); margin-bottom: 10px; font-family: var(--trendy-font); }
.detail-text { font-size: 15px; color: #4a5a4a; line-height: 1.9; white-space: pre-wrap; }
.detail-tech { display: flex; flex-wrap: wrap; gap: 8px; }
.tech-chip { padding: 5px 14px; border-radius: 16px; background: var(--nature-gradient); color: #fff; font-size: 13px; font-weight: 600; }
.detail-media { display: flex; flex-wrap: wrap; gap: 10px; }
.detail-media-img { width: 110px; height: 110px; border-radius: 14px; cursor: pointer; transition: transform 0.3s; }
.detail-media-img:hover { transform: scale(1.05); }
.detail-media-video { width: 100%; max-width: 360px; border-radius: 14px; }
.detail-links { display: flex; gap: 14px; padding-top: 18px; border-top: 1px dashed rgba(129,199,132,0.4); }
.d-link { padding: 11px 24px; border-radius: 26px; background: var(--nature-gradient); color: #fff; font-weight: 600; font-size: 14px; transition: all 0.3s; }
.d-link:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(76,175,80,0.4); }

/* ===== 响应式 ===== */
@media screen and (max-width: 768px) {
  .hero-title { font-size: 30px; letter-spacing: 4px; }
  .nav-item { padding: 9px 15px; }
  .nav-label { font-size: 14px; }
  .profile-card { padding: 34px 20px; }
  .preview-grid { grid-template-columns: 1fr; }
  .detail-card { padding: 24px 20px; }
  .detail-head { flex-direction: column; }
}
</style>
