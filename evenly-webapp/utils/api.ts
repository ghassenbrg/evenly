import { useRuntimeConfig, useCookie, navigateTo } from '#imports'

export interface ApiError {
  error?: string
  message?: string
  status?: number
}

export const useApi = () => {
  const config = useRuntimeConfig()
  const token = useCookie<string | null>('token', { default: () => null })
  const { $keycloak } = useNuxtApp()

  const getAuthToken = (): string | null => {
    // Prefer Keycloak token if available
    if (process.client && $keycloak && $keycloak.authenticated && $keycloak.token) {
      return $keycloak.token
    }
    // Fallback to cookie token
    return token.value
  }

  const request = async <T = any>(path: string, options: RequestInit = {}): Promise<T> => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string> || {})
    }
    
    const authToken = getAuthToken()
    if (authToken) {
      headers.Authorization = `Bearer ${authToken}`
    }

    try {
      const res = await fetch(`${config.public.apiBase}${path}`, { ...options, headers })
      
      if (!res.ok) {
        const error: ApiError = await res.json().catch(() => ({}))
        error.status = res.status
        
        // Only redirect to login on 401 for authenticated requests (not during login itself)
        if (res.status === 401 && path !== '/auth/login' && authToken) {
          token.value = null
          // If using Keycloak, logout and redirect to login
          if (process.client && $keycloak) {
            await $keycloak.logout({ redirectUri: window.location.origin + '/login' })
          } else {
            await navigateTo('/login')
          }
          throw new Error('Unauthorized')
        }
        
        const errorMessage = error.message || error.error || 'Request failed'
        const apiError = new Error(errorMessage)
        ;(apiError as any).status = res.status
        ;(apiError as any).error = error
        throw apiError
      }

      return res.status === 204 ? null as T : await res.json()
    } catch (err) {
      if (err instanceof Error && err.message === 'Unauthorized') {
        throw err
      }
      throw err
    }
  }

  const get = <T = any>(path: string) => request<T>(path, { method: 'GET' })
  const post = <T = any>(path: string, body?: any) => {
    const options: RequestInit = { method: 'POST' }
    if (body !== undefined && body !== null) {
      options.body = JSON.stringify(body)
    }
    return request<T>(path, options)
  }
  const put = <T = any>(path: string, body?: any) => request<T>(path, {
    method: 'PUT',
    body: body ? JSON.stringify(body) : undefined
  })
  const del = <T = any>(path: string) => request<T>(path, { method: 'DELETE' })

  return { request, get, post, put, delete: del, token: readonly(token) }
}
