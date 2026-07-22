<template>
  <div class="family-page">
    <!-- Hero Banner -->
    <div class="bg-wrap">
      <el-image
        class="family-banner-image"
        lazy
        :src="family.bgCover || '/assets/背景4.jpg'"
        fit="cover"
      >
        <template #error><div class="image-slot"></div></template>
      </el-image>
      <div class="banner-overlay"></div>

      <!-- Center card with avatars -->
      <div class="family-wrap transformCenter">
        <div>
          <el-avatar class="family-avatar" :src="family.manCover"></el-avatar>
          <div class="family-name">{{ family.manName || '他' }}</div>
        </div>
        <div>
          <img
            class="family-heart"
            src="data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 24 24%22%3E%3Cpath fill=%22%23ff4b2b%22 d=%22M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z%22/%3E%3C/svg%3E"
            alt="heart"
            @error="(e) => { e.target.style.display = 'none' }"
          />
        </div>
        <div>
          <el-avatar class="family-avatar" :src="family.womanCover"></el-avatar>
          <div class="family-name">{{ family.womanName || '她' }}</div>
        </div>
      </div>

      <!-- Wave effects -->
      <div id="bannerWave1"></div>
      <div id="bannerWave2"></div>
    </div>

    <!-- Content -->
    <div class="family-container">
      <!-- Timer & Countdown -->
      <div class="myCenter family-timer-section">
        <div>
          <div class="timer-title">
            这是我们一起走过的
          </div>
          <div class="timer-display">
            第
            <span class="timer-digit">{{ timing.year }}</span>年
            <span class="timer-digit">{{ timing.month }}</span>月
            <span class="timer-digit">{{ timing.day }}</span>日
            <span class="timer-digit">{{ timing.hour }}</span>时
            <span class="timer-digit">{{ timing.minute }}</span>分
            <span class="timer-digit">{{ timing.second }}</span>秒
          </div>
          <!-- Countdown -->
          <div
            v-if="family.countdownTitle || family.countdownTime"
            class="countdown-display"
          >
            {{ family.countdownTitle }}: {{ countdownText }}
          </div>
        </div>
      </div>

      <!-- Confession Wall Button -->
      <div class="confession-btn-wrap">
        <div class="confession-btn shadow-box-mini" @click="switchCard(4)">
          <span class="confession-btn-title">
            {{ activeCard === 4 ? '回到主人家' : '开往表白墙' }}
          </span>
          <span class="confession-btn-car">
            <svg viewBox="0 0 1024 1024" width="40" height="40">
              <path
                d="M399.502 655.103c0 7.902-6.665 14.311-14.88 14.311H72.188c-8.215 0-14.875-6.407-14.875-14.311v-28.634c0-7.913 6.66-14.315 14.875-14.315h312.435c8.217 0 14.88 6.402 14.88 14.315l-0.001 28.634zM968.167 655.103c0 7.902-6.664 14.311-14.882 14.311H640.851c-8.216 0-14.877-6.407-14.877-14.311v-28.634c0-7.913 6.661-14.315 14.877-14.315h312.436c8.218 0 14.882 6.402 14.882 14.315l-0.002 28.634z"
                fill="#EA0606"
              ></path>
              <path
                d="M968.097 624.008c0 11.563-17.723 20.937-39.583 20.937H97.263c-21.858 0-39.579-9.372-39.579-20.937v-41.876c0-11.562 17.72-20.935 39.579-20.935h831.25c21.86 0 39.583 9.373 39.583 20.935v41.876zM855.003 526.553h-12c0-161.793-151.025-293.421-336.66-293.421-185.633 0-336.656 131.628-336.656 293.421h-12c0-41.334 9.261-81.425 27.527-119.161 17.612-36.384 42.807-69.046 74.886-97.079 65.813-57.509 153.264-89.181 246.243-89.181 92.981 0 180.434 31.672 246.247 89.181 32.079 28.032 57.274 60.693 74.887 97.079 18.264 37.734 27.526 77.826 27.526 119.161z"
                fill="#EA0606"
              ></path>
            </svg>
          </span>
        </div>
      </div>

      <!-- Card Navigation (not shown when on confession wall) -->
      <div v-show="activeCard !== 4" class="card-nav">
        <div class="card-nav-inner">
          <!-- 点点滴滴 -->
          <div class="nav-card shadow-box-mini" @click="switchCard(1)">
            <div>
              <el-avatar :size="100" src="/assets/头像1.jpg" />
            </div>
            <div class="nav-card-right">
              <div class="nav-card-title">点点滴滴</div>
              <div class="nav-card-desc">☀️今朝有酒今朝醉</div>
            </div>
          </div>

          <!-- 时光相册 -->
          <div class="nav-card shadow-box-mini" @click="switchCard(2)">
            <div>
              <el-avatar :size="100" src="/assets/背景.jpg" />
            </div>
            <div class="nav-card-right">
              <div class="nav-card-title">时光相册</div>
              <div class="nav-card-desc">📸记录美好瞬间</div>
            </div>
          </div>

          <!-- 祝福板 -->
          <div class="nav-card shadow-box-mini" @click="switchCard(3)">
            <div>
              <el-avatar :size="100" src="/assets/头像2.jpg" />
            </div>
            <div class="nav-card-right">
              <div class="nav-card-title">祝福板</div>
              <div class="nav-card-desc">📋写下对我们的祝福</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Card Content Area -->
      <div class="card-content-area">
        <!-- 点点滴滴 (TreeHole style) -->
        <div v-show="activeCard === 1 && treeHoleList.length > 0" class="treehole-section">
          <div
            v-for="(item, index) in treeHoleList"
            :key="item.id"
            class="treehole-item"
          >
            <div class="treehole-item-inner">
              <div class="th-avatar-col">
                <el-avatar shape="square" :size="36" :src="item.avatar || appStore.webInfo.avatar" />
              </div>
              <div class="th-body-col">
                <div class="th-user-row">
                  <span class="th-username">{{ item.username }}</span>
                </div>
                <div class="th-content" v-html="item.content"></div>
                <div class="th-footer">
                  <span class="th-time">{{ formatRelative(item.createTime) }}</span>
                  <span
                    v-if="userStore.isLoggedIn && userStore.user?.userId === item.userId"
                    class="th-delete"
                    @click="deleteTreeHole(item.id)"
                  >
                    <svg viewBox="0 0 1024 1024" width="16" height="16" style="vertical-align: -4px;">
                      <path
                        d="M921.1392 155.392h-270.592v-48.2816c0-22.7328-18.432-41.1648-41.1648-41.1648H426.3424a41.1648 41.1648 0 0 0-41.1648 41.1648v48.2816H110.6432c-14.1312 0-25.6 11.4688-25.6 25.6s11.4688 25.6 25.6 25.6h810.496c14.1312 0 25.6-11.4688 25.6-25.6s-11.4688-25.6-25.6-25.6zM170.8032 260.0448v592.8448c0 50.8928 41.2672 92.16 92.16 92.16h500.6848c50.8928 0 92.16-41.2672 92.16-92.16V260.0448H170.8032z m249.1392 462.7968c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z m243.1488 0c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z"
                        fill="#FF623E"
                      ></path>
                    </svg>
                  </span>
                </div>
              </div>
            </div>
            <hr class="th-divider" />
          </div>
          <div class="pagination-wrap">
            <div
              v-if="treeHoleTotal > treeHoleList.length"
              class="pagination-btn"
              @click="loadMoreTreeHoles"
            >
              下一页
            </div>
            <div v-else class="pagination-end">~~到底啦~~</div>
          </div>
        </div>

        <!-- 时光相册 (Records/Photos) -->
        <div v-show="activeCard === 2" class="photo-section">
          <div class="photo-grid">
            <div v-for="item in photoList" :key="item.id" class="photo-card shadow-box">
              <el-image
                v-if="item.cover"
                :src="item.cover"
                fit="cover"
                class="photo-cover"
                :preview-src-list="[item.cover]"
              />
              <div class="photo-body">
                <h4>{{ item.title }}</h4>
                <p v-if="item.content">{{ item.content.substring(0, 60) }}</p>
                <span class="photo-date">📅 {{ formatDate(item.createTime) }}</span>
              </div>
            </div>
          </div>
          <div v-if="photoList.length === 0" style="text-align:center;padding:40px;">
            <el-empty description="暂无相册" />
          </div>
          <div class="pagination-wrap">
            <div
              v-if="photoTotal > photoList.length"
              class="pagination-btn"
              @click="loadMorePhotos"
            >
              下一页
            </div>
            <div v-else class="pagination-end">~~到底啦~~</div>
          </div>
        </div>

        <!-- 祝福板 (Comments) -->
        <div v-show="activeCard === 3" class="blessing-section">
          <div class="comment-input-area">
            <textarea
              v-model="blessingText"
              placeholder="写下祝福吧..."
              maxlength="500"
              class="blessing-textarea"
              rows="3"
            ></textarea>
            <div class="blessing-action-row">
              <span class="char-count">{{ blessingText.length }}/500</span>
              <el-button type="primary" size="small" @click="submitBlessing" :loading="blessingPosting">
                发送祝福
              </el-button>
            </div>
          </div>
          <div class="blessing-list">
            <div v-for="c in blessingList" :key="c.id" class="blessing-row">
              <el-avatar :size="32" :src="c.avatar" shape="square" />
              <div class="blessing-body">
                <span class="blessing-user">{{ c.username }}</span>
                <span class="blessing-content">{{ c.content }}</span>
                <span class="blessing-time">{{ formatRelative(c.createTime) }}</span>
              </div>
            </div>
            <div v-if="blessingList.length === 0" style="text-align:center;padding:40px;">
              <el-empty description="还没有祝福，快来写下第一条吧" />
            </div>
          </div>
        </div>

        <!-- 表白墙 (Family Cards Grid) -->
        <div v-show="activeCard === 4" class="confession-wall">
          <div class="family-grid" v-if="randomFamilies.length > 0">
            <div
              v-for="(item, index) in randomFamilies"
              :key="index"
              class="family-grid-card"
              @click="selectFamily(item)"
              :style="{ background: `url(${item.bgCover}) center center / cover no-repeat` }"
            >
              <div class="fgc-overlay"></div>
              <div class="fgc-content">
                <div>
                  <el-avatar class="fgc-avatar" :src="item.manCover" />
                  <div class="fgc-name">{{ item.manName }}</div>
                </div>
                <div>
                  <img
                    class="fgc-heart"
                    src="data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 24 24%22%3E%3Cpath fill=%22%23ff4b2b%22 d=%22M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z%22/%3E%3C/svg%3E"
                    alt="heart"
                    @error="(e) => { e.target.style.display = 'none' }"
                  />
                </div>
                <div>
                  <el-avatar class="fgc-avatar" :src="item.womanCover" />
                  <div class="fgc-name">{{ item.womanName }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="confession-bottom">
            <div
              v-if="randomFamilies.length > 10"
              class="confession-action-btn"
              style="background-color: var(--maxLightRed)"
              @click="fetchRandomFamilies"
            >
              <span class="action-btn-text">换一换</span>
              <svg viewBox="0 0 1024 1024" width="30" height="30"><path
                d="M952 511.5L567.4 183v164.3s-251.3 30.3-369.1 131.2C76.4 582.9 73 840 73 840l20.4-2s72.9-164 258.5-162.3c99.9 0.9 171.3 0.8 215.6 0.6V840L952 511.5z"
                fill="#FF9D3A"
              ></path></svg>
            </div>
            <div
              class="confession-action-btn"
              style="background-color: var(--lightGreen)"
              @click="openFamilyForm"
            >
              <span class="action-btn-text">申请入住</span>
              <svg viewBox="0 0 1024 1024" width="30" height="30"><path
                d="M731.0848 143.7696c-125.0816-54.528-270.7456 2.6624-325.2736 127.7952l-3.7376 8.6016-8.6016-3.7376c-125.0816-54.5792-270.6944 2.6112-325.2224 127.744-54.528 125.0816 2.6624 270.7456 127.7952 325.2736l368.0256 160.4096c51.712 22.528 111.872-1.1264 134.4-52.7872l0.0512-0.1024c0-0.0512 0.0512-0.1024 0.0512-0.1536l160.3072-367.7696c54.528-125.1328-2.6624-270.7456-127.7952-325.2736z"
                fill="#F85F69"
              ></path></svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Family Form Dialog -->
    <el-dialog
      v-model="familyDialogVisible"
      title="入住表白墙"
      width="45%"
      :close-on-click-modal="false"
      destroy-on-close
      center
    >
      <div class="form-main">
        <div class="form-friend-body">
          <div class="form-field">
            <label class="form-label">背景封面</label>
            <el-input v-model="userFamily.bgCover" placeholder="输入背景图片URL" maxlength="120" />
          </div>
          <div class="form-field">
            <label class="form-label">男生头像</label>
            <el-input v-model="userFamily.manCover" placeholder="输入头像URL" maxlength="120" />
          </div>
          <div class="form-field">
            <label class="form-label">女生头像</label>
            <el-input v-model="userFamily.womanCover" placeholder="输入头像URL" maxlength="120" />
          </div>
          <div class="form-field">
            <label class="form-label">男生昵称</label>
            <el-input v-model="userFamily.manName" placeholder="输入昵称" maxlength="10" />
          </div>
          <div class="form-field">
            <label class="form-label">女生昵称</label>
            <el-input v-model="userFamily.womanName" placeholder="输入昵称" maxlength="10" />
          </div>
          <div class="form-field">
            <label class="form-label">计时时间</label>
            <el-date-picker
              v-model="userFamily.timing"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="datetime"
              placeholder="选择计时开始时间"
              style="width:100%"
            />
          </div>
          <div class="form-field">
            <label class="form-label">倒计时标题</label>
            <el-input v-model="userFamily.countdownTitle" placeholder="例如: 生日倒计时" maxlength="20" />
          </div>
          <div class="form-field">
            <label class="form-label">倒计时时间</label>
            <el-date-picker
              v-model="userFamily.countdownTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="datetime"
              placeholder="选择倒计时日期"
              style="width:100%"
            />
          </div>
          <div class="form-field">
            <label class="form-label">告白信</label>
            <el-input
              v-model="userFamily.familyInfo"
              type="textarea"
              :rows="4"
              maxlength="1000"
              show-word-limit
              placeholder="写下想说的话..."
            />
          </div>
          <div style="text-align:center;margin-top:20px;">
            <el-button type="primary" @click="submitFamily" :loading="familyPosting">提交</el-button>
          </div>
          <p style="font-size:12px;text-align:center;color:#999;margin-top:10px;">欢迎入住表白墙</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { familyApi, treeHoleApi, commentApi, recordApi } from '../../api/modules'
import { useUserStore } from '../../stores/user'
import { useAppStore } from '../../stores/app'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const appStore = useAppStore()

// --- Family state ---
const family = ref({
  bgCover: '',
  manCover: '',
  womanCover: '',
  manName: '',
  womanName: '',
  countdownTitle: '',
  countdownTime: '',
  timing: ''
})
const adminFamily = ref({})

// --- Timer state ---
const timing = ref({ year: 0, month: 0, day: 0, hour: 0, minute: 0, second: 0 })
const countdownText = ref('')
let timerInterval = null

// --- Card state ---
const activeCard = ref(2)

// --- TreeHole state (点点滴滴) ---
const treeHoleList = ref([])
const treeHolePage = ref(1)
const treeHoleTotal = ref(0)

// --- Photo state (时光相册) ---
const photoList = ref([])
const photoPage = ref(1)
const photoTotal = ref(0)

// --- Blessing state (祝福板) ---
const blessingList = ref([])
const blessingText = ref('')
const blessingPosting = ref(false)

// --- Confession Wall state (表白墙) ---
const randomFamilies = ref([])

// --- Family form ---
const familyDialogVisible = ref(false)
const familyPosting = ref(false)
const userFamily = ref({
  bgCover: '',
  manCover: '',
  womanCover: '',
  manName: '',
  womanName: '',
  countdownTitle: '',
  countdownTime: '',
  timing: '',
  familyInfo: ''
})

// --- lifecycle ---
onMounted(async () => {
  await fetchAdminFamily()
  fetchPhotos()
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})

// --- API: Admin Family & Timer ---
async function fetchAdminFamily() {
  try {
    const data = await familyApi.list()
    if (data && (Array.isArray(data) ? data.length > 0 : data.id)) {
      const f = Array.isArray(data) ? data[0] : data
      family.value = f
      adminFamily.value = f
      startTimer()
    }
  } catch (e) { /* silent */ }
}

function startTimer() {
  if (timerInterval) clearInterval(timerInterval)
  timerInterval = setInterval(() => {
    computeTiming()
    computeCountdown()
  }, 1000)
  computeTiming()
  computeCountdown()
}

function computeTiming() {
  if (!family.value.timing) return
  const start = new Date(family.value.timing).getTime()
  const now = Date.now()
  if (now < start) return
  const diffMs = now - start
  const diffSec = Math.floor(diffMs / 1000)
  timing.value.second = diffSec % 60
  timing.value.minute = Math.floor(diffSec / 60) % 60
  timing.value.hour = Math.floor(diffSec / 3600) % 24
  const totalDays = Math.floor(diffSec / 86400)
  timing.value.day = totalDays
  timing.value.month = Math.floor(totalDays / 30)
  timing.value.year = Math.floor(totalDays / 365)
}

function computeCountdown() {
  if (!family.value.countdownTime) return
  const target = new Date(family.value.countdownTime).getTime()
  const now = Date.now()
  const diffMs = target - now
  if (diffMs <= 0) {
    countdownText.value = '已到来!'
    return
  }
  const d = Math.floor(diffMs / 86400000)
  const h = Math.floor((diffMs % 86400000) / 3600000)
  const m = Math.floor((diffMs % 3600000) / 60000)
  const s = Math.floor((diffMs % 60000) / 1000)
  countdownText.value = `${d}天${h}时${m}分${s}秒`
}

// --- Card switching ---
function switchCard(card) {
  if (card === 4 || activeCard.value !== card) {
    activeCard.value = card
  } else {
    activeCard.value = 1
    family.value = adminFamily.value
  }
  if (card === 1 && treeHoleList.value.length === 0) fetchTreeHoles()
  if (card === 2 && photoList.value.length === 0) fetchPhotos()
  if (card === 3 && blessingList.value.length === 0) fetchBlessings()
  if (card === 4 && randomFamilies.value.length === 0) fetchRandomFamilies()
}

function selectFamily(f) {
  family.value = f
  startTimer()
  activeCard.value = 1
}

// --- TreeHole ---
async function fetchTreeHoles(reset = false) {
  try {
    if (reset) { treeHolePage.value = 1; treeHoleList.value = [] }
    const data = await treeHoleApi.list({ page: treeHolePage.value, size: 10 })
    if (data && data.records) {
      data.records.forEach((c) => {
        if (c.content) {
          c.content = c.content.replace(/\n{2,}/g, '<div style="height:12px"></div>')
          c.content = c.content.replace(/\n/g, '<br/>')
        }
      })
      treeHoleList.value = treeHoleList.value.concat(data.records)
      treeHoleTotal.value = data.total
    }
  } catch (e) { /* silent */ }
}

function loadMoreTreeHoles() {
  treeHolePage.value++
  fetchTreeHoles()
}

async function deleteTreeHole(id) {
  if (!userStore.isLoggedIn) { ElMessage.error('请先登录！'); return }
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'success', center: true })
    await treeHoleApi.delete(id)
    ElMessage.success('删除成功!')
    treeHolePage.value = 1
    treeHoleList.value = []
    await fetchTreeHoles()
  } catch (e) { /* cancelled */ }
}

