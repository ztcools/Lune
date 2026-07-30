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

    <!-- 访问分析 -->
    <div class="section-title">📈 访问分析</div>

    <el-row :gutter="16">
      <!-- 折线图 -->
      <el-col :xs="24" :md="14">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">近 30 天访问趋势</span>
            <span class="chart-tip">📊 按日聚合</span>
          </div>
          <div ref="trendChartRef" class="chart trend-chart"></div>
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
            <el-table-column prop="path" label="路径" min-width="160" show-overflow-tooltip>
              <template #default="{row}">
                <span class="path-text">{{ row.path }}</span>
              </template>
            </el-table-column>
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
import * as echarts from 'echarts'

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
let trendChart = null
let mapChart = null
let chinaMapLoaded = false

async function loadChinaMap() {
  if (chinaMapLoaded) return
  try {
    const resp = await fetch('/maps/china.json')
    const chinaJson = await resp.json()
    echarts.registerMap('china', chinaJson)
    chinaMapLoaded = true
  } catch (e) {
    console.error('加载中国地图失败', e)
  }
}

function renderTrendChart(data) {
  if (!trendChartRef.value) return
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
  if (!mapChartRef.value || !chinaMapLoaded) return
  if (!mapChart) mapChart = echarts.init(mapChartRef.value)
  const maxVal = Math.max(...data.map(d => d.value), 1)
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

async function loadData() {
  // 总览
  try { const d = await articleApi.list({ page: 1, size: 1 }); stats[0].value = d.total } catch (e) {}
  try { const d = await categoryApi.listAll(); stats[1].value = d.length } catch (e) {}
  try { const d = await userApi.list({ page: 1, size: 1 }); stats[2].value = d.total } catch (e) {}
  try {
    const d = await visitStatsApi.summary()
    stats[3].value = d.total || 0
    stats[3].sub = `今日 ${d.today || 0} · 独立IP ${d.uniqueIps || 0}`
  } catch (e) {}

  // 文章
  try { const d = await articleApi.list({ page: 1, size: 5 }); articles.value = d.records } catch (e) {}

  // 图表数据
  await loadChinaMap()
  await nextTick()
  try { const d = await visitStatsApi.trend(30); renderTrendChart(d) } catch (e) {}
  try { const d = await visitStatsApi.map(); renderMapChart(d) } catch (e) {}
  try { const d = await visitStatsApi.topIps(15); topIps.value = d } catch (e) {}
  try { const d = await visitStatsApi.recent(50); recentVisits.value = d } catch (e) {}
}

function onResize() {
  trendChart?.resize()
  mapChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', onResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  mapChart?.dispose()
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
