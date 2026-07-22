<template>
  <div class="treehole-manage">
    <div class="page-header">
      <h2>树洞管理</h2>
    </div>

    <el-table :data="treeholes" stripe v-loading="tableLoading" class="table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="color" label="颜色" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.color" :color="row.color" size="small" style="color: #fff; border: none">
            {{ row.color }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
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
        @current-change="fetchTreeholes"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { treeHoleApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const treeholes = ref([])
const tableLoading = ref(false)

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

onMounted(() => {
  fetchTreeholes()
})

async function fetchTreeholes() {
  tableLoading.value = true
  try {
    const res = await treeHoleApi.listAll({ page: pagination.current, size: pagination.size })
    treeholes.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) {
    ElMessage.error('加载树洞列表失败')
  } finally {
    tableLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该树洞？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    })
    await treeHoleApi.delete(id)
    ElMessage.success('删除成功！')
    pagination.current = 1
    await fetchTreeholes()
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