// --- Photos ---
async function fetchPhotos() {
  try {
    const data = await recordApi.list({ page: photoPage.value, size: 12 })
    if (data && data.records) {
      photoList.value = photoList.value.concat(data.records)
      photoTotal.value = data.total
    }
  } catch (e) { /* silent */ }
}

function loadMorePhotos() {
  photoPage.value++
  fetchPhotos()
}

// --- Blessings ---
async function fetchBlessings() {
  try {
    // Use comments with type 'love' for blessings
    const data = await commentApi.list({ type: 'love', page: 1, size: 50 })
    blessingList.value = data?.records || data || []
  } catch (e) { /* silent */ }
}

async function submitBlessing() {
  if (!blessingText.value.trim()) return
  if (!userStore.isLoggedIn) { ElMessage.error('请先登录！'); return }
  blessingPosting.value = true
  try {
    await commentApi.create({ content: blessingText.value, type: 'love' })
    ElMessage.success('祝福发送成功')
    blessingText.value = ''
    await fetchBlessings()
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    blessingPosting.value = false
  }
}

// --- Random Families ---
async function fetchRandomFamilies() {
  try {
    const data = await familyApi.listAll()
    if (data && (data.records || Array.isArray(data))) {
      randomFamilies.value = data.records || data
    }
  } catch (e) { /* silent */ }
}

