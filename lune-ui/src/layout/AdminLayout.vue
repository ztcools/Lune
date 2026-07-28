<template>
  <div>
    <div class="my-header myBetween">
      <div class="logo">🌙 Lune 后台</div>
      <div class="header-right">
        <div class="admin-index" @click="$router.push({path: '/'})">首页</div>
        <div class="header-user-con">
          <el-dropdown placement="bottom" trigger="hover" :hide-on-click="false">
            <span class="admin-avatar-wrap">
              <el-avatar class="user-avatar" :size="40" :src="userStore.user?.avatar">
                {{ (userStore.nickname || 'A').charAt(0) }}
              </el-avatar>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <span style="font-weight:600">{{ userStore.nickname }}</span>
                  <span style="color:#999;font-size:12px;margin-left:8px">{{ userStore.user?.role === 'ADMIN' ? '管理员' : '用户' }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="$router.push({path:'/'})">🏠 返回首页</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">🚪 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    <div class="sidebar" :style="{ width: collapsed ? '44px' : '130px' }">
      <div @click="collapsed=!collapsed" style="color:#606266;cursor:pointer;background:#ebf1f6;display:flex">
        <el-icon style="margin:14px;font-size:17px"><Menu /></el-icon>
        <div style="font-size:15px;margin-top:13px">{{ collapsed?'':'折叠' }}</div>
      </div>
      <el-menu class="sidebar-el-menu" background-color="#ebf1f6" text-color="#606266" active-text-color="#20a0ff"
        :default-active="route.path" router :collapse="collapsed">
        <el-menu-item index="/admin"><el-icon><HomeFilled /></el-icon><span>系统首页</span></el-menu-item>
        <el-menu-item index="/admin/settings"><el-icon><Tools /></el-icon><span>网站设置</span></el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><UserFilled /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/articles"><el-icon><Document /></el-icon><span>文章管理</span></el-menu-item>
        <el-menu-item index="/admin/categories"><el-icon><Collection /></el-icon><span>分类管理</span></el-menu-item>
        <el-menu-item index="/admin/comments"><el-icon><ChatDotSquare /></el-icon><span>评论管理</span></el-menu-item>
        <el-menu-item index="/admin/treeholes"><el-icon><ChatLineSquare /></el-icon><span>树洞管理</span></el-menu-item>
        <el-menu-item index="/admin/resources"><el-icon><Picture /></el-icon><span>资源管理</span></el-menu-item>
        <el-menu-item index="/admin/records"><el-icon><Notebook /></el-icon><span>记录管理</span></el-menu-item>
        <el-menu-item index="/admin/essays"><el-icon><EditPen /></el-icon><span>随笔管理</span></el-menu-item>
        <el-menu-item index="/admin/family"><el-icon><House /></el-icon><span>家园管理</span></el-menu-item>
        <el-menu-item index="/admin/diaries"><el-icon><Notebook /></el-icon><span>日记管理</span></el-menu-item>
        <el-menu-item index="/admin/resume"><el-icon><Suitcase /></el-icon><span>简历管理</span></el-menu-item>
        <el-menu-item index="/admin/wishes"><el-icon><MagicStick /></el-icon><span>许愿池</span></el-menu-item>
      </el-menu>
    </div>
    <div class="content-box" :style="{ left: collapsed ? '44px' : '130px' }">
      <div class="admin-content"><router-view /></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)
async function handleLogout() { await userStore.logout(); router.push('/admin/login') }
</script>

<style scoped>
.my-header{position:relative;width:100%;height:70px;color:#fff;background:linear-gradient(135deg,#43a047,#66bb6a);box-shadow:0 2px 12px rgba(67,160,71,.25)}
.my-header .logo{line-height:70px;margin-left:70px;font-size:22px;font-weight:700;font-family:var(--trendy-font);letter-spacing:1px}
.header-right{display:flex;justify-content:flex-end;margin-right:40px;align-items:center;height:70px}
.admin-index{height:70px;line-height:70px;font-size:15px;margin-right:24px;cursor:pointer;font-weight:600;transition:all .3s;border-radius:8px;padding:0 10px}
.admin-index:hover{background:rgba(255,255,255,.18)}
.header-user-con{display:flex;align-items:center}
.sidebar{display:block;position:absolute;left:0;top:70px;bottom:0;overflow-y:scroll;width:130px;user-select:none;overflow-x:hidden;background:#f4f9f4;border-right:1px solid #e0eee0;transition:width .3s ease-in-out}
.sidebar::-webkit-scrollbar{width:0}
.sidebar-el-menu{background:#f4f9f4!important;border-right:none}
.sidebar-el-menu .el-menu-item{padding:0 10px!important;border-radius:12px;margin:4px 8px;transition:all .25s}
.sidebar-el-menu .el-menu-item:hover{background:#e0f0e0!important}
.sidebar-el-menu .el-menu-item.is-active{background:linear-gradient(135deg,#66bb6a,#81c784)!important;color:#fff!important;border-radius:12px}
.content-box{position:absolute;left:130px;right:0;top:70px;bottom:0;transition:left .3s ease-in-out;background:#f4f9f4}
.admin-content{width:auto;height:100%;padding:30px;overflow-y:scroll}
@media screen and (max-width:768px){.admin-content{padding:14px}}
</style>
