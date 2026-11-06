<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">接口定义</div>
        <div class="page-subtitle">维护被测系统的接口清单，是接口自动化的数据来源</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增接口</el-button>
    </div>

    <div class="panel">
      <div class="filter-bar">
        <el-select v-model="query.projectId" placeholder="全部项目" clearable @change="load">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select v-model="query.method" placeholder="请求方法" clearable @change="load">
          <el-option v-for="m in ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']" :key="m" :label="m" :value="m" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="接口名称 / 路径" clearable :prefix-icon="Search"
          style="width: 230px" @keyup.enter="load" @clear="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column label="方法" width="90">
          <template #default="{ row }">
            <el-tag :type="methodType(row.method)" size="small" class="tag-method">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="240">
          <template #default="{ row }"><span class="mono">{{ row.path }}</span></template>
        </el-table-column>
        <el-table-column prop="name" label="接口名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="bodyType" label="Body 类型" width="110" />
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑接口' : '新增接口'" width="720px" top="6vh">
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
            <el-form-item label="模块" prop="module">
              <el-input v-model="form.module" placeholder="如 订单中心" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="接口名称" prop="name">
          <el-input v-model="form.name" placeholder="如 创建订单" />
        </el-form-item>
        <el-row :gutter="14">
          <el-col :span="8">
            <el-form-item label="请求方法" prop="method">
              <el-select v-model="form.method" style="width: 100%">
                <el-option v-for="m in ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="请求路径" prop="path">
              <el-input v-model="form.path" placeholder="/api/v1/order/create" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请求头">
          <el-input v-model="form.headers" type="textarea" :rows="2" placeholder='{"Content-Type":"application/json"}' />
        </el-form-item>
        <el-form-item label="Body 类型">
          <el-select v-model="form.bodyType" style="width: 180px">
            <el-option label="JSON" value="JSON" />
            <el-option label="FORM" value="FORM" />
            <el-option label="RAW" value="RAW" />
            <el-option label="无" value="NONE" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求体">
          <el-input v-model="form.body" type="textarea" :rows="4" placeholder='{"skuId":1001,"quantity":2}' />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
import { Plus, Search } from '@element-plus/icons-vue'
import { apiInfoApi, projectApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const rows = ref<any[]>([])
const projects = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const query = reactive<any>({
  page: 1, size: 10, keyword: '', projectId: userStore.currentProjectId, method: ''
})

const form = reactive<any>({
  id: null, projectId: userStore.currentProjectId, module: '', name: '', method: 'POST',
  path: '', headers: '{"Content-Type":"application/json"}', bodyType: 'JSON', body: '', description: '',
  status: 'ACTIVE'
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  module: [{ required: true, message: '请输入模块', trigger: 'blur' }],
  name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入请求路径', trigger: 'blur' }]
}

const methodType = (m: string) =>
  ({ GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger', PATCH: 'info' } as any)[m] || 'info'

async function load() {
  loading.value = true
  try {
    const data = await apiInfoApi.page(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.assign(form, row || {
    id: null, projectId: query.projectId || userStore.currentProjectId, module: '', name: '',
    method: 'POST', path: '', headers: '{"Content-Type":"application/json"}', bodyType: 'JSON',
    body: '', description: '', status: 'ACTIVE'
  })
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await apiInfoApi.update(form.id, form)
      } else {
        await apiInfoApi.create(form)
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
  await ElMessageBox.confirm(`确认删除接口「${row.name}」？`, '提示', { type: 'warning' })
  await apiInfoApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  projects.value = await projectApi.options()
  load()
})
</script>
