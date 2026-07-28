<template>
  <div>
    <div class="page-header"><h2>家园管理</h2><el-button type="primary" @click="showDialog(null)">新建</el-button></div>

    <el-table :data="items" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="title" label="标题" min-width="120" />
      <el-table-column label="封面" width="140" align="center">
        <template #default="{row}">
          <el-image v-if="row.cover" :src="row.cover" fit="cover" class="table-thumb" />
          <span v-else class="no-image">暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="背景" width="140" align="center">
        <template #default="{row}">
          <el-image v-if="row.bgCover" :src="row.bgCover" fit="cover" class="table-thumb" />
          <span v-else class="no-image">暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="150" align="center">
        <template #default="{row}">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" :title="edit?'编辑':'新建'" width="640px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="fm" label-width="100px">
        <el-form-item label="标题"><el-input v-model="fm.title" placeholder="姓名/称呼" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="fm.content" type="textarea" :rows="4" placeholder="简介描述" /></el-form-item>

        <!-- Cover -->
        <el-form-item label="封面">
          <el-select v-model="coverMethod" class="method-select">
            <el-option label="📁 本地上传" value="upload" />
            <el-option label="🔗 URL输入" value="url" />
          </el-select>
          <div v-if="coverMethod==='url'" class="img-input-row">
            <el-input v-model="fm.cover" placeholder="封面图片URL" style="flex:1" />
          </div>
          <div v-else class="img-input-row">
            <el-upload class="upload-drop" :action="uploadUrl" :headers="uploadHeaders"
              :on-success="r => { if(r.code===200&&r.data) fm.cover=r.data.path }"
              :show-file-list="false" accept="image/*" drag>
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">拖拽图片或点击上传</div>
            </el-upload>
          </div>
          <div class="img-preview-row" v-if="fm.cover">
            <el-image :src="fm.cover" fit="cover" class="form-preview-img" />
            <span class="preview-path">{{ fm.cover }}</span>
            <el-button link type="danger" size="small" @click="fm.cover=''">清除</el-button>
          </div>
        </el-form-item>

        <!-- BgCover -->
        <el-form-item label="背景">
          <el-select v-model="bgMethod" class="method-select">
            <el-option label="📁 本地上传" value="upload" />
            <el-option label="🔗 URL输入" value="url" />
          </el-select>
          <div v-if="bgMethod==='url'" class="img-input-row">
            <el-input v-model="fm.bgCover" placeholder="背景图片URL" style="flex:1" />
          </div>
          <div v-else class="img-input-row">
            <el-upload class="upload-drop" :action="uploadUrl" :headers="uploadHeaders"
              :on-success="r => { if(r.code===200&&r.data) fm.bgCover=r.data.path }"
              :show-file-list="false" accept="image/*" drag>
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">拖拽图片或点击上传</div>
            </el-upload>
          </div>
          <div class="img-preview-row" v-if="fm.bgCover">
            <el-image :src="fm.bgCover" fit="cover" class="form-preview-img" />
            <span class="preview-path">{{ fm.bgCover }}</span>
            <el-button link type="danger" size="small" @click="fm.bgCover=''">清除</el-button>
          </div>
        </el-form-item>

        <el-form-item label="纪念日"><el-input v-model="fm.timing" placeholder="如：相识纪念日" /></el-form-item>
        <el-form-item label="倒计时标题"><el-input v-model="fm.countdownTitle" placeholder="如：距离生日还有" /></el-form-item>
        <el-form-item label="倒计时日期"><el-date-picker v-model="fm.countdownTime" type="datetime" placeholder="选择日期" style="width:100%" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="fm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { familyApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const items = ref([]); const dlg = ref(false); const edit = ref(null)
const coverMethod = ref('upload'); const bgMethod = ref('upload')
const uploadUrl = '/api/admin/resources/upload'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }
const fm = ref({ title:'', content:'', cover:'', bgCover:'', timing:'', countdownTitle:'', countdownTime:null, status:1 })

onMounted(() => fetch())
async function fetch() { try { items.value = await familyApi.list() } catch(e){} }
function showDialog(row) {
  edit.value = row; coverMethod.value='upload'; bgMethod.value='upload'
  fm.value = row ? { ...row, countdownTime: row.countdownTime ? new Date(row.countdownTime) : null }
    : { title:'', content:'', cover:'', bgCover:'', timing:'', countdownTitle:'', countdownTime:null, status:1 }
  dlg.value = true
}
async function save() {
  try {
    const data = { ...fm.value }
    if (data.countdownTime) data.countdownTime = data.countdownTime.toISOString()
    edit.value ? await familyApi.update(edit.value.id, data) : await familyApi.create(data)
    dlg.value=false; ElMessage.success('成功'); await fetch()
  } catch(e){}
}
async function del(id) {
  try { await ElMessageBox.confirm('确定删除？','提示',{type:'warning'}); await familyApi.delete(id); ElMessage.success('删除成功'); await fetch() } catch(e){}
}
function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '' }
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px }
.page-header h2 { margin:0; font-size:20px; font-weight:700; color:#303133; letter-spacing:1px }
.admin-table { width:100%; font-size:14px }
.admin-table :deep(.el-table__row) { height:90px }
.admin-table :deep(.el-table__cell) { padding:8px 0 }
.table-thumb { width:120px; height:74px; border-radius:8px }
.no-image { color:#ccc; font-size:12px }

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
