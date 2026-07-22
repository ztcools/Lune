<template>
  <div class="three-stage" ref="containerRef" @mouseenter="onMouseEnter" @mouseleave="onMouseLeave" @mousemove="onMouseMove">
    <div class="speech-bubble" :class="{ visible: showBubble }">
      <span>大小姐驾到，统统闪开！</span>
    </div>
    <canvas ref="canvasRef" class="three-canvas"></canvas>
    <div v-if="isMobile" class="mobile-fallback">
      <div class="fallback-char">🌸</div>
      <p>春日治愈时光</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as THREE from 'three'

const containerRef = ref(null)
const canvasRef = ref(null)
const showBubble = ref(true)
const isMobile = ref(false)

// Three.js objects
let scene, camera, renderer, clock
let character, headGroup, bodyGroup
let spotlights = []
let cherryBlossoms, confettiParticles
let stagePlatform, grassGround
let animationId
let mouseX = 0, mouseY = 0
let targetMouseX = 0, targetMouseY = 0
let isHovering = false
let confettiTimer = null
let introProgress = 0
let isIntro = true

// Cherry blossom data
const petalData = []
const PETAL_COUNT = 300
const CONFETTI_COUNT = 80

function createCherryBlossomMaterial() {
  const canvas = document.createElement('canvas')
  canvas.width = 16
  canvas.height = 16
  const ctx = canvas.getContext('2d')
  const gradient = ctx.createRadialGradient(8, 8, 1, 8, 8, 8)
  gradient.addColorStop(0, '#ffb7c5')
  gradient.addColorStop(0.5, '#ff9eb5')
  gradient.addColorStop(1, 'rgba(255,158,181,0)')
  ctx.fillStyle = gradient
  ctx.beginPath()
  // Draw petal shape
  ctx.moveTo(8, 2)
  ctx.quadraticCurveTo(2, 4, 2, 8)
  ctx.quadraticCurveTo(2, 12, 8, 14)
  ctx.quadraticCurveTo(14, 12, 14, 8)
  ctx.quadraticCurveTo(14, 4, 8, 2)
  ctx.fill()
  return new THREE.CanvasTexture(canvas)
}

function createConfettiMaterial(color) {
  const canvas = document.createElement('canvas')
  canvas.width = 8
  canvas.height = 8
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = color
  ctx.beginPath()
  // Tiny star shape
  ctx.arc(4, 4, 3.5, 0, Math.PI * 2)
  ctx.fill()
  return new THREE.CanvasTexture(canvas)
}

