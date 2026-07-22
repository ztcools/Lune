<template>
  <div>
    <el-tag effect="dark" class="my-tag">📊 统计信息</el-tag>
    <div class="history-title">总览</div>
    <el-row :gutter="20">
      <el-col :xs="12" :sm="6" v-for="s in stats" :key="s.label">
        <el-card shadow="hover" style="text-align:center;margin-bottom:16px">
          <div style="font-size:36px;font-weight:bold;color:var(--themeBackground)">{{ s.value }}</div>
          <div style="font-size:14px;color:var(--greyFont);margin-top:8px">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <div class="history-title">最近文章</div>
    <el-table :data="articles" border>
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
      <el-table-column label="时间" width="160" align="center">
        <template #default="{row}">{{ new Date(row.createTime).toLocaleDateString() }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { articleApi, categoryApi, userApi } from '../api/modules'
const stats = reactive([
  { label:'文章总数', value:0 }, { label:'分类总数', value:0 }, { label:'用户总数', value:0 }, { label:'访问量', value:0 }
])
const articles = ref([])
onMounted(async () => {
  try { const d = await articleApi.list({page:1,size:1}); stats[0].value = d.total } catch(e){}
  try { const d = await categoryApi.listAll(); stats[1].value = d.length } catch(e){}
  try { const d = await userApi.list({page:1,size:1}); stats[2].value = d.total } catch(e){}
  try { const d = await articleApi.list({page:1,size:5}); articles.value = d.records } catch(e){}
})
</script>

<style scoped>
.my-tag{width:100%;text-align:left;background:var(--lightYellow);border:none;height:40px;line-height:40px;font-size:16px;color:var(--black)}
.history-title{margin:15px auto;text-align:center;padding:10px 20px;background:var(--lightGreen);color:var(--white);font-weight:bold;border-radius:5px;width:150px}
</style>
