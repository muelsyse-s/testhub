<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">接口脚本</div>
        <div class="page-subtitle">
          请求配置 + 断言 + 变量提取，一键执行并落库，支持批量回归
        </div>
      </div>
      <div style="display: flex; gap: 8px">
        <el-select v-model="runEnvId" size="default" style="width: 150px" placeholder="执行环境">
          <el-option v-for="e in envs" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
        <el-button :icon="VideoPlay" @click="runBatch">批量执行</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增脚本</el-button>
      </div>
    </div>

    <div class="panel">
      <div class="filter-bar">
        <el-select v-model="query.projectId" placeholder="全部项目" clearable @change="load">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="query.scriptType" placeholder="用例类型" clearable @change="load">
          <el-option label="正向" value="POSITIVE" />
          <el-option label="异常" value="NEGATIVE" />
          <el-option label="边界" value="BOUNDARY" />
          <el-option label="性能" value="PERFORMANCE" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="脚本名称" clearable :prefix-icon="Search"
          style="width: 230px" @keyup.enter="load" @clear="load" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button :icon="Tickets" @click="openLogs">执行日志</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="name" label="脚本名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ typeText(row.scriptType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近结果" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.lastRunStatus" :type="row.lastRunStatus === 'PASS' ? 'success' : 'danger'" size="small">
              {{ row.lastRunStatus }}
            </el-tag>
            <span v-else class="muted">未执行</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastRunAt" label="最近执行" width="170">
          <template #default="{ row }">{{ row.lastRunAt || '—' }}</template>
        </el-table-column>
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small" effect="plain">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :loading="runningId === row.id" @click="run(row)">执行</el-button>
            <el-button link type="primary" @click="showConfig(row)">配置</el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
          @current-change="load" @size-change="load" />
      </div>
    </div>

    <!-- 执行结果 -->
    <el-drawer v-model="resultVisible" title="执行结果" size="620px">
      <template v-if="result">
        <el-alert
          :title="result.status === 'PASS' ? '全部断言通过' : '断言未通过'"
          :type="result.status === 'PASS' ? 'success' : 'error'"
          :description="result.failMessage || ''"
          show-icon
          :closable="false"
        />
        <el-descriptions :column="2" border size="small" style="margin-top: 16px">
          <el-descriptions-item label="请求">{{ result.method }} {{ result.requestUrl }}</el-descriptions-item>
          <el-descriptions-item label="HTTP 状态码">{{ result.statusCode ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ result.durationMs }} ms</el-descriptions-item>
          <el-descriptions-item label="执行时间">{{ result.executedAt }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title" style="margin-top: 18px">断言明细</div>
        <el-table :data="result.assertionResults" size="small" border>
          <el-table-column label="断言" prop="message" min-width="200" />
          <el-table-column label="期望" prop="expected" width="120" />
          <el-table-column label="实际" prop="actual" width="140" />
          <el-table-column label="结果" width="80">
            <template #default="{ row }">
              <el-tag :type="row.passed ? 'success' : 'danger'" size="small">
                {{ row.passed ? '通过' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="Object.keys(result.extractedVars || {}).length" style="margin-top: 16px">
          <div class="section-title">提取变量</div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item v-for="(v, k) in result.extractedVars" :key="k" :label="String(k)">
              {{ v }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section-title" style="margin-top: 18px">响应体</div>
        <div class="code-block">{{ result.responseBody || '（空）' }}</div>
      </template>
    </el-drawer>

    <!-- 配置查看 -->
    <el-drawer v-model="configVisible" title="脚本配置" size="620px">
      <template v-if="current">
        <div class="section-title">请求配置</div>
        <div class="code-block">{{ pretty(current.requestConfig) }}</div>
        <div class="section-title" style="margin-top: 16px">断言配置</div>
        <div class="code-block">{{ pretty(current.assertions) }}</div>
        <div class="section-title" style="margin-top: 16px">变量提取</div>
        <div class="code-block">{{ pretty(current.extractors) }}</div>
        <div class="section-title" style="margin-top: 16px">变量定义</div>
        <div class="code-block">{{ pretty(current.variables) }}</div>
      </template>
    </el-drawer>

    <!-- 编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑脚本' : '新增脚本'" width="840px" top="5vh">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="95px">
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="form.projectId" style="width: 100%" :disabled="!!form.id">
                <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联接口">
              <el-select v-model="form.apiId" style="width: 100%" clearable placeholder="可选">
                <el-option v-for="a in apis" :key="a.id" :label="`${a.method} ${a.path}`" :value="a.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="脚本名称" prop="name">
          <el-input v-model="form.name" placeholder="如 下单-库存不足拦截" />
        </el-form-item>
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="用例类型">
              <el-select v-model="form.scriptType" style="width: 100%">
                <el-option label="正向" value="POSITIVE" />
                <el-option label="异常" value="NEGATIVE" />
                <el-option label="边界" value="BOUNDARY" />
                <el-option label="性能" value="PERFORMANCE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行环境">
              <el-select v-model="form.envId" style="width: 100%" clearable>
                <el-option v-for="e in envs" :key="e.id" :label="e.name" :value="e.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请求配置">
          <el-input v-model="form.requestConfig" type="textarea" :rows="5"
            placeholder='{"method":"POST","path":"/api/v1/order/create","headers":{},"body":{}}' />
        </el-form-item>
        <el-form-item label="断言配置">
          <el-input v-model="form.assertions" type="textarea" :rows="4"
            placeholder='[{"type":"JSON_PATH","expression":"$.code","operator":"EQ","expected":"0"}]' />
        </el-form-item>
        <el-form-item label="变量提取">
          <el-input v-model="form.extractors" type="textarea" :rows="2"
            placeholder='[{"name":"token","type":"JSON_PATH","expression":"$.data.token"}]' />
        </el-form-item>
        <el-row :gutter="14">
          <el-col :span="16">
            <el-form-item label="变量定义">
              <el-input v-model="form.variables" placeholder='{"token":"xxx"}' />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="启用">
              <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 执行日志 -->
    <el-drawer v-model="logVisible" title="接口脚本执行日志" size="760px">
      <el-table :data="logs" size="small" v-loading="logLoading">
        <el-table-column prop="id" label="#" width="60" />
        <el-table-column prop="scriptName" label="脚本" min-width="170" show-overflow-tooltip />
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PASS' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusCode" label="状态码" width="80" />
        <el-table-column prop="durationMs" label="耗时" width="80" />
        <el-table-column prop="executedAt" label="执行时间" width="165" />
      </el-table>
      <div class="table-footer">
        <el-pagination v-model:current-page="logQuery.page" :page-size="logQuery.size" :total="logTotal"
          layout="total, prev, pager, next" @current-change="loadLogs" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Search, VideoPlay, Tickets } from '@element-plus/icons-vue'
import { scriptApi, apiInfoApi, envApi, projectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const rows = ref<any[]>([])
const projects = ref<any[]>([])
const apis = ref<any[]>([])
const envs = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const runningId = ref<number | null>(null)
const runEnvId = ref<number | undefined>()
const dialogVisible = ref(false)
const resultVisible = ref(false)
const configVisible = ref(false)
const logVisible = ref(false)
const result = ref<any>(null)
const current = ref<any>(null)
const formRef = ref<FormInstance>()

const logs = ref<any[]>([])
const logTotal = ref(0)
const logLoading = ref(false)
const logQuery = reactive({ page: 1, size: 12 })

const query = reactive<any>({
  page: 1, size: 10, keyword: '', projectId: userStore.currentProjectId, scriptType: ''
})

const form = reactive<any>({
  id: null, projectId: userStore.currentProjectId, apiId: null, name: '', scriptType: 'POSITIVE',
  envId: null, requestConfig: '', assertions: '[]', extractors: '[]', variables: '{}', enabled: 1
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  name: [{ required: true, message: '请输入脚本名称', trigger: 'blur' }]
}

const typeText = (v: string) =>
  ({ POSITIVE: '正向', NEGATIVE: '异常', BOUNDARY: '边界', PERFORMANCE: '性能' } as any)[v] || v

function pretty(raw: string) {
  if (!raw) return '（空）'
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  } catch {
    return raw
  }
}

async function load() {
  loading.value = true
  try {
    const data = await scriptApi.page(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function run(row: any) {
  runningId.value = row.id
  try {
    result.value = await scriptApi.run(row.id, runEnvId.value)
    resultVisible.value = true
    load()
  } finally {
    runningId.value = null
  }
}

async function runBatch() {
  const enabled = rows.value.filter((r) => r.enabled).map((r) => r.id)
  if (!enabled.length) {
    ElMessage.warning('当前页没有启用的脚本')
    return
  }
  await ElMessageBox.confirm(`将对当前页 ${enabled.length} 个脚本依次执行，确认继续？`, '批量执行', { type: 'info' })
  const res = await scriptApi.runBatch(enabled, runEnvId.value)
  ElMessage.success(`执行完成：成功 ${res.pass} 条，失败 ${res.fail} 条，通过率 ${res.passRate}%`)
  load()
}

function showConfig(row: any) {
  current.value = row
  configVisible.value = true
}

function openDialog(row?: any) {
  Object.assign(form, row || {
    id: null, projectId: query.projectId || userStore.currentProjectId, apiId: null, name: '',
    scriptType: 'POSITIVE', envId: envs.value[0]?.id || null,
    requestConfig: '{"method":"POST","path":"/api/v1/example","headers":{"Content-Type":"application/json"},"body":{}}',
    assertions: '[{"type":"STATUS_CODE","expected":"200","operator":"EQ","message":"HTTP 200"}]',
    extractors: '[]', variables: '{}', enabled: 1
  })
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await scriptApi.update(form.id, form)
      } else {
        await scriptApi.create(form)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除脚本「${row.name}」？`, '提示', { type: 'warning' })
  await scriptApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

async function openLogs() {
  logVisible.value = true
  logQuery.page = 1
  loadLogs()
}

async function loadLogs() {
  logLoading.value = true
  try {
    const data = await scriptApi.logs({ ...logQuery, projectId: query.projectId || undefined })
    logs.value = data.records
    logTotal.value = data.total
  } finally {
    logLoading.value = false
  }
}

onMounted(async () => {
  projects.value = await projectApi.options()
  apis.value = await apiInfoApi.all(query.projectId || undefined)
  envs.value = await envApi.list(query.projectId || undefined)
  // 不默认选中环境：留空时按脚本自身配置的环境执行
  load()
})
</script>
