import type { AuthResponse, RegisterRequest, LoginRequest, User } from '~/types/api'
import { useApi } from '~/utils/api'
import { useCookie } from '#imports'

export const useAuth = () => {
  const api = useApi()
  const token = useCookie<string | null>('token', { default: () => null })
  const user = useCookie<User | null>('user', { default: () => null })

  const register = async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/register', data)
    token.value = response.token
    user.value = response.user
    return response
  }

  const login = async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', data)
    token.value = response.token
    user.value = response.user
    return response
  }

  const logout = () => {
    token.value = null
    user.value = null
  }

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  return {
    user: readonly(user),
    token: readonly(token),
    isAuthenticated,
    register,
    login,
    logout
  }
}

