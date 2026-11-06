<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">执行记录</div>
        <div class="page-subtitle">所有用例的执行流水，可按计划、结果筛选</div>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="panel">
      <div class="filter-bar">
        <el-select v-model="query.projectId" placeholder="全部项目" clearable @change="load">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="query.planId" placeholder="全部计划" clearable @change="load">
          <el-option v-for="p in plans" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="query.result" placeholder="执行结果" clearable @change="load">
          <el-option label="通过" value="PASS" />
          <el-option label="失败" value="FAIL" />
          <el-option label="阻塞" value="BLOCK" />
          <el-option label="跳过" value="SKIP" />
        </el-select>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="planId" label="计划" width="80" />
        <el-table-column prop="caseId" label="用例" width="80" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="resultType(row.result)" size="small">{{ resultText(row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executorName" label="执行人" width="110" />
        <el-table-column prop="actualResult" label="实际结果" min-width="300" show-overflow-tooltip />
        <el-table-column prop="durationMs" label="耗时(ms)" width="100" />
        <el-table-column prop="executedAt" label="执行时间" width="170" />
      </el-table>

      <div class="table-footer">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
          @current-change="load" @size-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { executionApi, testPlanApi, projectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const rows = ref<any[]>([])
const projects = ref<any[]>([])
const plans = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

const query = reactive<any>({
  page: 1, size: 10, projectId: userStore.currentProjectId, planId: undefined, result: ''
})

const resultText = (v: string) => ({ PASS: '通过', FAIL: '失败', BLOCK: '阻塞', SKIP: '跳过' } as any)[v] || v
const resultType = (v: string) => ({ PASS: 'success', FAIL: 'danger', BLOCK: 'warning', SKIP: 'info' } as any)[v] || 'info'

async function load() {
  loading.value = true
  try {
    const data = await executionApi.page(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  projects.value = await projectApi.options()
  plans.value = (await testPlanApi.page({ page: 1, size: 50, projectId: query.projectId || undefined })).records
  load()
})
</script>
