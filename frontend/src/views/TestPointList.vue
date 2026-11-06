<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">测试点管理</div>
        <div class="page-subtitle">一条需求拆成多个测试点，测试点是「测什么」的最小单元</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增测试点</el-button>
    </div>

    <div class="panel">
      <div class="filter-bar">
        <el-select v-model="query.projectId" placeholder="全部项目" clearable @change="load">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="query.module" placeholder="模块" clearable @change="load">
          <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
        </el-select>
        <el-select v-model="query.priority" placeholder="优先级" clearable @change="load">
          <el-option label="高" value="HIGH" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="低" value="LOW" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable @change="load">
          <el-option label="设计中" value="DRAFT" />
          <el-option label="已就绪" value="READY" />
          <el-option label="已废弃" value="OBSOLETE" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="编号 / 标题 / 描述" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" style="width: 220px" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="pointNo" label="测试点编号" width="140">
          <template #default="{ row }"><span class="mono">{{ row.pointNo }}</span></template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="title" label="测试点标题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="description" label="设计说明" min-width="280" show-overflow-tooltip />
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small" effect="plain">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险" width="80">
          <template #default="{ row }">
            <span :style="{ color: riskColor(row.riskLevel) }">{{ priorityText(row.riskLevel) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用例数" width="85" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">{{ row.caseCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="95" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="toCases(row)">用例</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑测试点' : '新增测试点'" width="640px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="95px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" style="width: 100%" placeholder="请选择项目" :disabled="!!form.id">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块" prop="module">
          <el-input v-model="form.module" placeholder="如 订单中心" />
        </el-form-item>
        <el-form-item label="测试点编号">
          <el-input v-model="form.pointNo" placeholder="留空自动生成，如 TP-1-005" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="一句话描述要验证的点，如 提交订单库存扣减" />
        </el-form-item>
        <el-form-item label="设计说明">
          <el-input v-model="form.description" type="textarea" :rows="3"
            placeholder="说明验证逻辑、边界条件、异常场景" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio-button label="HIGH">高</el-radio-button>
            <el-radio-button label="MEDIUM">中</el-radio-button>
            <el-radio-button label="LOW">低</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-radio-group v-model="form.riskLevel">
            <el-radio-button label="HIGH">高</el-radio-button>
            <el-radio-button label="MEDIUM">中</el-radio-button>
            <el-radio-button label="LOW">低</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="form.sourceType" style="width: 100%">
            <el-option label="需求文档" value="REQUIREMENT" />
            <el-option label="安全专项" value="SECURITY" />
            <el-option label="性能专项" value="PERFORMANCE" />
            <el-option label="线上问题" value="INCIDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="设计中" value="DRAFT" />
            <el-option label="已就绪" value="READY" />
            <el-option label="已废弃" value="OBSOLETE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { testPointApi, projectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const rows = ref<any[]>([])
const projects = ref<any[]>([])
const modules = ref<string[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const query = reactive<any>({
  page: 1, size: 10, keyword: '', projectId: userStore.currentProjectId, module: '', priority: '', status: ''
})

const form = reactive<any>({
  id: null, projectId: userStore.currentProjectId, module: '', pointNo: '', title: '',
  description: '', priority: 'MEDIUM', riskLevel: 'MEDIUM', sourceType: 'REQUIREMENT', status: 'DRAFT'
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  module: [{ required: true, message: '请输入模块名', trigger: 'blur' }],
  title: [{ required: true, message: '请输入测试点标题', trigger: 'blur' }]
}

const priorityText = (v: string) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' } as any)[v] || v
const priorityType = (v: string) => ({ HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' } as any)[v] || 'info'
const statusText = (v: string) => ({ DRAFT: '设计中', READY: '已就绪', OBSOLETE: '已废弃' } as any)[v] || v
const statusType = (v: string) => ({ DRAFT: 'info', READY: 'success', OBSOLETE: 'danger' } as any)[v] || 'info'
const riskColor = (v: string) => ({ HIGH: '#dc2626', MEDIUM: '#d97706', LOW: '#6b7280' } as any)[v]

async function load() {
  loading.value = true
  try {
    const data = await testPointApi.page(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadModules() {
  modules.value = await testPointApi.modules(query.projectId || undefined)
}

function openDialog(row?: any) {
  Object.assign(form, row || {
    id: null, projectId: query.projectId || userStore.currentProjectId, module: '', pointNo: '',
    title: '', description: '', priority: 'MEDIUM', riskLevel: 'MEDIUM',
    sourceType: 'REQUIREMENT', status: 'DRAFT'
  })
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await testPointApi.update(form.id, form)
      } else {
        await testPointApi.create(form)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      await load()
      await loadModules()
    } finally {
      saving.value = false
    }
  })
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除测试点「${row.title}」？`, '提示', { type: 'warning' })
  await testPointApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

function toCases(row: any) {
  router.push({ path: '/test-cases', query: { testPointId: row.id } })
}

onMounted(async () => {
  projects.value = await projectApi.options()
  await load()
  await loadModules()
})
</script>
