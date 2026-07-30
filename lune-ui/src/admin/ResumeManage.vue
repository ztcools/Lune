<template>
  <div class="resume-manage">
    <el-tabs v-model="tab" class="resume-tabs">
      <!-- ========== 工作经历 ========== -->
      <el-tab-pane label="💼 工作经历" name="work">
        <div class="page-header">
          <h2>工作经历</h2>
          <el-button type="primary" @click="showWorkDialog(null)">添加工作经历</el-button>
        </div>
        <el-table :data="workList" stripe v-loading="workLoading" class="admin-table">
          <el-table-column prop="id" label="ID" width="55" align="center" />
          <el-table-column prop="company" label="公司" min-width="130" show-overflow-tooltip />
          <el-table-column prop="position" label="岗位" min-width="110" show-overflow-tooltip />
          <el-table-column label="时间" width="180" align="center">
            <template #default="{ row }">
              {{ fmt(row.startDate) }} ~ {{ row.isCurrent ? '至今' : fmt(row.endDate) }}
              <el-tag v-if="row.isCurrent" size="small" type="success" style="margin-left:4px">在职</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="地点" width="90" align="center" />
          <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link icon="Edit" @click="showWorkDialog(row)">编辑</el-button>
              <el-button link icon="Delete" style="color:#f56c6c" @click="delWork(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ========== 项目经历 ========== -->
      <el-tab-pane label="🚀 项目经历" name="project">
        <div class="page-header">
          <h2>项目经历</h2>
          <el-button type="primary" @click="showProjDialog(null)">添加项目</el-button>
        </div>
        <el-table :data="projectList" stripe v-loading="projLoading" class="admin-table">
          <el-table-column prop="id" label="ID" width="55" align="center" />
          <el-table-column label="封面" width="110" align="center">
            <template #default="{ row }">
              <el-image v-if="row.cover" :src="row.cover" fit="cover" class="table-thumb" />
              <span v-else class="no-image">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="项目名" min-width="130" show-overflow-tooltip />
          <el-table-column prop="role" label="角色" width="100" align="center" />
          <el-table-column prop="devPeriod" label="开发周期" width="140" align="center" />
          <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link icon="Edit" @click="showProjDialog(row)">编辑</el-button>
              <el-button link icon="Delete" style="color:#f56c6c" @click="delProj(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- ========== 工作经历对话框 ========== -->
    <el-dialog v-model="workDlg" :title="editWork ? '编辑工作经历' : '添加工作经历'" width="640px" destroy-on-close :close-on-click-modal="false">
      <el-form :model="workForm" :rules="workRules" ref="workFormRef" label-width="90px">
        <el-form-item label="公司" prop="company"><el-input v-model="workForm.company" maxlength="100" placeholder="公司名称" /></el-form-item>
        <el-form-item label="岗位" prop="position"><el-input v-model="workForm.position" maxlength="100" placeholder="如：前端开发工程师" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="workForm.location" maxlength="100" placeholder="如：上海 / 远程" /></el-form-item>
        <el-form-item label="开始时间" prop="startDate">
          <el-date-picker v-model="workForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="是否在职">
          <el-switch v-model="workForm.isCurrent" active-text="至今" />
        </el-form-item>
        <el-form-item label="结束时间" v-if="!workForm.isCurrent">
          <el-date-picker v-model="workForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="工作内容"><el-input v-model="workForm.description" type="textarea" :rows="3" maxlength="1000" placeholder="做了些什么…" /></el-form-item>
        <el-form-item label="核心职责"><el-input v-model="workForm.responsibilities" type="textarea" :rows="3" maxlength="1000" placeholder="技术栈、职责、成果…" /></el-form-item>
        <el-form-item label="图片/视频">
          <MediaEditor v-model="workForm.media" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="workForm.sortOrder" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workDlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveWork">保存</el-button>
      </template>
    </el-dialog>

    <!-- ========== 项目对话框 ========== -->
    <el-dialog v-model="projDlg" :title="editProj ? '编辑项目' : '添加项目'" width="640px" destroy-on-close :close-on-click-modal="false">
      <el-form :model="projForm" :rules="projRules" ref="projFormRef" label-width="90px">
        <el-form-item label="项目名" prop="name"><el-input v-model="projForm.name" maxlength="100" placeholder="项目名称" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="projForm.summary" maxlength="300" placeholder="一句话介绍" /></el-form-item>
        <el-form-item label="详细描述"><el-input v-model="projForm.description" type="textarea" :rows="3" maxlength="2000" placeholder="项目背景、亮点…" /></el-form-item>
        <el-form-item label="技术栈">
          <el-select v-model="projTechArr" multiple filterable allow-create default-first-option placeholder="输入后回车添加" style="width:100%">
            <el-option v-for="t in commonTech" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="我的角色"><el-input v-model="projForm.role" maxlength="100" placeholder="如：独立全栈 / 前端负责人" /></el-form-item>
        <el-form-item label="开发周期"><el-input v-model="projForm.devPeriod" maxlength="50" placeholder="如：2023.06 - 2023.12" /></el-form-item>
        <el-form-item label="封面">
          <div class="cover-row">
            <el-upload :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false" :on-success="onCoverUp" accept="image/*">
              <el-button>上传封面</el-button>
            </el-upload>
            <el-input v-model="projForm.cover" placeholder="或粘贴图片URL" style="flex:1;margin-left:10px" />
          </div>
          <el-image v-if="projForm.cover" :src="projForm.cover" fit="cover" class="cover-preview" />
        </el-form-item>
        <el-form-item label="展示媒体"><MediaEditor v-model="projForm.media" /></el-form-item>
        <el-form-item label="预览地址"><el-input v-model="projForm.projectUrl" maxlength="500" placeholder="https://…" /></el-form-item>
        <el-form-item label="源码地址"><el-input v-model="projForm.repoUrl" maxlength="500" placeholder="https://github.com/…" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="projForm.sortOrder" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="projDlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProj">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { resumeApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import MediaEditor from './MediaEditor.vue'

const tab = ref('work')
const workList = ref([])
const projectList = ref([])
const workLoading = ref(false)
const projLoading = ref(false)
const saving = ref(false)

const uploadUrl = '/api/admin/resources/upload'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

const commonTech = ['Vue3', 'React', 'TypeScript', 'Spring Boot', 'Node.js', 'MySQL', 'Redis', 'MongoDB', 'Docker', 'Python', 'Go', 'ECharts', 'Vite', 'Element Plus']

// ===== 工作 =====
const workDlg = ref(false)
const editWork = ref(null)
const workFormRef = ref(null)
const workForm = reactive({ company: '', position: '', location: '', startDate: '', endDate: '', isCurrent: false, description: '', responsibilities: '', media: '', sortOrder: 0 })
const workRules = {
  company: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  position: [{ required: true, message: '请输入岗位', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

// ===== 项目 =====
const projDlg = ref(false)
const editProj = ref(null)
const projFormRef = ref(null)
const projTechArr = ref([])
const projForm = reactive({ name: '', summary: '', description: '', techStack: '', role: '', projectUrl: '', repoUrl: '', cover: '', media: '', devPeriod: '', sortOrder: 0 })
const projRules = { name: [{ required: true, message: '请输入项目名', trigger: 'blur' }] }

onMounted(() => { fetchWork(); fetchProj() })

async function fetchWork() { workLoading.value = true; try { workList.value = await resumeApi.listWork() || [] } catch (e) { console.error('加载工作经历失败:', e); ElMessage.error('加载工作经历失败') } finally { workLoading.value = false } }
async function fetchProj() { projLoading.value = true; try { projectList.value = await resumeApi.listProject() || [] } catch (e) { console.error('加载项目失败:', e); ElMessage.error('加载项目失败') } finally { projLoading.value = false } }

function showWorkDialog(row) {
  editWork.value = row
  if (row) Object.assign(workForm, { company: row.company, position: row.position, location: row.location || '', startDate: row.startDate || '', endDate: row.endDate || '', isCurrent: !!row.isCurrent, description: row.description || '', responsibilities: row.responsibilities || '', media: row.media || '', sortOrder: row.sortOrder || 0 })
  else Object.assign(workForm, { company: '', position: '', location: '', startDate: '', endDate: '', isCurrent: false, description: '', responsibilities: '', media: '', sortOrder: 0 })
  workDlg.value = true
}

// 勾上「至今」只是把结束时间的输入框 v-if 掉，值还留在表单里，
// 于是会存下 isCurrent=true 且 endDate=2024-05-01 这种自相矛盾的记录。
watch(() => workForm.isCurrent, (cur) => { if (cur) workForm.endDate = '' })

async function saveWork() {
  const valid = await workFormRef.value?.validate().catch(() => false); if (!valid) return
  if (!workForm.isCurrent && !workForm.endDate) { ElMessage.warning('请选择结束时间，或勾选「至今」'); return }
  saving.value = true
  try {
    // 兜底：即使有别的路径改了 isCurrent，也不把过期的 endDate 送上去
    const payload = { ...workForm, endDate: workForm.isCurrent ? null : workForm.endDate }
    if (editWork.value) await resumeApi.updateWork(editWork.value.id, payload)
    else await resumeApi.createWork(payload)
    ElMessage.success('已保存'); workDlg.value = false; fetchWork()
  } catch (e) { ElMessage.error(e?.message || '保存失败') } finally { saving.value = false }
}

async function delWork(id) {
  try { await ElMessageBox.confirm('确认删除该工作经历？', '提示', { type: 'warning', center: true }); await resumeApi.deleteWork(id); ElMessage.success('已删除'); fetchWork() } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

function showProjDialog(row) {
  editProj.value = row
  if (row) {
    Object.assign(projForm, { name: row.name, summary: row.summary || '', description: row.description || '', techStack: row.techStack || '', role: row.role || '', projectUrl: row.projectUrl || '', repoUrl: row.repoUrl || '', cover: row.cover || '', media: row.media || '', devPeriod: row.devPeriod || '', sortOrder: row.sortOrder || 0 })
    try { projTechArr.value = row.techStack ? JSON.parse(row.techStack) : [] } catch { projTechArr.value = [] }
  } else {
    Object.assign(projForm, { name: '', summary: '', description: '', techStack: '', role: '', projectUrl: '', repoUrl: '', cover: '', media: '', devPeriod: '', sortOrder: 0 })
    projTechArr.value = []
  }
  projDlg.value = true
}

function onCoverUp(res) { if (res.code === 200 && res.data) { projForm.cover = res.data.path; ElMessage.success('封面上传成功') } else ElMessage.error('上传失败') }

async function saveProj() {
  const valid = await projFormRef.value?.validate().catch(() => false); if (!valid) return
  saving.value = true
  try {
    const data = { ...projForm, techStack: JSON.stringify(projTechArr.value) }
    if (editProj.value) await resumeApi.updateProject(editProj.value.id, data)
    else await resumeApi.createProject(data)
    ElMessage.success('已保存'); projDlg.value = false; fetchProj()
  } catch (e) { ElMessage.error(e?.message || '保存失败') } finally { saving.value = false }
}

async function delProj(id) {
  try { await ElMessageBox.confirm('确认删除该项目？', '提示', { type: 'warning', center: true }); await resumeApi.deleteProject(id); ElMessage.success('已删除'); fetchProj() } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

function fmt(d) { return d ? d.slice(0, 7).replace('-', '.') : '' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 700; color: #303133; }
.admin-table { width: 100%; font-size: 14px; }
.table-thumb { width: 90px; height: 56px; border-radius: 8px; }
.no-image { color: #ccc; font-size: 12px; }
.cover-row { display: flex; align-items: center; width: 100%; }
.cover-preview { width: 140px; height: 84px; border-radius: 8px; margin-top: 10px; }
.resume-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 600; }
</style>