function createScene() {
  const container = containerRef.value
  if (!container) return

  const width = container.clientWidth
  const height = container.clientHeight || 600

  // Scene
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xe8f4fd)
  scene.fog = new THREE.Fog(0xe8f4fd, 8, 30)

  // Camera
  camera = new THREE.PerspectiveCamera(45, width / height, 0.5, 50)
  camera.position.set(0, 2.5, 7)
  camera.lookAt(0, 0.5, 0)

  // Renderer
  renderer = new THREE.WebGLRenderer({ canvas: canvasRef.value, antialias: true, alpha: true })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2

  clock = new THREE.Clock()

  // Ambient light
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)

  // Directional sun light
  const sunLight = new THREE.DirectionalLight(0xfff5e6, 1.8)
  sunLight.position.set(5, 10, 2)
  sunLight.castShadow = true
  sunLight.shadow.mapSize.width = 1024
  sunLight.shadow.mapSize.height = 1024
  sunLight.shadow.camera.near = 0.5
  sunLight.shadow.camera.far = 30
  sunLight.shadow.camera.left = -8
  sunLight.shadow.camera.right = 8
  sunLight.shadow.camera.top = 8
  sunLight.shadow.camera.bottom = -8
  sunLight.shadow.bias = -0.0001
  scene.add(sunLight)

  // Spotlight 1 - top left (pink)
  const spot1 = new THREE.SpotLight(0xffb6c1, 300, 15, Math.PI / 6, 0.3, 0.5)
  spot1.position.set(-3, 6, 2)
  spot1.castShadow = true
  spot1.shadow.mapSize.width = 512
  spot1.shadow.mapSize.height = 512
  scene.add(spot1)
  spotlights.push({ light: spot1, baseIntensity: 300, phase: 0 })

  // Spotlight 2 - top right (gold)
  const spot2 = new THREE.SpotLight(0xffd700, 250, 15, Math.PI / 5, 0.3, 0.5)
  spot2.position.set(3, 6, 2)
  spot2.castShadow = true
  spot2.shadow.mapSize.width = 512
  spot2.shadow.mapSize.height = 512
  scene.add(spot2)
  spotlights.push({ light: spot2, baseIntensity: 250, phase: Math.PI * 2 / 3 })

  // Spotlight 3 - top center (soft lavender)
  const spot3 = new THREE.SpotLight(0xe6e6fa, 350, 12, Math.PI / 4, 0.2, 0.4)
  spot3.position.set(0, 7, 0.5)
  spot3.castShadow = true
  spot3.shadow.mapSize.width = 512
  spot3.shadow.mapSize.height = 512
  scene.add(spot3)
  spotlights.push({ light: spot3, baseIntensity: 350, phase: Math.PI * 4 / 3 })

  // Grass ground
  const groundGeom = new THREE.CircleGeometry(5, 48)
  const groundMat = new THREE.MeshStandardMaterial({ color: 0x90c695, roughness: 0.8, metalness: 0.05 })
  grassGround = new THREE.Mesh(groundGeom, groundMat)
  grassGround.rotation.x = -Math.PI / 2
  grassGround.position.y = -2.2
  grassGround.receiveShadow = true
  scene.add(grassGround)

  // Decorative flowers on grass
  createGrassFlowers()

  // Round stage platform
  const stageGeom = new THREE.CylinderGeometry(1.0, 1.15, 0.25, 48)
  const stageMat = new THREE.MeshStandardMaterial({
    color: 0xffffff,
    roughness: 0.3,
    metalness: 0.1,
    transparent: true,
    opacity: 0.7
  })
  stagePlatform = new THREE.Mesh(stageGeom, stageMat)
  stagePlatform.position.y = -1.55
  stagePlatform.castShadow = true
  stagePlatform.receiveShadow = true
  scene.add(stagePlatform)

  // Stage ring decoration
  const ringGeom = new THREE.TorusGeometry(1.05, 0.04, 16, 64)
  const ringMat = new THREE.MeshStandardMaterial({ color: 0xffd700, roughness: 0.2, metalness: 0.7, emissive: 0xffd700, emissiveIntensity: 0.3 })
  const ring = new THREE.Mesh(ringGeom, ringMat)
  ring.rotation.x = Math.PI / 2
  ring.position.y = -1.42
  ring.receiveShadow = true
  scene.add(ring)

  // Inner stage glow
  const innerGlowGeom = new THREE.CylinderGeometry(0.9, 0.95, 0.05, 48)
  const innerGlowMat = new THREE.MeshStandardMaterial({
    color: 0xffb6c1,
    roughness: 0.2,
    metalness: 0.1,
    emissive: 0xffb6c1,
    emissiveIntensity: 0.4,
    transparent: true,
    opacity: 0.6
  })
  const innerGlow = new THREE.Mesh(innerGlowGeom, innerGlowMat)
  innerGlow.position.y = -1.40
  scene.add(innerGlow)

  // Build character
  createCharacter()

  // Cherry blossom particles
  createCherryBlossoms()

  // Confetti particles (hidden initially)
  createConfetti()

  // Start animation loop
  animate()
}

