<template>
  <canvas ref="canvasRef" class="sakura-canvas" />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  count: { type: Number, default: 40 },
  minSize: { type: Number, default: 8 },
  maxSize: { type: Number, default: 18 },
  fallSpeed: { type: Number, default: 1.5 },
  wind: { type: Number, default: 0.5 }
})

const canvasRef = ref(null)
let ctx = null
let animId = null
let petals = []
let w = 0, h = 0

const colors = [
  'rgba(255,183,197,0.9)', 'rgba(255,160,180,0.85)',
  'rgba(255,200,210,0.8)', 'rgba(255,150,170,0.9)',
  'rgba(255,190,200,0.75)'
]

function createPetal() {
  return {
    x: Math.random() * w,
    y: -20 - Math.random() * h,
    size: props.minSize + Math.random() * (props.maxSize - props.minSize),
    speed: 0.5 + Math.random() * props.fallSpeed,
    wind: (Math.random() - 0.5) * props.wind * 2,
    rotation: Math.random() * 360,
    rotSpeed: (Math.random() - 0.5) * 2,
    sway: Math.random() * Math.PI * 2,
    swaySpeed: 0.01 + Math.random() * 0.02,
    color: colors[Math.floor(Math.random() * colors.length)],
    opacity: 0.5 + Math.random() * 0.5
  }
}

function drawPetal(p) {
  ctx.save()
  ctx.translate(p.x, p.y)
  ctx.rotate((p.rotation * Math.PI) / 180)
  ctx.globalAlpha = p.opacity
  ctx.fillStyle = p.color
  // Draw petal shape
  ctx.beginPath()
  ctx.ellipse(0, 0, p.size * 0.6, p.size * 0.25, 0, 0, Math.PI * 2)
  ctx.fill()
  // Smaller overlapping ellipse for petal look
  ctx.beginPath()
  ctx.ellipse(p.size * 0.2, 0, p.size * 0.4, p.size * 0.2, 0.3, 0, Math.PI * 2)
  ctx.fill()
  ctx.restore()
}

function animate() {
  ctx.clearRect(0, 0, w, h)
  for (const p of petals) {
    p.y += p.speed
    p.sway += p.swaySpeed
    p.x += p.wind + Math.sin(p.sway) * 0.5
    p.rotation += p.rotSpeed
    if (p.y > h + 30) {
      p.y = -30
      p.x = Math.random() * w
    }
    if (p.x > w + 30) p.x = -30
    if (p.x < -30) p.x = w + 30
    drawPetal(p)
  }
  animId = requestAnimationFrame(animate)
}

function resize() {
  const c = canvasRef.value
  if (!c) return
  w = c.width = window.innerWidth
  h = c.height = window.innerHeight
  petals = Array.from({ length: props.count }, createPetal)
}

onMounted(() => {
  const c = canvasRef.value
  if (!c) return
  ctx = c.getContext('2d')
  resize()
  animate()
  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  cancelAnimationFrame(animId)
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.sakura-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 1;
}
</style>
