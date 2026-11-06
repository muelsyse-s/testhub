<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">{{ plan.name || '计划详情' }}</div>
        <div class="page-subtitle">
          {{ plan.description || '—' }} ·
          {{ plan.startDate }} ~ {{ plan.endDate }}
        </div>
      </div>
      <div style="display: flex; gap: 8px">
        <el-button :icon="Back" @click="$router.push('/test-plans')">返回</el-button>
        <el-button type="primary" :icon="Plus" @click="openAdd">加入用例</el-button>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">用例总数</div>
        <div class="value">{{ progress.total || 0 }}<span class="unit">条</span></div>
      </div>
      <div class="stat-card">
        <div class="label">已执行</div>
        <div class="value" style="color: #0284c7">{{ progress.executed || 0 }}<span class="unit">条</span></div>
      </div>
      <div class="stat-card">
        <div class="label">通过率</div>
        <div class="value" style="color: #16a34a">{{ progress.passRate || 0 }}<span class="unit">%</span></div>
      </div>
      <div class="stat-card">
        <div class="label">失败 / 阻塞</div>
        <div class="value" style="color: #dc2626">
          {{ progress.fail || 0 }}<span class="unit">/ {{ progress.block || 0 }}</span>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="section-title">计划执行进度</div>
      <el-progress
        :percentage="progress.progress || 0"
        :stroke-width="16"
        :text-inside="true"
        :status="progress.progress === 100 ? 'success' : undefined"
      />
      <div class="progress-legend">
        <span><i style="background: #16a34a"></i>通过 {{ progress.pass || 0 }}</span>
        <span><i style="background: #dc2626"></i>失败 {{ progress.fail || 0 }}</span>
        <span><i style="background: #f59e0b"></i>阻塞 {{ progress.block || 0 }}</span>
        <span><i style="background: #94a3b8"></i>跳过 {{ progress.skip || 0 }}</span>
        <span><i style="background: #e2e8f0"></i>未执行 {{ progress.notRun || 0 }}</span>
      </div>
    </div>

    <div class="panel">
      <div class="section-title">计划内用例</div>
      <el-table :data="cases" v-loading="loading" size="default" stripe>
        <el-table-column prop="caseNo" label="用例编号" width="130">
          <template #default="{ row }"><span class="mono">{{ row.caseNo }}</span></template>
        </el-table-column>
        <el-table-column prop="title" label="用例标题" min-width="250" show-overflow-tooltip />
        <el-table-column label="优先级" width="86">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small" effect="plain">
              {{ priorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="执行人" width="100" />
        <el-table-column label="最近结果" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.lastResult" :type="resultType(row.lastResult)" size="small">
              {{ resultText(row.lastResult) }}
            </el-tag>
            <span v-else class="muted">未执行</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastExecutedAt" label="最近执行" width="170">
          <template #default="{ row }">{{ row.lastExecutedAt || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRun(row)">执行</el-button>
            <el-button link type="danger" @click="removeCase(row)">移出</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 加入用例 -->
    <el-dialog v-model="addVisible" title="从用例库加入计划" width="760px">
      <el-table :data="candidates" height="360" @selection-change="(v: any[]) => (selected = v)">
        <el-table-column type="selection" width="44" />
        <el-table-column prop="caseNo" label="编号" width="120" />
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">{{ priorityText(row.priority) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="addCases">加入 {{ selected.length }} 条</el-button>
      </template>
    </el-dialog>

    <!-- 执行 -->
    <el-dialog v-model="runVisible" title="登记执行结果" width="560px">
      <el-descriptions :column="1" border size="small" style="margin-bottom: 16px">
        <el-descriptions-item label="用例编号">{{ current?.caseNo }}</el-descriptions-item>
        <el-descriptions-item label="用例标题">{{ current?.title }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="runForm" label-width="90px">
        <el-form-item label="执行结果">
          <el-radio-group v-model="runForm.result">
            <el-radio-button label="PASS">通过</el-radio-button>
            <el-radio-button label="FAIL">失败</el-radio-button>
            <el-radio-button label="BLOCK">阻塞</el-radio-button>
            <el-radio-button label="SKIP">跳过</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="实际结果">
          <el-input v-model="runForm.actualResult" type="textarea" :rows="3"
            placeholder="记录实际现象，失败时务必写清复现步骤与期望差异" />
        </el-form-item>
        <el-form-item label="耗时(ms)">
          <el-input-number v-model="runForm.durationMs" :min="0" :step="100" style="width: 180px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="runVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRun">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Back } from '@element-plus/icons-vue'
import { testPlanApi, testCaseApi, executionApi } from '@/api'

const route = useRoute()
const planId = Number(route.params.id)
const plan = ref<any>({})
const cases = ref<any[]>([])
const progress = ref<any>({})
const candidates = ref<any[]>([])
const selected = ref<any[]>([])
const current = ref<any>(null)
const loading = ref(false)
const saving = ref(false)
const addVisible = ref(false)
const runVisible = ref(false)

const runForm = reactive<any>({ result: 'PASS', actualResult: '', durationMs: 1000 })

const priorityText = (v: string) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' } as any)[v] || v
const priorityType = (v: string) => ({ HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' } as any)[v] || 'info'
const resultText = (v: string) => ({ PASS: '通过', FAIL: '失败', BLOCK: '阻塞', SKIP: '跳过' } as any)[v] || v
const resultType = (v: string) => ({ PASS: 'success', FAIL: 'danger', BLOCK: 'warning', SKIP: 'info' } as any)[v] || 'info'

async function load() {
  loading.value = true
  try {
    plan.value = await testPlanApi.detail(planId)
    cases.value = await testPlanApi.cases(planId)
    progress.value = await testPlanApi.progress(planId)
  } finally {
    loading.value = false
  }
}

async function openAdd() {
  candidates.value = await testCaseApi.candidates(plan.value.projectId)
  selected.value = []
  addVisible.value = true
}

async function addCases() {
  if (!selected.value.length) {
    ElMessage.warning('请选择用例')
    return
  }
  saving.value = true
  try {
    const res = await testPlanApi.addCases(planId, {
      caseIds: selected.value.map((s) => s.id)
    })
    ElMessage.success(`成功加入 ${res.added} 条用例`)
    addVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function removeCase(row: any) {
  await ElMessageBox.confirm(`确认将用例「${row.caseNo}」移出计划？`, '提示', { type: 'warning' })
  await testPlanApi.removeCase(planId, row.caseId)
  ElMessage.success('已移出')
  load()
}

function openRun(row: any) {
  current.value = row
  Object.assign(runForm, { result: 'PASS', actualResult: '', durationMs: 1000 })
  runVisible.value = true
}

async function submitRun() {
  saving.value = true
  try {
    await executionApi.record({ planId, caseId: current.value.caseId, ...runForm })
    ElMessage.success('执行结果已记录')
    runVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.progress-legend {
  display: flex;
  gap: 22px;
  margin-top: 14px;
  font-size: 12.5px;
  color: #5c6373;
}

.progress-legend i {
  display: inline-block;
  width: 9px;
  height: 9px;
  border-radius: 2px;
  margin-right: 6px;
}
</style>
