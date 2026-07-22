<template>
  <div class="essay-manage">
    <div class="page-header">
      <h2>随笔管理</h2>
      <el-button type="primary" @click="showDialog(null)">新建随笔</el-button>
    </div>

    <el-table :data="essays" stripe v-loading="tableLoading" class="table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="weather" label="天气" width="90" align="center" />
      <el-table-column prop="mood" label="心情" width="90" align="center" />
      <el-table-column prop="location" label="位置" width="120" align="center" show-overflow-tooltip />
      <el-table-column label="创建时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link icon="Edit" @click="showDialog(row)">编辑</el-button>
          <el-button link icon="Delete" style="color: #f56c6c" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        :page-size="pagination.size"
        :total="pagination.total"
        :pager-count="5"
        background
        layout="total, prev, pager, next"
        @current-change="fetchEssays"
      />
    </div>

    <el-dialog
      v-model="dlgVisible"
      :title="editRow ? '编辑随笔' : '新建随笔'"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" placeholder="请输入随笔标题" />
        </el-form-item>
        <el-form-item label="天气">
          <el-input v-model="form.weather" maxlength="50" placeholder="例如：晴、多云、雨" />
        </el-form-item>
        <el-form-item label="心情">
          <el-input v-model="form.mood" maxlength="50" placeholder="例如：开心、平静、emo" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" maxlength="200" placeholder="例如：北京·海淀" />
        </el-form-item>
        <el-form-item label="封面">
          <el-input v-model="form.cover" placeholder="封面图片URL" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            maxlength="10000"
            show-word-limit
            placeholder="请输入随笔内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { essayApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const essays = ref([])
const tableLoading = ref(false)
const dlgVisible = ref(false)
const editRow = ref(null)
const saveLoading = ref(false)
const formRef = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  title: '',
  weather: '',
  mood: '',
  location: '',
  cover: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入随笔标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入随笔内容', trigger: 'blur' }]
}

onMounted(() => {
  fetchEssays()
})

async function fetchEssays() {
  tableLoading.value = true
  try {
    const res = await essayApi.list({ page: pagination.current, size: pagination.size })
    essays.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) {
    ElMessage.error('加载随笔列表失败')
  } finally {
    tableLoading.value = false
  }
}

function showDialog(row) {
  editRow.value = row
  if (row) {
    Object.assign(form, {
      title: row.title || '',
      weather: row.weather || '',
      mood: row.mood || '',
      location: row.location || '',
      cover: row.cover || '',
      content: row.content || ''
    })
  } else {
    Object.assign(form, {
      title: '',
      weather: '',
      mood: '',
      location: '',
      cover: '',
      content: ''
    })
  }
  dlgVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saveLoading.value = true
  try {
    const data = { ...form }
    if (editRow.value) {
      await essayApi.update(editRow.value.id, data)
      ElMessage.success('更新成功！')
    } else {
      await essayApi.create(data)
      ElMessage.success('创建成功！')
    }
    dlgVisible.value = false
    await fetchEssays()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该随笔？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    })
    await essayApi.delete(id)
    ElMessage.success('删除成功！')
    pagination.current = 1
    await fetchEssays()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

function formatDate(d) {
  return d ? new Date(d).toLocaleDateString('zh-CN') : ''
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.table {
  width: 100%;
  font-size: 14px;
}

.pagination {
  margin: 20px 0;
  text-align: right;
}
</style>
