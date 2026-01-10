import { useRuntimeConfig, useCookie, navigateTo } from '#imports'

export interface ApiError {
  error?: string
  message?: string
  status?: number
  errors?: Array<{
    field: string
    message: string
  }>
}

interface KeycloakTokenResponse {
  access_token: string
  refresh_token?: string
  expires_in: number
  refresh_expires_in: number
  token_type: string
}

const refreshKeycloakToken = async (config: any, refreshToken: string): Promise<KeycloakTokenResponse | null> => {
  try {
    const keycloakUrl = config.public.keycloak.url
    const realm = config.public.keycloak.realm
    const clientId = config.public.keycloak.clientId
    const tokenEndpoint = `${keycloakUrl}/realms/${realm}/protocol/openid-connect/token`

    const formData = new URLSearchParams()
    formData.append('grant_type', 'refresh_token')
    formData.append('client_id', clientId)
    formData.append('refresh_token', refreshToken)

    const response = await fetch(tokenEndpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: formData.toString()
    })

    if (!response.ok) {
      return null
    }

    return await response.json()
  } catch (error) {
    console.error('Token refresh failed:', error)
    return null
  }
}

export const useApi = (baseOverride?: string) => {
  const config = useRuntimeConfig()
  const token = useCookie<string | null>('token', { default: () => null })
  const refreshToken = useCookie<string | null>('refreshToken', { default: () => null })

  const getAuthToken = (): string | null => {
    // Use Keycloak token from cookie
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
      const base = baseOverride || config.public.apiBase
      const res = await fetch(`${base}${path}`, { ...options, headers })
      
      if (!res.ok) {
        const errorData: ApiError = await res.json().catch(() => ({}))
        errorData.status = res.status
        
        // Handle 401 - try to refresh token if we have one
        if (res.status === 401 && path !== '/auth/login' && path !== '/auth/register' && authToken && refreshToken.value) {
          // Try to refresh the token
          const tokenData = await refreshKeycloakToken(config, refreshToken.value)
          
          if (tokenData) {
            // Update tokens
            token.value = tokenData.access_token
            if (tokenData.refresh_token) {
              refreshToken.value = tokenData.refresh_token
            }
            
            // Retry the request with the new token
            headers.Authorization = `Bearer ${tokenData.access_token}`
            const retryRes = await fetch(`${base}${path}`, { ...options, headers })
            if (retryRes.ok) {
              if (retryRes.status === 204) {
                return null as T
              }
              const json = await retryRes.json()
              // Handle response extraction (same logic as below)
              if (json && typeof json === 'object') {
                if (path === '/api/notifications/unread-count' && 'data' in json && typeof json.data === 'object' && 'count' in json.data) {
                  return { unreadCount: json.data.count } as T
                }
                if (path === '/api/notifications' && 'data' in json && 'unreadCount' in json) {
                  return json as T
                }
                if (('data' in json && Array.isArray(json.data)) && ('page' in json || 'sort' in json)) {
                  return json as T
                }
                if (path.includes('/analytics/expenses-snapshot') && 'data' in json && 'categoriesCount' in json) {
                  return json as T
                }
                if ('data' in json) {
                  return json.data as T
                }
                if ('workspace' in json) {
                  return json.workspace as T
                }
              }
              return json as T
            }
          }
          
          // Refresh failed or retry failed, clear tokens and redirect to login
          token.value = null
          refreshToken.value = null
          await navigateTo('/login')
          throw new Error('Unauthorized')
        }
        
        // Handle API error schema from endpoints.json
        const errorMessage = errorData.message || errorData.error || 'Request failed'
        const apiError = new Error(errorMessage)
        ;(apiError as any).status = res.status
        ;(apiError as any).error = errorData
        ;(apiError as any).errors = errorData.errors // Validation errors array
        throw apiError
      }

      if (res.status === 204) {
        return null as T
      }
      
      const json = await res.json()
      
      // Extract 'data' property if present (matches endpoints.json structure)
      // Also handle 'workspace' property for PUT /api/workspaces/{id}
      if (json && typeof json === 'object') {
        // Special handling for /api/notifications/unread-count which returns { data: { count: ... } }
        // but frontend expects { unreadCount: ... }
        if (path === '/api/notifications/unread-count' && 'data' in json && typeof json.data === 'object' && 'count' in json.data) {
          return { unreadCount: json.data.count } as T
        }
        
        // For /api/notifications, return full response (has both data and unreadCount)
        if (path === '/api/notifications' && 'data' in json && 'unreadCount' in json) {
          return json as T
        }
        
        // For paginated responses (expenses, payments), return full response with data, page, sort
        if (('data' in json && Array.isArray(json.data)) && ('page' in json || 'sort' in json)) {
          return json as T
        }
        
        // For expense snapshot, return full response (has data, categoriesCount, remainingCategoriesCount)
        if (path.includes('/analytics/expenses-snapshot') && 'data' in json && 'categoriesCount' in json) {
          return json as T
        }
        
        if ('data' in json) {
          return json.data as T
        }
        if ('workspace' in json) {
          return json.workspace as T
        }
      }
      
      return json as T
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
