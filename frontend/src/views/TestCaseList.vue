<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">测试用例</div>
        <div class="page-subtitle">支持步骤化编写、批量选择与用例复制，沉淀可复用的测试资产</div>
      </div>
      <div style="display: flex; gap: 8px">
        <el-button :disabled="!selection.length" :icon="CopyDocument" @click="batchDelete">
          批量删除{{ selection.length ? `(${selection.length})` : '' }}
        </el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新建用例</el-button>
      </div>
    </div>

    <div class="panel">
      <div class="filter-bar">
        <el-select v-model="query.projectId" placeholder="全部项目" clearable @change="load">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="query.priority" placeholder="优先级" clearable @change="load">
          <el-option label="高" value="HIGH" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="低" value="LOW" />
        </el-select>
        <el-select v-model="query.caseType" placeholder="用例类型" clearable @change="load">
          <el-option label="功能" value="FUNCTION" />
          <el-option label="接口" value="INTERFACE" />
          <el-option label="性能" value="PERFORMANCE" />
          <el-option label="安全" value="SECURITY" />
          <el-option label="界面" value="UI" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable @change="load">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="就绪" value="READY" />
          <el-option label="废弃" value="OBSOLETE" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="编号 / 标题 / 预期" clearable :prefix-icon="Search"
          style="width: 230px" @keyup.enter="load" @clear="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <div v-if="query.testPointId" style="margin-bottom: 10px">
        <el-alert :title="`当前仅显示测试点 #${query.testPointId} 下的用例`" type="info" :closable="true"
          show-icon @close="clearPoint" />
      </div>

      <el-table :data="rows" v-loading="loading" stripe @selection-change="(v: any[]) => (selection = v)">
        <el-table-column type="selection" width="44" />
        <el-table-column prop="caseNo" label="用例编号" width="130">
          <template #default="{ row }"><span class="mono">{{ row.caseNo }}</span></template>
        </el-table-column>
        <el-table-column prop="title" label="用例标题" min-width="250" show-overflow-tooltip />
        <el-table-column label="优先级" width="86">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small" effect="plain">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="86">
          <template #default="{ row }">{{ typeText(row.caseType) }}</template>
        </el-table-column>
        <el-table-column label="设计方法" width="105">
          <template #default="{ row }">{{ methodText(row.designMethod) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="86">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="自动化" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.autoFlag" color="#16a34a"><CircleCheck /></el-icon>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="编写人" width="95" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="preview(row)">查看</el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" @click="copy(row)">复制</el-button>
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

    <!-- 编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用例' : '新建用例'" width="760px" top="6vh">
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
            <el-form-item label="关联测试点">
              <el-select v-model="form.testPointId" style="width: 100%" clearable placeholder="可选">
                <el-option v-for="tp in points" :key="tp.id" :label="`${tp.pointNo} ${tp.title}`" :value="tp.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="用例标题" prop="title">
          <el-input v-model="form.title" placeholder="如 库存不足时下单被拦截" />
        </el-form-item>
        <el-form-item label="用例编号">
          <el-input v-model="form.caseNo" placeholder="留空自动生成，如 TC-1-021" />
        </el-form-item>
        <el-form-item label="前置条件">
          <el-input v-model="form.precondition" type="textarea" :rows="2"
            placeholder="如：用户已登录；商品 A 库存为 3" />
        </el-form-item>

        <el-form-item label="测试步骤">
          <div style="width: 100%">
            <div v-for="(step, index) in steps" :key="index" class="step-row">
              <span class="step-index">{{ index + 1 }}</span>
              <el-input v-model="steps[index]" placeholder="描述一步操作" />
              <el-button link type="danger" :icon="Delete" @click="steps.splice(index, 1)" />
            </div>
            <el-button link type="primary" :icon="Plus" @click="steps.push('')">添加步骤</el-button>
          </div>
        </el-form-item>

        <el-form-item label="预期结果">
          <el-input v-model="form.expectedResult" type="textarea" :rows="3"
            placeholder="每条预期尽量可判定，如：提示“库存不足”，购物车数量不变" />
        </el-form-item>

        <el-row :gutter="14">
          <el-col :span="8">
            <el-form-item label="优先级">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="高" value="HIGH" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用例类型">
              <el-select v-model="form.caseType" style="width: 100%">
                <el-option label="功能" value="FUNCTION" />
                <el-option label="接口" value="INTERFACE" />
                <el-option label="性能" value="PERFORMANCE" />
                <el-option label="安全" value="SECURITY" />
                <el-option label="界面" value="UI" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="就绪" value="READY" />
                <el-option label="废弃" value="OBSOLETE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="设计方法">
              <el-select v-model="form.designMethod" style="width: 100%" clearable>
                <el-option label="等价类划分" value="EQUIVALENCE" />
                <el-option label="边界值分析" value="BOUNDARY" />
                <el-option label="场景法" value="SCENARIO" />
                <el-option label="错误推测" value="ERROR_GUESS" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否自动化">
              <el-switch v-model="form.autoFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看 -->
    <el-drawer v-model="drawerVisible" title="用例详情" size="520px">
      <template v-if="current">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="用例编号">{{ current.caseNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ current.title }}</el-descriptions-item>
          <el-descriptions-item label="前置条件">{{ current.precondition || '无' }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ priorityText(current.priority) }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeText(current.caseType) }}</el-descriptions-item>
          <el-descriptions-item label="编写人">{{ current.creatorName }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title" style="margin-top: 20px">测试步骤</div>
        <ol class="step-list">
          <li v-for="(s, i) in parseSteps(current.steps)" :key="i">{{ s }}</li>
        </ol>

        <div class="section-title" style="margin-top: 20px">预期结果</div>
        <div class="expect-box">{{ current.expectedResult }}</div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Search, Delete, CopyDocument, CircleCheck } from '@element-plus/icons-vue'
