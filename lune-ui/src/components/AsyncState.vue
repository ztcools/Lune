<template>
  <div v-if="errorRef" class="async-state">
    <el-result icon="error" :title="errorTitle" :sub-title="errorRef">
      <template #extra>
        <el-button v-if="retry" type="primary" @click="retry">重试</el-button>
      </template>
    </el-result>
  </div>
  <div v-else-if="loading" class="async-state">
    <el-skeleton :rows="skeletonRows" animated />
  </div>
  <div v-else-if="empty" class="async-state">
    <el-empty :description="emptyText" :image-size="emptyImageSize" />
  </div>
  <slot v-else />
</template>

<script setup>
/**
 * AsyncState — 统一异步状态展示组件
 *
 * 为各视图提供一致的 loading / empty / error + retry UI，
 * 减少各页面中散落的 skeleton / empty / error 模板片段。
 *
 * 用法:
 * <AsyncState :loading="loading" :error="error" :empty="!list.length"
 *   empty-text="暂无数据" @retry="fetch">
 *   <YourList :items="list" />
 * </AsyncState>
 */
defineProps({
  loading:     { type: Boolean, default: false },
  error:       { type: [String, Error, Object], default: '' },
  empty:       { type: Boolean, default: false },
  emptyText:   { type: String, default: '暂无数据' },
  emptyImageSize: { type: Number, default: 80 },
  errorTitle:  { type: String, default: '加载失败' },
  skeletonRows:{ type: Number, default: 6 }
})

const emit = defineEmits(['retry'])

function retry() {
  emit('retry')
}
</script>

<style scoped>
.async-state {
  padding: 40px 0;
}
</style>