function createGrassFlowers() {
  const flowerColors = [0xffb6c1, 0xffd700, 0xff69b4, 0xfff0f5, 0xffa07a, 0x98fb98]
  for (let i = 0; i < 20; i++) {
    const angle = Math.random() * Math.PI * 2
    const radius = 1.8 + Math.random() * 2.5
    const x = Math.cos(angle) * radius
    const z = Math.sin(angle) * radius
    const flowerGroup = new THREE.Group()

    // Stem
    const stemGeom = new THREE.CylinderGeometry(0.02, 0.03, 0.3 + Math.random() * 0.3, 6)
    const stemMat = new THREE.MeshStandardMaterial({ color: 0x5dae5a, roughness: 0.7 })
    const stem = new THREE.Mesh(stemGeom, stemMat)
    stem.position.y = 0.15
    flowerGroup.add(stem)

    // Petal
    const petalGeom = new THREE.SphereGeometry(0.08, 8, 6)
    const petalMat = new THREE.MeshStandardMaterial({ color: flowerColors[Math.floor(Math.random() * flowerColors.length)], roughness: 0.3 })
    const petal = new THREE.Mesh(petalGeom, petalMat)
    petal.position.y = 0.3
    flowerGroup.add(petal)

    flowerGroup.position.set(x, -2.08, z)
    flowerGroup.castShadow = true
    flowerGroup.userData = { angle, radius, phase: Math.random() * Math.PI * 2 }
    scene.add(flowerGroup)
  }
}

