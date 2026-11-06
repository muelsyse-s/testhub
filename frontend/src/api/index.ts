import request, { type PageData } from './request'

/* ---------------- 认证 ---------------- */
export const authApi = {
  login: (data: { username: string; password: string }) =>
    request.post<any, { token: string; id: number; username: string; realName: string; roles: string[] }>(
      '/auth/login',
      data
    ),
  me: () => request.get<any, any>('/auth/me'),
  logout: () => request.post<any, any>('/auth/logout')
}

/* ---------------- 用户 ---------------- */
export const userApi = {
  page: (params: any) => request.get<any, PageData<any>>('/users', { params }),
  roles: () => request.get<any, any[]>('/users/roles'),
  create: (data: any) => request.post<any, any>('/users', data),
  update: (id: number, data: any) => request.put<any, any>(`/users/${id}`, data),
  remove: (id: number) => request.delete<any, any>(`/users/${id}`),
  resetPassword: (id: number, password: string) =>
    request.put<any, any>(`/users/${id}/password`, null, { params: { password } })
}

/* ---------------- 项目 ---------------- */
export const projectApi = {
  page: (params: any) => request.get<any, PageData<any>>('/projects', { params }),
  options: () => request.get<any, any[]>('/projects/options'),
  create: (data: any) => request.post<any, any>('/projects', data),
  update: (id: number, data: any) => request.put<any, any>(`/projects/${id}`, data),
  remove: (id: number) => request.delete<any, any>(`/projects/${id}`)
}

/* ---------------- 测试点 ---------------- */
export const testPointApi = {
  page: (params: any) => request.get<any, PageData<any>>('/test-points', { params }),
  modules: (projectId?: number) =>
    request.get<any, string[]>('/test-points/modules', { params: { projectId } }),
  detail: (id: number) => request.get<any, any>(`/test-points/${id}`),
  create: (data: any) => request.post<any, any>('/test-points', data),
  update: (id: number, data: any) => request.put<any, any>(`/test-points/${id}`, data),
  remove: (id: number) => request.delete<any, any>(`/test-points/${id}`)
}

/* ---------------- 用例 ---------------- */
export const testCaseApi = {
  page: (params: any) => request.get<any, PageData<any>>('/test-cases', { params }),
  candidates: (projectId?: number) =>
    request.get<any, any[]>('/test-cases/candidates', { params: { projectId } }),
  detail: (id: number) => request.get<any, any>(`/test-cases/${id}`),
  create: (data: any) => request.post<any, any>('/test-cases', data),
  update: (id: number, data: any) => request.put<any, any>(`/test-cases/${id}`, data),
  copy: (id: number) => request.post<any, any>(`/test-cases/${id}/copy`),
  remove: (id: number) => request.delete<any, any>(`/test-cases/${id}`),
  batchRemove: (ids: number[]) => request.delete<any, any>('/test-cases/batch', { data: ids })
}

/* ---------------- 计划 ---------------- */
export const testPlanApi = {
  page: (params: any) => request.get<any, PageData<any>>('/test-plans', { params }),
  detail: (id: number) => request.get<any, any>(`/test-plans/${id}`),
  create: (data: any) => request.post<any, any>('/test-plans', data),
  update: (id: number, data: any) => request.put<any, any>(`/test-plans/${id}`, data),
  remove: (id: number) => request.delete<any, any>(`/test-plans/${id}`),
  cases: (id: number) => request.get<any, any[]>(`/test-plans/${id}/cases`),
  addCases: (id: number, data: any) => request.post<any, any>(`/test-plans/${id}/cases`, data),
  removeCase: (id: number, caseId: number) =>
    request.delete<any, any>(`/test-plans/${id}/cases/${caseId}`),
  progress: (id: number) => request.get<any, any>(`/test-plans/${id}/progress`)
}

/* ---------------- 执行 ---------------- */
export const executionApi = {
  page: (params: any) => request.get<any, PageData<any>>('/executions', { params }),
  record: (data: any) => request.post<any, any>('/executions', data),
  byCase: (caseId: number) => request.get<any, any[]>(`/executions/case/${caseId}`)
}

/* ---------------- 缺陷 ---------------- */
export const defectApi = {
  page: (params: any) => request.get<any, PageData<any>>('/defects', { params }),
  create: (data: any) => request.post<any, any>('/defects', data),
  update: (id: number, data: any) => request.put<any, any>(`/defects/${id}`, data),
  changeStatus: (id: number, status: string) =>
    request.put<any, any>(`/defects/${id}/status`, { status }),
  remove: (id: number) => request.delete<any, any>(`/defects/${id}`)
}

/* ---------------- 接口定义 ---------------- */
export const apiInfoApi = {
  page: (params: any) => request.get<any, PageData<any>>('/apis', { params }),
  all: (projectId?: number) => request.get<any, any[]>('/apis/all', { params: { projectId } }),
  create: (data: any) => request.post<any, any>('/apis', data),
  update: (id: number, data: any) => request.put<any, any>(`/apis/${id}`, data),
  remove: (id: number) => request.delete<any, any>(`/apis/${id}`)
}

/* ---------------- 接口脚本 ---------------- */
export const scriptApi = {
  page: (params: any) => request.get<any, PageData<any>>('/api-scripts', { params }),
  detail: (id: number) => request.get<any, any>(`/api-scripts/${id}`),
  create: (data: any) => request.post<any, any>('/api-scripts', data),
  update: (id: number, data: any) => request.put<any, any>(`/api-scripts/${id}`, data),
  remove: (id: number) => request.delete<any, any>(`/api-scripts/${id}`),
  run: (id: number, envId?: number) =>
    request.post<any, any>(`/api-scripts/${id}/run`, null, { params: { envId } }),
  runBatch: (scriptIds: number[], envId?: number) =>
    request.post<any, any>('/api-scripts/run-batch', { scriptIds, envId }),
  logs: (params: any) => request.get<any, PageData<any>>('/api-run-logs', { params }),
  variables: () => request.get<any, Record<string, string>>('/api-scripts/variables'),
  clearVariables: () => request.delete<any, any>('/api-scripts/variables')
}

/* ---------------- 环境 ---------------- */
export const envApi = {
  list: (projectId?: number) => request.get<any, any[]>('/environments', { params: { projectId } }),
  create: (data: any) => request.post<any, any>('/environments', data),
  update: (id: number, data: any) => request.put<any, any>(`/environments/${id}`, data),
  remove: (id: number) => request.delete<any, any>(`/environments/${id}`)
}

/* ---------------- 报表 ---------------- */
export const reportApi = {
  overview: (projectId?: number) =>
    request.get<any, any>('/report/overview', { params: { projectId } }),
  trend: (projectId?: number, days = 14) =>
    request.get<any, any[]>('/report/execution-trend', { params: { projectId, days } }),
  casePriority: (projectId?: number) =>
    request.get<any, any[]>('/report/case-priority', { params: { projectId } }),
  caseType: (projectId?: number) =>
    request.get<any, any[]>('/report/case-type', { params: { projectId } }),
  defectSeverity: (projectId?: number) =>
    request.get<any, any[]>('/report/defect-severity', { params: { projectId } }),
  defectStatus: (projectId?: number) =>
    request.get<any, any[]>('/report/defect-status', { params: { projectId } }),
  moduleCoverage: (projectId?: number) =>
    request.get<any, any[]>('/report/module-coverage', { params: { projectId } }),
  workload: (projectId?: number) =>
    request.get<any, any[]>('/report/workload', { params: { projectId } })
}
