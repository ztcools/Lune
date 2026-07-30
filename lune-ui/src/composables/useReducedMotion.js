import { ref, getCurrentScope, onScopeDispose } from 'vue'

/**
 * 「减少动态效果」偏好探测。
 *
 * 系统开了这个开关的用户，多数是前庭功能障碍 / 眩晕敏感 / 晕动症，全屏飘落的
 * 花瓣和 WebGL 雪会直接让人不适；也有一部分是老设备用户靠它换性能。
 * CSS 侧用 `@media (prefers-reduced-motion: reduce)` 就够了，但 canvas / WebGL
 * 这类效果得在 JS 里判断 —— 光把画布 display:none 没用，requestAnimationFrame
 * 循环和 three.js 那 500KB 依赖照样在跑、在下。
 */
const QUERY = '(prefers-reduced-motion: reduce)'

const supported = () => typeof window !== 'undefined' && typeof window.matchMedia === 'function'

/** 同步读一次。适合「要不要初始化动画 / 要不要异步加载动画依赖」这种一次性判断。 */
export function prefersReducedMotion() {
  return supported() && window.matchMedia(QUERY).matches
}

/** 响应式版本：用户在系统设置里中途改动也会跟着变。 */
export function useReducedMotion() {
  const reduced = ref(prefersReducedMotion())
  if (!supported()) return reduced

  const mq = window.matchMedia(QUERY)
  const onChange = (e) => { reduced.value = e.matches }
  mq.addEventListener('change', onChange)
  // 组件外调用（比如 store 里）不会有 scope，此时不注册清理也不会泄漏到组件上
  if (getCurrentScope()) onScopeDispose(() => mq.removeEventListener('change', onChange))
  return reduced
}
