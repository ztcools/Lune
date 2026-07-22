<template>
  <div>
    <div class="page-header"><h2>家园管理</h2><el-button type="primary" @click="showDialog(null)">新建</el-button></div>
    <el-table :data="items" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="创建时间" width="160"><template #default="{row}">{{ formatDate(row.createTime) }}</template></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dlg" :title="edit?'编辑':'新建'" width="600px">
      <el-form :model="fm" label-width="100px">
        <el-form-item label="标题"><el-input v-model="fm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="fm.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="封面"><el-input v-model="fm.cover" placeholder="图片URL" /></el-form-item>
        <el-form-item label="背景"><el-input v-model="fm.bgCover" placeholder="图片URL" /></el-form-item>
        <el-form-item label="纪念日"><el-input v-model="fm.timing" /></el-form-item>
        <el-form-item label="倒计时标题"><el-input v-model="fm.countdownTitle" /></el-form-item>
        <el-form-item label="倒计时日期"><el-date-picker v-model="fm.countdownTime" type="datetime" placeholder="选择日期" /></el-form-item>
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
const items = ref([]); const dlg = ref(false); const edit = ref(null)
const fm = ref({ title:'', content:'', cover:'', bgCover:'', timing:'', countdownTitle:'', countdownTime:null, status:1 })
onMounted(() => fetch())
async function fetch() { try { items.value = await familyApi.listAll() } catch(e){} }
function showDialog(row) { edit.value = row; fm.value = row ? { ...row, countdownTime: row.countdownTime ? new Date(row.countdownTime) : null } : { title:'', content:'', cover:'', bgCover:'', timing:'', countdownTitle:'', countdownTime:null, status:1 }; dlg.value = true }
async function save() { try { const data = { ...fm.value }; if (data.countdownTime) data.countdownTime = data.countdownTime.toISOString(); edit.value ? await familyApi.update(edit.value.id, data) : await familyApi.create(data); dlg.value=false; ElMessage.success('成功'); await fetch() } catch(e){} }
async function del(id) { try { await ElMessageBox.confirm('确定删除？','提示',{type:'warning'}); await familyApi.delete(id); ElMessage.success('删除成功'); await fetch() } catch(e){} }
function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '' }
</script>
<style scoped>.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}</style>