function createCharacter() {
  character = new THREE.Group()
  bodyGroup = new THREE.Group()
  headGroup = new THREE.Group()

  // Body - round cute shape
  const bodyGeom = new THREE.SphereGeometry(0.45, 32, 32)
  // Scale to make body slightly chubby
  bodyGeom.scale(1, 0.85, 0.85)
  const bodyMat = new THREE.MeshStandardMaterial({ color: 0xfff5e6, roughness: 0.4, metalness: 0.05 })
  const body = new THREE.Mesh(bodyGeom, bodyMat)
  body.position.y = 0.05
  body.castShadow = true
  bodyGroup.add(body)

  // Dress/skirt (cone)
  const dressGeom = new THREE.CylinderGeometry(0.3, 0.5, 0.55, 32)
  const dressMat = new THREE.MeshStandardMaterial({ color: 0xffb6c1, roughness: 0.3, metalness: 0.1 })
  const dress = new THREE.Mesh(dressGeom, dressMat)
  dress.position.y = -0.45
  dress.castShadow = true
  bodyGroup.add(dress)

  // Dress ribbon
  const ribbonGeom = new THREE.TorusGeometry(0.35, 0.04, 8, 32)
  const ribbonMat = new THREE.MeshStandardMaterial({ color: 0xff69b4, roughness: 0.3, metalness: 0.2 })
  const ribbon = new THREE.Mesh(ribbonGeom, ribbonMat)
  ribbon.rotation.x = Math.PI / 2
  ribbon.position.y = -0.2
  bodyGroup.add(ribbon)

  // Arms
  for (let side = -1; side <= 1; side += 2) {
    const armGroup = new THREE.Group()
    armGroup.position.set(side * 0.45, 0.1, 0)

    const upperArmGeom = new THREE.CapsuleGeometry(0.08, 0.3, 8, 12)
    const armMat = new THREE.MeshStandardMaterial({ color: 0xfff5e6, roughness: 0.4, metalness: 0.05 })
    const upperArm = new THREE.Mesh(upperArmGeom, armMat)
    upperArm.castShadow = true
    armGroup.add(upperArm)

    // Little hand
    const handGeom = new THREE.SphereGeometry(0.07, 12, 12)
    const handMat = new THREE.MeshStandardMaterial({ color: 0xfff5e6, roughness: 0.4, metalness: 0.05 })
    const hand = new THREE.Mesh(handGeom, handMat)
    hand.position.y = -0.22
    armGroup.add(hand)

    armGroup.rotation.z = side * 0.3
    armGroup.name = side === -1 ? 'leftArm' : 'rightArm'
    bodyGroup.add(armGroup)
  }

  // Legs
  for (let side = -1; side <= 1; side += 2) {
    const legGeom = new THREE.CapsuleGeometry(0.1, 0.35, 8, 12)
    const legMat = new THREE.MeshStandardMaterial({ color: 0xfff5e6, roughness: 0.4, metalness: 0.05 })
    const leg = new THREE.Mesh(legGeom, legMat)
    leg.position.set(side * 0.15, -0.8, 0)
    leg.castShadow = true
    bodyGroup.add(leg)

    // Shoe
    const shoeGeom = new THREE.SphereGeometry(0.12, 12, 10)
    shoeGeom.scale(1, 0.5, 1.2)
    const shoeMat = new THREE.MeshStandardMaterial({ color: 0xff69b4, roughness: 0.3, metalness: 0.15 })
    const shoe = new THREE.Mesh(shoeGeom, shoeMat)
    shoe.position.set(side * 0.15, -1.0, -0.04)
    bodyGroup.add(shoe)
  }

  bodyGroup.position.y = -0.4
  character.add(bodyGroup)

  // Head
  const headGeom = new THREE.SphereGeometry(0.38, 32, 32)
  // Slightly wider head
  headGeom.scale(1, 0.92, 0.95)
  const headMat = new THREE.MeshStandardMaterial({ color: 0xfff5e6, roughness: 0.35, metalness: 0.03 })
  const head = new THREE.Mesh(headGeom, headMat)
  head.position.y = 0.05
  head.castShadow = true
  headGroup.add(head)

  // Eyes
  for (let side = -1; side <= 1; side += 2) {
    const eyeGeom = new THREE.SphereGeometry(0.07, 16, 16)
    const eyeMat = new THREE.MeshStandardMaterial({ color: 0x2c1810, roughness: 0.2, metalness: 0.1 })
    const eye = new THREE.Mesh(eyeGeom, eyeMat)
    eye.position.set(side * 0.13, 0.1, 0.32)
    headGroup.add(eye)

    // Eye highlight
    const highlightGeom = new THREE.SphereGeometry(0.03, 8, 8)
    const highlightMat = new THREE.MeshBasicMaterial({ color: 0xffffff })
    const highlight = new THREE.Mesh(highlightGeom, highlightMat)
    highlight.position.set(side * 0.13 + 0.02, 0.13, 0.38)
    headGroup.add(highlight)

    // Blush
    const blushGeom = new THREE.SphereGeometry(0.06, 12, 12)
    blushGeom.scale(1, 0.6, 0.3)
    const blushMat = new THREE.MeshStandardMaterial({ color: 0xffb6c1, roughness: 0.2, transparent: true, opacity: 0.6 })
    const blush = new THREE.Mesh(blushGeom, blushMat)
    blush.position.set(side * 0.2, -0.02, 0.3)
    headGroup.add(blush)
  }

  // Tiny mouth
  const mouthGeom = new THREE.TorusGeometry(0.05, 0.015, 8, 8, Math.PI)
  const mouthMat = new THREE.MeshStandardMaterial({ color: 0xe57373, roughness: 0.2 })
  const mouth = new THREE.Mesh(mouthGeom, mouthMat)
  mouth.position.set(0, -0.05, 0.33)
  mouth.rotation.z = Math.PI
  headGroup.add(mouth)

  // Cute hair/bangs
  for (let i = -3; i <= 3; i++) {
    const hairGeom = new THREE.SphereGeometry(0.07, 12, 12)
    hairGeom.scale(0.8, 0.6, 0.5)
    const hairMat = new THREE.MeshStandardMaterial({ color: 0x5c3317, roughness: 0.5, metalness: 0.05 })
    const hair = new THREE.Mesh(hairGeom, hairMat)
    hair.position.set(i * 0.1, 0.33, 0.2)
    headGroup.add(hair)
  }

  // Side hair
  for (const side of [-1, 1]) {
    for (let y = 0; y < 2; y++) {
      const sideHairGeom = new THREE.SphereGeometry(0.06, 10, 10)
      sideHairGeom.scale(0.7, 0.7, 0.5)
      const sideHair = new THREE.Mesh(sideHairGeom, new THREE.MeshStandardMaterial({ color: 0x5c3317, roughness: 0.5 }))
      sideHair.position.set(side * 0.28, 0.25 - y * 0.1, 0.1)
      headGroup.add(sideHair)
    }
  }

  // Cute bow on head
  const bowGroup = new THREE.Group()
  for (const s of [-1, 1]) {
    const bowLoopGeom = new THREE.SphereGeometry(0.12, 12, 8)
    bowLoopGeom.scale(1.2, 0.6, 0.3)
    const bowLoop = new THREE.Mesh(bowLoopGeom, new THREE.MeshStandardMaterial({ color: 0xff69b4, roughness: 0.25, metalness: 0.1 }))
    bowLoop.position.set(s * 0.14, 0.08, 0)
    bowLoop.rotation.z = s * 0.5
    bowGroup.add(bowLoop)
  }
  const bowCenterGeom = new THREE.SphereGeometry(0.05, 10, 10)
  const bowCenter = new THREE.Mesh(bowCenterGeom, new THREE.MeshStandardMaterial({ color: 0xff1493, roughness: 0.2, metalness: 0.2 }))
  bowGroup.add(bowCenter)
  bowGroup.position.set(0, 0.42, 0.05)
  headGroup.add(bowGroup)

  headGroup.position.y = 0.65
  character.add(headGroup)

  // Initial scale for intro animation
  character.scale.set(0.3, 0.3, 0.3)
  character.position.set(0, -1.3, 0)
  scene.add(character)
}

