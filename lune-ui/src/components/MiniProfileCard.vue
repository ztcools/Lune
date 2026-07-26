<template>
  <teleport to="body">
    <transition name="mini-fade">
      <div v-if="visible" class="mini-overlay" @click.self="close">
        <div class="mini-card" :style="{ top: pos.y + 'px', left: pos.x + 'px' }">
          <div class="mini-glow" />
          <button class="mini-close" @click="close">✕</button>
          <div class="mini-inner">
            <el-avatar :size="64" :src="user.avatar" class="mini-avatar">
              {{ (user.nickname || '?').charAt(0) }}
            </el-avatar>
            <h3 class="mini-nick">{{ user.nickname || user.username || '未知' }}</h3>
            <div class="mini-tags">
              <span v-if="user.gender" class="mini-tag">{{ user.gender }}</span>
              <span v-if="age" class="mini-tag">{{ age }}岁</span>
            </div>
            <p v-if="user.signature" class="mini-sig">{{ user.signature }}</p>
            <p v-else class="mini-sig muted">这个人很懒，什么都没写~</p>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import request from '../api/request'

const props = defineProps({
  userId: { type: Number, default: null },
  position: { type: Object, default: () => ({ x: 0, y: 0 }) },
  show: { type: Boolean, default: false }
})
const emit = defineEmits(['close'])

const visible = ref(false)
const user = ref({})
const pos = ref({ x: 0, y: 0 })

const age = computed(() => {
  if (!user.value.birthday) return null
  const b = new Date(user.value.birthday)
  const diff = Date.now() - b.getTime()
  return Math.floor(diff / 31557600000)
})

watch(() => props.show, async (v) => {
  if (v && props.userId) {
    try {
      const data = await request.get(`/user/profile/${props.userId}`)
      user.value = data || {}
    } catch (e) { user.value = {} }
    pos.value = {
      x: Math.min(props.position.x, window.innerWidth - 250),
      y: Math.min(props.position.y, window.innerHeight - 260)
    }
    visible.value = true
  }
})

function close() { visible.value = false; emit('close') }
</script>

<style scoped>
.mini-overlay {
  position: fixed; inset: 0; z-index: 10001;
}
.mini-card {
  position: fixed;
  width: 220px;
  background: linear-gradient(155deg, rgba(255,255,255,0.92) 0%, rgba(232,245,233,0.95) 50%, rgba(200,230,201,0.92) 100%);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1.5px solid rgba(255,255,255,0.5);
  box-shadow: 0 8px 40px rgba(56,142,60,0.18), 0 2px 8px rgba(0,0,0,0.08);
  overflow: hidden;
  font-family: var(--trendy-font);
  transform: translate(-50%, -110%);
}
.mini-glow {
  position: absolute; top: -30%; left: -30%; width: 160%; height: 160%;
  background: radial-gradient(circle at 30% 20%, rgba(129,199,132,0.12) 0%, transparent 50%);
  pointer-events: none;
}
.mini-close {
  position: absolute; top: 8px; right: 8px; z-index: 2;
  width: 24px; height: 24px; border-radius: 50%; border: none;
  background: rgba(0,0,0,0.05); color: #999; font-size: 12px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.mini-close:hover { background: #ff5252; color: #fff; }
.mini-inner {
  position: relative; z-index: 1; padding: 24px 20px 18px;
  display: flex; flex-direction: column; align-items: center;
  text-align: center;
}
.mini-avatar {
  border: 3px solid #81c784;
  box-shadow: 0 4px 12px rgba(76,175,80,0.2);
  margin-bottom: 10px;
}
.mini-nick {
  font-size: 17px; font-weight: 700; color: #2e7d32; margin: 0 0 6px;
}
.mini-tags { display: flex; gap: 6px; margin-bottom: 8px; }
.mini-tag {
  font-size: 11px; font-weight: 600; color: #66bb6a;
  background: rgba(76,175,80,0.1); padding: 2px 10px; border-radius: 10px;
}
.mini-sig { font-size: 13px; color: #555; margin: 0; line-height: 1.5; }
.mini-sig.muted { color: #bbb; font-style: italic; }

.mini-fade-enter-active, .mini-fade-leave-active { transition: opacity 0.2s ease; }
.mini-fade-enter-from, .mini-fade-leave-to { opacity: 0; }
</style>