// --- Family Form ---
function openFamilyForm() {
  if (!userStore.isLoggedIn) { ElMessage.error('请先登录！'); return }
  familyDialogVisible.value = true
}

async function submitFamily() {
  const uf = userFamily.value
  if (!uf.bgCover.trim()) { ElMessage.warning('你还没设置背景封面呢~'); return }
  if (!uf.manCover.trim()) { ElMessage.warning('你还没设置男生头像呢~'); return }
  if (!uf.womanCover.trim()) { ElMessage.warning('你还没设置女生头像呢~'); return }
  if (!uf.manName.trim()) { ElMessage.warning('你还没写男生昵称呢~'); return }
  if (!uf.womanName.trim()) { ElMessage.warning('你还没写女生昵称呢~'); return }
  if (!uf.timing) { ElMessage.warning('你还没设置计时时间呢~'); return }
  familyPosting.value = true
  try {
    await familyApi.create({
      bgCover: uf.bgCover,
      manCover: uf.manCover,
      womanCover: uf.womanCover,
      manName: uf.manName,
      womanName: uf.womanName,
      timing: uf.timing,
      countdownTitle: uf.countdownTitle,
      countdownTime: uf.countdownTime,
      content: uf.familyInfo
    })
    ElMessage.success('提交成功，待管理员审核！')
    userFamily.value = {
      bgCover: '', manCover: '', womanCover: '',
      manName: '', womanName: '',
      countdownTitle: '', countdownTime: '', timing: '', familyInfo: ''
    }
    familyDialogVisible.value = false
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    familyPosting.value = false
  }
}

