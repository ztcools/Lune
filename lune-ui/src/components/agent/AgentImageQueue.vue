<template>
  <div v-if="images.length" class="image-queue">
    <div
      v-for="(img, idx) in images"
      :key="idx"
      class="img-chip"
      :class="{ uploading: img.uploading }"
    >
      <img :src="img.url" class="chip-thumb" alt="" />
      <div v-if="img.uploading" class="chip-spinner">
        <span class="spinner-dot"></span>
      </div>
      <button v-else class="chip-remove" @click="$emit('remove', idx)" title="移除">×</button>
      <span class="chip-name" v-if="img.uploading">上传中</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  images: { type: Array, default: () => [] }
})

defineEmits(['remove'])
</script>

<style scoped>
.image-queue {
  display: flex; flex-wrap: wrap; gap: 8px;
  padding: 6px 0 10px 2px;
}
.img-chip {
  position: relative; width: 56px; height: 56px;
  border-radius: 10px; overflow: hidden;
  border: 2px solid #e0e6e0; transition: border-color 0.2s;
  animation: chipIn 0.2s ease-out;
}
.img-chip:hover { border-color: #81c784; }
.img-chip.uploading { border-color: #e0e6e0; opacity: 0.7; }
.chip-thumb { width: 100%; height: 100%; object-fit: cover; }
.chip-spinner {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.5);
}
.spinner-dot {
  width: 16px; height: 16px; border: 2px solid #81c784; border-top-color: transparent;
  border-radius: 50%; animation: spin 0.6s linear infinite;
}
.chip-remove {
  position: absolute; top: -2px; right: -2px;
  width: 18px; height: 18px; border: none; border-radius: 50%;
  background: rgba(0,0,0,0.55); color: #fff; font-size: 11px; line-height: 1;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.img-chip:hover .chip-remove { opacity: 1; }
.chip-name {
  position: absolute; bottom: 2px; left: 2px; right: 2px;
  text-align: center; font-size: 9px; color: #666; background: rgba(255,255,255,0.8);
  border-radius: 3px; padding: 0 2px; overflow: hidden; white-space: nowrap;
  text-overflow: ellipsis;
}
@keyframes chipIn { from { opacity: 0; transform: scale(0.8); } }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
