<template>
  <div>
    <div class="handle-box">
      <!-- 选项必须是 resource.type 里真实存在的值。原来那八项（userAvatar /
           articleCover / …）本项目从来没写过，选中即空列表。后端只按
           MIME 分 image / file 两类（见 ResourceServiceImpl#upload）。 -->
      <el-select clearable v-model="resourceType" placeholder="资源类型" class="handle-select mrb10" @change="search">
        <el-option label="图片" value="image" />
        <el-option label="文件" value="file" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button type="primary" @click="resourceDialog=true">新增资源</el-button>
    </div>

    <el-table :data="resources" border class="admin-table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center" />
      <el-table-column label="预览" width="140" align="center">
        <template #default="{ row }">
          <LuneImage v-if="isImage(row)" :src="row.path" variant="thumb" alt="" />
          <el-icon v-else :size="36" color="#ccc"><Document /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="文件名" min-width="180">
        <template #default="{ row }">
          <span class="filename-text">{{ row.filename }}</span>
          <div class="file-path">{{ row.path }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="80" align="center">
        <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
      </el-table-column>
      <el-table-column label="大小" width="80" align="center">
        <template #default="{ row }">{{ formatSize(row.size) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="150" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link style="color:#f56c6c" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="total,prev,pager,next" :current-page="page"
        :page-size="10" :total="total" @current-change="onPageChange" />
    </div>

    <!-- Upload Dialog -->
    <el-dialog title="新增资源" v-model="resourceDialog" width="520px"
      :close-on-click-modal="false" destroy-on-close center>
      <el-select v-model="resMethod" class="method-select">
        <el-option label="📁 本地上传" value="upload" />
        <el-option label="🔗 URL导入" value="url" />
      </el-select>

      <div v-if="resMethod==='url'" class="res-input-row">
        <el-input v-model="resUrl" placeholder="输入图片URL" style="flex:1" />
        <el-button type="primary" :loading="urlImporting" @click="importFromUrl" style="margin-left:10px">导入</el-button>
      </div>

      <div v-else class="res-input-row">
        <el-upload class="upload-drop" :action="'/api/admin/resources/upload'"
          :headers="uploadHeaders" :on-success="onUploadSuccess"
          :show-file-list="false" accept="image/*" drag>
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">拖拽图片或点击上传</div>
        </el-upload>
      </div>

      <div class="img-preview-row" v-if="resPreview">
        <LuneImage :src="resPreview" variant="thumb" alt="" />
        <span class="preview-path">{{ resPreview }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { resourceApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Document } from '@element-plus/icons-vue'
import LuneImage from '../components/LuneImage.vue'

const resources = ref([])
const page = ref(1)
const total = ref(0)
const resourceType = ref('')
const resourceDialog = ref(false)
const resMethod = ref('upload')
const resUrl = ref('')
const resPreview = ref('')
const urlImporting = ref(false)
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

onMounted(() => getResources())

async function getResources() {
  try {
    const params = { page: page.value, size: 10 }
    // 「资源类型」下拉此前只是摆设，参数从没送出去过
    if (resourceType.value) params.type = resourceType.value
    const data = await resourceApi.list(params)
    resources.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    console.error('加载资源列表失败:', e)
    ElMessage.error('加载资源列表失败')
  }
}

function search() { page.value=1; getResources() }
function onPageChange(p) { page.value=p; getResources() }

function onUploadSuccess(res) {
  if (res.code===200&&res.data) {
    ElMessage.success('上传成功')
    resourceDialog.value = false
    resPreview.value = ''
    getResources()
  } else ElMessage.error('上传失败')
}

// URL preview + import
watch(resUrl, (v) => { if (v && (v.startsWith('http') || v.startsWith('/'))) resPreview.value = v; else resPreview.value = '' })
async function importFromUrl() {
  if (!resUrl.value) { ElMessage.warning('请输入URL'); return }
  urlImporting.value = true
  try {
    await resourceApi.importUrl(resUrl.value)
    ElMessage.success('导入成功')
    resourceDialog.value = false
    resUrl.value = ''
    resPreview.value = ''
    getResources()
  } catch(e) { ElMessage.error('导入失败') }
  finally { urlImporting.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？','提示',{ type:'warning',center:true })
    await resourceApi.delete(row.id)
    ElMessage.success('删除成功')
    getResources()
  } catch(e){}
}

function isImage(row) {
	  if (!row.mimeType) return false
	  // Exclude SVG — `<img>` tag can't render most SVGs (e.g. icon fonts)
	  if (row.mimeType.includes('svg')) return false
	  return row.mimeType.includes('image')
	}
function formatSize(bytes) {
  if (!bytes) return '0 KB'
  if (bytes<1024*1024) return Math.round(bytes/1024)+' KB'
  return (bytes/(1024*1024)).toFixed(1)+' MB'
}
function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '' }
</script>

<style scoped>
.handle-box { margin-bottom:20px; display:flex; align-items:center }
.handle-select { width:200px }
.mrb10 { margin-right:10px; margin-bottom:10px }
.admin-table { width:100%; font-size:14px }
.admin-table :deep(.el-table__row) { height:92px }
.admin-table :deep(.el-table__cell) { padding:8px 0 }
.table-thumb { width:120px; height:74px; border-radius:8px }
.filename-text { font-size:14px; font-weight:600; color:#303133 }
.file-path { font-size:11px; color:#aaa; margin-top:2px; word-break:break-all }
.pagination { margin:20px 0; text-align:right }

.method-select { width:140px; margin-bottom:12px }
.res-input-row { width:100%; display:flex; align-items:center }
.upload-drop { width:100% }
.upload-drop :deep(.el-upload-dragger) { border-radius:12px; border:2px dashed #d9d9d9; padding:40px 20px; text-align:center; transition:all .3s }
.upload-drop :deep(.el-upload-dragger:hover) { border-color:#409EFF; background:#f9fbff }
.upload-icon { font-size:42px; color:#c0c4cc; margin-bottom:8px }
.upload-text { font-size:15px; color:#666 }

.img-preview-row { display:flex; align-items:center; gap:12px; margin-top:14px; padding:10px; background:#f8f9fa; border-radius:10px }
.form-preview-img { width:120px; height:74px; border-radius:8px; flex-shrink:0 }
.preview-path { flex:1; font-size:12px; color:#999; word-break:break-all }
</style>
