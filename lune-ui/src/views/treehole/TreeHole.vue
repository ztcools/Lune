<template>
  <div class="treehole-page">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="bg-image" :style="{ backgroundImage: `url(${bannerImage})` }"></div>
      <div class="bg-overlay"></div>
      <div class="hero-info">
        <h1>树洞</h1>
        <p>把秘密说给树洞听</p>
      </div>
    </div>

    <!-- TwoPoem - Decorative poem display -->
    <div class="my-animation-slide-top">
      <div class="poem-container" v-if="poemText">
        <div class="poem-image" :style="{ backgroundImage: `url(${randomCover})` }"></div>
        <div class="poem-wrap">
          <div><span>{{ poemSource }}</span></div>
          <p class="poem-line">{{ poemText }}</p>
          <p class="poem-author" v-if="poemAuthor">{{ poemAuthor }}</p>
        </div>
      </div>
    </div>

    <!-- Tree Hole Timeline -->
    <div class="timeline-wrapper my-animation-hideToShow">
      <div class="tree-hole-container">
        <ol class="tree-hole-list" v-if="treeHoleList.length > 0">
          <li
            class="tree-hole-li"
            v-for="(treeHole, index) in treeHoleList"
            :key="treeHole.id"
          >
            <div
              class="tree-hole-content"
              :class="mobile
                ? 'rightTreeHole'
                : index % 2 === 0
                  ? 'leftTreeHole'
                  : 'rightTreeHole'"
            >
              <el-avatar
                shape="square"
                class="avatar-img"
                :size="36"
                :src="userStore.user?.avatar || appStore.webInfo.avatar || defaultAvatar"
              />
              <div
                class="tree-hole-box"
                :style="{ background: colors[index % colors.length] }"
              >
                <div
                  class="box-tag"
                  :class="mobile || index % 2 !== 0 ? 'tag-left' : 'tag-right'"
                  :style="mobile || index % 2 !== 0
                    ? { 'border-color': 'transparent transparent transparent ' + colors[index % colors.length] }
                    : { 'border-color': 'transparent ' + colors[index % colors.length] + ' transparent transparent' }"
                ></div>
                <div class="my-content" v-html="treeHole.content"></div>
                <div class="tree-hole-footer">
                  <span>{{ formatDate(treeHole.createTime) }}</span>
                  <span
                    v-if="userStore.isLoggedIn && userStore.user?.userId === treeHole.userId"
                    class="tree-hole-delete"
                    @click="handleDelete(treeHole.id)"
                  >
                    <svg viewBox="0 0 1024 1024" width="18" height="18" style="vertical-align: -2px;">
                      <path
                        d="M921.1392 155.392h-270.592v-48.2816c0-22.7328-18.432-41.1648-41.1648-41.1648H426.3424a41.1648 41.1648 0 0 0-41.1648 41.1648v48.2816H110.6432c-14.1312 0-25.6 11.4688-25.6 25.6s11.4688 25.6 25.6 25.6h810.496c14.1312 0 25.6-11.4688 25.6-25.6s-11.4688-25.6-25.6-25.6zM170.8032 260.0448v592.8448c0 50.8928 41.2672 92.16 92.16 92.16h500.6848c50.8928 0 92.16-41.2672 92.16-92.16V260.0448H170.8032z m249.1392 462.7968c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z m243.1488 0c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z"
                        fill="#FF623E"
                      ></path>
                    </svg>
                  </span>
                </div>
              </div>
            </div>
          </li>
        </ol>
        <div v-else style="text-align:center; padding:80px 0;">
          <el-empty description="暂无树洞消息" />
        </div>

        <!-- Send Button -->
        <div class="tree-hole-go">
          <svg
            class="send-icon"
            @click="openDialog"
            viewBox="0 0 1024 1024"
            width="28"
            height="28"
          >
            <path
              d="M931.4 498.8L93.4 51.4c-15.4-7.6-34-3.2-44 10.2-4.8 6.4-6.8 14.2-6 22l70.6 334.8c1.6 7.6 6.2 14.2 13 18L301.8 524c6.6 3.8 10.8 10.8 10.8 18.4v233.6c0 24 21 44 46.6 41.4 13.6-1.4 26-8.8 34.2-19.6l96.2-124.2c5.8-7.6 14.8-12.6 24.8-12.6 2.8 0 5.6 0.4 8.4 1l272.8 72.2c15.4 4.2 28.4-2.6 33.8-13.8 3-5.8 3.6-12.4 2-18.6L940 541c-2.8-21.4-5.8-22.8-8.6-42.2z"
              fill="currentColor"
            ></path>
          </svg>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- Send Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="微言"
      width="40%"
      :close-on-click-modal="false"
      destroy-on-close
      center
      :before-close="handleDialogClose"
    >
      <div class="dialog-body">
        <div class="dialog-radio-wrap">
          <el-radio-group v-model="isPublic">
            <el-radio-button :value="true">公开</el-radio-button>
            <el-radio-button :value="false">私密</el-radio-button>
          </el-radio-group>
        </div>
        <div class="dialog-textarea-wrap">
          <textarea
            v-model="content"
            placeholder="说点什么吧..."
            maxlength="500"
            class="dialog-textarea"
            rows="5"
          ></textarea>
          <div class="dialog-actions">
            <span class="char-count">{{ content.length }}/500</span>
            <el-button type="primary" @click="submitWeiYan" :loading="posting">
              发布
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { treeHoleApi } from '../../api/modules'
import { useUserStore } from '../../stores/user'
import { useAppStore } from '../../stores/app'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const appStore = useAppStore()

