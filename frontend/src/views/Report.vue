<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">统计报表</div>
        <div class="page-subtitle">模块覆盖情况、缺陷状态流转与成员工作量</div>
      </div>
      <el-button :icon="Refresh" @click="loadAll">刷新</el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">测试点总数</div>
        <div class="value" style="color: #4f46e5">{{ overview.pointCount ?? 0 }}<span class="unit">个</span></div>
      </div>
      <div class="stat-card">
        <div class="label">测试点覆盖率</div>
        <div class="value" style="color: #16a34a">{{ overview.coverageRate ?? 0 }}<span class="unit">%</span></div>
      </div>
      <div class="stat-card">
        <div class="label">缺陷总数</div>
        <div class="value" style="color: #dc2626">{{ overview.defectTotal ?? 0 }}<span class="unit">个</span></div>
      </div>
      <div class="stat-card">
        <div class="label">接口脚本数</div>
        <div class="value" style="color: #0284c7">{{ overview.scriptCount ?? 0 }}<span class="unit">个</span></div>
      </div>
    </div>

    <div class="panel">
      <div class="section-title">模块测试点与用例覆盖</div>
      <div ref="moduleRef" class="chart-box"></div>
    </div>

    <div class="chart-grid equal" style="margin-top: 16px">
      <div class="panel">
        <div class="section-title">缺陷状态分布</div>
        <div ref="statusRef" class="chart-box" style="height: 280px"></div>
      </div>
      <div class="panel">
        <div class="section-title">成员执行工作量</div>
        <div ref="workloadRef" class="chart-box" style="height: 280px"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { reportApi } from '@/api'

const userStore = useUserStore()
const overview = ref<any>({})
const moduleRef = ref<HTMLElement>()
const statusRef = ref<HTMLElement>()
const workloadRef = ref<HTMLElement>()
let charts: echarts.ECharts[] = []

const labelMap: Record<string, string> = {
  OPEN: '待处理', FIXING: '修复中', RESOLVED: '已解决', CLOSED: '已关闭', REJECTED: '已拒绝'
}

async function loadAll() {
  const pid = userStore.currentProjectId || undefined
  const [ov, modules, status, workload] = await Promise.all([
    reportApi.overview(pid),
    reportApi.moduleCoverage(pid),
    reportApi.defectStatus(pid),
    reportApi.workload(pid)
  ])
  overview.value = ov
  await nextTick()

  const mChart = echarts.init(moduleRef.value!)
  mChart.setOption({
    grid: { left: 50, right: 20, top: 40, bottom: 40 },
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 0, icon: 'circle', itemWidth: 8 },
    xAxis: {
      type: 'category',
      data: modules.map((m: any) => m.module),
      axisLabel: { color: '#8a90a2', fontSize: 11 },
      axisLine: { lineStyle: { color: '#dfe3ec' } }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f2f7' } },
      axisLabel: { color: '#8a90a2', fontSize: 11 }
    },
    series: [
      { name: '测试点', type: 'bar', data: modules.map((m: any) => m.pointCount), barMaxWidth: 26, itemStyle: { color: '#4f46e5', borderRadius: [4, 4, 0, 0] } },
      { name: '用例', type: 'bar', data: modules.map((m: any) => m.caseCount), barMaxWidth: 26, itemStyle: { color: '#93c5fd', borderRadius: [4, 4, 0, 0] } }
    ]
  })

  const sChart = echarts.init(statusRef.value!)
  sChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, textStyle: { fontSize: 11 } },
    color: ['#f43f5e', '#f59e0b', '#10b981', '#64748b', '#8b5cf6'],
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '44%'],
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{c}', fontSize: 11, color: '#5c6373' },
        data: status.map((s: any) => ({ name: labelMap[s.name] || s.name, value: s.value }))
      }
    ]
  })

  const wChart = echarts.init(workloadRef.value!)
  wChart.setOption({
    grid: { left: 60, right: 30, top: 40, bottom: 30 },
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 0, icon: 'circle', itemWidth: 8 },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f2f7' } }, axisLabel: { color: '#8a90a2', fontSize: 11 } },
    yAxis: {
      type: 'category',
      data: workload.map((w: any) => w.name),
      axisLabel: { color: '#5c6373', fontSize: 11 },
      axisLine: { lineStyle: { color: '#dfe3ec' } }
    },
    series: [
      { name: '通过', type: 'bar', stack: 'a', data: workload.map((w: any) => w.passCount), itemStyle: { color: '#4f46e5' }, barMaxWidth: 20 },
      { name: '失败', type: 'bar', stack: 'a', data: workload.map((w: any) => w.failCount), itemStyle: { color: '#f43f5e' }, barMaxWidth: 20 }
    ]
  })

  charts = [mChart, sChart, wChart]
}

function resize() {
  charts.forEach((c) => c.resize())
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  charts.forEach((c) => c.dispose())
})
</script>
