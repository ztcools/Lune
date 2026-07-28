<template>
  <div class="spotlight-layer" aria-hidden="true">
    <div class="spot spot-1"></div>
    <div class="spot spot-2"></div>
    <div class="spot spot-3"></div>
  </div>
</template>

<script setup>
// 柔和移动聚光灯装饰，填补空白区域
</script>

<style scoped>
.spotlight-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}
.spot {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.5;
  will-change: transform;
}
.spot-1 {
  width: 42vw; height: 42vw;
  background: radial-gradient(circle, rgba(129,199,132,0.35), transparent 70%);
  top: -10%; left: -8%;
  animation: drift-1 22s ease-in-out infinite;
}
.spot-2 {
  width: 36vw; height: 36vw;
  background: radial-gradient(circle, rgba(255,183,77,0.3), transparent 70%);
  bottom: -12%; right: -6%;
  animation: drift-2 26s ease-in-out infinite;
}
.spot-3 {
  width: 28vw; height: 28vw;
  background: radial-gradient(circle, rgba(79,195,247,0.28), transparent 70%);
  top: 40%; left: 55%;
  animation: drift-3 30s ease-in-out infinite;
}
@keyframes drift-1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(6vw, 8vh) scale(1.15); }
}
@keyframes drift-2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-8vw, -6vh) scale(1.1); }
}
@keyframes drift-3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-5vw, 7vh) scale(0.9); }
}
</style>
