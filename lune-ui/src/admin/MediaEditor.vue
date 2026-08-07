<template>
  <div class="media-editor">
    <div class="media-list">
      <div v-for="(m, i) in items" :key="i" class="media-item">
        <LuneImage v-if="m.type === 'image'" :src="m.url" variant="thumb" alt="" />
        <video v-else :src="m.url" class="media-thumb video" controls preload="metadata"></video>
        <div class="media-mask">
          <el-icon class="media-del" @click="remove(i)"><Delete /></el-icon>
        </div>
        <span class="media-type">{{ m.type === 'image' ? '图' : '视' }}</span>
      </div>
      <el-upload
        v-if="items.length < max"
        class="media-uploader"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        :on-success="onUploaded"
        :before-upload="beforeUpload"
        accept="image/*,video/*"
      >
        <div class="upload-box">
          <el-icon class="upload-plus"><Plus /></el-icon>
          <span class="upload-tip">添加</span>
        </div>
      </el-upload>
    </div>
    <div class="media-hint">支持图片/视频，最多 {{ max }} 个，单个不超过 50MB</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import LuneImage from '../components/LuneImage.vue'

const props = defineProps({
  modelValue: { type: String, default: '' }, // JSON 字符串
  max: { type: Number, default: 9 }
})
const emit = defineEmits(['update:modelValue'])

const uploadUrl = '/api/admin/resources/upload'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

const items = computed({
  get() {
    if (!props.modelValue) return []
    try { const a = JSON.parse(props.modelValue); return Array.isArray(a) ? a : [] } catch { return [] }
  },
  set(val) { emit('update:modelValue', val.length ? JSON.stringify(val) : '') }
})

function beforeUpload(file) {
  if (file.size > 50 * 1024 * 1024) { ElMessage.error('文件不能超过 50MB'); return false }
  return true
}

function onUploaded(res) {
  if (res.code === 200 && res.data) {
    const isVideo = /\.(mp4|webm|ogg|mov|avi|mkv)(\?|$)/i.test(res.data.path) || (res.data.mimeType || '').startsWith('video/')
    items.value = [...items.value, { type: isVideo ? 'video' : 'image', url: res.data.path }]
    ElMessage.success('上传成功')
  } else ElMessage.error('上传失败')
}

function remove(i) { const arr = [...items.value]; arr.splice(i, 1); items.value = arr }
</script>

<style scoped>
.media-list { display: flex; flex-wrap: wrap; gap: 10px; }
.media-item { position: relative; width: 92px; height: 92px; border-radius: 12px; overflow: hidden; }
.media-thumb { width: 100%; height: 100%; object-fit: cover; }
.media-thumb.video { background: #000; }
.media-mask { position: absolute; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.25s; }
.media-item:hover .media-mask { opacity: 1; }
.media-del { color: #fff; font-size: 22px; cursor: pointer; }
.media-type { position: absolute; top: 4px; left: 4px; background: rgba(0,0,0,0.55); color: #fff; font-size: 11px; padding: 1px 6px; border-radius: 8px; }
.media-uploader { width: 92px; height: 92px; }
.upload-box { width: 92px; height: 92px; border: 2px dashed #c8e6c9; border-radius: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; transition: all 0.3s; color: #81c784; }
.upload-box:hover { border-color: #4caf50; background: #f1f8f1; }
.upload-plus { font-size: 26px; }
.upload-tip { font-size: 12px; margin-top: 2px; }
.media-hint { font-size: 12px; color: #999; margin-top: 8px; }
:deep(.el-upload) { width: 92px; height: 92px; }
</style>
