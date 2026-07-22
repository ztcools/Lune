<template>
  <div>
    <div class="handle-box">
      <el-select clearable v-model="resourceType" placeholder="资源类型" class="handle-select mrb10">
        <el-option label="用户头像" value="userAvatar" />
        <el-option label="文章封面" value="articleCover" />
        <el-option label="文章图片" value="articlePicture" />
        <el-option label="网站头像" value="webAvatar" />
        <el-option label="背景图片" value="webBackgroundImage" />
        <el-option label="随机头像" value="randomAvatar" />
        <el-option label="随机封面" value="randomCover" />
        <el-option label="收藏夹封面" value="favoritesCover" />
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="search">搜索</el-button>
      <el-button type="primary" @click="resourceDialog=true">新增资源</el-button>
    </div>
    <el-table :data="resources" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="55" align="center" />
      <el-table-column prop="filename" label="名称" align="center" />
      <el-table-column prop="type" label="类型" align="center">
        <template #default="{row}"><el-tag>{{ row.type }}</el-tag></template>
      </el-table-column>
      <el-table-column label="路径" align="center">
        <template #default="{row}">
          <el-image v-if="row.mimeType && row.mimeType.includes('image')" :preview-src-list="[row.path]" class="table-td-thumb" :src="row.path" fit="cover" />
          <span v-else>{{ row.path }}</span>
        </template>
      </el-table-column>
      <el-table-column label="大小(KB)" align="center">
        <template #default="{row}">{{ Math.round(row.size / 1024) }}</template>
      </el-table-column>
      <el-table-column prop="storeType" label="存储" align="center" />
      <el-table-column prop="createTime" label="创建时间" align="center" />
      <el-table-column label="操作" width="100" align="center">
        <template #default="{row}">
          <el-button link style="color:var(--orangeRed)" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination"><el-pagination background layout="total,prev,pager,next" :current-page="page" :page-size="10" :total="total" @current-change="onPageChange" /></div>

    <el-dialog title="上传文件" v-model="resourceDialog" width="400px" :close-on-click-modal="false" destroy-on-close center>
      <el-upload :action="'/api/admin/resources/upload'" :headers="uploadHeaders" :on-success="onUploadSuccess" :show-file-list="false" accept="image/*" drag>
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽或点击上传</div>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { resourceApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
const resources = ref([])
const page = ref(1)
const total = ref(0)
const resourceType = ref('')
const resourceDialog = ref(false)
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }
onMounted(() => getResources())
async function getResources() {
  try {
    const data = await resourceApi.list({ page: page.value, size: 10 })
    resources.value = data; total.value = data.length > 0 ? page.value * 10 + 1 : 0
  } catch(e){}
}
function search() { page.value = 1; getResources() }
function onPageChange(p) { page.value = p; getResources() }
function onUploadSuccess(res) { if (res.code===200) { ElMessage.success('上传成功'); resourceDialog.value=false; getResources() } else { ElMessage.error('上传失败') } }
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？','提示',{type:'success',center:true})
    await resourceApi.delete(row.id)
    ElMessage.success('删除成功')
    getResources()
  } catch(e){}
}
</script>

<style scoped>
.handle-box{margin-bottom:20px}
.handle-select{width:200px}
.mrb10{margin-right:10px;margin-bottom:10px}
.table{width:100%;font-size:14px}
.table-td-thumb{display:block;margin:auto;width:40px;height:40px}
.pagination{margin:20px 0;text-align:right}
</style>