// --- Helpers ---
function formatRelative(d) {
  if (!d) return ''
  const diff = Date.now() - new Date(d).getTime()
  const sec = Math.floor(diff / 1000)
  const min = Math.floor(sec / 60)
  const hrs = Math.floor(min / 60)
  const dys = Math.floor(hrs / 24)
  if (sec < 60) return '刚刚'
  if (min < 60) return `${min}分钟前`
  if (hrs < 24) return `${hrs}小时前`
  if (dys < 30) return `${dys}天前`
  if (dys < 365) return `${Math.floor(dys / 30)}个月前`
  return `${Math.floor(dys / 365)}年前`
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
/* ====== Banner ====== */
.bg-wrap {
  height: 55vh;
  position: relative;
  overflow: hidden;
}
.family-banner-image {
  position: absolute;
  inset: 0;
}
.family-banner-image::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: var(--miniMask);
  z-index: 1;
}
.banner-overlay {
  position: absolute;
  inset: 0;
  background: var(--miniMask);
  z-index: 1;
}

.family-wrap {
  width: 90%;
  max-width: 950px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3em;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 50px 70px 30px;
  z-index: 10;
}
.family-avatar {
  width: 180px !important;
  height: 180px !important;
  border: rgba(255, 255, 255, 0.2) 4px solid;
}
.family-name {
  margin-top: 15px;
  text-align: center;
  font-size: 25px;
  font-weight: 700;
  color: var(--white);
}
.family-heart {
  animation: imgScale 2s linear infinite;
  width: 120px;
  height: 120px;
}

