<template>
  <div ref="containerRef" :class="['pixel-snow-container', className]"></div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import {
  Color,
  Mesh,
  OrthographicCamera,
  PlaneGeometry,
  Scene,
  ShaderMaterial,
  Vector2,
  Vector3,
  WebGLRenderer
} from 'three'

const props = defineProps({
  className: { type: String, default: '' },
  color: { type: String, default: '#ffffff' },
  flakeSize: { type: Number, default: 0.01 },
  minFlakeSize: { type: Number, default: 1.25 },
  pixelResolution: { type: Number, default: 200 },
  speed: { type: Number, default: 1.25 },
  depthFade: { type: Number, default: 8 },
  farPlane: { type: Number, default: 20 },
  brightness: { type: Number, default: 1 },
  gamma: { type: Number, default: 0.4545 },
  density: { type: Number, default: 0.3 },
  variant: { type: String, default: 'square' },
  direction: { type: Number, default: 125 }
})

const containerRef = ref(null)
let animationId = 0
let renderer = null
let material = null
let geometry = null
let resizeTimeout = null

const variantValue = computed(() => {
  return props.variant === 'round' ? 1.0 : props.variant === 'snowflake' ? 2.0 : 0.0
})

const colorVector = computed(() => {
  const c = new Color(props.color)
  return new Vector3(c.r, c.g, c.b)
})

// Shaders
const vertexShader = `
void main() {
  gl_Position = vec4(position, 1.0);
}`

