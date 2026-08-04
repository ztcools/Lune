<template>
  <div class="dashboard">
    <el-tag effect="dark" class="my-tag">📊 数据看板</el-tag>

    <!-- 总览卡片 -->
    <div class="stat-cards">
      <div v-for="s in stats" :key="s.label" class="stat-card" :style="{ background: s.gradient }">
        <div class="stat-icon">{{ s.icon }}</div>
        <div class="stat-body">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
          <div v-if="s.sub" class="stat-sub">{{ s.sub }}</div>
        </div>
      </div>
    </div>

    <!-- 加载失败集中提示：不静默失败，也不逐个弹窗 -->
    <el-alert
      v-if="loadErrors.length"
      class="load-error"
      type="warning"
      show-icon
      :closable="false"
      title="部分数据加载失败">
      <template #default>
        <span>{{ loadErrors.join('、') }} 未能加载，显示的数字可能不完整。</span>
        <el-button link type="primary" size="small" @click="loadData">重试</el-button>
      </template>
    </el-alert>

    <!-- 访问分析 -->
    <div class="section-title">📈 访问分析</div>

    <el-row :gutter="16">
      <!-- 折线图 -->
      <el-col :xs="24" :md="14">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">访问趋势</span>
            <div class="chart-switch">
              <button
                v-for="d in [7, 30, 90]"
                :key="d"
                class="switch-btn"
                :class="{ active: trendDays === d }"
                :disabled="trendLoading"
                @click="switchTrend(d)">
                {{ d }} 天
              </button>
            </div>
          </div>
          <div ref="trendChartRef" class="chart trend-chart" v-loading="trendLoading"></div>
        </div>
      </el-col>

      <!-- 中国地图 -->
      <el-col :xs="24" :md="10">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">访客地理分布</span>
            <span class="chart-tip">🗺 按省份</span>
          </div>
          <div ref="mapChartRef" class="chart map-chart"></div>

          <!-- 对账行：GeoLite2 免费库对部分中国 IP 没有省级数据，
               地图之和必然小于总访问量，这里把差额显式列出，四项相加 = total -->
          <div v-if="geoMeta" class="geo-meta">
            <span class="geo-pill matched">已定位 {{ geoMeta.matched }}</span>
            <span class="geo-pill unresolved">未识别 {{ geoMeta.unresolved }}</span>
            <span class="geo-pill overseas">海外 {{ geoMeta.overseas }}</span>
            <span class="geo-pill intranet">内网 {{ geoMeta.intranet }}</span>
            <span class="geo-total">合计 {{ geoMeta.total }}</span>
          </div>
          <div v-if="geoMeta && !geoMeta.geoAvailable" class="geo-warn">
            未加载 GeoLite2 离线库，仅记录 IP 不解析地区（见 docs/GEOIP-SETUP.md）
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <!-- Top IP -->
      <el-col :xs="24" :md="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">🔥 活跃 IP 排行</span>
            <span class="chart-tip">Top {{ topIps.length }}</span>
          </div>
          <el-table :data="topIps" size="small" max-height="380">
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="ip" label="IP 地址" width="130" />
            <el-table-column prop="location" label="位置" min-width="140" show-overflow-tooltip />
            <el-table-column prop="count" label="访问次数" width="90" align="center" sortable>
              <template #default="{row}">
                <span class="count-badge">{{ row.count }}</span>
              </template>
            </el-table-column>
            <el-table-column label="最后访问" width="150" align="center">
              <template #default="{row}">{{ formatTime(row.lastVisit) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 最近访问 -->
      <el-col :xs="24" :md="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">⏱ 最近访问</span>
            <span class="chart-tip">实时</span>
          </div>
          <el-table :data="recentVisits" size="small" max-height="380">
            <el-table-column label="时间" width="150" align="center">
              <template #default="{row}">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column prop="ip" label="IP" width="130" />
            <el-table-column prop="location" label="位置" min-width="120" show-overflow-tooltip />
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- 最近文章 -->
    <div class="section-title" style="margin-top:20px">📝 最近文章</div>
    <el-table :data="articles" border>
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
      <el-table-column prop="likeCount" label="点赞" width="80" align="center" />
      <el-table-column label="时间" width="160" align="center">
        <template #default="{row}">{{ new Date(row.createTime).toLocaleDateString() }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { articleApi, categoryApi, userApi, visitStatsApi } from '../api/modules'

