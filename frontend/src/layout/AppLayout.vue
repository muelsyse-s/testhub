<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-mark">TH</div>
        <div class="logo-text">
          <div class="logo-title">TestHub</div>
          <div class="logo-sub">测试资产管理平台</div>
        </div>
      </div>

      <el-menu
        :default-active="route.path"
        class="menu"
        background-color="transparent"
        text-color="#b6bccd"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">v1.0.0 · 2026 综合实践训练</div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="topbar-left">
          <span class="breadcrumb">{{ route.meta.title || '' }}</span>
          <el-tag v-if="userStore.currentProjectId" size="small" type="info" effect="plain">
            当前项目：{{ currentProjectName }}
          </el-tag>
        </div>
        <div class="topbar-right">
          <el-select
            v-model="projectId"
            placeholder="全部项目"
            clearable
            size="default"
            style="width: 190px"
            @change="onProjectChange"
          >
            <el-option label="全部项目" :value="0" />
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>

          <el-dropdown trigger="click" @command="onCommand">
            <div class="user">
              <el-avatar :size="30" style="background: #4f46e5">
                {{ (userStore.user?.realName || 'U').slice(0, 1) }}
              </el-avatar>
              <div class="user-meta">
                <div class="user-name">{{ userStore.user?.realName }}</div>
                <div class="user-role">{{ roleText }}</div>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <keep-alive :max="6">
            <component :is="Component" :key="route.fullPath" />
          </keep-alive>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { projectApi } from '@/api'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const projects = ref<any[]>([])
const projectId = ref<number>(userStore.currentProjectId || 0)

const menus = [
  { path: '/dashboard', title: '质量总览', icon: 'DataBoard' },
  { path: '/projects', title: '项目管理', icon: 'Folder' },
  { path: '/test-points', title: '测试点管理', icon: 'Aim' },
  { path: '/test-cases', title: '测试用例', icon: 'Document' },
  { path: '/test-plans', title: '测试计划', icon: 'Calendar' },
  { path: '/executions', title: '执行记录', icon: 'CircleCheck' },
  { path: '/defects', title: '缺陷管理', icon: 'Warning' },
  { path: '/apis', title: '接口定义', icon: 'Connection' },
  { path: '/api-scripts', title: '接口脚本', icon: 'Cpu' },
  { path: '/environments', title: '环境管理', icon: 'SetUp' },
  { path: '/reports', title: '统计报表', icon: 'TrendCharts' },
  { path: '/users', title: '用户权限', icon: 'User' }
]

const roleMap: Record<string, string> = {
  ADMIN: '系统管理员',
  TEST_MANAGER: '测试经理',
  TESTER: '测试工程师',
  DEVELOPER: '开发工程师'
}

const roleText = computed(() =>
  (userStore.user?.roles || []).map((r) => roleMap[r] || r).join(' / ')
)

const currentProjectName = computed(() => {
  const p = projects.value.find((x) => x.id === userStore.currentProjectId)
  return p ? p.name : ''
})

onMounted(async () => {
  projects.value = await projectApi.options()
})

function onProjectChange(value: number) {
  userStore.setProject(value || null)
  router.go(0)
}

async function onCommand(command: string) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 218px;
  background: var(--th-sidebar);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 18px 14px;
}

.logo-mark {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: linear-gradient(135deg, #6366f1, #4338ca);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.5px;
}

.logo-title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.2;
}

.logo-sub {
  color: #7f869b;
  font-size: 11px;
  margin-top: 2px;
}

.menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  padding: 6px 10px;
}

.menu :deep(.el-menu-item) {
  height: 42px;
  line-height: 42px;
  border-radius: 8px;
  margin-bottom: 3px;
  font-size: 13.5px;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
}

.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #4f46e5, #5b53e8);
  color: #fff;
}

.sidebar-footer {
  padding: 12px 18px;
  font-size: 11px;
  color: #5f6579;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 58px;
  background: #fff;
  border-bottom: 1px solid var(--th-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb {
  font-size: 15px;
  font-weight: 600;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.user {
  display: flex;
  align-items: center;
  gap: 9px;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 8px;
}

.user:hover {
  background: #f6f7fb;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.2;
}

.user-role {
  font-size: 11px;
  color: #8a90a2;
}

.content {
  flex: 1;
  overflow-y: auto;
  background: var(--th-bg);
}
</style>