// --- state ---
const treeHoleList = ref([])
const page = ref(1)
const pageSize = 10
const total = ref(0)
const content = ref('')
const posting = ref(false)
const dialogVisible = ref(false)
const isPublic = ref(true)
const mobile = ref(false)

// Poem / twoPoem state
const poemText = ref('')
const poemSource = ref('')
const poemAuthor = ref('')

// --- constants ---
const colors = [
  '#ffecd2', '#fcb69f', '#a1c4fd', '#c2e9fb', '#d4a5ff',
  '#fbc2eb', '#a6c1ee', '#fdcbf1', '#e6dee9', '#bae1ff'
]

const defaultAvatar = '/assets/头像1.jpg'
const bannerFallback = '/assets/背景5.jpg'

// --- computed-like ---
const bannerImage = ref(bannerFallback)
const randomCover = ref('')

// --- methods ---
function checkMobile() {
  mobile.value = window.innerWidth <= 600
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)

  // Set banner from config or fallback
  if (appStore.webInfo.backgroundImage) {
    bannerImage.value = appStore.webInfo.backgroundImage
  }

  // Set random cover
  try {
    const covers = JSON.parse(appStore.webInfo.randomCover || '[]')
    if (covers.length > 0) {
      randomCover.value = covers[Math.floor(Math.random() * covers.length)]
    }
  } catch (e) {
    randomCover.value = ''
  }

  fetchTreeHoles()
  fetchHitokoto()
})

// --- API calls ---
async function fetchTreeHoles() {
  try {
    const data = await treeHoleApi.list({ page: page.value, size: pageSize })
    if (data && data.records) {
      // Process content: replace double newlines with spacer, single newlines with <br/>
      data.records.forEach((c) => {
        if (c.content) {
          c.content = c.content.replace(/\n{2,}/g, '<div style="height:12px"></div>')
          c.content = c.content.replace(/\n/g, '<br/>')
        }
      })
      treeHoleList.value = data.records
      total.value = data.total
    }
  } catch (e) {
    // silent
  }
}

async function submitWeiYan() {
  if (!content.value.trim()) return
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录！')
    return
  }
  posting.value = true
  try {
    await treeHoleApi.create({ content: content.value, isPublic: isPublic.value })
    ElMessage.success('发布成功')
    content.value = ''
    dialogVisible.value = false
    page.value = 1
    await fetchTreeHoles()
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    posting.value = false
  }
}

async function handleDelete(id) {
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录！')
    return
  }
  try {
    await ElMessageBox.confirm('确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
      center: true
    })
    await treeHoleApi.delete(id)
    ElMessage.success('删除成功!')
    page.value = 1
    await fetchTreeHoles()
  } catch (e) {
    if (e !== 'cancel') {
      // cancelled confirm — do nothing
    }
  }
}

function handlePageChange(p) {
  page.value = p
  window.scrollTo({ top: 240, behavior: 'smooth' })
  fetchTreeHoles()
}

// --- dialog ---
function openDialog() {
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录！')
    return
  }
  dialogVisible.value = true
}

function handleDialogClose() {
  dialogVisible.value = false
}

// --- hitokoto (twoPoem) ---
async function fetchHitokoto() {
  try {
    const res = await fetch('https://v1.hitokoto.cn/')
    const data = await res.json()
    poemText.value = data.hitokoto
    poemSource.value = data.from || ''
    poemAuthor.value = data.from_who || ''
  } catch (e) {
    // fallback: show nothing or a static poem
    poemText.value = '人生如逆旅，我亦是行人'
    poemSource.value = '临江仙'
    poemAuthor.value = '苏轼'
  }
}

// --- helpers ---
function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
</script>

<style scoped>
/* ====== Hero Banner ====== */
.hero-banner {
  position: relative;
  height: 30vh;
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.bg-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
}
.bg-overlay {
  position: absolute;
  inset: 0;
  background: var(--mask);
}
.hero-info {
  position: relative;
  z-index: 2;
  text-align: center;
  color: var(--white);
}
.hero-info h1 {
  font-size: 36px;
  margin: 0 0 8px;
}
.hero-info p {
  font-size: 16px;
  opacity: 0.85;
}

