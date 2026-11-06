<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">测试计划</div>
        <div class="page-subtitle">把用例编排进计划，指派给执行人，跟踪执行进度</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新建计划</el-button>
    </div>

    <div class="panel">
      <div class="filter-bar">
        <el-select v-model="query.projectId" placeholder="全部项目" clearable @change="load">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable @change="load">
          <el-option label="规划中" value="PLANNING" />
          <el-option label="执行中" value="RUNNING" />
          <el-option label="已完成" value="FINISHED" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="计划名称" clearable :prefix-icon="Search" style="width: 220px"
          @keyup.enter="load" @clear="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="name" label="计划名称" min-width="230" show-overflow-tooltip />
        <el-table-column prop="description" label="说明" min-width="230" show-overflow-tooltip />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="进度" width="190">
          <template #default="{ row }">
            <el-progress
              :percentage="progressMap[row.id]?.progress || 0"
              :stroke-width="10"
              :color="progressMap[row.id]?.progress === 100 ? '#16a34a' : '#4f46e5'"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="toDetail(row)">用例与执行</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑计划' : '新建计划'" width="580px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" style="width: 100%" :disabled="!!form.id">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="form.name" placeholder="如 V2.0 迭代一 系统测试" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="测试范围、准入准出条件" />
        </el-form-item>
        <el-form-item label="计划周期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="规划中" value="PLANNING" />
            <el-option label="执行中" value="RUNNING" />
            <el-option label="已完成" value="FINISHED" />
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { testPlanApi, projectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const rows = ref<any[]>([])
const projects = ref<any[]>([])
const progressMap = ref<Record<number, any>>({})
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const dateRange = ref<string[]>([])

const query = reactive<any>({
  page: 1, size: 10, keyword: '', projectId: userStore.currentProjectId, status: ''
})

const form = reactive<any>({
  id: null, projectId: userStore.currentProjectId, name: '', description: '',
  startDate: '', endDate: '', status: 'PLANNING'
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }]
}

const statusText = (v: string) => ({ PLANNING: '规划中', RUNNING: '执行中', FINISHED: '已完成' } as any)[v] || v
const statusType = (v: string) => ({ PLANNING: 'info', RUNNING: 'warning', FINISHED: 'success' } as any)[v] || 'info'

watch(dateRange, (val) => {
  form.startDate = val?.[0] || ''
  form.endDate = val?.[1] || ''
})

async function load() {
  loading.value = true
  try {
    const data = await testPlanApi.page(query)
    rows.value = data.records
    total.value = data.total
    const entries = await Promise.all(
      data.records.map(async (row: any) => [row.id, await testPlanApi.progress(row.id)])
    )
    progressMap.value = Object.fromEntries(entries)
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.assign(form, row || {
    id: null, projectId: query.projectId || userStore.currentProjectId, name: '',
    description: '', startDate: '', endDate: '', status: 'PLANNING'
  })
  dateRange.value = row && row.startDate ? [row.startDate, row.endDate] : []
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await testPlanApi.update(form.id, form)
      } else {
        await testPlanApi.create(form)
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
  await ElMessageBox.confirm(`确认删除计划「${row.name}」？计划下的用例关联也会被移除。`, '提示', { type: 'warning' })
  await testPlanApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

function toDetail(row: any) {
  router.push(`/test-plans/${row.id}`)
}

onMounted(async () => {
  projects.value = await projectApi.options()
  load()
})
</script>
