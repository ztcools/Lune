<template>
  <div class="category-manage">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="showDialog(null)">新建分类</el-button>
    </div>

    <el-table :data="categories" stripe v-loading="tableLoading" class="table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="name" label="名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 'article' ? 'primary' : 'success'" size="small" disable-transitions>
            {{ row.type === 'article' ? '文章' : '记录' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link icon="Edit" @click="showDialog(row)">编辑</el-button>
          <el-button link icon="Delete" style="color: #f56c6c" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dlgVisible"
      :title="editRow ? '编辑分类' : '新建分类'"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" maxlength="200" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="文章" value="article" />
            <el-option label="记录" value="record" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-text="启用" inactive-text="禁用" />
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
import { categoryApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])
const tableLoading = ref(false)
const dlgVisible = ref(false)
const editRow = ref(null)
const saveLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  description: '',
  type: 'article',
  sortOrder: 0,
  status: true
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

onMounted(() => {
  fetchCategories()
})

async function fetchCategories() {
  tableLoading.value = true
  try {
    categories.value = await categoryApi.listAll()
  } catch (e) {
    ElMessage.error('加载分类列表失败')
  } finally {
    tableLoading.value = false
  }
}

function showDialog(row) {
  editRow.value = row
  if (row) {
    Object.assign(form, {
      name: row.name || '',
      description: row.description || '',
      type: row.type || 'article',
      sortOrder: row.sortOrder ?? 0,
      status: row.status !== false
    })
  } else {
    Object.assign(form, {
      name: '',
      description: '',
      type: 'article',
      sortOrder: 0,
      status: true
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
      await categoryApi.update(editRow.value.id, data)
      ElMessage.success('更新成功！')
    } else {
      await categoryApi.create(data)
      ElMessage.success('创建成功！')
    }
    dlgVisible.value = false
    await fetchCategories()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该分类？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    })
    await categoryApi.delete(id)
    ElMessage.success('删除成功！')
    await fetchCategories()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '删除失败')
    }
  }
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
</style>