// 按需引入：原先 import * as echarts from 'echarts' 会把全部图表与组件
// 打进 admin chunk（整包 ~1MB）。这里只用到折线图与地图两种。
import * as echarts from 'echarts/core'
import { LineChart, MapChart } from 'echarts/charts'
import {
  TooltipComponent, LegendComponent, GridComponent, VisualMapComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LineChart, MapChart,
  TooltipComponent, LegendComponent, GridComponent, VisualMapComponent,
  CanvasRenderer
])

const stats = reactive([
  { label: '文章总数', value: 0, icon: '📝', gradient: 'linear-gradient(135deg,#667eea 0%,#764ba2 100%)' },
  { label: '分类总数', value: 0, icon: '📂', gradient: 'linear-gradient(135deg,#f093fb 0%,#f5576c 100%)' },
  { label: '用户总数', value: 0, icon: '👥', gradient: 'linear-gradient(135deg,#4facfe 0%,#00f2fe 100%)' },
  { label: '总访问量', value: 0, icon: '👀', gradient: 'linear-gradient(135deg,#43e97b 0%,#38f9d7 100%)', sub: '今日 0 · 独立IP 0' }
])

const articles = ref([])
const topIps = ref([])
const recentVisits = ref([])
const trendChartRef = ref(null)
const mapChartRef = ref(null)
const trendDays = ref(30)
const trendLoading = ref(false)
/** 加载失败的面板名称，集中提示而不是逐个弹窗 */
const loadErrors = ref([])
/** 地图对账数据（各桶之和恒等于 total，见后端 /map 注释） */
const geoMeta = ref(null)

let trendChart = null
let mapChart = null
let chinaMapLoaded = false
let resizeObserver = null
/**
 * 组件是否仍挂载。所有 await 之后都要检查：
 * 请求在途时切走路由会走到 onUnmounted，此时容器已从文档移除，
 * 再 echarts.init 会在一个游离节点上建实例（宽高 0、永远不显示，
 * 且不会再被 dispose），既是内存泄漏也会吞掉后续渲染。
 */
let alive = true

async function loadChinaMap() {
  if (chinaMapLoaded) return
  try {
    const resp = await fetch('/maps/china.json')
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
    const chinaJson = await resp.json()
    echarts.registerMap('china', chinaJson)
    chinaMapLoaded = true
  } catch (e) {
    console.error('加载中国地图失败', e)
    loadErrors.value.push('中国地图底图')
  }
}

/** 容器可用（仍挂载、已插入文档）才允许创建/更新图表实例 */
function usable(el) {
  return alive && el && el.isConnected
}

function renderTrendChart(data) {
  if (!usable(trendChartRef.value)) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  const dates = data.map(d => d.date.slice(5)) // MM-DD
  const counts = data.map(d => d.count)
  const uniqueIps = data.map(d => d.uniqueIps)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['访问量', '独立IP'], right: 10, top: 0 },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#ccc' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: [
      {
        name: '访问量', type: 'line', smooth: true, data: counts,
        lineStyle: { color: '#667eea', width: 3 },
        itemStyle: { color: '#667eea' },
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(102,126,234,0.3)' },
              { offset: 1, color: 'rgba(102,126,234,0.02)' }
            ]
          }
        }
      },
      {
        name: '独立IP', type: 'line', smooth: true, data: uniqueIps,
        lineStyle: { color: '#43e97b', width: 2 },
        itemStyle: { color: '#43e97b' }
      }
    ]
  })
}

