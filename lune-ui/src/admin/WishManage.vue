<template>
  <div class="wish-manage">
    <div class="page-header">
      <h2>许愿池管理</h2>
      <span class="wish-count">共 {{ pagination.total }} 个心愿</span>
    </div>

    <el-table :data="wishes" stripe v-loading="loading" class="admin-table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="title" label="心愿标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="需求详情" min-width="220" show-overflow-tooltip />
      <el-table-column prop="nickname" label="发起人" width="110" align="center">
        <template #default="{ row }">{{ row.nickname || row.username || '—' }}</template>
      </el-table-column>
      <el-table-column label="点赞" width="80" align="center" sortable :sort-method="(a,b)=>a.likeCount-b.likeCount">
        <template #default="{ row }">
          <span class="like-num">❤️ {{ row.likeCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '公开' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="150" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link :icon="row.status === 1 ? 'View' : 'Open'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '隐藏' : '公开' }}
          </el-button>
          <el-button link icon="Delete" style="color:#f56c6c" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="pagination.current" :page-size="pagination.size"
        :total="pagination.total" :pager-count="5" background layout="total,prev,pager,next"
        @current-change="fetchWishes" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { wishApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const wishes = ref([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 20, total: 0 })

onMounted(fetchWishes)

async function fetchWishes() {
  loading.value = true
  try {
    const res = await wishApi.listAll({ page: pagination.current, size: pagination.size })
    wishes.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false }
}

async function toggleStatus(row) {
  try {
    await wishApi.update(row.id, { status: row.status === 1 ? 0 : 1 })
    ElMessage.success(row.status === 1 ? '已隐藏' : '已公开')
    fetchWishes()
  } catch (e) { ElMessage.error('操作失败') }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该心愿？', '提示', { type: 'warning', center: true })
    await wishApi.delete(id)
    ElMessage.success('已删除')
    fetchWishes()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

function formatDate(d) { return d ? new Date(d).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 700; color: #303133; }
.wish-count { color: #999; font-size: 14px; }
.admin-table { width: 100%; font-size: 14px; }
.like-num { color: #f56c6c; font-weight: 600; }
.pagination { margin: 20px 0; text-align: right; }
</style>