/* Waves */
#bannerWave1 {
  height: 84px;
  background: var(--bannerWave1, linear-gradient(180deg, rgba(255,255,255,0.1), rgba(255,255,255,0.3)));
  position: absolute;
  width: 200%;
  bottom: 0;
  z-index: 10;
  animation: gradientBG 120s linear infinite;
}
#bannerWave2 {
  height: 100px;
  background: var(--bannerWave2, linear-gradient(180deg, rgba(255,255,255,0.2), rgba(255,255,255,0.5)));
  position: absolute;
  width: 400%;
  bottom: 0;
  z-index: 5;
  animation: gradientBG 120s linear infinite;
}

/* ====== Container ====== */
.family-container {
  background-image:
    linear-gradient(to right, rgba(37, 82, 110, 0.1) 1px, var(--background) 1px),
    linear-gradient(to bottom, rgba(37, 82, 110, 0.1) 1px, var(--background) 1px);
  background-size: 3rem 3rem;
  padding-bottom: 40px;
}

/* ====== Timer ====== */
.family-timer-section {
  max-width: 1200px;
  overflow: hidden;
  margin: 20px auto 0;
  user-select: none;
  flex-direction: column;
}
.timer-title {
  font-size: 2rem;
  font-weight: 600;
  letter-spacing: 0.2rem;
  line-height: 4rem;
  text-align: center;
  background-image: linear-gradient(270deg,
    #ff4500, #ffa500, #ffd700, #90ee90, #00ffff, #1e90ff, #9370db, #ff69b4, #ff4500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: jianBian 60s linear infinite;
  width: 3000px;
}
.timer-display {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  color: var(--fontColor);
}
.timer-digit {
  font-size: 4rem;
  font-weight: 700;
}
.countdown-display {
  text-align: center;
  font-size: 1.5rem;
  line-height: 4rem;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--fontColor);
}

