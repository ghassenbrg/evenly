import { defineStore } from 'pinia'
import type { User } from '~/types/api'
import { useAuth } from '~/composables/useAuth'

export const useAuthStore = defineStore('auth', () => {
  const { user, isAuthenticated, login, register, logout: logoutComposable } = useAuth()

  const currentUser = computed(() => user.value)
  const authenticated = computed(() => isAuthenticated.value)

  const loginUser = async (username: string, password: string) => {
    await login({ username, password })
  }

  const registerUser = async (email: string, password: string, displayName: string, username: string, preferredCurrency: string) => {
    await register({ email, password, displayName, username, preferredCurrency })
  }

  const logout = () => {
    logoutComposable()
  }

  return {
    currentUser,
    authenticated,
    loginUser,
    registerUser,
    logout
  }
})

