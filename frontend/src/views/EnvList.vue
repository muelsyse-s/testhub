<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">环境管理</div>
        <div class="page-subtitle">按环境维护 baseUrl 与公共变量，脚本执行时自动拼接</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增环境</el-button>
    </div>

    <div class="panel">
      <div class="filter-bar">
        <el-select v-model="projectId" placeholder="全部项目" clearable @change="load">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="name" label="环境名称" width="150" />
        <el-table-column prop="baseUrl" label="Base URL" min-width="280">
          <template #default="{ row }"><span class="mono">{{ row.baseUrl }}</span></template>
        </el-table-column>
        <el-table-column prop="headers" label="公共请求头" min-width="240" show-overflow-tooltip>
          <template #default="{ row }"><span class="mono">{{ row.headers }}</span></template>
        </el-table-column>
        <el-table-column prop="variables" label="公共变量" min-width="180" show-overflow-tooltip>
          <template #default="{ row }"><span class="mono">{{ row.variables }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small" effect="plain">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑环境' : '新增环境'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="95px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" style="width: 100%" :disabled="!!form.id">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="环境名称" prop="name">
          <el-input v-model="form.name" placeholder="如 测试环境" />
        </el-form-item>
        <el-form-item label="Base URL" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="http://test-mall.testhub.cn:8080" />
        </el-form-item>
        <el-form-item label="公共请求头">
          <el-input v-model="form.headers" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="公共变量">
          <el-input v-model="form.variables" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
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
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { envApi, projectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const rows = ref<any[]>([])
const projects = ref<any[]>([])
const projectId = ref<number | undefined>(userStore.currentProjectId || undefined)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<any>({
  id: null, projectId: projectId.value, name: '', baseUrl: '',
  headers: '{"Content-Type":"application/json"}', variables: '{}', enabled: 1
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  name: [{ required: true, message: '请输入环境名称', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入 Base URL', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    rows.value = await envApi.list(projectId.value)
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.assign(form, row || {
    id: null, projectId: projectId.value, name: '', baseUrl: '',
    headers: '{"Content-Type":"application/json"}', variables: '{}', enabled: 1
  })
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await envApi.update(form.id, form)
      } else {
        await envApi.create(form)
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
  await ElMessageBox.confirm(`确认删除环境「${row.name}」？`, '提示', { type: 'warning' })
  await envApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  projects.value = await projectApi.options()
  load()
})
</script>
