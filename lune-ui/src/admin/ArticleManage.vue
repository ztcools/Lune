<template>
  <div class="article-manage">
    <div class="handle-box">
      <el-select v-model="pagination.categoryId" placeholder="分类筛选" clearable style="width:140px" class="mrb10">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-input v-model="pagination.searchKey" placeholder="文章标题" clearable class="handle-input mrb10" @keyup.enter="searchArticles" />
      <el-button type="primary" @click="searchArticles">搜索</el-button>
      <el-button type="danger" @click="clearSearch">清除参数</el-button>
      <el-button type="primary" @click="showDialog(null)">新增文章</el-button>
    </div>

    <el-table :data="articles" border class="admin-table" header-cell-class-name="table-header" v-loading="tableLoading">
      <el-table-column prop="id" label="ID" width="55" align="center" />
      <el-table-column prop="title" label="文章标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="封面" width="140" align="center">
        <template #default="{ row }">
          <el-image v-if="row.cover" :src="row.cover" fit="cover" class="table-thumb" />
          <span v-else class="no-image">暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="100" align="center">
        <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="80" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)" size="small" disable-transitions>{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="150" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link icon="Edit" @click="showDialog(row)">编辑</el-button>
          <el-button link icon="Delete" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="pagination.current" :page-size="pagination.size"
        :total="pagination.total" :pager-count="5" background layout="total,prev,pager,next"
        @current-change="fetchArticles" />
    </div>

    <el-dialog v-model="dlgVisible" :title="editRow?'编辑文章':'新增文章'" width="800px"
      :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" placeholder="文章标题" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%">
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>

        <!-- Cover: upload method dropdown + input -->
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
            <el-image :src="form.cover" fit="cover" class="form-preview-img" />
            <span class="preview-path">{{ form.cover }}</span>
            <el-button link type="danger" size="small" @click="form.cover=''">清除</el-button>
          </div>
        </el-form-item>

        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="文章摘要" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="文章内容（支持Markdown）" />
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
import { articleApi, categoryApi, tagApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const articles = ref([])
const categories = ref([])
const tags = ref([])
const tableLoading = ref(false)
const dlgVisible = ref(false)
const editRow = ref(null)
const saveLoading = ref(false)
const formRef = ref(null)
const coverMethod = ref('upload')
const uploadUrl = '/api/admin/resources/upload'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

const pagination = reactive({ current:1, size:10, total:0, searchKey:'', categoryId:null })
const form = reactive({ title:'', categoryId:null, tagIds:[], cover:'', summary:'', content:'' })
const rules = {
  title: [{ required:true, message:'请输入标题', trigger:'blur' }],
  content: [{ required:true, message:'请输入内容', trigger:'blur' }],
  categoryId: [{ required:true, message:'请选择分类', trigger:'change' }]
}

onMounted(async () => { await fetchCategories(); await fetchTags(); await fetchArticles() })

async function fetchCategories() { try { categories.value = await categoryApi.listAll() } catch(e){} }
async function fetchTags() { try { tags.value = await tagApi.listAll() } catch(e){} }

async function fetchArticles() {
  tableLoading.value = true
  try {
    const params = { page:pagination.current, size:pagination.size }
    if (pagination.searchKey) params.search = pagination.searchKey
    if (pagination.categoryId) params.categoryId = pagination.categoryId
    const res = await articleApi.list(params)
    articles.value = res?.records || []
    pagination.total = res?.total || 0
  } catch(e) { ElMessage.error('加载失败') }
  finally { tableLoading.value = false }
}

function searchArticles() { pagination.current=1; fetchArticles() }
function clearSearch() { pagination.current=1; pagination.searchKey=''; pagination.categoryId=null; fetchArticles() }

function getCategoryName(cid) {
  if (!cid) return '-'
  const c = categories.value.find(x=>x.id===cid)
  return c?c.name:'-'
}

function showDialog(row) {
  editRow.value = row
  coverMethod.value = 'upload'
  if (row) Object.assign(form, { title:row.title||'', categoryId:row.categoryId||null, tagIds:row.tagIds||[], cover:row.cover||'', summary:row.summary||'', content:row.content||'' })
  else Object.assign(form, { title:'', categoryId:null, tagIds:[], cover:'', summary:'', content:'' })
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
    if (editRow.value) await articleApi.update(editRow.value.id, data)
    else await articleApi.create(data)
    ElMessage.success(editRow.value?'更新成功':'创建成功')
    dlgVisible.value = false
    await fetchArticles()
  } catch(e) { ElMessage.error(e?.message||'保存失败') }
  finally { saveLoading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？','提示',{ type:'warning',center:true })
    await articleApi.delete(row.id)
    ElMessage.success('已删除')
    pagination.current=1
    await fetchArticles()
  } catch(e) { if (e!=='cancel') ElMessage.error(e?.message||'删除失败') }
}

function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '' }
function getStatusTag(s) { return { PUBLISHED:'success', DRAFT:'info', HIDDEN:'danger' }[s]||'info' }
function getStatusText(s) { return { PUBLISHED:'已发布', DRAFT:'草稿', HIDDEN:'隐藏' }[s]||s||'未知' }
</script>

<style scoped>
.handle-box { margin-bottom:20px; display:flex; flex-wrap:wrap; align-items:center; gap:0 }
.handle-input { width:180px }
.mrb10 { margin-right:10px; margin-bottom:10px }
.admin-table { width:100%; font-size:14px }
.admin-table :deep(.el-table__row) { height:88px }
.admin-table :deep(.el-table__cell) { padding:8px 0 }
.table-thumb { width:120px; height:74px; border-radius:8px; cursor:default }
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
.form-preview-img { width:120px; height:74px; border-radius:8px; flex-shrink:0 }
.preview-path { flex:1; font-size:12px; color:#999; word-break:break-all }
</style>
