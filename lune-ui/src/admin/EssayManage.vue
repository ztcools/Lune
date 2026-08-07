<template>
  <div class="essay-manage">
    <div class="page-header">
      <h2>随笔管理</h2>
      <el-button type="primary" @click="showDialog(null)">新建随笔</el-button>
    </div>

    <el-table :data="essays" stripe v-loading="tableLoading" class="admin-table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column label="封面" width="150" align="center">
        <template #default="{ row }">
          <LuneImage v-if="row.cover" :src="row.cover" variant="thumb" alt="" />
          <span v-else class="no-image">暂无</span>
        </template>
      </el-table-column>
      <el-table-column prop="weather" label="天气" width="80" align="center" />
      <el-table-column prop="mood" label="心情" width="80" align="center" />
      <el-table-column prop="location" label="位置" width="120" align="center" show-overflow-tooltip />
      <el-table-column label="创建时间" width="150" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link icon="Edit" @click="showDialog(row)">编辑</el-button>
          <el-button link icon="Delete" style="color:#f56c6c" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="pagination.current" :page-size="pagination.size"
        :total="pagination.total" :pager-count="5" background layout="total,prev,pager,next"
        @current-change="fetchEssays" />
    </div>

    <el-dialog v-model="dlgVisible" :title="editRow?'编辑随笔':'新建随笔'" width="640px"
      :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" placeholder="随笔标题" />
        </el-form-item>
        <el-form-item label="天气">
          <el-input v-model="form.weather" maxlength="50" placeholder="晴、多云、雨" />
        </el-form-item>
        <el-form-item label="心情">
          <el-input v-model="form.mood" maxlength="50" placeholder="开心、平静、emo" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" maxlength="200" placeholder="北京·海淀" />
        </el-form-item>

        <!-- Cover -->
        <el-form-item label="封面">
          <el-select v-model="coverMethod" class="method-select">
            <el-option label="📁 本地上传" value="upload" />
            <el-option label="🔗 URL输入" value="url" />
          </el-select>
          <div v-if="coverMethod==='url'" class="img-input-row">
            <el-input v-model="form.cover" placeholder="封面图片URL" style="flex:1" />
          </div>
          <div v-else class="img-input-row">
            <el-upload class="upload-drop" :action="uploadUrl" :headers="uploadHeaders"
              :on-success="onCoverUploaded" :show-file-list="false" accept="image/*" drag>
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">拖拽图片或点击上传</div>
            </el-upload>
          </div>
          <div class="img-preview-row" v-if="form.cover">
            <LuneImage :src="form.cover" variant="thumb" alt="" />
            <span class="preview-path">{{ form.cover }}</span>
            <el-button link type="danger" size="small" @click="form.cover=''">清除</el-button>
          </div>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" maxlength="10000" show-word-limit placeholder="随笔内容" />
        </el-form-item>
        <el-form-item label="图片/视频">
          <MediaEditor v-model="form.media" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible=false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { essayApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import LuneImage from '../components/LuneImage.vue'
import MediaEditor from './MediaEditor.vue'

const essays = ref([])
const tableLoading = ref(false)
const dlgVisible = ref(false)
const editRow = ref(null)
const saveLoading = ref(false)
const formRef = ref(null)
const coverMethod = ref('upload')
const uploadUrl = '/api/admin/resources/upload'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

const pagination = reactive({ current:1, size:10, total:0 })
const form = reactive({ title:'', weather:'', mood:'', location:'', cover:'', content:'', media:'' })
const rules = {
  title: [{ required:true, message:'请输入标题', trigger:'blur' }],
  content: [{ required:true, message:'请输入内容', trigger:'blur' }]
}

onMounted(() => fetchEssays())

async function fetchEssays() {
  tableLoading.value = true
  try {
    const res = await essayApi.list({ page:pagination.current, size:pagination.size })
    essays.value = res?.records || []
    pagination.total = res?.total || 0
  } catch(e) { ElMessage.error('加载失败') }
  finally { tableLoading.value = false }
}

function showDialog(row) {
  editRow.value = row
  coverMethod.value = 'upload'
  if (row) Object.assign(form, { title:row.title||'', weather:row.weather||'', mood:row.mood||'', location:row.location||'', cover:row.cover||'', content:row.content||'', media:row.media||'' })
  else Object.assign(form, { title:'', weather:'', mood:'', location:'', cover:'', content:'', media:'' })
  dlgVisible.value = true
}

function onCoverUploaded(res) {
  if (res.code===200&&res.data) { form.cover = res.data.path; ElMessage.success('上传成功') }
  else ElMessage.error('上传失败')
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(()=>false)
  if (!valid) return
  saveLoading.value = true
  try {
    const data = { ...form }
    if (editRow.value) await essayApi.update(editRow.value.id, data)
    else await essayApi.create(data)
    ElMessage.success(editRow.value?'更新成功':'创建成功')
    dlgVisible.value = false
    await fetchEssays()
  } catch(e) { ElMessage.error(e?.message||'保存失败') }
  finally { saveLoading.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除？','提示',{ type:'warning',center:true })
    await essayApi.delete(id)
    ElMessage.success('已删除')
    pagination.current=1
    await fetchEssays()
  } catch(e) { if (e!=='cancel') ElMessage.error(e?.message||'删除失败') }
}

function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '' }
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px }
.page-header h2 { margin:0; font-size:20px; font-weight:700; color:#303133; letter-spacing:1px }
.admin-table { width:100%; font-size:14px }
.admin-table :deep(.el-table__row) { height:90px }
.admin-table :deep(.el-table__cell) { padding:8px 0 }
.table-thumb { width:130px; height:78px; border-radius:8px }
.no-image { color:#ccc; font-size:12px }
.pagination { margin:20px 0; text-align:right }

.method-select { width:140px; margin-bottom:10px }
.img-input-row { width:100% }
.upload-drop { width:100% }
.upload-drop :deep(.el-upload-dragger) { border-radius:12px; border:2px dashed #d9d9d9; padding:40px 20px; text-align:center; transition:all .3s }
.upload-drop :deep(.el-upload-dragger:hover) { border-color:#409EFF; background:#f9fbff }
.upload-icon { font-size:42px; color:#c0c4cc; margin-bottom:8px }
.upload-text { font-size:15px; color:#666 }

.img-preview-row { display:flex; align-items:center; gap:12px; margin-top:12px; padding:10px; background:#f8f9fa; border-radius:10px }
.form-preview-img { width:130px; height:78px; border-radius:8px; flex-shrink:0 }
.preview-path { flex:1; font-size:12px; color:#999; word-break:break-all }
</style>
