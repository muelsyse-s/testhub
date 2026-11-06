import axios, { type AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'

/** 后端统一响应结构 */
export interface ApiResult<T = any> {
  code: number
  msg: string
  data: T
}

export interface PageData<T> {
  records: T[]
  total: number
  page: number
  size: number
}

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 60000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('testhub_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult
    if (body && typeof body.code === 'number' && body.code !== 0) {
      ElMessage.error(body.msg || '请求失败')
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    // 统一拆包：业务代码直接拿到 data，无需重复解构
    return (body?.data ?? null) as any
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('testhub_token')
      localStorage.removeItem('testhub_user')
      if (!location.hash.includes('/login')) {
        ElMessage.error('登录已过期，请重新登录')
        location.hash = '#/login'
      }
    } else if (status === 403) {
      ElMessage.error('没有访问该资源的权限')
    } else {
      ElMessage.error(error?.response?.data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
