<template>
  <div class="af" :class="{ 'is-error': !!error }">
    <div class="af-box">
      <span class="af-icon" aria-hidden="true"><slot name="icon" /></span>

      <input
        :id="fieldId"
        ref="inputEl"
        class="af-input"
        :type="revealed ? 'text' : type"
        :value="modelValue"
        :name="name || autocomplete || undefined"
        :autocomplete="autocomplete"
        :maxlength="maxlength"
        :disabled="disabled"
        :inputmode="inputmode"
        :aria-invalid="!!error"
        :aria-describedby="error ? fieldId + '-err' : undefined"
        placeholder=" "
        @input="$emit('update:modelValue', $event.target.value)"
        @keyup.enter="$emit('enter')"
      />
      <!-- placeholder=" " 让 :placeholder-shown 能当「空值」判据用，
           浮动标签因此纯 CSS 实现，不需要在 JS 里跟踪 focus/value -->
      <label class="af-label" :for="fieldId">{{ label }}</label>

      <button
        v-if="type === 'password'"
        class="af-eye"
        type="button"
        :aria-label="revealed ? '隐藏密码' : '显示密码'"
        :aria-pressed="revealed"
        @click="revealed = !revealed"
      >
        <svg v-if="!revealed" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M2 12s3.6-6.5 10-6.5S22 12 22 12s-3.6 6.5-10 6.5S2 12 2 12Z" stroke-linecap="round" />
          <circle cx="12" cy="12" r="2.6" />
        </svg>
        <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M4 4l16 16" stroke-linecap="round" />
          <path d="M9.7 5.8A9.9 9.9 0 0 1 12 5.5c6.4 0 10 6.5 10 6.5a17 17 0 0 1-2.7 3.5M6.4 7.9A16.6 16.6 0 0 0 2 12s3.6 6.5 10 6.5c1 0 2-.2 2.8-.4" stroke-linecap="round" />
        </svg>
      </button>

      <slot name="suffix" />
    </div>

    <transition name="af-err">
      <p v-if="error" :id="fieldId + '-err'" class="af-msg" role="alert">{{ error }}</p>
    </transition>
  </div>
</template>

<script setup>
/**
 * 登录/注册通用输入框：浮动标签 + 图标 + 密码可见性切换 + 行内错误。
 *
 * 抽出来是为了让前台弹窗（LoginCard）与后台登录页（admin/Login）共用同一套
 * 表单观感 —— 这两处此前各自复制了一份几乎相同的 input CSS，改一边忘一边。
 */
import { ref } from 'vue'

// 用模块内计数器而不是 Vue 3.5 的 useId()：package.json 声明的是 ^3.4.0，
// 装到 3.4 的机器上 useId 并不存在。
let seq = 0

defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, required: true },
  type: { type: String, default: 'text' },
  name: { type: String, default: '' },
  autocomplete: { type: String, default: 'off' },
  inputmode: { type: String, default: undefined },
  maxlength: { type: [String, Number], default: undefined },
  error: { type: String, default: '' },
  disabled: { type: Boolean, default: false }
})
defineEmits(['update:modelValue', 'enter'])

const fieldId = `af-${++seq}`
const revealed = ref(false)
const inputEl = ref(null)

defineExpose({ focus: () => inputEl.value?.focus() })
</script>

<style scoped>
.af { position: relative; }

.af-box {
  position: relative;
  display: flex; align-items: center; gap: 10px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.72);
  border: 1.5px solid rgba(67, 160, 71, 0.16);
  border-radius: 14px;
  transition: border-color 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
}
.af-box:focus-within {
  background: #fff;
  border-color: rgba(67, 160, 71, 0.55);
  box-shadow: 0 0 0 4px rgba(102, 187, 106, 0.14);
}
.af.is-error .af-box {
  border-color: rgba(229, 115, 115, 0.6);
  box-shadow: 0 0 0 4px rgba(229, 115, 115, 0.1);
}

.af-icon {
  display: flex; align-items: center;
  color: #7ba97e; flex-shrink: 0;
  transition: color 0.25s ease;
}
.af-box:focus-within .af-icon { color: var(--nature-green-dark); }

.af-input {
  flex: 1; min-width: 0;
  border: none; outline: none; background: transparent;
  /* 上多下少：浮动后的标签要落在这块 padding 里 */
  padding: 21px 0 8px;
  font-family: inherit; font-size: 15px; font-weight: 500;
  color: var(--articleFontColor);
}
.af-input:disabled { color: var(--greyFont); cursor: not-allowed; }

.af-label {
  position: absolute; left: 42px; top: 50%;
  transform: translateY(-50%);
  font-size: 15px; color: #9bb69e;
  pointer-events: none;
  transition: top 0.2s ease, font-size 0.2s ease, color 0.2s ease, transform 0.2s ease;
}
/* 有值 或 获得焦点 → 标签上浮 */
.af-input:focus ~ .af-label,
.af-input:not(:placeholder-shown) ~ .af-label {
  top: 9px; transform: none;
  font-size: 11.5px; font-weight: 600; letter-spacing: 0.4px;
  color: var(--nature-green-dark);
}
.af.is-error .af-input:not(:placeholder-shown) ~ .af-label { color: #d9736f; }

.af-eye {
  flex-shrink: 0; display: flex; align-items: center;
  border: none; background: transparent; padding: 4px;
  color: #9bb69e; cursor: pointer; border-radius: 8px;
  transition: color 0.2s ease, background 0.2s ease;
}
.af-eye:hover { color: var(--nature-green-dark); background: rgba(102, 187, 106, 0.1); }

.af-msg {
  margin: 5px 2px 0; font-size: 12px; line-height: 1.5;
  color: #d9736f;
}
.af-err-enter-active, .af-err-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.af-err-enter-from, .af-err-leave-to { opacity: 0; transform: translateY(-3px); }

/* Chrome 自动填充的亮黄底会把玻璃卡片打穿 */
.af-input:-webkit-autofill,
.af-input:-webkit-autofill:hover,
.af-input:-webkit-autofill:focus {
  -webkit-text-fill-color: var(--articleFontColor);
  -webkit-box-shadow: 0 0 0 1000px #fff inset;
  caret-color: var(--articleFontColor);
}
</style>