const fragmentShader = `
precision mediump float;

uniform float uTime;
uniform vec2 uResolution;
uniform float uFlakeSize;
uniform float uMinFlakeSize;
uniform float uPixelResolution;
uniform float uSpeed;
uniform float uDepthFade;
uniform float uFarPlane;
uniform vec3 uColor;
uniform float uBrightness;
uniform float uGamma;
uniform float uDensity;
uniform float uVariant;
uniform float uDirection;

#define PI 3.14159265
#define PI_OVER_6 0.5235988
#define PI_OVER_3 1.0471976
#define INV_SQRT3 0.57735027
#define M1 1597334677U
#define M2 3812015801U
#define M3 3299493293U
#define F0 2.3283064e-10

#define hash(n) (n * (n ^ (n >> 15)))
#define coord3(p) (uvec3(p).x * M1 ^ uvec3(p).y * M2 ^ uvec3(p).z * M3)

const vec3 camK = vec3(0.57735027, 0.57735027, 0.57735027);
const vec3 camI = vec3(0.70710678, 0.0, -0.70710678);
const vec3 camJ = vec3(-0.40824829, 0.81649658, -0.40824829);

const vec2 b1d = vec2(0.574, 0.819);

vec3 hash3(uint n) {
  uvec3 hashed = hash(n) * uvec3(1U, 511U, 262143U);
  return vec3(hashed) * F0;
}

float snowflakeDist(vec2 p) {
  float r = length(p);
  float a = atan(p.y, p.x);
  a = abs(mod(a + PI_OVER_6, PI_OVER_3) - PI_OVER_6);
  vec2 q = r * vec2(cos(a), sin(a));
  float dMain = max(abs(q.y), max(-q.x, q.x - 1.0));
  float b1t = clamp(dot(q - vec2(0.4, 0.0), b1d), 0.0, 0.4);
  float dB1 = length(q - vec2(0.4, 0.0) - b1t * b1d);
  float b2t = clamp(dot(q - vec2(0.7, 0.0), b1d), 0.0, 0.25);
  float dB2 = length(q - vec2(0.7, 0.0) - b2t * b1d);
  return min(dMain, min(dB1, dB2)) * 10.0;
}

void main() {
  float invPixelRes = 1.0 / uPixelResolution;
  float pixelSize = max(1.0, floor(0.5 + uResolution.x * invPixelRes));
  float invPixelSize = 1.0 / pixelSize;

  vec2 fragCoord = floor(gl_FragCoord.xy * invPixelSize);
  vec2 res = uResolution * invPixelSize;
  float invResX = 1.0 / res.x;

  vec3 ray = normalize(vec3((fragCoord - res * 0.5) * invResX, 1.0));
  ray = ray.x * camI + ray.y * camJ + ray.z * camK;

  float timeSpeed = uTime * uSpeed;
  float windX = cos(uDirection) * 0.4;
  float windY = sin(uDirection) * 0.4;
  vec3 camPos = (windX * camI + windY * camJ + 0.1 * camK) * timeSpeed;
  vec3 pos = camPos;

  vec3 absRay = max(abs(ray), vec3(0.001));
  vec3 strides = 1.0 / absRay;
  vec3 raySign = step(ray, vec3(0.0));
  vec3 phase = fract(pos) * strides;
  phase = mix(strides - phase, phase, raySign);

  float rayDotCamK = dot(ray, camK);
  float invRayDotCamK = 1.0 / rayDotCamK;
  float invDepthFade = 1.0 / uDepthFade;
  float halfInvResX = 0.5 * invResX;
  vec3 timeAnim = timeSpeed * 0.1 * vec3(7.0, 8.0, 5.0);

  float t = 0.0;
  for (int i = 0; i < 128; i++) {
    if (t >= uFarPlane) break;

    vec3 fpos = floor(pos);
    uint cellCoord = coord3(fpos);
    float cellHash = hash3(cellCoord).x;

    if (cellHash < uDensity) {
      vec3 h = hash3(cellCoord);

      vec3 sinArg1 = fpos.yzx * 0.073;
      vec3 sinArg2 = fpos.zxy * 0.27;
      vec3 flakePos = 0.5 - 0.5 * cos(4.0 * sin(sinArg1) + 4.0 * sin(sinArg2) + 2.0 * h + timeAnim);
      flakePos = flakePos * 0.8 + 0.1 + fpos;

      float toIntersection = dot(flakePos - pos, camK) * invRayDotCamK;

      if (toIntersection > 0.0) {
        vec3 testPos = pos + ray * toIntersection - flakePos;
        float testX = dot(testPos, camI);
        float testY = dot(testPos, camJ);
        vec2 testUV = abs(vec2(testX, testY));

        float depth = dot(flakePos - camPos, camK);
        float flakeSize = max(uFlakeSize, uMinFlakeSize * depth * halfInvResX);

        float dist;
        if (uVariant < 0.5) {
          dist = max(testUV.x, testUV.y);
        } else if (uVariant < 1.5) {
          dist = length(testUV);
        } else {
          float invFlakeSize = 1.0 / flakeSize;
          dist = snowflakeDist(vec2(testX, testY) * invFlakeSize) * flakeSize;
        }

        if (dist < flakeSize) {
          float flakeSizeRatio = uFlakeSize / flakeSize;
          float intensity = exp2(-(t + toIntersection) * invDepthFade) *
                           min(1.0, flakeSizeRatio * flakeSizeRatio) * uBrightness;
          gl_FragColor = vec4(uColor * pow(vec3(intensity), vec3(uGamma)), 1.0);
          return;
        }
      }
    }

    float nextStep = min(min(phase.x, phase.y), phase.z);
    vec3 sel = step(phase, vec3(nextStep));
    phase = phase - nextStep + strides * sel;
    t += nextStep;
    pos = mix(pos + ray * nextStep, floor(pos + ray * nextStep + 0.5), sel);
  }

  gl_FragColor = vec4(0.0);
}`

function updateSize() {
  const container = containerRef.value
  if (!container || !renderer || !material) return
  const w = container.offsetWidth || window.innerWidth
  const h = container.offsetHeight || window.innerHeight
  if (w > 0 && h > 0) {
    renderer.setSize(w, h)
    material.uniforms.uResolution.value.set(w, h)
  }
}

