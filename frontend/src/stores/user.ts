import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api'

export interface UserInfo {
  id: number
  username: string
  realName: string
  roles: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('testhub_token') || '')
  const user = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem('testhub_user') || 'null')
  )
  const currentProjectId = ref<number | null>(
    Number(localStorage.getItem('testhub_project') || 0) || null
  )

  async function login(username: string, password: string) {
    const data = await authApi.login({ username, password })
    token.value = data.token
    user.value = {
      id: data.id,
      username: data.username,
      realName: data.realName,
      roles: data.roles || []
    }
    localStorage.setItem('testhub_token', data.token)
    localStorage.setItem('testhub_user', JSON.stringify(user.value))
  }

  function setProject(id: number | null) {
    currentProjectId.value = id
    localStorage.setItem('testhub_project', String(id || ''))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('testhub_token')
    localStorage.removeItem('testhub_user')
  }

  function hasRole(...roles: string[]) {
    if (!user.value) return false
    return roles.some((r) => user.value!.roles.includes(r))
  }

  return { token, user, currentProjectId, login, logout, setProject, hasRole }
})
