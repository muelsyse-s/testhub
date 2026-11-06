<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">缺陷管理</div>
        <div class="page-subtitle">执行失败后登记缺陷，跟踪 待处理 → 修复中 → 已解决 → 已关闭</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">提交缺陷</el-button>
    </div>

    <div class="panel">
      <div class="filter-bar">
        <el-select v-model="query.projectId" placeholder="全部项目" clearable @change="load">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable @change="load">
          <el-option v-for="s in statuses" :key="s" :label="statusText(s)" :value="s" />
        </el-select>
        <el-select v-model="query.severity" placeholder="严重程度" clearable @change="load">
          <el-option v-for="s in severities" :key="s" :label="severityText(s)" :value="s" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="缺陷编号 / 标题" clearable :prefix-icon="Search"
          style="width: 220px" @keyup.enter="load" @clear="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="defectNo" label="缺陷编号" width="140">
          <template #default="{ row }"><span class="mono">{{ row.defectNo }}</span></template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
        <el-table-column label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)" size="small" effect="dark">
              {{ severityText(row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="指派给" width="100" />
        <el-table-column prop="reporterName" label="提交人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="detail(row)">详情</el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-dropdown @command="(cmd: string) => changeStatus(row, cmd)" trigger="click">
              <el-button link type="primary">流转<el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="s in statuses" :key="s" :command="s" :disabled="s === row.status">
                    {{ statusText(s) }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
          @current-change="load" @size-change="load" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑缺陷' : '提交缺陷'" width="680px" top="6vh">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="95px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" style="width: 100%" :disabled="!!form.id">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="缺陷标题" prop="title">
          <el-input v-model="form.title" placeholder="如 未支付订单超过 30 分钟未被自动取消" />
        </el-form-item>
        <el-form-item label="缺陷描述">
          <el-input v-model="form.description" type="textarea" :rows="4"
            placeholder="复现步骤 / 实际结果 / 期望结果 / 环境信息" />
        </el-form-item>
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="严重程度">
              <el-select v-model="form.severity" style="width: 100%">
                <el-option v-for="s in severities" :key="s" :label="severityText(s)" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="高" value="HIGH" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="指派给">
              <el-input v-model="form.assigneeName" placeholder="开发同学姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联用例ID">
              <el-input-number v-model="form.caseId" :min="0" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" title="缺陷详情" size="500px">
      <template v-if="current">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="缺陷编号">{{ current.defectNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ current.title }}</el-descriptions-item>
          <el-descriptions-item label="严重程度">{{ severityText(current.severity) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(current.status) }}</el-descriptions-item>
          <el-descriptions-item label="提交人">{{ current.reporterName }}</el-descriptions-item>
          <el-descriptions-item label="指派给">{{ current.assigneeName || '未指派' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ current.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="解决时间">{{ current.resolvedAt || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div class="section-title" style="margin-top: 20px">缺陷描述</div>
        <div class="desc-box">{{ current.description || '无' }}</div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Search, ArrowDown } from '@element-plus/icons-vue'
import { defectApi, projectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const rows = ref<any[]>([])
const projects = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const current = ref<any>(null)
const formRef = ref<FormInstance>()

const statuses = ['OPEN', 'FIXING', 'RESOLVED', 'CLOSED', 'REJECTED']
const severities = ['BLOCKER', 'CRITICAL', 'MAJOR', 'MINOR', 'TRIVIAL']

const query = reactive<any>({
  page: 1, size: 10, keyword: '', projectId: userStore.currentProjectId, status: '', severity: ''
})

const form = reactive<any>({
  id: null, projectId: userStore.currentProjectId, title: '', description: '',
  severity: 'MAJOR', priority: 'MEDIUM', assigneeName: '', caseId: undefined
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入缺陷标题', trigger: 'blur' }]
}

const statusText = (v: string) =>
  ({ OPEN: '待处理', FIXING: '修复中', RESOLVED: '已解决', CLOSED: '已关闭', REJECTED: '已拒绝' } as any)[v] || v
const statusTagType = (v: string) =>
  ({ OPEN: 'danger', FIXING: 'warning', RESOLVED: 'success', CLOSED: 'info', REJECTED: 'info' } as any)[v] || 'info'
const severityText = (v: string) =>
  ({ BLOCKER: '致命', CRITICAL: '严重', MAJOR: '一般', MINOR: '轻微', TRIVIAL: '提示' } as any)[v] || v
const severityType = (v: string) =>
  ({ BLOCKER: 'danger', CRITICAL: 'danger', MAJOR: 'warning', MINOR: 'info', TRIVIAL: 'info' } as any)[v] || 'info'

async function load() {
  loading.value = true
  try {
    const data = await defectApi.page(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.assign(form, row || {
    id: null, projectId: query.projectId || userStore.currentProjectId, title: '',
    description: '', severity: 'MAJOR', priority: 'MEDIUM', assigneeName: '', caseId: undefined
  })
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await defectApi.update(form.id, form)
      } else {
        await defectApi.create(form)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function changeStatus(row: any, status: string) {
  await defectApi.changeStatus(row.id, status)
  ElMessage.success(`缺陷状态已更新为「${statusText(status)}」`)
  load()
}

function detail(row: any) {
  current.value = row
  drawerVisible.value = true
}

onMounted(async () => {
  projects.value = await projectApi.options()
  load()
})
</script>

<style scoped>
.desc-box {
  background: #f7f8fc;
  border-radius: 6px;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.9;
  color: #384052;
  white-space: pre-wrap;
}
</style>
