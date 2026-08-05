<template>
  <router-view />
</template>

<script setup>
import { useAppStore } from './stores/app'

const appStore = useAppStore()

// 视口侦测必须在这里启动：Landing / 简历 / 后台都不经过 PublicLayout，
// 放在布局里等于让那些页面一直以为自己在 PC 上（移动端背景图因此从未生效）。
appStore.initViewport()

// 每日访客 ping：App.vue 是根组件，SPA 生命周期内只挂载一次，
// 比放在 PublicLayout 里更可靠（避免布局重挂载导致重复 ping）。
appStore.pingVisit()
</script>
