<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">质量总览</div>
        <div class="page-subtitle">
          测试资产与执行质量的实时快照 ·
          {{ projectName }}
        </div>
      </div>
      <el-button :icon="Refresh" @click="loadAll">刷新</el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card" v-for="card in statCards" :key="card.label">
        <div class="label">{{ card.label }}</div>
        <div class="value" :style="{ color: card.color }">
          {{ card.value }}<span class="unit">{{ card.unit }}</span>
        </div>
        <el-icon class="icon"><component :is="card.icon" /></el-icon>
      </div>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <div class="section-title">近 14 天执行趋势</div>
        <div ref="trendRef" class="chart-box"></div>
      </div>
      <div class="panel">
        <div class="section-title">缺陷严重程度分布</div>
        <div ref="severityRef" class="chart-box"></div>
      </div>
    </div>

    <div class="chart-grid equal" style="margin-top: 16px">
      <div class="panel">
        <div class="section-title">用例优先级分布</div>
        <div ref="priorityRef" class="chart-box" style="height: 260px"></div>
      </div>
      <div class="panel">
        <div class="section-title">用例类型分布</div>
        <div ref="typeRef" class="chart-box" style="height: 260px"></div>
      </div>
    </div>

    <div class="panel" style="margin-top: 16px">
      <div class="section-title">最近执行记录</div>
      <el-table :data="recent" size="small" stripe>
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="caseId" label="用例ID" width="90" />
        <el-table-column prop="executorName" label="执行人" width="110" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="resultType(row.result)" size="small" effect="light">{{ row.result }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actualResult" label="实际结果" show-overflow-tooltip />
        <el-table-column prop="executedAt" label="执行时间" width="170" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onBeforeUnmount, ref } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { reportApi, executionApi, projectApi } from '@/api'

const userStore = useUserStore()
const overview = ref<any>({})
const recent = ref<any[]>([])
const projectName = ref('全部项目')

const trendRef = ref<HTMLElement>()
const severityRef = ref<HTMLElement>()
const priorityRef = ref<HTMLElement>()
const typeRef = ref<HTMLElement>()

let charts: echarts.ECharts[] = []

const statCards = computed(() => [
  { label: '测试用例总数', value: overview.value.caseCount ?? 0, unit: '条', icon: 'Document', color: '#4f46e5' },
  { label: '执行通过率', value: overview.value.passRate ?? 0, unit: '%', icon: 'CircleCheck', color: '#16a34a' },
  { label: '未关闭缺陷', value: overview.value.defectOpenCount ?? 0, unit: '个', icon: 'Warning', color: '#dc2626' },
  { label: '接口自动化率', value: overview.value.automationRate ?? 0, unit: '%', icon: 'Cpu', color: '#0284c7' }
])

const resultType = (result: string) =>
  ({ PASS: 'success', FAIL: 'danger', BLOCK: 'warning', SKIP: 'info' } as Record<string, string>)[result] || 'info'

const baseChart = (el?: HTMLElement) => {
  if (!el) return null
  const instance = echarts.init(el)
  charts.push(instance)
  return instance
}

async function loadAll() {
  const pid = userStore.currentProjectId || undefined
  const [ov, trend, severity, priority, type, execs, projects] = await Promise.all([
    reportApi.overview(pid),
    reportApi.trend(pid, 14),
    reportApi.defectSeverity(pid),
    reportApi.casePriority(pid),
    reportApi.caseType(pid),
    executionApi.page({ page: 1, size: 6 }),
    projectApi.options()
  ])
  overview.value = ov
  recent.value = execs.records
  const p = projects.find((x: any) => x.id === pid)
  projectName.value = p ? p.name : '全部项目'

  await nextTick()
  renderTrend(trend)
  renderPie(severityRef.value, severity, '严重程度')
  renderPie(priorityRef.value, priority, '优先级')
  renderPie(typeRef.value, type, '类型')
}

const labelMap: Record<string, string> = {
  BLOCKER: '致命', CRITICAL: '严重', MAJOR: '一般', MINOR: '轻微', TRIVIAL: '提示',
  HIGH: '高', MEDIUM: '中', LOW: '低',
  FUNCTION: '功能', INTERFACE: '接口', PERFORMANCE: '性能', SECURITY: '安全', UI: '界面'
}

function renderTrend(data: any[]) {
  const chart = charts.find((c) => c.getDom() === trendRef.value) || baseChart(trendRef.value)
  if (!chart) return
  chart.setOption({
    grid: { left: 40, right: 20, top: 36, bottom: 30 },
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 0, icon: 'circle', itemWidth: 8 },
    xAxis: {
      type: 'category',
      data: data.map((d) => String(d.statDate).slice(5)),
      axisLine: { lineStyle: { color: '#dfe3ec' } },
      axisLabel: { color: '#8a90a2', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f2f7' } },
      axisLabel: { color: '#8a90a2', fontSize: 11 }
    },
    series: [
      {
        name: '通过',
        type: 'bar',
        stack: 'total',
        data: data.map((d) => d.passCount),
        itemStyle: { color: '#4f46e5', borderRadius: [0, 0, 0, 0] },
        barMaxWidth: 22
      },
      {
        name: '失败',
        type: 'bar',
        stack: 'total',
        data: data.map((d) => d.failCount),
        itemStyle: { color: '#f43f5e', borderRadius: [3, 3, 0, 0] },
        barMaxWidth: 22
      }
    ]
  })
}

function renderPie(el: HTMLElement | undefined, data: any[], name: string) {
  const chart = charts.find((c) => c.getDom() === el) || baseChart(el)
  if (!chart) return
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, textStyle: { fontSize: 11 } },
    color: ['#4f46e5', '#38bdf8', '#f59e0b', '#f43f5e', '#10b981', '#8b5cf6'],
    series: [
      {
        name,
        type: 'pie',
        radius: ['46%', '68%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: data.map((d) => ({ name: labelMap[d.name] || d.name, value: d.value }))
      }
    ]
  })
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
  charts = []
})
</script>