function createCherryBlossoms() {
  const texture = createCherryBlossomMaterial()
  const spriteMaterial = new THREE.SpriteMaterial({
    map: texture,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    transparent: true,
    opacity: 0.8
  })

  cherryBlossoms = new THREE.Group()

  for (let i = 0; i < PETAL_COUNT; i++) {
    const sprite = new THREE.Sprite(spriteMaterial.clone())
    const scale = 0.08 + Math.random() * 0.12
    sprite.scale.set(scale, scale, 1)
    sprite.position.set(
      (Math.random() - 0.5) * 8,
      Math.random() * 8 - 2,
      (Math.random() - 0.5) * 5
    )
    sprite.userData = {
      velocity: 0.3 + Math.random() * 0.8,
      driftX: (Math.random() - 0.5) * 0.3,
      driftZ: (Math.random() - 0.5) * 0.2,
      rotationSpeed: (Math.random() - 0.5) * 3,
      swayPhase: Math.random() * Math.PI * 2,
      swayAmp: 0.2 + Math.random() * 0.5,
      startY: sprite.position.y
    }
    cherryBlossoms.add(sprite)
  }

  scene.add(cherryBlossoms)
}

function createConfetti() {
  confettiParticles = new THREE.Group()
  const colors = ['#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#ff6bb5', '#ffb347', '#87ceeb', '#ff9ff3']

  for (let i = 0; i < CONFETTI_COUNT; i++) {
    const texture = createConfettiMaterial(colors[Math.floor(Math.random() * colors.length)])
    const spriteMat = new THREE.SpriteMaterial({
      map: texture,
      blending: THREE.AdditiveBlending,
      depthWrite: false,
      transparent: true,
      opacity: 0
    })
    const sprite = new THREE.Sprite(spriteMat)
    sprite.scale.set(0.08, 0.08, 1)
    sprite.position.set(0, 1.8, 0)
    sprite.userData = {
      velocity: new THREE.Vector3(
        (Math.random() - 0.5) * 3,
        2 + Math.random() * 5,
        (Math.random() - 0.5) * 3
      ),
      gravity: 2 + Math.random() * 2,
      life: 0,
      maxLife: 1.5 + Math.random() * 2,
      spinSpeed: (Math.random() - 0.5) * 10
    }
    confettiParticles.add(sprite)
  }

  scene.add(confettiParticles)
}

