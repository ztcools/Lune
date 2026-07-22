<template>
  <div class="settings-manage">
    <div class="page-header">
      <h2>系统设置</h2>
      <el-button type="primary" @click="showDialog(null)">添加配置</el-button>
    </div>

    <el-table :data="items" stripe v-loading="tableLoading" class="table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="configKey" label="键" min-width="140" show-overflow-tooltip />
      <el-table-column prop="configValue" label="值" min-width="200" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.configType === 'public' ? 'success' : 'warning'"
            size="small"
            disable-transitions
          >
            {{ row.configType === 'public' ? '公开' : '私有' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link icon="Edit" @click="showDialog(row)">编辑</el-button>
          <el-button link icon="Delete" style="color: #f56c6c" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dlgVisible"
      :title="editRow ? '编辑配置' : '添加配置'"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="键" prop="configKey">
          <el-input v-model="form.configKey" maxlength="100" placeholder="请输入配置键名" />
        </el-form-item>
        <el-form-item label="值" prop="configValue">
          <el-input
            v-model="form.configValue"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
            placeholder="请输入配置值"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" maxlength="500" placeholder="请输入配置描述" />
        </el-form-item>
        <el-form-item label="类型" prop="configType">
          <el-select v-model="form.configType" placeholder="请选择类型" style="width: 100%">
            <el-option label="公开" value="public" />
            <el-option label="私有" value="private" />
          </el-select>
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
import { siteConfigApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const items = ref([])
const tableLoading = ref(false)
const dlgVisible = ref(false)
const editRow = ref(null)
const saveLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  configKey: '',
  configValue: '',
  description: '',
  configType: 'public'
})

const rules = {
  configKey: [{ required: true, message: '请输入配置键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

onMounted(() => {
  fetchItems()
})

async function fetchItems() {
  tableLoading.value = true
  try {
    items.value = await siteConfigApi.listAll()
  } catch (e) {
    ElMessage.error('加载配置列表失败')
  } finally {
    tableLoading.value = false
  }
}

function showDialog(row) {
  editRow.value = row
  if (row) {
    Object.assign(form, {
      configKey: row.configKey || '',
      configValue: row.configValue || '',
      description: row.description || '',
      configType: row.configType || 'public'
    })
  } else {
    Object.assign(form, {
      configKey: '',
      configValue: '',
      description: '',
      configType: 'public'
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
      data.id = editRow.value.id
    }
    await siteConfigApi.save(data)
    ElMessage.success(editRow.value ? '更新成功！' : '创建成功！')
    dlgVisible.value = false
    await fetchItems()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该配置？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    })
    await siteConfigApi.delete(id)
    ElMessage.success('删除成功！')
    await fetchItems()
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
