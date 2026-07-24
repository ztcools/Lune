<template>
  <div class="settings-manage">
    <div class="page-header">
      <h2>系统设置</h2>
      <el-button type="primary" @click="showDialog(null)">添加配置</el-button>
    </div>

    <el-table :data="items" stripe v-loading="tableLoading" class="admin-table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="configKey" label="键" width="150" show-overflow-tooltip />
      <el-table-column label="值" min-width="240">
        <template #default="{ row }">
          <div class="value-cell" v-if="isImageKey(row.configKey) && row.configValue">
            <template v-if="isMultiImage(row.configKey)">
              <div class="multi-thumbs">
                <el-image v-for="(url, i) in parseMultiUrl(row.configValue)" :key="i"
                  :src="url" fit="cover" class="table-thumb" />
              </div>
            </template>
            <el-image v-else :src="row.configValue" fit="cover" class="table-thumb" />
          </div>
          <span v-else class="value-text">{{ row.configValue }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.configType==='public'?'success':'warning'" size="small" disable-transitions>
            {{ row.configType==='public'?'公开':'私有' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link icon="Edit" @click="showDialog(row)">编辑</el-button>
          <el-button link icon="Delete" style="color:#f56c6c" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- ====== Edit Dialog ====== -->
    <el-dialog v-model="dlgVisible" :title="editRow?'编辑配置':'添加配置'" width="680px"
      :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="键" prop="configKey">
          <el-input v-model="form.configKey" maxlength="100" placeholder="配置键名" />
        </el-form-item>

        <!-- Image value editing -->
        <template v-if="isImageKey(form.configKey)">
          <el-form-item :label="isMulti?'图片列表':'图片'" prop="configValue">

            <!-- INPUT AREA: shown when no images yet, or when user wants to add more -->
            <template v-if="showImgInput">
              <div class="img-input-wrap">
                <el-select v-model="imgMethod" class="method-select">
                  <el-option label="📁 本地上传" value="upload" />
                  <el-option label="🔗 URL输入" value="url" />
                  <el-option label="🖼️ 资源库选择" value="picker" />
                </el-select>

                <!-- URL mode -->
                <div v-if="imgMethod==='url'" class="url-row">
                  <el-input v-model="imgUrlInput" placeholder="输入图片URL，回车添加"
                    @keyup.enter="addImageUrl" style="flex:1" />
                  <el-button type="primary" @click="addImageUrl" style="margin-left:8px">添加</el-button>
                </div>

                <!-- Upload mode -->
                <div v-else-if="imgMethod==='upload'" class="upload-row">
                  <el-upload class="upload-big" :action="uploadUrl" :headers="uploadHeaders"
                    :on-success="onImageUploaded" :show-file-list="false" accept="image/*" drag>
                    <el-icon class="upload-big-icon"><UploadFilled /></el-icon>
                    <div class="upload-big-text">将元素拖到此处</div>
                    <div class="upload-big-hint">或 点击上传</div>
                  </el-upload>
                </div>

                <!-- Resource picker mode -->
                <div v-else class="picker-area">
                  <div class="picker-grid" v-if="pickerResources.length">
                    <div v-for="r in pickerResources" :key="r.id" class="picker-card"
                      :class="{'picker-sel': isMulti ? imgList.includes(r.path) : imgList[0]===r.path}"
                      @click="pickFromResource(r.path)">
                      <el-image v-if="r.mimeType&&r.mimeType.includes('image')" :src="r.path" fit="cover" class="picker-img" />
                      <span class="picker-fn">{{ r.filename }}</span>
                    </div>
                  </div>
                  <el-empty v-else description="暂无资源" :image-size="50" />
                  <el-pagination v-if="pickerTotal>12" small background layout="prev,pager,next"
                    :page-size="12" :total="pickerTotal" :current-page="pickerPage"
                    @current-change="onPickerPage" class="picker-pager" />
                </div>

                <!-- Cancel add for multi -->
                <div v-if="isMulti && imgList.length > 0" style="margin-top:10px">
                  <el-button size="small" @click="showImgInput=false">完成添加</el-button>
                </div>
              </div>
            </template>

            <!-- GALLERY: image preview cards (always shown when images exist) -->
            <div class="img-gallery" v-if="imgList.length">
              <div v-for="(url, i) in imgList" :key="i" class="img-card">
                <div class="img-card-inner">
                  <img :src="url" class="img-card-img" @error="onImgError($event)" />
                </div>
                <span class="img-card-name" :title="url">{{ getShortName(url) }}</span>
                <el-button link class="img-card-del" @click="removeImage(i)">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
              <!-- Add more button for multi-image -->
              <div v-if="isMulti && !showImgInput" class="img-card img-card-add" @click="openAddMore">
                <el-icon :size="30"><Plus /></el-icon>
                <span>添加图片</span>
              </div>
            </div>

            <!-- Replace button for single image -->
            <el-button v-if="!isMulti && imgList.length && !showImgInput"
              type="primary" plain @click="openAddMore" style="margin-top:8px">更换图片</el-button>

          </el-form-item>
        </template>

        <!-- Normal value (non-image) -->
        <el-form-item label="值" prop="configValue" v-else>
          <el-input v-model="form.configValue" type="textarea" :rows="4"
            maxlength="2000" show-word-limit placeholder="配置值" />
        </el-form-item>

        <el-form-item label="描述">
          <el-input v-model="form.description" maxlength="500" placeholder="配置描述" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.configType" style="width:100%">
            <el-option label="公开" value="public" />
            <el-option label="私有" value="private" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible=false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed, nextTick } from 'vue'
import { siteConfigApi, resourceApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Plus, Close } from '@element-plus/icons-vue'

const IMAGE_KEYS = ['logo', 'background', 'avatar', 'cover', 'image', 'favicon', 'icon']
const MULTI_KEYS = ['random_cover', 'randomCover']

const items = ref([])
const tableLoading = ref(false)
const dlgVisible = ref(false)
const editRow = ref(null)
const saveLoading = ref(false)
const formRef = ref(null)
const imgMethod = ref('upload')
const imgUrlInput = ref('')
const imgList = ref([])
const showImgInput = ref(true)
const uploadUrl = '/api/admin/resources/upload'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

const pickerResources = ref([])
const pickerPage = ref(1)
const pickerTotal = ref(0)

const form = reactive({ configKey:'', configValue:'', description:'', configType:'public' })
const rules = {
  configKey: [{ required:true, message:'请输入键名', trigger:'blur' }],
  configValue: [{ required:true, message:'请输入值', trigger:'blur' }]
}

const isMulti = computed(() => isMultiImage(form.configKey))

onMounted(() => fetchItems())

async function fetchItems() {
  tableLoading.value = true
  try { items.value = await siteConfigApi.listAll() } catch(e) { ElMessage.error('加载失败') }
  finally { tableLoading.value = false }
}

function isImageKey(key) { return key && IMAGE_KEYS.some(p => key.toLowerCase().includes(p)) }
function isMultiImage(key) { return key && MULTI_KEYS.some(k => key.toLowerCase().includes(k)) }
function parseMultiUrl(val) { try { const a = JSON.parse(val); return Array.isArray(a) ? a : [] } catch { return [] } }
function getShortName(url) { return url ? url.split('/').pop() : '' }
function onImgError(e) { e.target.style.display = 'none' }

// Sync imgList → form.configValue
function listToValue(list) {
  if (isMulti.value) return JSON.stringify(list)
  return list[0] || ''
}
function valueToList(val) {
  if (!val) return []
  if (isMulti.value) {
    const parsed = parseMultiUrl(val)
    return parsed.length ? parsed : (val ? [val] : [])
  }
  return [val]
}

watch(imgList, (list) => { form.configValue = listToValue(list) }, { deep:true })
watch(() => form.configKey, () => { imgMethod.value='upload'; imgList.value=[]; imgUrlInput.value=''; showImgInput.value=true })

function showDialog(row) {
  editRow.value = row
  imgMethod.value = 'upload'
  imgUrlInput.value = ''
  // Need to set form first so isMulti computed works, then parse value
  const key = row ? (row.configKey || '') : ''
  const val = row ? (row.configValue || '') : ''
  Object.assign(form, { configKey:key, configValue:val, description:row?.description||'', configType:row?.configType||'public' })
  nextTick(() => {
    imgList.value = valueToList(val)
    showImgInput.value = imgList.value.length === 0
  })
  dlgVisible.value = true
}

function openAddMore() {
  imgUrlInput.value = ''
  imgMethod.value = 'upload'
  showImgInput.value = true
}

function addImageUrl() {
  const url = imgUrlInput.value.trim()
  if (!url) return
  if (!isMulti.value) imgList.value = [url]
  else if (!imgList.value.includes(url)) imgList.value.push(url)
  imgUrlInput.value = ''
  showImgInput.value = false
}

function removeImage(i) {
  imgList.value.splice(i, 1)
  if (imgList.value.length === 0) showImgInput.value = true
}

function onImageUploaded(res) {
  if (res.code===200&&res.data) {
    const path = res.data.path
    if (!isMulti.value) imgList.value = [path]
    else if (!imgList.value.includes(path)) imgList.value.push(path)
    showImgInput.value = false
    ElMessage.success('上传成功')
  } else ElMessage.error('上传失败')
}

watch(() => imgMethod.value, (v) => { if (v==='picker') { pickerPage.value=1; loadPicker() } })
async function loadPicker() {
  try { const d = await resourceApi.list({ page:pickerPage.value, size:12 }); pickerResources.value=d?.records||[]; pickerTotal.value=d?.total||0 } catch(e){}
}
function onPickerPage(p) { pickerPage.value=p; loadPicker() }
function pickFromResource(path) {
  if (!isMulti.value) { imgList.value = [path]; showImgInput.value = false; return }
  const idx = imgList.value.indexOf(path)
  if (idx>=0) imgList.value.splice(idx,1)
  else imgList.value.push(path)
  if (imgList.value.length > 0) showImgInput.value = false
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(()=>false)
  if (!valid) return
  saveLoading.value = true
  try {
    const data = { ...form }
    if (editRow.value) data.id = editRow.value.id
    await siteConfigApi.save(data)
    ElMessage.success(editRow.value?'更新成功':'创建成功')
    dlgVisible.value = false
    await fetchItems()
  } catch(e) { ElMessage.error(e?.message||'保存失败') }
  finally { saveLoading.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除？','提示',{ type:'warning',center:true })
    await siteConfigApi.delete(id)
    ElMessage.success('已删除')
    await fetchItems()
  } catch(e){}
}
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px }
.page-header h2 { margin:0; font-size:20px; font-weight:700; color:#303133; letter-spacing:1px }

/* ====== Table ====== */
.admin-table { width:100%; font-size:14px }
.admin-table :deep(.el-table__row) { height:82px }
.admin-table :deep(.el-table__cell) { padding:8px 0 }
.table-thumb { width:120px; height:74px; border-radius:8px }
.multi-thumbs { display:flex; gap:6px; flex-wrap:wrap }
.value-cell { display:flex; align-items:center }
.value-text { font-size:13px; color:#666; word-break:break-all }
/* ====== Image input area ====== */
.img-input-wrap { width:100% }
.method-select { width:140px; margin-bottom:12px }

/* ====== URL row ====== */
.url-row { display:flex; align-items:center; width:100% }

/* ====== Big upload area ====== */
.upload-row { width:100% }
.upload-big { width:100% }
.upload-big :deep(.el-upload) { width:100% }
.upload-big :deep(.el-upload-dragger) {
  width:100%; border-radius:12px; border:2px dashed #d9d9d9;
  padding:60px 20px; transition:all .3s; text-align:center;
}
.upload-big :deep(.el-upload-dragger:hover) { border-color:#409EFF; background:#f9fbff }
.upload-big-icon { font-size:48px; color:#c0c4cc; margin-bottom:10px }
.upload-big-text { font-size:16px; color:#666; font-weight:500 }
.upload-big-hint { font-size:13px; color:#aaa; margin-top:6px }

/* ====== Picker ====== */
.picker-area { width:100%; margin-bottom:0 }
.picker-grid { display:grid; grid-template-columns:repeat(5,1fr); gap:8px; max-height:260px; overflow-y:auto }
.picker-card { border:2px solid transparent; border-radius:8px; overflow:hidden; cursor:pointer; transition:all .15s; background:#f9f9f9 }
.picker-card:hover { border-color:#a0cfff }
.picker-sel { border-color:#409EFF!important; box-shadow:0 0 0 2px rgba(64,158,255,.15) }
.picker-img { width:100%; height:74px; object-fit:cover }
.picker-fn { display:block; padding:2px 4px; font-size:10px; color:#aaa; text-align:center; overflow:hidden; text-overflow:ellipsis; white-space:nowrap }
.picker-pager { margin-top:8px; justify-content:center }

/* ====== Image Gallery ====== */
.img-gallery {
  display:grid; grid-template-columns:repeat(auto-fill, minmax(140px,1fr));
  gap:10px;
}
.img-card {
  position:relative; border-radius:10px; overflow:hidden;
  background:#f5f6f8; border:1px solid #ebeef5;
  transition:all .2s;
}
.img-card:hover { border-color:#c0c4cc }
.img-card-inner {
  width:100%; height:100px; display:flex; align-items:center; justify-content:center;
  background:#f0f0f0; overflow:hidden;
}
.img-card-img {
  width:100%; height:100%; object-fit:cover; display:block;
}
.img-card-name {
  display:block; padding:5px 8px; font-size:11px; color:#888;
  overflow:hidden; text-overflow:ellipsis; white-space:nowrap; text-align:center;
}
.img-card-del {
  position:absolute; top:4px; right:4px;
  background:rgba(0,0,0,.5); color:#fff; border-radius:50%;
  width:22px; height:22px; display:flex; align-items:center; justify-content:center;
  opacity:0; transition:opacity .2s;
}
.img-card:hover .img-card-del { opacity:1 }

.img-card-add {
  cursor:pointer; display:flex; flex-direction:column; align-items:center; justify-content:center;
  min-height:100px; border:2px dashed #d9d9d9; background:transparent; color:#bbb; gap:4px;
  transition:all .2s;
}
.img-card-add:hover { border-color:#409EFF; color:#409EFF; background:#f0f7ff }
</style>
