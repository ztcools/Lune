<template>
  <router-view />
</template>

<script setup>
import { useAppStore } from './stores/app'

// 视口侦测必须在这里启动：Landing / 简历 / 后台都不经过 PublicLayout，
// 放在布局里等于让那些页面一直以为自己在 PC 上（移动端背景图因此从未生效）。
useAppStore().initViewport()
</script>
