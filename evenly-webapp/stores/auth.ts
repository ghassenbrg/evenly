import { defineStore } from 'pinia'
import type { User } from '~/types/api'
import { useAuth } from '~/composables/useAuth'

export const useAuthStore = defineStore('auth', () => {
  const { user, isAuthenticated, login, logout: logoutComposable } = useAuth()

  const currentUser = computed(() => user.value)
  const authenticated = computed(() => isAuthenticated.value)

  const loginUser = async () => {
    await login()
  }

  const logout = async () => {
    await logoutComposable()
  }

  return {
    currentUser,
    authenticated,
    loginUser,
    logout
  }
})
