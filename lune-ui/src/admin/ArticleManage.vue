<template>
  <div class="article-manage">
    <!-- Search & Actions -->
    <div class="handle-box">
      <el-select v-model="pagination.categoryId" placeholder="分类筛选" clearable style="width: 140px" class="mrb10">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-input
        v-model="pagination.searchKey"
        placeholder="文章标题"
        clearable
        class="handle-input mrb10"
        @keyup.enter="searchArticles"
      />
      <el-button type="primary" @click="searchArticles">搜索</el-button>
      <el-button type="danger" @click="clearSearch">清除参数</el-button>
      <el-button type="primary" @click="showDialog(null)">新增文章</el-button>
    </div>

    <!-- Table -->
    <el-table :data="articles" border class="table" header-cell-class-name="table-header" v-loading="tableLoading">
      <el-table-column prop="id" label="ID" width="55" align="center" />
      <el-table-column prop="title" label="文章标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="分类" width="120" align="center">
        <template #default="{ row }">
          {{ row.category?.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="90" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)" size="small" disable-transitions>
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link icon="Edit" @click="showDialog(row)">编辑</el-button>
          <el-button link icon="Delete" style="color: #f56c6c" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        :page-size="pagination.size"
        :total="pagination.total"
        :pager-count="5"
        background
        layout="total, prev, pager, next"
        @current-change="fetchArticles"
      />
    </div>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="dlgVisible"
      :title="editRow ? '编辑文章' : '新增文章'"
      width="800px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tagIds">
          <el-select v-model="form.tagIds" multiple placeholder="请选择标签" style="width: 100%">
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面" prop="cover">
          <div style="display: flex; align-items: center; gap: 12px">
            <el-input v-model="form.cover" placeholder="封面图片URL" style="flex: 1" />
            <el-image
              v-if="form.cover"
              :src="form.cover"
              fit="cover"
              :preview-src-list="[form.cover]"
              style="width: 48px; height: 48px; border-radius: 4px; flex-shrink: 0"
            />
          </div>
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="请输入文章内容（支持 Markdown）"
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
import { articleApi, categoryApi, tagApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

// ---- State ----
const articles = ref([])
const categories = ref([])
const tags = ref([])
const tableLoading = ref(false)
const dlgVisible = ref(false)
const editRow = ref(null)
const saveLoading = ref(false)
const formRef = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  searchKey: '',
  categoryId: null
})

const form = reactive({
  title: '',
  categoryId: null,
  tagIds: [],
  cover: '',
  summary: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

// ---- Lifecycle ----
onMounted(async () => {
  await fetchCategories()
  await fetchTags()
  await fetchArticles()
})

// ---- API helpers ----
async function fetchCategories() {
  try {
    categories.value = await categoryApi.listAll()
  } catch (e) { /* ignore */ }
}

async function fetchTags() {
  try {
    tags.value = await tagApi.listAll()
  } catch (e) { /* ignore */ }
}

async function fetchArticles() {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size
    }
    if (pagination.searchKey) params.search = pagination.searchKey
    if (pagination.categoryId) params.categoryId = pagination.categoryId
    const res = await articleApi.list(params)
    articles.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) {
    ElMessage.error('加载文章列表失败')
  } finally {
    tableLoading.value = false
  }
}

function searchArticles() {
  pagination.current = 1
  pagination.total = 0
  fetchArticles()
}

function clearSearch() {
  pagination.current = 1
  pagination.total = 0
  pagination.searchKey = ''
  pagination.categoryId = null
  fetchArticles()
}

// ---- Edit dialog ----
function showDialog(row) {
  editRow.value = row
  if (row) {
    Object.assign(form, {
      title: row.title || '',
      categoryId: row.categoryId || null,
      tagIds: row.tagIds || [],
      cover: row.cover || '',
      summary: row.summary || '',
      content: row.content || ''
    })
  } else {
    Object.assign(form, {
      title: '',
      categoryId: null,
      tagIds: [],
      cover: '',
      summary: '',
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
      await articleApi.update(editRow.value.id, data)
      ElMessage.success('更新成功！')
    } else {
      await articleApi.create(data)
      ElMessage.success('创建成功！')
    }
    dlgVisible.value = false
    await fetchArticles()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该文章？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    })
    await articleApi.delete(row.id)
    ElMessage.success('删除成功！')
    pagination.current = 1
    await fetchArticles()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

// ---- Utils ----
function formatDate(d) {
  return d ? new Date(d).toLocaleDateString('zh-CN') : ''
}

function getStatusTag(status) {
  const map = { PUBLISHED: 'success', DRAFT: 'info', HIDDEN: 'danger' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { PUBLISHED: '已发布', DRAFT: '草稿', HIDDEN: '隐藏' }
  return map[status] || status || '未知'
}
</script>

<style scoped>
.handle-box {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0;
}

.handle-input {
  width: 180px;
}

.mrb10 {
  margin-right: 10px;
  margin-bottom: 10px;
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