function renderMapChart(data) {
  if (!usable(mapChartRef.value) || !chinaMapLoaded) return
  if (!mapChart) mapChart = echarts.init(mapChartRef.value)
  // 展开传参在数组较大时会爆栈，且这里 data 可能为空，reduce 更稳妥
  const maxVal = data.reduce((m, d) => Math.max(m, d.value || 0), 0) || 1
  mapChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: p => `${p.name}<br/>访问量: ${p.value || 0}`
    },
    visualMap: {
      min: 0, max: maxVal,
      left: 10, bottom: 10,
      text: ['高', '低'],
      inRange: { color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695'] },
      textStyle: { color: '#666' }
    },
    series: [{
      type: 'map', map: 'china',
      roam: false,
      label: { show: false },
      emphasis: {
        label: { show: true, color: '#fff' },
        itemStyle: { areaColor: '#ffb74d' }
      },
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 1
      },
      data: data
    }]
  })
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  const now = new Date()
  const diff = (now - d) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  return d.toLocaleDateString() + ' ' + d.toTimeString().slice(0, 5)
}

/**
 * 统一的分片加载：任一面板失败只影响自己，并把失败面板名记下来集中提示。
 * 原实现是 8 个 `catch (e) {}` 空块 —— 接口 500、token 过期、地理库缺失
 * 全都表现为「数字是 0、图表空白」，与「确实没有访问」无法区分，
 * 排查时也没有任何线索。
 */
async function section(label, fn) {
  try {
    await fn()
  } catch (e) {
    console.error(`[Dashboard] ${label} 加载失败`, e)
    if (!loadErrors.value.includes(label)) loadErrors.value.push(label)
  }
}

async function loadTrend() {
  trendLoading.value = true
  try {
    const d = await visitStatsApi.trend(trendDays.value)
    if (!alive) return
    renderTrendChart(d || [])
  } finally {
    trendLoading.value = false
  }
}

/** 切换趋势区间（后端 days 已 clamp 到 7..90） */
async function switchTrend(days) {
  if (trendDays.value === days) return
  trendDays.value = days
  loadErrors.value = loadErrors.value.filter(x => x !== '访问趋势')
  await section('访问趋势', loadTrend)
}

async function loadData() {
  loadErrors.value = []

  await Promise.all([
    section('文章总数', async () => {
      const d = await articleApi.list({ page: 1, size: 1 })
      if (alive) stats[0].value = d?.total ?? 0
    }),
    section('分类总数', async () => {
      const d = await categoryApi.listAll()
      if (alive) stats[1].value = d?.length ?? 0
    }),
    section('用户总数', async () => {
      const d = await userApi.list({ page: 1, size: 1 })
      if (alive) stats[2].value = d?.total ?? 0
    }),
    section('访问总览', async () => {
      const d = await visitStatsApi.summary()
      if (!alive) return
      stats[3].value = d?.total ?? 0
      stats[3].sub = `今日 ${d?.today ?? 0} · 独立IP ${d?.uniqueIps ?? 0}`
    }),
    section('最近文章', async () => {
      const d = await articleApi.list({ page: 1, size: 5 })
      if (alive) articles.value = d?.records ?? []
    }),
    section('活跃 IP 排行', async () => {
      const d = await visitStatsApi.topIps(15)
      if (alive) topIps.value = d ?? []
    }),
    section('最近访问', async () => {
      const d = await visitStatsApi.recent(50)
      if (alive) recentVisits.value = d ?? []
    })
  ])

  if (!alive) return

  // 图表需要底图与 DOM 就绪后再渲染
  await loadChinaMap()
  await nextTick()
  if (!alive) return

  await Promise.all([
    section('访问趋势', loadTrend),
    section('访客地理分布', async () => {
      const d = await visitStatsApi.map()
      if (!alive) return
      geoMeta.value = d ?? null
      renderMapChart(d?.regions ?? [])
    })
  ])

  if (alive) observeResize()
}

function onResize() {
  trendChart?.resize()
  mapChart?.resize()
}

/**
 * 侧边栏折叠只改变内容区的 left，不会触发 window resize，
 * 单靠 window 监听会让图表停留在旧宽度（右侧留白或被裁切）。
 * 用 ResizeObserver 直接盯容器尺寸，顺带覆盖所有布局变化。
 */