function triggerConfetti() {
  confettiParticles.children.forEach(sprite => {
    const ud = sprite.userData
    ud.life = 0
    ud.velocity.set(
      (Math.random() - 0.5) * 3,
      2 + Math.random() * 5,
      (Math.random() - 0.5) * 3
    )
    sprite.position.set(0, 1.8, 0)
    sprite.material.opacity = 1
  })
}

function animate() {
  animationId = requestAnimationFrame(animate)

  const dt = Math.min(clock.getDelta(), 0.1)
  const time = performance.now() * 0.001

  // Intro animation
  if (isIntro) {
    introProgress += dt * 0.6
    if (introProgress >= 1) {
      introProgress = 1
      isIntro = false
    }
    const ease = 1 - Math.pow(1 - introProgress, 3) // easeOutCubic
    character.scale.setScalar(0.3 + ease * 0.7)
  }

  // Mouse smooth follow
  mouseX += (targetMouseX - mouseX) * 3 * dt
  mouseY += (targetMouseY - mouseY) * 3 * dt

  // Character idle animation
  const sway = Math.sin(time * 1.5) * 0.06
  const bounce = Math.abs(Math.sin(time * 2.0)) * 0.04
  character.rotation.y = mouseX * 0.8 + sway
  character.position.y = -1.3 + bounce * (isHovering ? 2.5 : 1)

  // Head tracking
  if (headGroup) {
    headGroup.rotation.y = mouseX * 1.0
    headGroup.rotation.x = -mouseY * 0.5
    const happyBounce = isHovering ? Math.abs(Math.sin(time * 4)) * 0.08 : 0
    headGroup.position.y = 0.65 + bounce + happyBounce
  }

  // Arm swaying
  if (isHovering) {
    const arms = bodyGroup.children.filter(c => c.name === 'leftArm' || c.name === 'rightArm')
    arms.forEach(arm => {
      const side = arm.name === 'leftArm' ? -1 : 1
      arm.rotation.z = side * (0.3 + Math.sin(time * 5) * 0.6)
      arm.rotation.x = Math.cos(time * 5) * 0.3
    })
  }

  // Spotlight pulsing
  spotlights.forEach(s => {
    const intensity = s.baseIntensity * (0.7 + 0.3 * Math.sin(time * 0.8 + s.phase) * 0.5 + 0.15 * Math.sin(time * 1.7 + s.phase))
    s.light.intensity = intensity
  })

  // Stage ring glow pulse
  const ringPulse = 0.3 + Math.sin(time * 1.2) * 0.2
  scene.children.forEach(child => {
    if (child.material && child.material.emissiveIntensity !== undefined && child.material.color && child.material.color.getHex() === 0xffd700) {
      child.material.emissiveIntensity = ringPulse
    }
  })

  // Cherry blossom animation
  if (cherryBlossoms && !isMobile.value) {
    cherryBlossoms.children.forEach(petal => {
      const ud = petal.userData
      petal.position.y -= ud.velocity * dt
      petal.position.x += Math.sin(time * 0.7 + ud.swayPhase) * ud.swayAmp * dt
      petal.position.z += ud.driftZ * dt
      petal.material.rotation += ud.rotationSpeed * dt

      if (petal.position.y < -3) {
        petal.position.y = 6
        petal.position.x = (Math.random() - 0.5) * 8
      }
    })
  }

  // Confetti animation
  if (confettiParticles) {
    confettiParticles.children.forEach(sprite => {
      const ud = sprite.userData
      if (ud.life < ud.maxLife) {
        ud.life += dt
        sprite.position.x += ud.velocity.x * dt
        sprite.position.y += ud.velocity.y * dt
        sprite.position.z += ud.velocity.z * dt
        ud.velocity.y -= ud.gravity * dt
        sprite.material.rotation += ud.spinSpeed * dt
        const progress = ud.life / ud.maxLife
        sprite.material.opacity = progress < 0.7 ? 1 : 1 - (progress - 0.7) / 0.3
        sprite.scale.multiplyScalar(0.998)
      } else if (sprite.material.opacity > 0) {
        sprite.material.opacity = Math.max(0, sprite.material.opacity - dt * 2)
      }
    })
  }

  // Speech bubble visibility
  showBubble.value = !isHovering || Math.sin(time * 4) > -0.3

  renderer.render(scene, camera)
}