import { testCaseApi, testPointApi, projectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const rows = ref<any[]>([])
const projects = ref<any[]>([])
const points = ref<any[]>([])
const selection = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const current = ref<any>(null)
const formRef = ref<FormInstance>()
const steps = ref<string[]>([''])

const query = reactive<any>({
  page: 1, size: 10, keyword: '', projectId: userStore.currentProjectId,
  priority: '', caseType: '', status: '',
  testPointId: route.query.testPointId ? Number(route.query.testPointId) : undefined
})

const form = reactive<any>({
  id: null, projectId: userStore.currentProjectId, testPointId: null, caseNo: '', title: '',
  precondition: '', steps: '', expectedResult: '', priority: 'MEDIUM', caseType: 'FUNCTION',
  designMethod: 'EQUIVALENCE', status: 'DRAFT', autoFlag: 0
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入用例标题', trigger: 'blur' }]
}

const priorityText = (v: string) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' } as any)[v] || v
const priorityType = (v: string) => ({ HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' } as any)[v] || 'info'
const typeText = (v: string) => ({ FUNCTION: '功能', INTERFACE: '接口', PERFORMANCE: '性能', SECURITY: '安全', UI: '界面' } as any)[v] || v
const methodText = (v: string) => ({ EQUIVALENCE: '等价类', BOUNDARY: '边界值', SCENARIO: '场景法', ERROR_GUESS: '错误推测' } as any)[v] || '—'
const statusText = (v: string) => ({ DRAFT: '草稿', READY: '就绪', OBSOLETE: '废弃' } as any)[v] || v
const statusType = (v: string) => ({ DRAFT: 'info', READY: 'success', OBSOLETE: 'danger' } as any)[v] || 'info'

function parseSteps(raw: string): string[] {
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr : [String(raw)]
  } catch {
    return [raw]
  }
}

async function load() {
  loading.value = true
  try {
    const data = await testCaseApi.page(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function clearPoint() {
  query.testPointId = undefined
  load()
}

async function openDialog(row?: any) {
  Object.assign(form, row || {
    id: null, projectId: query.projectId || userStore.currentProjectId, testPointId: query.testPointId || null,
    caseNo: '', title: '', precondition: '', steps: '', expectedResult: '', priority: 'MEDIUM',
    caseType: 'FUNCTION', designMethod: 'EQUIVALENCE', status: 'DRAFT', autoFlag: 0
  })
  steps.value = row ? parseSteps(row.steps) : ['']
  if (!steps.value.length) steps.value = ['']
  dialogVisible.value = true
  if (form.projectId) {
    points.value = (await testPointApi.page({ page: 1, size: 200, projectId: form.projectId })).records
  }
}

async function submit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    form.steps = JSON.stringify(steps.value.filter((s) => s && s.trim()))
    saving.value = true
    try {
      if (form.id) {
        await testCaseApi.update(form.id, form)
      } else {
        await testCaseApi.create(form)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function copy(row: any) {
  await testCaseApi.copy(row.id)
  ElMessage.success('已复制为新草稿用例')
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除用例「${row.title}」？`, '提示', { type: 'warning' })
  await testCaseApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

async function batchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${selection.value.length} 条用例？`, '提示', { type: 'warning' })
  await testCaseApi.batchRemove(selection.value.map((s) => s.id))
  ElMessage.success('已批量删除')
  selection.value = []
  load()
}

function preview(row: any) {
  current.value = row
  drawerVisible.value = true
}

onMounted(async () => {
  projects.value = await projectApi.options()
  if (route.query.testPointId) {
    points.value = (await testPointApi.page({ page: 1, size: 200, projectId: query.projectId })).records
  }
  load()
})
</script>

<style scoped>
.step-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.step-index {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #eef0f7;
  color: #5c6373;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  line-height: 1.9;
  color: #384052;
}

.expect-box {
  background: #f7f8fc;
  border-left: 3px solid #4f46e5;
  border-radius: 4px;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