/* ====== Confession Button ====== */
.confession-btn-wrap {
  padding: 0 20px;
}
.confession-btn {
  position: relative;
  overflow: hidden;
  height: 150px;
  color: var(--white);
  margin: 50px auto 15px;
  border-radius: 20px;
  max-width: 350px;
  cursor: pointer;
  transition: all 0.3s;
  background: var(--love, linear-gradient(135deg, #ff416c, #ff4b2b)) center center / cover no-repeat;
  user-select: none;
}
.confession-btn:hover {
  transform: translateY(-6px);
}
.confession-btn::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: var(--miniMask);
}
.confession-btn-title {
  position: absolute;
  line-height: 150px;
  margin-left: 20px;
  font-size: 25px;
  font-weight: 700;
  color: var(--white);
}
.confession-btn-car {
  position: absolute;
  margin-left: 220px;
  margin-top: 55px;
  animation: passing 4s linear infinite;
}

/* ====== Card Navigation ====== */
.card-nav {
  padding: 0 20px;
}
.card-nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
.nav-card {
  display: flex;
  padding: 25px;
  margin: 25px;
  border-radius: 20px;
  max-width: 400px;
  cursor: pointer;
  transition: all 0.3s;
  background: var(--background);
}
.nav-card:hover {
  transform: translateY(-6px);
}
.nav-card-right {
  margin-left: 20px;
}
.nav-card-title {
  font-size: 1.6rem;
  letter-spacing: 0.2rem;
  line-height: 3.5rem;
  font-weight: 700;
}
.nav-card-desc {
  font-size: 1.1rem;
  letter-spacing: 0.2rem;
  color: #777777;
}

/* ====== Card Content Area ====== */
.card-content-area {
  max-width: 1500px;
  margin: 20px auto 40px;
  padding: 0 20px;
}

/* TreeHole section */
.treehole-section {
  max-width: 1000px;
  margin: 0 auto;
}
.treehole-item-inner {
  display: flex;
}
.th-avatar-col {
  margin: 10px 15px 10px 10px;
}
.th-body-col {
  flex: 1;
}
.th-user-row {
  margin: 10px 0 15px;
}
.th-username {
  color: #607199;
  font-size: 16px;
  font-weight: 600;
}
.th-content {
  margin-right: 20px;
  letter-spacing: 1px;
  line-height: 1.4;
}
.th-footer {
  display: flex;
  gap: 10px;
  margin: 20px 0 10px;
}
.th-time {
  font-size: 14px;
  color: var(--greyFont);
}
.th-delete {
  cursor: pointer;
}
.th-divider {
  border: 1px solid #f5f5f5;
  margin: 10px auto;
}

/* Photo section */
.photo-section {
  max-width: 1170px;
  margin: 0 auto;
}
.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.photo-card {
  border-radius: 12px;
  overflow: hidden;
  background: var(--background);
  transition: all 0.3s;
}
.photo-card:hover {
  transform: translateY(-2px);
}
.photo-cover {
  width: 100%;
  height: 200px;
}
.photo-body {
  padding: 16px;
}
.photo-body h4 {
  margin-bottom: 8px;
  font-size: 16px;
}
.photo-body p {
  color: var(--greyFont);
  font-size: 13px;
  line-height: 1.5;
}
.photo-date {
  display: block;
  margin-top: 10px;
  color: var(--greyFont);
  font-size: 12px;
}

/* Blessing section */
.blessing-section {
  max-width: 1000px;
  margin: 0 auto;
}
.comment-input-area {
  margin-bottom: 24px;
}
.blessing-textarea {
  width: 100%;
  border: 2px solid var(--lightGreen);
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  resize: vertical;
  outline: none;
  background: var(--background);
  color: var(--fontColor);
  box-sizing: border-box;
}
.blessing-action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.blessing-list {
  margin-top: 20px;
}
.blessing-row {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}
.blessing-body {
  flex: 1;
}
.blessing-user {
  font-weight: 600;
  font-size: 13px;
  color: #607199;
  margin-right: 8px;
}
.blessing-content {
  font-size: 13px;
  color: var(--fontColor);
}
.blessing-time {
  display: block;
  font-size: 11px;
  color: var(--greyFont);
  margin-top: 2px;
}
.char-count {
  font-size: 12px;
  color: var(--greyFont);
}

/* Confession Wall */
.confession-wall {
  padding: 0 20px;
}
.family-grid {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  margin-bottom: 40px;
}
.family-grid-card {
  cursor: pointer;
  width: 350px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 15px 25px 5px;
  margin: 20px;
  transition: all 0.3s;
  user-select: none;
  position: relative;
  overflow: hidden;
}
.family-grid-card:hover {
  transform: translateY(-6px);
}
.fgc-overlay {
  position: absolute;
  inset: 0;
  background: var(--miniMask);
  border-radius: 20px;
}
.fgc-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-around;
  width: 100%;
}
.fgc-avatar {
  width: 90px !important;
  height: 90px !important;
  border: rgba(255, 255, 255, 0.2) 4px solid;
}
.fgc-name {
  margin-top: 15px;
  text-align: center;
  font-size: 20px;
  font-weight: 700;
  color: var(--white);
}
.fgc-heart {
  animation: imgScale 2s linear infinite;
  width: 60px;
  height: 60px;
}
.confession-bottom {
  display: flex;
  justify-content: space-around;
  margin: 0 0 40px;
}
.confession-action-btn {
  color: var(--white);
  border-radius: 3rem;
  width: 150px;
  text-align: center;
  height: 50px;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
.action-btn-text {
  line-height: 50px;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
.pagination-btn {
  padding: 13px 15px;
  border: 1px solid var(--lightGray);
  border-radius: 3rem;
  color: var(--greyFont);
  width: 100px;
  user-select: none;
  cursor: pointer;
  text-align: center;
}
.pagination-btn:hover {
  border: 1px solid var(--themeBackground);
  color: var(--themeBackground);
  box-shadow: 0 0 5px var(--themeBackground);
}
.pagination-end {
  user-select: none;
  color: var(--greyFont);
}

/* Form Dialog */
.form-main {
  border-radius: 12px;
  overflow: hidden;
}
.form-friend-body {
  background-color: #eeeeee;
  padding: 20px;
  border-radius: 12px;
}
.form-field {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  gap: 12px;
}
.form-label {
  width: 100px;
  text-align: right;
  font-size: 14px;
  flex-shrink: 0;
}

/* ====== Responsive ====== */
@media screen and (max-width: 1200px) {
  .card-nav-inner {
    flex-wrap: wrap;
  }
}
@media screen and (max-width: 800px) {
  .family-wrap {
    border-radius: 1.5em;
    padding: 40px 30px 10px;
  }
  .family-avatar {
    width: 120px !important;
    height: 120px !important;
  }
  .family-heart {
    width: 100px;
    height: 100px;
  }
  .timer-display {
    font-size: 1.4rem;
  }
  .timer-digit {
    font-size: 3rem;
  }
}
@media screen and (max-width: 600px) {
  .family-wrap {
    padding: 30px 20px 10px;
  }
  .family-avatar {
    width: 100px !important;
    height: 100px !important;
  }
  .family-heart {
    width: 80px;
    height: 80px;
  }
  .timer-display {
    font-size: 1rem;
  }
  .timer-digit {
    font-size: 1.8rem;
  }
  .countdown-display {
    font-size: 1.2rem;
  }
  .form-field {
    flex-direction: column;
    align-items: stretch;
  }
  .form-label {
    text-align: left;
    width: auto;
  }
}
</style>
