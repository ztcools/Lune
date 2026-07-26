<template>
  <div class="settings-page">
    <div class="page-header">
      <h2>⚙️ 网站设置</h2>
      <span class="header-sub">管理站点基础信息和各页面背景</span>
    </div>

    <!-- ====== 基础设置卡片 ====== -->
    <div class="section-card">
      <div class="section-title">📋 基础信息</div>
      <div class="basic-grid">
        <div class="form-item">
          <label>网站名称</label>
          <el-input v-model="basic.site_name" placeholder="Lune" @change="saveBasic('site_name')" />
        </div>
        <div class="form-item">
          <label>网站标题</label>
          <el-input v-model="basic.site_title" placeholder="Lune - 记录美好生活" @change="saveBasic('site_title')" />
        </div>
        <div class="form-item">
          <label>网站描述</label>
          <el-input v-model="basic.site_description" type="textarea" :rows="2" placeholder="个人博客..." @change="saveBasic('site_description')" />
        </div>
        <div class="form-item">
          <label>页脚信息</label>
          <el-input v-model="basic.site_footer" placeholder="© 2024 Lune" @change="saveBasic('site_footer')" />
        </div>
        <div class="form-item">
          <label>网站公告</label>
          <el-input v-model="noticeStr" type="textarea" :rows="2" placeholder="一行一条公告" @change="saveNotices" />
        </div>
        <div class="form-item">
          <label>网站 Logo (Favicon)</label>
          <div class="logo-row">
            <el-image v-if="basic.site_logo" :src="basic.site_logo" class="logo-preview" fit="contain" />
            <el-button size="small" @click="openPicker('site_logo', false)">{{ basic.site_logo ? '更换' : '选择' }}</el-button>
            <el-button v-if="basic.site_logo" size="small" type="danger" plain @click="clearBasic('site_logo')">清除</el-button>
          </div>
        </div>
        <div class="form-item">
          <label>开放注册</label>
          <el-switch v-model="basic.enable_register" active-value="true" inactive-value="false" @change="saveBasic('enable_register')" />
        </div>
        <div class="form-item">
          <label>开放评论</label>
          <el-switch v-model="basic.enable_comment" active-value="true" inactive-value="false" @change="saveBasic('enable_comment')" />
        </div>
      </div>
    </div>

    <!-- ====== 页面背景设置 ====== -->
    <div class="section-card">
      <div class="section-title">🖼️ 页面背景设置</div>
      <p class="section-hint">每个区域可添加多张图片，页面将随机选取显示</p>

      <el-collapse v-model="activeBgSections" class="bg-collapse">
        <el-collapse-item v-for="bg in bgSections" :key="bg.key" :name="bg.key">
          <template #title>
            <div class="bg-title">
              <span class="bg-icon">{{ bg.icon }}</span>
              <span class="bg-label">{{ bg.label }}</span>
              <el-tag size="small" round :type="bgImages[bg.key]?.length ? 'success' : 'info'">
                {{ bgImages[bg.key]?.length || 0 }} 张
              </el-tag>
            </div>
          </template>

          <div class="bg-content">
            <!-- 图片网格 -->
            <div class="bg-grid" v-if="bgImages[bg.key]?.length">
              <div v-for="(url, i) in bgImages[bg.key]" :key="i" class="bg-img-card">
                <div class="bg-img-wrap">
                  <el-image :src="url" fit="cover" :preview-src-list="bgImages[bg.key]" :initial-index="i" />
                </div>
                <span class="bg-img-name">{{ getShortName(url) }}</span>
                <el-button class="bg-img-del" circle size="small" :icon="Close" @click="removeBg(bg.key, i)" />
              </div>
            </div>
            <el-empty v-else description="暂无图片，点击下方按钮添加" :image-size="48" />

            <!-- 添加按钮 -->
            <div class="add-bg-row">
              <el-button type="primary" plain :icon="Plus" @click="openBgAdd(bg.key)">添加图片</el-button>
              <el-button v-if="curBgAddKey === bg.key" size="small" @click="curBgAddKey = null">收起</el-button>
            </div>

            <!-- 添加面板 -->
            <div v-if="curBgAddKey === bg.key" class="bg-add-panel">
              <el-tabs v-model="addMethod" type="border-card" class="add-tabs">
                <el-tab-pane label="📁 本地上传" name="upload">
                  <el-upload class="bg-upload" :action="uploadUrl" :headers="uploadHeaders"
                    :on-success="r => onBgUploaded(bg.key, r)" :show-file-list="false" accept="image/*" drag>
                    <el-icon :size="32"><UploadFilled /></el-icon>
                    <div>拖拽或点击上传</div>
                  </el-upload>
                </el-tab-pane>
                <el-tab-pane label="🖼️ 资源库" name="picker">
                  <div class="picker-grid" v-if="pickerList.length">
                    <div v-for="r in pickerList" :key="r.id" class="picker-item"
                      :class="{ selected: curBgImgs.includes(r.path) }"
                      @click="togglePickerItem(r.path)">
                      <el-image :src="r.path" fit="cover" />
                      <span>{{ r.filename }}</span>
                    </div>
                  </div>
                  <el-empty v-else description="暂无资源" :image-size="40" />
                  <el-pagination v-if="pickerTotal > 12" small background layout="prev,pager,next"
                    :page-size="12" :total="pickerTotal" :current-page="pickerPage"
                    @current-change="p => { pickerPage = p; loadPicker() }" style="justify-content:center;margin-top:10px" />
                </el-tab-pane>
                <el-tab-pane label="🔗 URL 输入" name="url">
                  <div class="url-row">
                    <el-input v-model="urlInput" placeholder="图片 URL" @keyup.enter="addBgUrl(bg.key)" />
                    <el-button type="primary" @click="addBgUrl(bg.key)">添加</el-button>
                  </div>
                </el-tab-pane>
              </el-tabs>
              <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px">
                <el-button size="small" @click="finishBgAdd(bg.key)">完成</el-button>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- ====== 资源选择器弹窗（Logo等单图） ====== -->
    <el-dialog v-model="pickerDlg" title="选择图片" width="560px" :close-on-click-modal="false">
      <div class="picker-grid" v-if="pickerList.length">
        <div v-for="r in pickerList" :key="r.id" class="picker-item"
          :class="{ selected: pickerSelected === r.path }"
          @click="pickerSelected = r.path">
          <el-image :src="r.path" fit="cover" />
          <span>{{ r.filename }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无资源" :image-size="60" />
      <template #footer>
        <el-button @click="pickerDlg = false">取消</el-button>
        <el-button type="primary" @click="confirmPicker">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { siteConfigApi, resourceApi } from '../api/modules'
import { ElMessage } from 'element-plus'
import { Plus, Close, UploadFilled } from '@element-plus/icons-vue'

// ====== 基础设置 ======
const basic = reactive({})
const noticeStr = ref('')

const bgSections = [
  { key: 'landing_bg', label: 'Landing 页', icon: '🏠' },
  { key: 'home_hero_bg', label: '首页 · 顶部 Banner', icon: '📰' },
  { key: 'home_content_bg', label: '首页 · 内容区', icon: '📄' },
  { key: 'family_hero_bg', label: '家 · 顶部 Banner', icon: '👨‍👩‍👧' },
  { key: 'family_content_bg', label: '家 · 内容区', icon: '💝' },
  { key: 'treehole_danmaku_bg', label: '树洞 · 弹幕区', icon: '💬' },
  { key: 'treehole_content_bg', label: '树洞 · 时间线', icon: '📜' },
  { key: 'essay_hero_bg', label: '随笔 · 顶部 Banner', icon: '✍️' },
  { key: 'essay_content_bg', label: '随笔 · 内容区', icon: '📝' },
  { key: 'record_hero_bg', label: '记录 · 顶部 Banner', icon: '📸' },
  { key: 'record_content_bg', label: '记录 · 内容区', icon: '🎞️' },
]

const bgImages = reactive({})
bgSections.forEach(s => { bgImages[s.key] = [] })

const activeBgSections = ref([])
const curBgAddKey = ref(null)
const curBgImgs = ref([])
const addMethod = ref('upload')
const urlInput = ref('')
const uploadUrl = '/api/admin/resources/upload'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

// ====== 资源选择器 ======
const pickerList = ref([])
const pickerPage = ref(1)
const pickerTotal = ref(0)
const pickerDlg = ref(false)
const pickerSelected = ref('')
const pickerTarget = ref('')   // which basic field to set

// ====== Load ======
onMounted(async () => {
  await loadAllConfigs()
  await loadPicker()
})

async function loadAllConfigs() {
  try {
    const data = await siteConfigApi.getPublic()
    if (!data) return
    Object.assign(basic, data)

    // 解析左侧公告字符串
    try {
      const arr = JSON.parse(data.notices || '[]')
      noticeStr.value = Array.isArray(arr) ? arr.join('\n') : (data.notices || '')
    } catch { noticeStr.value = data.notices || '' }

    // 解析背景图 JSON 数组
    bgSections.forEach(s => {
      try {
        const arr = JSON.parse(data[s.key] || '[]')
        bgImages[s.key] = Array.isArray(arr) ? arr : []
      } catch { bgImages[s.key] = [] }
    })
  } catch (e) { ElMessage.error('加载设置失败') }
}

async function loadPicker() {
  try {
    const d = await resourceApi.list({ page: pickerPage.value, size: 12 })
    pickerList.value = d?.records || []
    pickerTotal.value = d?.total || 0
  } catch { }
}

// ====== Basic save ======
async function saveBasicConfig(key, value) {
  try {
    await siteConfigApi.save({ configKey: key, configValue: String(value || ''), configType: 'public', description: '' })
    ElMessage.success('已保存')
  } catch (e) { ElMessage.error('保存失败') }
}

function saveBasic(key) { saveBasicConfig(key, basic[key]) }
function clearBasic(key) { basic[key] = ''; saveBasicConfig(key, '') }

function saveNotices() {
  const lines = noticeStr.value.split('\n').filter(l => l.trim())
  const val = JSON.stringify(lines)
  basic.notices = val
  saveBasicConfig('notices', val)
}

// ====== Background image management ======
function openBgAdd(key) {
  curBgAddKey.value = key
  curBgImgs.value = [...bgImages[key]]
  addMethod.value = 'upload'
  urlInput.value = ''
  pickerPage.value = 1
  loadPicker()
}

function finishBgAdd(key) {
  bgImages[key] = [...curBgImgs.value]
  saveBgConfig(key)
  curBgAddKey.value = null
}

function addBgUrl(key) {
  const url = urlInput.value.trim()
  if (!url) return
  if (!curBgImgs.value.includes(url)) curBgImgs.value.push(url)
  urlInput.value = ''
}

function onBgUploaded(key, res) {
  if (res.code === 200 && res.data) {
    const path = res.data.path
    if (!curBgImgs.value.includes(path)) curBgImgs.value.push(path)
    ElMessage.success('上传成功')
  }
}

function removeBg(key, i) {
  bgImages[key].splice(i, 1)
  saveBgConfig(key)
}

function togglePickerItem(path) {
  const idx = curBgImgs.value.indexOf(path)
  if (idx >= 0) curBgImgs.value.splice(idx, 1)
  else curBgImgs.value.push(path)
}

async function saveBgConfig(key) {
  try {
    await siteConfigApi.save({ configKey: key, configValue: JSON.stringify(bgImages[key]), configType: 'public', description: '' })
    ElMessage.success('背景已更新')
  } catch (e) { ElMessage.error('保存失败') }
}

// ====== Picker (single image for basic) ======
function openPicker(field, multi = false) {
  pickerTarget.value = field
  pickerSelected.value = basic[field] || ''
  pickerPage.value = 1
  loadPicker()
  pickerDlg.value = true
}

function confirmPicker() {
  basic[pickerTarget.value] = pickerSelected.value
  saveBasicConfig(pickerTarget.value, pickerSelected.value)
  pickerDlg.value = false
}

function getShortName(url) {
  if (!url) return ''
  const n = url.split('/').pop()
  return n ? (n.length > 20 ? n.slice(0, 17) + '...' : n) : url
}
</script>

<style scoped>
/* ====== Page Layout ====== */
.settings-page {
  padding: 24px; max-width: 960px; margin: 0 auto;
}
.page-header {
  margin-bottom: 28px;
}
.page-header h2 {
  margin: 0 0 4px; font-size: 24px; font-weight: 700;
  background: linear-gradient(135deg, #43a047, #66bb6a);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.header-sub { font-size: 13px; color: #999; }

/* ====== Section Cards ====== */
.section-card {
  background: #fff; border-radius: 18px; padding: 28px; margin-bottom: 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,.04), 0 0 0 1px rgba(0,0,0,.03);
  transition: box-shadow .2s;
}
.section-card:hover { box-shadow: 0 4px 24px rgba(0,0,0,.08), 0 0 0 1px rgba(0,0,0,.05); }
.section-title {
  font-size: 17px; font-weight: 600; color: #2c3e50; margin-bottom: 20px;
  padding-bottom: 14px; border-bottom: 1px solid #f0f0f0;
}
.section-hint { font-size: 13px; color: #999; margin: -12px 0 16px; }

/* ====== Basic Grid ====== */
.basic-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 20px 28px;
}
.basic-grid .form-item { display: flex; flex-direction: column; gap: 6px; }
.basic-grid .form-item label {
  font-size: 13px; font-weight: 500; color: #666;
}
.logo-row { display: flex; align-items: center; gap: 12px; }
.logo-preview { width: 40px; height: 40px; border-radius: 8px; border: 1px solid #eee; }

/* ====== Background Collapse ====== */
.bg-collapse { border: none; }
.bg-collapse :deep(.el-collapse-item) {
  margin-bottom: 8px; border-radius: 14px !important; overflow: hidden;
  border: 1px solid #f0f0f0; transition: box-shadow .2s;
}
.bg-collapse :deep(.el-collapse-item:hover) { box-shadow: 0 2px 12px rgba(0,0,0,.04); }
.bg-collapse :deep(.el-collapse-item__header) {
  height: 52px; padding: 0 20px; font-size: 15px; font-weight: 500;
  background: #fafbfc; border-radius: 14px; border: none;
}
.bg-collapse :deep(.el-collapse-item__wrap) { border: none; padding: 0 20px 16px; }
.bg-collapse :deep(.el-collapse-item__content) { padding-bottom: 0; }

.bg-title { display: flex; align-items: center; gap: 10px; flex: 1; }
.bg-icon { font-size: 18px; }
.bg-label { font-weight: 500; color: #333; }
.bg-content { padding-top: 12px; }

/* ====== Image Grid ====== */
.bg-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 14px; margin-bottom: 16px;
}
.bg-img-card {
  position: relative; border-radius: 14px; overflow: hidden;
  background: #f8f9fb; border: 1px solid #eef0f4; transition: all .2s;
}
.bg-img-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,.08); }
.bg-img-wrap { width: 100%; height: 110px; overflow: hidden; border-radius: 14px 14px 0 0; }
.bg-img-wrap :deep(.el-image) { width: 100%; height: 100%; }
.bg-img-name {
  display: block; padding: 6px 10px; font-size: 11px; color: #888;
  text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.bg-img-del {
  position: absolute; top: 6px; right: 6px;
  opacity: 0; transition: opacity .2s; background: rgba(0,0,0,.45); color: #fff; border: none;
}
.bg-img-card:hover .bg-img-del { opacity: 1; }

/* ====== Add Panel ====== */
.add-bg-row { display: flex; align-items: center; gap: 8px; }
.bg-add-panel {
  margin-top: 14px; padding: 16px; background: #fafbfc; border-radius: 14px; border: 1px solid #f0f0f0;
}
.add-tabs :deep(.el-tabs__header) { margin-bottom: 12px; }
.bg-upload :deep(.el-upload) { width: 100%; }
.bg-upload :deep(.el-upload-dragger) {
  width: 100%; border-radius: 12px; padding: 30px;
}
.url-row { display: flex; gap: 10px; }

/* ====== Picker Grid ====== */
.picker-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px;
  max-height: 300px; overflow-y: auto;
}
.picker-item {
  border: 2px solid transparent; border-radius: 12px; overflow: hidden;
  cursor: pointer; transition: all .15s; background: #f5f6f8; text-align: center;
}
.picker-item:hover { border-color: #a0cfff; }
.picker-item.selected { border-color: #67c23a !important; box-shadow: 0 0 0 3px rgba(103,194,58,.15); }
.picker-item :deep(.el-image) { width: 100%; height: 80px; }
.picker-item span {
  display: block; padding: 4px 6px; font-size: 10px; color: #999;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

@media (max-width: 768px) {
  .basic-grid { grid-template-columns: 1fr; }
  .bg-grid { grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); }
  .picker-grid { grid-template-columns: repeat(3, 1fr); }
  .settings-page { padding: 12px; }
}
</style>