function handleResize() {
  if (resizeTimeout) clearTimeout(resizeTimeout)
  resizeTimeout = setTimeout(updateSize, 100)
}

function initScene() {
  const container = containerRef.value
  if (!container) return

  const scene = new Scene()
  const camera = new OrthographicCamera(-1, 1, 1, -1, 0, 1)
  renderer = new WebGLRenderer({
    antialias: false,
    alpha: true,
    premultipliedAlpha: false,
    powerPreference: 'high-performance',
    stencil: false,
    depth: false
  })

  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  container.appendChild(renderer.domElement)

  // Defer size measurement until layout is complete
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      updateSize()
    })
  })

  material = new ShaderMaterial({
    vertexShader,
    fragmentShader,
    uniforms: {
      uTime: { value: 0 },
      uResolution: { value: new Vector2(window.innerWidth, window.innerHeight) },
      uFlakeSize: { value: props.flakeSize },
      uMinFlakeSize: { value: props.minFlakeSize },
      uPixelResolution: { value: props.pixelResolution },
      uSpeed: { value: props.speed },
      uDepthFade: { value: props.depthFade },
      uFarPlane: { value: props.farPlane },
      uColor: { value: colorVector.value.clone() },
      uBrightness: { value: props.brightness },
      uGamma: { value: props.gamma },
      uDensity: { value: props.density },
      uVariant: { value: variantValue.value },
      uDirection: { value: (props.direction * Math.PI) / 180 }
    },
    transparent: true
  })

  geometry = new PlaneGeometry(2, 2)
  scene.add(new Mesh(geometry, material))

  window.addEventListener('resize', handleResize)

  const startTime = performance.now()
  const animate = () => {
    animationId = requestAnimationFrame(animate)
    if (material && renderer) {
      material.uniforms.uTime.value = (performance.now() - startTime) * 0.001
      renderer.render(scene, camera)
    }
  }
  animate()
}

function cleanup() {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', handleResize)
  if (resizeTimeout) clearTimeout(resizeTimeout)
  if (renderer) {
    const container = containerRef.value
    if (container && container.contains(renderer.domElement)) {
      container.removeChild(renderer.domElement)
    }
    renderer.dispose()
    renderer.forceContextLoss()
    renderer = null
  }
  if (geometry) { geometry.dispose(); geometry = null }
  if (material) { material.dispose(); material = null }
}

// Watch all props, update uniforms
watch(
  () => [props.flakeSize, props.minFlakeSize, props.pixelResolution, props.speed,
         props.depthFade, props.farPlane, props.brightness, props.gamma, props.density,
         variantValue.value, props.direction, colorVector.value],
  () => {
    if (!material) return
    material.uniforms.uFlakeSize.value = props.flakeSize
    material.uniforms.uMinFlakeSize.value = props.minFlakeSize
    material.uniforms.uPixelResolution.value = props.pixelResolution
    material.uniforms.uSpeed.value = props.speed
    material.uniforms.uDepthFade.value = props.depthFade
    material.uniforms.uFarPlane.value = props.farPlane
    material.uniforms.uBrightness.value = props.brightness
    material.uniforms.uGamma.value = props.gamma
    material.uniforms.uDensity.value = props.density
    material.uniforms.uVariant.value = variantValue.value
    material.uniforms.uDirection.value = (props.direction * Math.PI) / 180
    material.uniforms.uColor.value.copy(colorVector.value)
  }
)

// ResizeObserver for reliable container sizing
let resizeObs = null
onMounted(async () => {
  await nextTick()
  initScene()

  if (containerRef.value) {
    resizeObs = new ResizeObserver(() => {
      updateSize()
    })
    resizeObs.observe(containerRef.value)
  }
})

onUnmounted(() => {
  if (resizeObs) resizeObs.disconnect()
  cleanup()
})
</script>

<style scoped>
@import './PixelSnow.css';
</style>