function onMouseEnter() {
  isHovering = true
  triggerConfetti()
  if (confettiTimer) clearTimeout(confettiTimer)
  confettiTimer = setTimeout(() => {
    if (isHovering) triggerConfetti()
  }, 4000)
}

function onMouseLeave() {
  isHovering = false
  targetMouseX = 0
  targetMouseY = 0
  if (confettiTimer) clearTimeout(confettiTimer)
}

function onMouseMove(e) {
  const rect = containerRef.value?.getBoundingClientRect()
  if (!rect) return
  const x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  const y = -((e.clientY - rect.top) / rect.height) * 2 + 1
  targetMouseX = Math.max(-0.6, Math.min(0.6, x * 0.8))
  targetMouseY = Math.max(-0.3, Math.min(0.3, y * 0.4))
}

function onResize() {
  const container = containerRef.value
  if (!container || !renderer || !camera) return
  const width = container.clientWidth
  const height = container.clientHeight || 600
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)

  isMobile.value = width < 700
  // Reduce petals on mobile
  if (cherryBlossoms && isMobile.value) {
    cherryBlossoms.children.forEach((p, i) => {
      if (i > 40) p.visible = false
    })
  }
}

function cleanup() {
  if (animationId) cancelAnimationFrame(animationId)
  if (confettiTimer) clearTimeout(confettiTimer)
  if (renderer) renderer.dispose()
  if (scene) {
    scene.traverse(obj => {
      if (obj.geometry) obj.geometry.dispose()
      if (obj.material) {
        if (obj.material.map) obj.material.map.dispose()
        obj.material.dispose()
      }
    })
  }
}

onMounted(async () => {
  await nextTick()
  const container = containerRef.value
  if (container) {
    isMobile.value = container.clientWidth < 700
  }
  createScene()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  cleanup()
})
</script>

<style scoped>
.three-stage {
  width: 100%;
  height: 100%;
  min-height: 620px;
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  background: linear-gradient(180deg, #c9e8ff 0%, #e8f4fd 40%, #d4f0d4 80%, #b8e0b8 100%);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), inset 0 0 60px rgba(255, 255, 255, 0.3);
}

.three-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.speech-bubble {
  position: absolute;
  top: 12%;
  right: 15%;
  background: rgba(255, 255, 255, 0.92);
  color: #333;
  padding: 10px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  z-index: 10;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  pointer-events: none;
  white-space: nowrap;
  animation: bubble-float 3s ease-in-out infinite;
  opacity: 0;
  transition: opacity 0.4s;
}

.speech-bubble::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-top: 12px solid rgba(255, 255, 255, 0.92);
}

.speech-bubble.visible {
  opacity: 1;
}

.mobile-fallback {
  display: none;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 5;
}

.fallback-char {
  font-size: 60px;
  animation: bubble-float 2s ease-in-out infinite;
}

.mobile-fallback p {
  margin-top: 12px;
  color: #666;
  font-size: 14px;
}

@keyframes bubble-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@media (max-width: 700px) {
  .three-stage {
    min-height: 200px;
    max-height: 300px;
  }
  .mobile-fallback {
    display: block;
  }
  .three-canvas {
    opacity: 0.4;
  }
  .speech-bubble {
    font-size: 11px;
    padding: 6px 12px;
  }
}
</style>
