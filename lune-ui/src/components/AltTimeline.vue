<template>
  <ol class="alt-timeline" :class="{ single: single }" :style="{ '--line': lineColor, '--accent': accent, '--bubble-w': bubbleWidth }">
    <li
      v-for="(item, i) in items"
      :key="item[itemKey] ?? i"
      class="alt-li"
      :style="{ '--delay': i * 0.12 + 's' }"
    >
      <div class="alt-content" :class="sideClass(i)">
        <!-- 中轴上的圆点 -->
        <span class="alt-node"></span>

        <!-- 头像（可选）：没有这个插槽时气泡不预留 90px 让位 -->
        <div v-if="$slots.avatar" class="alt-avatar">
          <slot name="avatar" :item="item" :index="i" />
        </div>

        <div
          class="alt-bubble"
          :class="{ 'has-avatar': !!$slots.avatar, clickable: clickable }"
          :style="{ background: bubbleBg(i) }"
          @click="clickable && $emit('item-click', item)"
        >
          <span class="alt-tail" :style="tailStyle(i)"></span>
          <slot :item="item" :index="i" />
        </div>
      </div>
    </li>
  </ol>
</template>

<script setup>
/**
 * 左右交错时间线。原本长在树洞页里（那版 UI 做得不错），树洞改成纯弹幕后
 * 把这套几何抽出来复用 —— 目前用在简历页「履痕」。
 *
 * 只负责布局（中轴线、交错、圆点、气泡尖角、移动端收成单侧），
 * 气泡里长什么样由调用方用默认插槽决定，配色走 props，不写死绿系。
 */
const props = defineProps({
  items: { type: Array, default: () => [] },
  // 收成单侧（移动端，或调用方就想要单列）
  single: { type: Boolean, default: false },
  // 气泡底色：给数组则按下标循环，不给就是纯白
  colors: { type: Array, default: () => [] },
  lineColor: { type: String, default: 'var(--themeBackground)' },
  accent: { type: String, default: 'var(--blue)' },
  bubbleWidth: { type: String, default: '380px' },
  clickable: { type: Boolean, default: false },
  itemKey: { type: String, default: 'id' }
})

defineEmits(['item-click'])

function sideClass(i) {
  if (props.single) return 'alt-right'
  return i % 2 === 0 ? 'alt-left' : 'alt-right'
}

function bubbleBg(i) {
  if (!props.colors.length) return '#ffffff'
  return props.colors[i % props.colors.length]
}

// 尖角是用 border 画的三角，颜色必须跟气泡底色一致，所以只能内联算
function tailStyle(i) {
  const c = bubbleBg(i)
  const left = props.single || i % 2 !== 0
  return left
    ? { borderColor: `transparent transparent transparent ${c}` }
    : { borderColor: `transparent ${c} transparent transparent` }
}
</script>

<style scoped>
.alt-timeline {
  position: relative;
  list-style: none;
  margin: 0;
  padding: 20px 0;
}

/* 中轴线 */
.alt-timeline::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  width: 2px;
  transform: translateX(-50%);
  background: linear-gradient(180deg, transparent, var(--line) 8%, var(--line) 92%, transparent);
  border-radius: 2px;
}
.alt-timeline.single::before { left: 0; transform: none; }

.alt-li {
  margin: 0 auto 22px;
  animation: altIn 0.6s ease both;
  animation-delay: var(--delay);
}

.alt-content { position: relative; width: 50%; }
.alt-left { text-align: right; }
.alt-right { margin-left: 50%; }
.alt-timeline.single .alt-content { width: 100%; margin-left: 0; text-align: left; }

/* 中轴圆点 */
.alt-node {
  position: absolute;
  top: 14px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #fff;
  border: 3px solid var(--accent);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.9);
  z-index: 2;
}
.alt-left .alt-node { right: 0; transform: translateX(8.5px); }
.alt-right .alt-node { left: 0; transform: translateX(-8.5px); }
.alt-timeline.single .alt-node { left: 0; right: auto; transform: translateX(-8.5px); }

/* 头像 */
.alt-avatar { position: absolute; top: 4px; }
.alt-left .alt-avatar { right: 26px; }
.alt-right .alt-avatar { left: 26px; }
.alt-timeline.single .alt-avatar { left: 26px; right: auto; }

/* 气泡 */
.alt-bubble {
  position: relative;
  display: inline-block;
  width: var(--bubble-w);
  max-width: 100%;
  padding: 16px 18px;
  border-radius: 12px;
  text-align: left;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 6px 20px rgba(15, 40, 40, 0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-sizing: border-box;
}
.alt-bubble.clickable { cursor: pointer; }
.alt-bubble:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(15, 40, 40, 0.12); }

.alt-left .alt-bubble.has-avatar { margin-right: 90px; }
.alt-right .alt-bubble.has-avatar { margin-left: 90px; }
.alt-left .alt-bubble:not(.has-avatar) { margin-right: 36px; }
.alt-right .alt-bubble:not(.has-avatar) { margin-left: 36px; }
.alt-timeline.single .alt-bubble.has-avatar { margin-left: 90px; margin-right: 0; }
.alt-timeline.single .alt-bubble:not(.has-avatar) { margin-left: 36px; margin-right: 0; }

/* 尖角 */
.alt-tail { position: absolute; top: 18px; border-style: solid; }
.alt-left .alt-tail { right: -10px; border-width: 12px 0 6px 10px; }
.alt-right .alt-tail { left: -10px; border-width: 12px 10px 6px 0; }
.alt-timeline.single .alt-tail { left: -10px; right: auto; border-width: 12px 10px 6px 0; }

@keyframes altIn {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 窄屏统一收成单侧（与 single 同一套规则） */
@media screen and (max-width: 760px) {
  .alt-timeline::before { left: 0; transform: none; }
  .alt-content { width: 100% !important; margin-left: 0 !important; text-align: left !important; }
  .alt-node { left: 0 !important; right: auto !important; transform: translateX(-8.5px) !important; }
  .alt-avatar { left: 22px !important; right: auto !important; }
  .alt-bubble.has-avatar { margin-left: 78px !important; margin-right: 0 !important; }
  .alt-bubble:not(.has-avatar) { margin-left: 26px !important; margin-right: 0 !important; }
  .alt-tail { left: -10px !important; right: auto !important; border-width: 12px 10px 6px 0 !important; }
}

@media (prefers-reduced-motion: reduce) {
  .alt-li { animation: none; }
  .alt-bubble:hover { transform: none; }
}
</style>
