<template>
  <div class="resume-page">
    <!-- ===== Hero ===== -->
    <div class="hero-banner">
      <div class="bg-image" :style="{ backgroundImage: `url(${heroBg})` }"></div>
      <div class="bg-overlay"></div>
      <div class="hero-info">
        <h1 class="hero-title">山海志</h1>
        <p class="hero-subtitle">一程山海，一段光阴</p>
      </div>
      <div class="hero-wave"></div>
    </div>

    <!-- ===== 导航 ===== -->
    <div class="nav-card-wrap">
      <div class="nav-card">
        <div v-for="tab in tabs" :key="tab.key" class="nav-item" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
          <LineIcon :name="tab.icon" :size="17" />
          <span class="nav-label">{{ tab.label }}</span>
        </div>
      </div>
    </div>

    <!-- ===== 内容 ===== -->
    <div class="resume-content">
      <transition name="tab-fade" mode="out-in">
        <!-- 小传 -->
        <div v-if="activeTab === 'about'" key="about" class="tab-pane">
          <div class="profile-card paper-card">
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
              <div class="p-cell">
                <LineIcon name="gift" :size="20" class="p-cell-icon" />
                <span class="p-cell-label">出生</span><span class="p-cell-value">{{ profile.birthday }}</span>
              </div>
              <div class="p-cell">
                <LineIcon name="code" :size="20" class="p-cell-icon" />
                <span class="p-cell-label">技术栈</span><span class="p-cell-value">{{ profile.skills }}</span>
              </div>
              <div class="p-cell">
                <LineIcon name="droplet" :size="20" class="p-cell-icon" />
                <span class="p-cell-label">爱好</span><span class="p-cell-value">{{ profile.hobbies }}</span>
              </div>
              <div class="p-cell">
                <LineIcon name="mail" :size="20" class="p-cell-icon" />
                <span class="p-cell-label">邮箱</span><span class="p-cell-value">{{ profile.email }}</span>
              </div>
            </div>
            <div class="profile-links">
              <a v-if="profile.github" :href="profile.github" target="_blank" class="p-link github">
                <svg viewBox="0 0 16 16" width="18" height="18" fill="currentColor"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8z"/></svg>
                GitHub
              </a>
              <a v-if="profile.email" :href="`mailto:${profile.email}`" class="p-link email">
                <LineIcon name="mail" :size="17" />
                邮箱
              </a>
            </div>
          </div>
        </div>

        <!-- 履痕（左右交错时间线，点击展开详情） -->
        <div v-else-if="activeTab === 'work'" key="work" class="tab-pane">
          <AltTimeline
            v-if="workList.length"
            :items="workList"
            clickable
            line-color="rgba(47,111,106,0.2)"
            accent="#2f6f6a"
            bubble-width="340px"
            @item-click="openWork"
          >
            <template #default="{ item }">
              <span v-if="item.isCurrent" class="wt-current">在职</span>
              <div class="wt-period">{{ fmtRange(item.startDate, item.endDate, item.isCurrent) }}</div>
              <h3 class="wt-company">{{ item.company }}</h3>
              <div class="wt-role">
                {{ item.position }}
                <span v-if="item.location" class="wt-loc"><LineIcon name="pin" :size="13" />{{ item.location }}</span>
              </div>
              <div class="wt-more">查看详情<LineIcon name="arrow-right" :size="14" /></div>
            </template>
          </AltTimeline>
          <el-empty v-else description="暂无履痕" />
        </div>

        <!-- 造物集（预览卡片，点击展开详情） -->
        <div v-else key="project" class="tab-pane">
          <div class="preview-grid" v-if="projectList.length">
            <div
              v-for="(p, i) in projectList"
              :key="p.id"
              class="preview-card paper-card"
              :style="{ '--delay': i * 0.08 + 's' }"
              @click="openProject(p)"
            >
              <div class="pc-cover" v-if="p.cover">
                <LuneImage :src="p.cover" variant="cover" alt="" class="pc-cover-img" />
              </div>
              <div class="pc-body">
                <div class="preview-date" v-if="p.devPeriod"><LineIcon name="clock" :size="13" />{{ p.devPeriod }}</div>
                <h3 class="preview-title">{{ p.name }}</h3>
                <div class="preview-sub">{{ p.summary }}</div>
                <div class="preview-tech" v-if="parseTech(p.techStack).length">
                  <span v-for="t in parseTech(p.techStack).slice(0, 4)" :key="t" class="mini-tech">{{ t }}</span>
                </div>
                <div class="pc-foot">
                  <span class="pc-flag" v-if="hasVideo(p.media)"><LineIcon name="play" :size="12" />视频演示</span>
                  <span class="pc-more">查看详情<LineIcon name="arrow-right" :size="14" /></span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无造物" />
        </div>
      </transition>
    </div>

    <!-- ===== 履痕详情 ===== -->
    <transition name="detail-pop">
      <div v-if="detailWork" class="detail-overlay" @click.self="detailWork = null">
        <div class="detail-card">
          <button class="detail-close" @click="detailWork = null">×</button>
          <div class="detail-head">
            <span class="detail-badge"><LineIcon name="briefcase" :size="26" /></span>
            <div>
              <h2 class="detail-title">{{ detailWork.company }}</h2>
              <div class="detail-sub">
                {{ detailWork.position }}
                <span v-if="detailWork.location" class="detail-loc"><LineIcon name="pin" :size="13" />{{ detailWork.location }}</span>
              </div>
              <div class="detail-date">
                {{ fmtRange(detailWork.startDate, detailWork.endDate, detailWork.isCurrent) }}
                <span v-if="detailWork.isCurrent" class="wt-current static">在职</span>
              </div>
            </div>
          </div>
          <div class="detail-body">
            <div class="detail-section" v-if="detailWork.description">
              <div class="detail-sec-title"><LineIcon name="file-text" :size="16" />工作内容</div>
              <p class="detail-text">{{ detailWork.description }}</p>
            </div>
            <div class="detail-section" v-if="detailWork.responsibilities">
              <div class="detail-sec-title"><LineIcon name="target" :size="16" />核心职责</div>
              <p class="detail-text">{{ detailWork.responsibilities }}</p>
            </div>
            <div class="detail-section" v-if="workMedia.total">
              <div class="detail-sec-title"><LineIcon name="image" :size="16" />工作记录</div>
              <MediaGrid :images="workMedia.images" :videos="workMedia.videos" @play="playVideo" />
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- ===== 造物详情 ===== -->
    <transition name="detail-pop">
      <div v-if="detailProject" class="detail-overlay" @click.self="detailProject = null">
        <div class="detail-card">
          <button class="detail-close" @click="detailProject = null">×</button>
          <div class="detail-cover" v-if="detailProject.cover">
            <LuneImage :src="detailProject.cover" variant="hero" alt="" class="detail-cover-img" />
          </div>
          <div class="detail-head">
            <span class="detail-badge"><LineIcon name="layers" :size="26" /></span>
            <div>
              <h2 class="detail-title">{{ detailProject.name }}</h2>
              <div class="detail-sub">{{ detailProject.summary }}</div>
              <div class="detail-date" v-if="detailProject.devPeriod">
                <LineIcon name="clock" :size="14" />{{ detailProject.devPeriod }}
                <span v-if="detailProject.role" class="detail-role"><LineIcon name="user" :size="14" />{{ detailProject.role }}</span>
              </div>
            </div>
          </div>
          <div class="detail-body">
            <div class="detail-section" v-if="detailProject.description">
              <div class="detail-sec-title"><LineIcon name="book" :size="16" />项目背景</div>
              <p class="detail-text">{{ detailProject.description }}</p>
            </div>
            <div class="detail-section" v-if="parseTech(detailProject.techStack).length">
              <div class="detail-sec-title"><LineIcon name="tool" :size="16" />技术栈</div>
              <div class="detail-tech"><span v-for="t in parseTech(detailProject.techStack)" :key="t" class="tech-chip">{{ t }}</span></div>
            </div>
            <div class="detail-section" v-if="projMedia.total">
              <div class="detail-sec-title"><LineIcon name="image" :size="16" />效果展示</div>
              <MediaGrid :images="projMedia.images" :videos="projMedia.videos" @play="playVideo" />
            </div>
            <div class="detail-links" v-if="detailProject.projectUrl || detailProject.repoUrl">
              <a v-if="detailProject.projectUrl" :href="detailProject.projectUrl" target="_blank" class="d-link">
                <LineIcon name="link" :size="16" />在线预览
              </a>
              <a v-if="detailProject.repoUrl" :href="detailProject.repoUrl" target="_blank" class="d-link ghost">
                <LineIcon name="code" :size="16" />源码仓库
              </a>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- ===== 视频播放层 =====
         teleport 到 body：详情弹层有 backdrop-filter，会给 fixed 子元素造新的包含块，
         播放器留在里面会被卡片的 overflow 裁掉（和之前大图看不见是同一个坑）。 -->
    <teleport to="body">
      <transition name="detail-pop">
        <div v-if="playingUrl" class="video-layer" @click.self="playingUrl = null">
          <button class="video-close" @click="playingUrl = null">×</button>
          <video :src="playingUrl" class="video-el" controls autoplay playsinline></video>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { resumeApi, userProfileApi, siteConfigApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
import LuneImage from '../../components/LuneImage.vue'
import AltTimeline from '../../components/AltTimeline.vue'
import LineIcon from '../../components/LineIcon.vue'
import MediaGrid from './MediaGrid.vue'

const heroBg = usePageBackground('resumeHero')

const tabs = [
  { key: 'about', label: '小传', icon: 'feather' },
  { key: 'work', label: '履痕', icon: 'route' },
  { key: 'project', label: '造物集', icon: 'layers' }
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
const playingUrl = ref(null)

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
function playVideo(url) { playingUrl.value = url }

function parseMedia(json) { if (!json) return []; try { const a = JSON.parse(json); return Array.isArray(a) ? a.filter(m => m && m.url) : [] } catch { return [] } }
function parseTech(json) { if (!json) return []; try { const a = JSON.parse(json); return Array.isArray(a) ? a : [] } catch { return [] } }

// media 原来在模板里一行 parseMedia 三次（每次都重新 JSON.parse），拆成 computed
function splitMedia(json) {
  const all = parseMedia(json)
  const images = all.filter(m => m.type === 'image')
  const videos = all.filter(m => m.type === 'video')
  return { images, videos, total: images.length + videos.length }
}
const workMedia = computed(() => splitMedia(detailWork.value?.media))
const projMedia = computed(() => splitMedia(detailProject.value?.media))

// 列表卡片上的「视频演示」标记
function hasVideo(json) { return parseMedia(json).some(m => m.type === 'video') }

function fmt(d) { if (!d) return ''; const dt = new Date(d); return `${dt.getFullYear()}.${String(dt.getMonth() + 1).padStart(2, '0')}` }
function fmtRange(s, e, cur) { return `${fmt(s)} — ${cur ? '至今' : fmt(e)}` }
</script>

<style scoped>
/* 墨青色系只在本页生效（局部变量，不动 variables.css 的全站绿） */
.resume-page {
  --ink: #2f6f6a;
  --ink-deep: #1f4f4b;
  --ink-soft: #5c8a86;
  --ink-pale: #edf5f4;
  --ink-line: rgba(47, 111, 106, 0.18);
  --paper-shadow: 0 6px 20px rgba(15, 40, 40, 0.06);
  --paper-shadow-hover: 0 14px 34px rgba(15, 40, 40, 0.13);
}

/* ===== Hero ===== */
.hero-banner { position: relative; height: 40vh; min-height: 280px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.bg-image { position: absolute; inset: 0; background-size: cover; background-position: center; }
/* 原来是绿→橙渐变，是「像 AI 生成」的主要来源，换成中性墨色 */
.bg-overlay { position: absolute; inset: 0; background: linear-gradient(160deg, rgba(16, 36, 38, 0.58), rgba(31, 79, 75, 0.34)); }
.hero-info { position: relative; z-index: 2; text-align: center; color: #fff; }
.hero-title { font-family: var(--calligraphy-font); font-size: 52px; font-weight: 700; letter-spacing: 10px; margin: 0 0 14px; text-shadow: 0 4px 24px rgba(0,0,0,0.35); }
.hero-subtitle { font-family: var(--handwriting-font); font-size: 19px; opacity: 0.92; letter-spacing: 4px; }
.hero-wave { position: absolute; bottom: -2px; left: 0; width: 100%; height: 56px; background: var(--background); border-radius: 50% 50% 0 0 / 100% 100% 0 0; z-index: 3; }

/* ===== 导航 ===== */
.nav-card-wrap { display: flex; justify-content: center; margin: -30px auto 0; position: relative; z-index: 10; padding: 0 16px; }
.nav-card { display: flex; gap: 6px; background: rgba(255,255,255,0.92); backdrop-filter: blur(20px); border-radius: 18px; padding: 7px; box-shadow: 0 10px 30px rgba(15,40,40,0.12); border: 1px solid rgba(255,255,255,0.8); }
.nav-item { display: flex; align-items: center; gap: 7px; padding: 10px 22px; border-radius: 13px; cursor: pointer; color: var(--ink-soft); transition: color 0.28s ease, background 0.28s ease; user-select: none; }
.nav-label { font-family: var(--calligraphy-font); font-size: 17px; font-weight: 600; letter-spacing: 2px; }
.nav-item:hover { color: var(--ink-deep); background: var(--ink-pale); }
.nav-item.active { background: var(--ink); color: #fff; }

/* ===== 内容 ===== */
.resume-content { max-width: 1060px; margin: 0 auto; padding: 36px 20px 70px; }
.tab-fade-enter-active, .tab-fade-leave-active { transition: all 0.32s ease; }
.tab-fade-enter-from { opacity: 0; transform: translateY(18px); }
.tab-fade-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 纸卡（参考随笔/记录页的纯白卡，去掉原来的渐变+光斑） ===== */
.paper-card {
  position: relative;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 16px;
  box-shadow: var(--paper-shadow);
}

/* ===== 小传 ===== */
.profile-card { padding: 46px 38px; text-align: center; }
.profile-avatar-wrap { position: relative; display: inline-block; margin-bottom: 16px; }
.profile-avatar { border: 4px solid #fff; box-shadow: 0 8px 24px rgba(47,111,106,0.22); }
.avatar-ring { position: absolute; inset: -10px; border-radius: 50%; border: 2px dashed var(--ink-line); animation: spin 20s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.profile-name { font-family: var(--calligraphy-font); font-size: 34px; font-weight: 700; color: var(--ink-deep); margin: 0 0 6px; letter-spacing: 2px; }
.profile-motto { font-size: 16px; color: var(--ink-soft); margin: 0 0 20px; font-family: var(--handwriting-font); letter-spacing: 1px; }
.profile-tags { display: flex; flex-wrap: wrap; gap: 9px; justify-content: center; margin-bottom: 28px; }
.p-tag { padding: 5px 15px; border-radius: 6px; background: var(--ink-pale); color: var(--ink-deep); font-size: 13px; font-weight: 600; border: 1px solid var(--ink-line); transition: background 0.3s, color 0.3s; }
.p-tag:hover { background: var(--ink); color: #fff; border-color: var(--ink); }
.profile-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); gap: 12px; margin-bottom: 28px; }
.p-cell { display: flex; flex-direction: column; align-items: flex-start; gap: 5px; padding: 16px 18px; border-radius: 12px; background: #fafbfb; border: 1px solid rgba(0,0,0,0.05); text-align: left; transition: border-color 0.3s, background 0.3s; }
.p-cell:hover { background: var(--ink-pale); border-color: var(--ink-line); }
.p-cell-icon { color: var(--ink); margin-bottom: 2px; }
.p-cell-label { font-size: 12px; color: #9aa8a6; font-weight: 600; letter-spacing: 1px; }
.p-cell-value { font-size: 14.5px; color: #3a4a49; font-weight: 600; word-break: break-all; }
.profile-links { display: flex; gap: 12px; justify-content: center; }
.p-link { display: flex; align-items: center; gap: 7px; padding: 11px 24px; border-radius: 10px; font-weight: 600; font-size: 15px; transition: transform 0.3s, box-shadow 0.3s; }
.p-link.github { background: #24292e; color: #fff; }
.p-link.email { background: var(--ink); color: #fff; }
.p-link:hover { transform: translateY(-2px); box-shadow: 0 8px 18px rgba(15,40,40,0.2); }

/* ===== 履痕（气泡内容，几何在 AltTimeline 里） ===== */
.wt-current { position: absolute; top: 16px; right: 16px; padding: 3px 10px; border-radius: 6px; background: var(--ink); color: #fff; font-size: 11px; font-weight: 600; letter-spacing: 1px; }
.wt-current.static { position: static; }
.wt-period { font-size: 12.5px; color: var(--ink); font-weight: 700; letter-spacing: 0.5px; margin-bottom: 8px; }
.wt-company { font-family: var(--calligraphy-font); font-size: 22px; font-weight: 700; color: #243433; margin: 0 0 6px; letter-spacing: 1px; }
.wt-role { font-size: 14.5px; color: #5a6a69; font-weight: 600; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.wt-loc { display: inline-flex; align-items: center; gap: 3px; font-size: 12.5px; color: #9aa8a6; font-weight: 500; }
.wt-more { display: flex; align-items: center; gap: 4px; font-size: 13px; color: var(--ink); font-weight: 600; margin-top: 12px; opacity: 0.55; transition: opacity 0.3s, gap 0.3s; }
.alt-bubble:hover .wt-more { opacity: 1; gap: 8px; }

/* ===== 造物集 ===== */
.preview-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.preview-card { overflow: hidden; cursor: pointer; transition: transform 0.35s ease, box-shadow 0.35s ease; animation: cardIn 0.5s ease backwards; animation-delay: var(--delay, 0s); }
@keyframes cardIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.preview-card:hover { transform: translateY(-5px); box-shadow: var(--paper-shadow-hover); }
.pc-cover { height: 152px; overflow: hidden; background: #f2f4f4; }
.pc-cover-img { width: 100%; height: 100%; }
.pc-body { padding: 20px 22px 18px; }
.preview-date { display: flex; align-items: center; gap: 5px; font-size: 12.5px; color: var(--ink); font-weight: 700; margin-bottom: 8px; }
.preview-title { font-family: var(--calligraphy-font); font-size: 21px; font-weight: 700; color: #243433; margin: 0 0 6px; letter-spacing: 1px; }
.preview-sub { font-size: 14px; color: #6a7a79; line-height: 1.7; margin-bottom: 12px; }
.preview-tech { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.mini-tech { padding: 3px 10px; border-radius: 5px; background: var(--ink-pale); color: var(--ink-deep); font-size: 11px; font-weight: 600; }
.pc-foot { display: flex; align-items: center; justify-content: space-between; gap: 10px; padding-top: 12px; border-top: 1px solid rgba(0,0,0,0.05); }
.pc-flag { display: inline-flex; align-items: center; gap: 4px; font-size: 11.5px; font-weight: 600; color: var(--ink); background: var(--ink-pale); padding: 3px 9px; border-radius: 5px; }
.pc-more { display: inline-flex; align-items: center; gap: 4px; margin-left: auto; font-size: 13px; color: var(--ink); font-weight: 600; opacity: 0.55; transition: opacity 0.3s, gap 0.3s; }
.preview-card:hover .pc-more { opacity: 1; gap: 8px; }

/* ===== 详情弹窗 ===== */
.detail-overlay { position: fixed; inset: 0; z-index: 1000; background: rgba(18, 32, 32, 0.45); backdrop-filter: blur(8px); display: flex; align-items: center; justify-content: center; padding: 24px; }
.detail-card { position: relative; width: 720px; max-width: 94vw; max-height: 86vh; overflow-y: auto; background: #fff; border-radius: 20px; box-shadow: 0 24px 70px rgba(0,0,0,0.28); padding: 32px 34px; }
.detail-pop-enter-active { transition: all 0.34s cubic-bezier(0.34,1.4,0.64,1); }
.detail-pop-leave-active { transition: all 0.2s ease-in; }
.detail-pop-enter-from { opacity: 0; transform: scale(0.9) translateY(24px); }
.detail-pop-leave-to { opacity: 0; transform: scale(0.94) translateY(12px); }
.detail-close { position: absolute; top: 16px; right: 16px; width: 38px; height: 38px; border-radius: 10px; border: none; background: rgba(255,255,255,0.9); color: #5a6a69; font-size: 22px; cursor: pointer; transition: background 0.3s, color 0.3s; z-index: 5; line-height: 1; }
.detail-close:hover { background: var(--ink); color: #fff; }
.detail-cover { margin: -32px -34px 22px; height: 200px; overflow: hidden; border-radius: 20px 20px 0 0; }
.detail-cover-img { width: 100%; height: 100%; }
.detail-head { display: flex; gap: 16px; align-items: flex-start; margin-bottom: 24px; }
.detail-badge { width: 54px; height: 54px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; background: var(--ink-pale); color: var(--ink); }
.detail-title { font-family: var(--calligraphy-font); font-size: 27px; font-weight: 700; color: #243433; margin: 0 0 6px; letter-spacing: 1px; }
.detail-sub { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; font-size: 15px; color: #5a6a69; font-weight: 600; margin-bottom: 6px; }
.detail-loc, .detail-role { display: inline-flex; align-items: center; gap: 3px; font-size: 13px; color: #9aa8a6; font-weight: 500; }
.detail-date { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 13px; color: var(--ink); font-weight: 700; }
.detail-section { margin-bottom: 24px; }
.detail-sec-title { display: flex; align-items: center; gap: 7px; font-size: 15px; font-weight: 700; color: var(--ink-deep); margin-bottom: 10px; padding-bottom: 8px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.detail-sec-title :deep(.line-icon) { color: var(--ink); }
.detail-text { font-size: 15px; color: #4a5a59; line-height: 1.9; white-space: pre-wrap; }
.detail-tech { display: flex; flex-wrap: wrap; gap: 8px; }
.tech-chip { padding: 5px 13px; border-radius: 6px; background: var(--ink-pale); color: var(--ink-deep); font-size: 13px; font-weight: 600; border: 1px solid var(--ink-line); }
.detail-links { display: flex; gap: 12px; padding-top: 20px; border-top: 1px solid rgba(0,0,0,0.06); }
.d-link { display: inline-flex; align-items: center; gap: 7px; padding: 10px 22px; border-radius: 10px; background: var(--ink); color: #fff; font-weight: 600; font-size: 14px; transition: transform 0.3s, box-shadow 0.3s; }
.d-link.ghost { background: var(--ink-pale); color: var(--ink-deep); }
.d-link:hover { transform: translateY(-2px); box-shadow: 0 8px 18px rgba(47,111,106,0.28); }

/* ===== 视频播放层（teleport 到 body，所以样式不带 scoped 依赖的祖先） ===== */
.video-layer { position: fixed; inset: 0; z-index: 3200; background: rgba(0,0,0,0.85); display: flex; align-items: center; justify-content: center; padding: 24px; }
.video-el { max-width: 92vw; max-height: 84vh; border-radius: 12px; background: #000; outline: none; }
.video-close { position: absolute; top: 20px; right: 22px; width: 42px; height: 42px; border-radius: 12px; border: none; background: rgba(255,255,255,0.14); color: #fff; font-size: 24px; line-height: 1; cursor: pointer; transition: background 0.3s; }
.video-close:hover { background: rgba(255,255,255,0.3); }

/* ===== 响应式 ===== */
@media screen and (max-width: 768px) {
  .hero-title { font-size: 34px; letter-spacing: 6px; }
  .hero-subtitle { font-size: 16px; letter-spacing: 2px; }
  .nav-item { padding: 9px 15px; }
  .nav-label { font-size: 15px; }
  .profile-card { padding: 34px 20px; }
  .preview-grid { grid-template-columns: 1fr; }
  .detail-card { padding: 24px 20px; border-radius: 16px; }
  .detail-cover { margin: -24px -20px 18px; height: 168px; border-radius: 16px 16px 0 0; }
  .detail-head { flex-direction: column; }
}

@media (prefers-reduced-motion: reduce) {
  .avatar-ring { animation: none; }
  .preview-card { animation: none; }
  .preview-card:hover, .p-link:hover, .d-link:hover { transform: none; }
}
</style>
