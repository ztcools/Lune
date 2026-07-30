<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <el-table :data="users" stripe v-loading="tableLoading" class="table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
      <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.role === 'ADMIN' ? 'danger' : 'info'"
            size="small"
            disable-transitions
          >
            {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="toggleRole(row)">
            {{ row.role === 'ADMIN' ? '设为用户' : '设为管理员' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
        @current-change="fetchUsers"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const users = ref([])
const tableLoading = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  fetchUsers()
})

async function fetchUsers() {
  tableLoading.value = true
  try {
    const res = await userApi.list({ page: pagination.current, size: pagination.size })
    users.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) {
    ElMessage.error('加载用户列表失败')
  } finally {
    tableLoading.value = false
  }
}

async function toggleRole(row) {
  const newRole = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  const actionText = newRole === 'ADMIN' ? '设为管理员' : '设为用户'

  // 自降级会当场丢掉后台入口。后端也拦（UserServiceImpl#updateRole），
  // 这里提前挡一次是为了给出人话提示，而不是等一个 500。
  if (newRole === 'USER' && userStore.user?.id === row.id) {
    ElMessage.warning('不能取消自己的管理员权限！')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认将用户「${row.nickname || row.username}」${actionText}？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )
    await userApi.updateRole(row.id, newRole)
    ElMessage.success('操作成功！')
    row.role = newRole
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '操作失败')
    }
  }
}

async function handleDelete(row) {
  const currentUser = userStore.user
  if (currentUser && currentUser.id === row.id) {
    ElMessage.warning('不能删除自己的账号！')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除用户「${row.nickname || row.username}」？此操作不可恢复！`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )
    await userApi.delete(row.id)
    ElMessage.success('删除成功！')
    pagination.current = 1
    await fetchUsers()
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

.pagination {
  margin: 20px 0;
  text-align: right;
}
</style>
