<template>
  <svg
    class="line-icon"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    :stroke-width="strokeWidth"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path v-for="(d, i) in icon.paths" :key="i" :d="d" />
    <circle v-for="(c, i) in icon.circles || []" :key="'c' + i" :cx="c[0]" :cy="c[1]" :r="c[2]" />
  </svg>
</template>

<script setup>
/**
 * 线条图标。项目里原来到处是 emoji（💼🚀📋🎯🖼️🏠💕🌳），不同系统渲染差别大、
 * 观感也偏幼稚，统一换成 1.5px 描边的单色图标；颜色跟 currentColor 走，
 * 调用方只管改 color 就行。
 *
 * 24x24 网格、无填充，风格参考 Feather（MIT）。**不引依赖**：生产机没有外网、
 * CSP 是 script-src 'self'，图标只能内联进包里。
 */
import { computed } from 'vue'

const ICONS = {
  // —— 导航 ——
  home: { paths: ['M3 9.5 12 3l9 6.5V20a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z', 'M9.5 22v-8h5v8'] },
  heart: { paths: ['M20.8 5.1a5.2 5.2 0 0 0-7.4 0L12 6.5l-1.4-1.4a5.2 5.2 0 0 0-7.4 7.4L12 21l8.8-8.5a5.2 5.2 0 0 0 0-7.4z'] },
  leaf: {
    paths: [
      'M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.5 19 2c1 2 2 4.2 2 8 0 5.5-4.8 10-10 10z',
      'M2 21c0-3 1.9-5.4 5.1-6C9.5 14.5 12 13 13 12'
    ]
  },
  brush: { paths: ['M12 20h9', 'M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z'] },
  user: { paths: ['M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'], circles: [[12, 7, 4]] },
  // —— 简历 ——
  feather: { paths: ['M20.2 12.2a6 6 0 0 0-8.5-8.5L5 10.5V19h8.5z', 'M16 8 2 22', 'M17.5 15H9'] },
  route: { paths: ['M6 3v12', 'M18 9a9 9 0 0 1-9 9'], circles: [[18, 6, 3], [6, 18, 3]] },
  layers: { paths: ['M12 2l10 5-10 5L2 7z', 'M2 12l10 5 10-5', 'M2 17l10 5 10-5'] },
  briefcase: { paths: ['M5 7h14a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2z', 'M16 20V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v15'] },
  'file-text': { paths: ['M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z', 'M14 2v6h6', 'M16 13H8', 'M16 17H8'] },
  target: { paths: [], circles: [[12, 12, 9], [12, 12, 5], [12, 12, 1.5]] },
  image: { paths: ['M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z', 'M21 15l-5-5L5 21'], circles: [[8.5, 8.5, 1.5]] },
  book: { paths: ['M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z', 'M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z'] },
  tool: { paths: ['M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.8-3.8a6 6 0 0 1-8 8l-6.9 6.9a2.1 2.1 0 0 1-3-3l6.9-6.9a6 6 0 0 1 8-8l-3.8 3.8z'] },
  link: { paths: ['M10 13a5 5 0 0 0 7.5.5l3-3a5 5 0 0 0-7-7L11.8 6.2', 'M14 11a5 5 0 0 0-7.5-.5l-3 3a5 5 0 0 0 7 7l1.7-1.7'] },
  code: { paths: ['M16 18l6-6-6-6', 'M8 6l-6 6 6 6'] },
  gift: { paths: ['M20 12v9a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1v-9', 'M3 8h18v4H3z', 'M12 22V8', 'M12 8H7.5a2.5 2.5 0 0 1 0-5C11 3 12 8 12 8z', 'M12 8h4.5a2.5 2.5 0 0 0 0-5C13 3 12 8 12 8z'] },
  droplet: { paths: ['M12 2.7l5.7 5.6a8 8 0 1 1-11.4 0z'] },
  pin: { paths: ['M20 10c0 6.5-8 12.5-8 12.5S4 16.5 4 10a8 8 0 0 1 16 0z'], circles: [[12, 10, 2.8]] },
  clock: { paths: ['M12 7v5l3.5 2'], circles: [[12, 12, 9]] },
  mail: { paths: ['M4 5h16a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z', 'M21 6.5 12 13 3 6.5'] },
  play: { paths: ['M8 5.5v13l11-6.5z'] },
  moon: { paths: ['M21 12.8A9 9 0 1 1 11.2 3 7 7 0 0 0 21 12.8z'] },
  star: { paths: ['M12 3l2.9 5.9 6.1.9-4.5 4.3 1.1 6-5.6-3.1-5.6 3.1 1.1-6L3 9.8l6.1-.9z'] },
  sliders: { paths: ['M4 21v-7', 'M4 10V3', 'M12 21v-9', 'M12 8V3', 'M20 21v-5', 'M20 12V3', 'M1 14h6', 'M9 8h6', 'M17 16h6'] },
  grid: { paths: ['M4 3h16a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z', 'M3 9h18', 'M9 21V9'] },
  music: { paths: ['M9 18V5l10-2v13'], circles: [[6, 18, 3], [16, 16, 3]] },
  trash: { paths: ['M3 6h18', 'M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2', 'M5.5 6l1 14a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1l1-14'] },
  'arrow-right': { paths: ['M4 12h15', 'M13 6l6 6-6 6'] },
  'log-out': { paths: ['M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4', 'M16 17l5-5-5-5', 'M21 12H9'] },
  check: { paths: ['M4 12.5 9.5 18 20 6'] }
}

const FALLBACK = { paths: [], circles: [[12, 12, 9]] }

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 18 },
  strokeWidth: { type: [Number, String], default: 1.5 }
})

const icon = computed(() => ICONS[props.name] || FALLBACK)
</script>

<style scoped>
.line-icon {
  display: block;
  flex-shrink: 0;
}
</style>
