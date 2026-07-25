<template>
  <div>
    <div class="handle-box">
      <el-button type="primary" @click="showDialog(null)">新增日记页</el-button>
    </div>

    <el-table :data="list" border class="admin-table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center" />
      <el-table-column label="预览" width="100" align="center">
        <template #default="{ row }">
          <el-image v-if="firstImage(row)" :src="firstImage(row)" fit="cover" class="table-thumb" />
          <span v-else style="color:#ccc">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" width="140" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">{{ stripHtml(row.content).substring(0, 60) }}</template>
      </el-table-column>
      <el-table-column prop="pageOrder" label="页码" width="70" align="center" />
      <el-table-column prop="recordTime" label="记录时间" width="150" align="center">
        <template #default="{ row }">{{ formatDate(row.recordTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link icon="Edit" @click="showDialog(row)">编辑</el-button>
          <el-button link icon="Delete" style="color:#f56c6c" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="total,prev,pager,next" :current-page="page"
        :page-size="20" :total="total" @current-change="onPageChange" />
    </div>

    <el-dialog v-model="dlgVisible" :title="editRow ? '编辑日记页' : '新增日记页'" width="680px"
      :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="100" placeholder="日记标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" maxlength="5000" show-word-limit placeholder="日记内容（支持HTML）" />
        </el-form-item>
        <el-form-item label="图片">
          <el-input v-model="form.images" placeholder="JSON数组格式：['url1','url2']" />
        </el-form-item>
        <el-form-item label="记录时间">
          <el-date-picker v-model="form.recordTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="页码">
          <el-input-number v-model="form.pageOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option :value="1" label="发布" />
            <el-option :value="0" label="隐藏" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { diaryApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const page = ref(1)
const total = ref(0)
const dlgVisible = ref(false)
const editRow = ref(null)
const saving = ref(false)
const form = ref({ title:'', content:'', images:'[]', recordTime:'', pageOrder:0, status:1 })

onMounted(() => fetchList())

async function fetchList() {
  try {
    const data = await diaryApi.adminList({ page: page.value, size: 20 })
    list.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) { /* silent */ }
}

function onPageChange(p) { page.value = p; fetchList() }

function showDialog(row) {
  editRow.value = row
  form.value = row ? { ...row } : { title:'', content:'', images:'[]', recordTime:'', pageOrder:0, status:1 }
  dlgVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editRow.value) {
      await diaryApi.update(editRow.value.id, form.value)
    } else {
      await diaryApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dlgVisible.value = false
    fetchList()
  } catch (e) { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type:'warning', center:true })
    await diaryApi.delete(id)
    ElMessage.success('已删除')
    fetchList()
  } catch (e) { /* cancelled */ }
}

function firstImage(row) {
  try { const arr = JSON.parse(row.images || '[]'); return arr[0] || '' } catch { return '' }
}
function stripHtml(html) { return (html || '').replace(/<[^>]+>/g, '') }
function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '' }
</script>

<style scoped>
.handle-box { margin-bottom:20px }
.admin-table { width:100%; font-size:14px }
.table-thumb { width:70px; height:50px; border-radius:6px }
.pagination { margin:20px 0; text-align:right }
</style>