function observeResize() {
  if (resizeObserver || typeof ResizeObserver === 'undefined') return
  resizeObserver = new ResizeObserver(() => {
    if (alive) onResize()
  })
  if (trendChartRef.value) resizeObserver.observe(trendChartRef.value)
  if (mapChartRef.value) resizeObserver.observe(mapChartRef.value)
}

onMounted(() => {
  alive = true
  loadData()
  window.addEventListener('resize', onResize)
})
onUnmounted(() => {
  alive = false
  window.removeEventListener('resize', onResize)
  resizeObserver?.disconnect()
  resizeObserver = null
  trendChart?.dispose()
  mapChart?.dispose()
  trendChart = null
  mapChart = null
})
</script>

<style scoped>
.my-tag { width: 100%; text-align: left; background: var(--lightYellow); border: none; height: 40px; line-height: 40px; font-size: 16px; color: var(--black); margin-bottom: 20px; }

/* 总览卡片（渐变彩色） */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}
.stat-icon {
  font-size: 40px;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.2));
}
.stat-body { flex: 1; min-width: 0; }
.stat-value {
  font-size: 32px;
  font-weight: 800;
  line-height: 1.2;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}
.stat-label {
  font-size: 13px;
  opacity: 0.95;
  margin-top: 4px;
  font-weight: 500;
  letter-spacing: 0.5px;
}
.stat-sub {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 4px;
  font-weight: 400;
}

/* 章节标题 */
.section-title {
  font-size: 17px;
  font-weight: 700;
  color: #2c3e50;
  margin: 20px 0 14px;
  padding-left: 12px;
  border-left: 4px solid var(--themeBackground);
  letter-spacing: 0.5px;
}

/* 图表卡片 */
.chart-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.06);
}
.chart-title {
  font-size: 15px;
  font-weight: 700;
  color: #2c3e50;
}
.chart-tip {
  font-size: 12px;
  color: #95a5a6;
}
.chart { width: 100%; }
.trend-chart { height: 320px; }
.map-chart { height: 320px; }

/* 加载失败提示（柔和，不打断操作） */
.load-error { margin-bottom: 16px; border-radius: 10px; }

/* 趋势区间切换 */
.chart-switch { display: flex; gap: 4px; }
.switch-btn {
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #fff;
  color: #7f8c8d;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.25s ease, color 0.25s ease, border-color 0.25s ease;
}
.switch-btn:hover:not(:disabled) { background: #f6f8fb; color: #5a6c7d; }
.switch-btn.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.12), rgba(118, 75, 162, 0.12));
  border-color: rgba(102, 126, 234, 0.35);
  color: #667eea;
  font-weight: 600;
}
.switch-btn:disabled { opacity: 0.6; cursor: default; }

/* 地理分布对账行 */
.geo-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(0, 0, 0, 0.06);
}
.geo-pill {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 8px;
  color: #5a6c7d;
  background: #f4f6f9;
}
.geo-pill.matched { background: rgba(69, 117, 180, 0.12); color: #4575b4; }
.geo-pill.unresolved { background: rgba(255, 183, 77, 0.16); color: #b07d24; }
.geo-pill.overseas { background: rgba(67, 233, 123, 0.14); color: #2e8b57; }
.geo-pill.intranet { background: rgba(0, 0, 0, 0.05); color: #8c98a4; }
.geo-total { font-size: 11px; color: #95a5a6; margin-left: auto; }
.geo-warn {
  margin-top: 8px;
  font-size: 11px;
  line-height: 1.5;
  color: #b07d24;
  background: rgba(255, 183, 77, 0.1);
  border-radius: 8px;
  padding: 6px 8px;
}

/* 表格 */
.count-badge {
  display: inline-block;
  padding: 3px 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 12px;
  font-weight: 700;
  font-size: 12px;
}
.path-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #7f8c8d;
}

@media (max-width: 768px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .stat-card { padding: 14px; gap: 10px; border-radius: 12px; }
  .stat-icon { font-size: 28px; }
  .stat-value { font-size: 22px; }
  .stat-label { font-size: 11px; }
  .trend-chart, .map-chart { height: 240px; }
}
</style>
