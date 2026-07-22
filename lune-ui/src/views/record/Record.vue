<template>
  <div class="record-page">
    <!-- Hero Banner -->
    <div class="hero-banner myCenter">
      <el-image
        class="banner-image"
        v-once
        lazy
        :src="bannerImage"
        fit="cover"
      >
        <template #error>
          <div class="image-slot"></div>
        </template>
      </el-image>
      <div class="hero-info">
        <div class="hero-title">📋 记录</div>
        <div class="hero-subtitle">记录生活的每一个精彩瞬间</div>
      </div>
    </div>

    <!-- Content -->
    <div class="record-container">
      <!-- Category Tags -->
      <div class="tags-wrap shadow-box">
        <div
          v-for="(cat, index) in categoryList"
          :key="cat.id"
          :class="{ 'is-active-tag': activeCategoryId === cat.id }"
          class="tag-item"
          @click="switchCategory(cat)"
        >
          <span
            class="tag-badge"
            :style="{ color: tagColors[index % tagColors.length] }"
          >
            {{ cat.name }}
          </span>
        </div>
        <div v-if="categoryList.length === 0" class="tag-empty">
          暂无分类
        </div>
      </div>

      <!-- Record List -->
      <div class="record-list-wrap">
        <div class="record-grid" v-if="recordList.length > 0">
          <div
            v-for="item in recordList"
            :key="item.id"
            class="record-card shadow-box"
          >
            <el-image
              v-if="item.cover"
              :src="item.cover"
              fit="cover"
              class="record-cover"
              :preview-src-list="[item.cover]"
            />
            <div class="record-body">
              <h4 class="record-title">{{ item.title }}</h4>
              <p v-if="item.content" class="record-desc">
                {{ item.content.substring(0, 100) }}
              </p>
              <div class="record-meta">
                <span class="record-date">📅 {{ formatDate(item.createTime) }}</span>
                <span class="record-tag" v-if="item.categoryName">
                  {{ item.categoryName }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div v-else style="text-align:center;padding:80px 0;">
          <el-empty description="暂无记录" />
        </div>

        <!-- Pagination -->
        <div class="pagination-wrap">
          <div
            v-if="totalCount > recordList.length"
            class="pagination-btn"
            @click="loadMore"
          >
            下一页
          </div>
          <div v-else class="pagination-end">
            ~~到底啦~~
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { recordApi, categoryApi } from '../../api/modules'
import { useAppStore } from '../../stores/app'

const appStore = useAppStore()

// --- state ---
const recordList = ref([])
const categoryList = ref([])
const activeCategoryId = ref(null)
const page = ref(1)
const pageSize = 12
const totalCount = ref(0)

// --- constants ---
const bannerImage = ref('/assets/背景3.jpg')
const tagColors = [
  '#FF7744', '#e73c7e', '#23a6d5', '#39c5bb',
  '#ff4b2b', '#9370db', '#FF9D3A', '#67c23a'
]

// --- lifecycle ---
onMounted(() => {
  if (appStore.webInfo.backgroundImage) {
    bannerImage.value = appStore.webInfo.backgroundImage
  }
  fetchCategories()
  fetchRecords()
})

// --- methods ---
async function fetchCategories() {
  try {
    const data = await categoryApi.list('record')
    categoryList.value = data || []
  } catch (e) { /* silent */ }
}

async function fetchRecords() {
  try {
    const params = { page: page.value, size: pageSize }
    if (activeCategoryId.value) {
      params.categoryId = activeCategoryId.value
    }
    const data = await recordApi.list(params)
    if (data && data.records) {
      recordList.value = recordList.value.concat(data.records)
      totalCount.value = data.total
    }
  } catch (e) { /* silent */ }
}

function switchCategory(cat) {
  if (activeCategoryId.value === cat.id) {
    // click same: deselect, show all
    activeCategoryId.value = null
  } else {
    activeCategoryId.value = cat.id
  }
  page.value = 1
  recordList.value = []
  fetchRecords()
}

function loadMore() {
  if (totalCount.value > recordList.value.length) {
    page.value++
    fetchRecords()
  }
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
/* ====== Hero Banner ====== */
.hero-banner {
  position: relative;
  height: 40vh;
  min-height: 250px;
  overflow: hidden;
}
.banner-image {
  position: absolute;
  inset: 0;
}
.banner-image::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: var(--translucent);
  z-index: 1;
}
.hero-info {
  z-index: 10;
  letter-spacing: 4px;
  line-height: 40px;
  font-weight: bold;
  text-align: center;
  color: var(--white);
}
.hero-title {
  font-size: 30px;
}
.hero-subtitle {
  font-size: 18px;
}

/* ====== Container ====== */
.record-container {
  background: var(--background);
  padding-top: 40px;
  padding-bottom: 40px;
}

/* ====== Tags Row ====== */
.tags-wrap {
  width: 70%;
  max-width: 780px;
  margin: 0 auto;
  padding: 20px;
  border-radius: 10px;
  display: flex;
  flex-wrap: wrap;
  background: var(--background);
}
.tag-item {
  margin: 6px;
  cursor: pointer;
  user-select: none;
}
.tag-badge {
  display: inline-block;
  padding: 5px 14px;
  border: 1px solid currentColor;
  border-radius: 1rem;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}
.tag-badge:hover {
  transform: scale(1.1);
}
.is-active-tag {
  animation: scale 1.5s ease-in-out infinite;
}
.is-active-tag .tag-badge {
  box-shadow: 0 0 8px currentColor;
}
.tag-empty {
  color: var(--greyFont);
  font-size: 14px;
}

/* ====== Record List ====== */
.record-list-wrap {
  width: 70%;
  margin: 40px auto;
  min-height: 600px;
}
.record-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.record-card {
  background: var(--background);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}
.record-card:hover {
  transform: translateY(-2px);
}
.record-cover {
  width: 100%;
  height: 180px;
}
.record-body {
  padding: 16px;
}
.record-title {
  margin-bottom: 8px;
  font-size: 16px;
  color: var(--fontColor);
}
.record-desc {
  color: var(--greyFont);
  font-size: 13px;
  line-height: 1.5;
}
.record-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.record-date {
  font-size: 12px;
  color: var(--greyFont);
}
.record-tag {
  font-size: 11px;
  border: 1px solid var(--themeBackground);
  color: var(--themeBackground);
  border-radius: 1rem;
  padding: 2px 8px;
}

/* ====== Pagination ====== */
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

/* ====== Responsive ====== */
@media screen and (max-width: 900px) {
  .tags-wrap {
    width: 90%;
  }
  .record-list-wrap {
    width: 90%;
  }
}
</style>