/* ====== TwoPoem ====== */
.poem-container {
  padding: 90px 0 40px;
  position: relative;
  overflow: hidden;
  text-align: center;
}
.poem-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  margin-top: -50px;
  filter: brightness(0.5);
}
.poem-wrap {
  position: relative;
  border-radius: 10px;
  z-index: 10;
  text-align: center;
  letter-spacing: 4px;
  font-weight: 300;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.poem-wrap div span {
  padding: 5px 10px;
  color: var(--white);
  font-size: 2em;
  border-radius: 5px;
}
.poem-wrap p {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  color: var(--white);
}
.poem-wrap p.poem-line {
  margin: 40px auto;
  font-size: 1.5em;
  line-height: 1.8;
}
.poem-wrap p.poem-author {
  margin: 20px auto 40px;
  font-size: 1.1em;
}

/* ====== Timeline Wrapper ====== */
.timeline-wrapper {
  background: var(--background);
  padding: 20px;
}

/* ====== Tree Hole Container ====== */
.tree-hole-container {
  padding: 20px;
  margin: 0 auto;
}

.tree-hole-list {
  padding: 100px 0 20px;
  margin: 0;
  position: relative;
  list-style: none;
}

/* Center line */
.tree-hole-list::before {
  content: '';
  width: 4px;
  border-radius: 50%;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  background-color: var(--themeBackground);
}

/* Pulsing red circle at top */
.tree-hole-list::after {
  content: '';
  width: 12px;
  height: 12px;
  border: 4px solid var(--maxLightRed);
  border-radius: 50%;
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  background-color: var(--white);
  animation: weiYanShadowFlashing 1.5s linear infinite;
}

.tree-hole-li {
  margin: 5px auto;
}

.tree-hole-content {
  position: relative;
  width: 50%;
}

.leftTreeHole {
  text-align: right;
}

.rightTreeHole {
  margin-left: 50%;
}

/* Circle markers on center line for each item */
.tree-hole-content::before {
  content: '';
  width: 12px;
  height: 12px;
  border: 4px solid var(--blue);
  border-radius: 50%;
  position: absolute;
  top: 10px;
  background-color: var(--white);
}

.leftTreeHole::before {
  right: 0;
  transform: translateX(10px);
}

.rightTreeHole::before {
  left: 0;
  transform: translateX(-10px);
}

/* Avatar */
.avatar-img {
  position: absolute;
  top: 0;
  transition: all 0.3s ease-in-out;
}

.leftTreeHole .avatar-img {
  right: 25px;
}

.rightTreeHole .avatar-img {
  left: 25px;
}

/* Message Bubble */
.tree-hole-box {
  font-size: 16px;
  padding: 10px;
  width: 360px;
  border-radius: 5px;
  position: relative;
  letter-spacing: 0.1em;
  font-weight: 400;
  transition: all 0.3s ease-in-out;
  color: var(--black);
  text-align: left;
  display: inline-block;
}

.leftTreeHole .tree-hole-box {
  margin-right: 90px;
}

.rightTreeHole .tree-hole-box {
  margin-left: 90px;
}

.tree-hole-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 16px 3px var(--miniMask);
}

.avatar-img:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 16px 3px var(--miniMask);
}

/* Triangular Tail */
.box-tag {
  content: '';
  position: absolute;
  border-style: solid;
}

.leftTreeHole .box-tag {
  right: -10px;
  border-width: 15px 0 5px 10px;
}

.rightTreeHole .box-tag {
  left: -10px;
  border-width: 15px 10px 5px 0;
}

/* Content area */
.my-content {
  margin: 0 10px 10px;
  line-height: 30px;
}

/* Footer inside bubble */
.tree-hole-footer {
  color: var(--greyFont);
  padding: 10px 10px 0;
  border-top: 1px dashed var(--white);
  font-size: 14px;
  display: flex;
  justify-content: space-between;
}

.tree-hole-delete {
  font-size: 14px;
  cursor: pointer;
}

/* Send Button */
.tree-hole-go {
  color: var(--blue);
  font-weight: 700;
  font-size: 25px;
  margin: 20px auto;
  text-align: center;
}

.send-icon {
  cursor: pointer;
  transition: all 0.3s;
}

.send-icon:hover {
  animation: scale 1s linear infinite;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-bottom: 40px;
}

/* ====== Dialog ====== */
.dialog-body {
  padding: 10px 0;
}
.dialog-radio-wrap {
  text-align: center;
  padding-bottom: 20px;
}
.dialog-textarea {
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
.dialog-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.char-count {
  font-size: 12px;
  color: var(--greyFont);
}

/* ====== Responsive ====== */
@media screen and (max-width: 1000px) {
  .tree-hole-box {
    width: calc(100% - 90px);
  }
}

@media screen and (max-width: 600px) {
  .tree-hole-content {
    margin-bottom: 50px;
  }

  .tree-hole-list::after {
    left: 0;
  }

  .tree-hole-list::before {
    left: 0;
  }

  .tree-hole-content {
    width: 100%;
  }

  .rightTreeHole {
    margin-left: unset;
  }

  .tree-hole-content::before {
    left: 0 !important;
    right: auto !important;
    transform: translateX(-10px) !important;
  }

  .leftTreeHole,
  .rightTreeHole {
    text-align: left;
  }

  .avatar-img {
    left: 25px !important;
    right: auto !important;
  }

  .tree-hole-box {
    margin-left: 90px !important;
    margin-right: 0 !important;
  }

  .box-tag {
    left: -10px !important;
    right: auto !important;
    border-width: 15px 10px 5px 0 !important;
  }
}
</style>
