import type { User, LoginRequest, RegisterRequest, AuthResponse } from '~/types/api'
import { useCookie, navigateTo, useRuntimeConfig } from '#imports'

interface KeycloakTokenResponse {
  access_token: string
  refresh_token: string
  expires_in: number
  refresh_expires_in: number
  token_type: string
}

export const useAuth = () => {
  const config = useRuntimeConfig()
  const token = useCookie<string | null>('token', { default: () => null })
  const refreshToken = useCookie<string | null>('refreshToken', { default: () => null })
  const user = useCookie<User | null>('user', { default: () => null })
  const api = useApi()

  const getKeycloakTokenEndpoint = () => {
    const keycloakUrl = config.public.keycloak.url
    const realm = config.public.keycloak.realm
    return `${keycloakUrl}/realms/${realm}/protocol/openid-connect/token`
  }

  const login = async (credentials: LoginRequest, redirectPath?: string) => {
    try {
      const tokenEndpoint = getKeycloakTokenEndpoint()
      const clientId = config.public.keycloak.clientId

      // Call Keycloak token endpoint with password grant
      const formData = new URLSearchParams()
      formData.append('grant_type', 'password')
      formData.append('client_id', clientId)
      formData.append('username', credentials.username)
      formData.append('password', credentials.password)

      const response = await fetch(tokenEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData.toString()
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        const error = new Error(errorData.error_description || errorData.error || 'Login failed')
        ;(error as any).status = response.status
        throw error
      }

      const tokenData: KeycloakTokenResponse = await response.json()
      
      // Store tokens
      token.value = tokenData.access_token
      refreshToken.value = tokenData.refresh_token
      
      // Extract user info from token
      await loadUserProfile()
      
      // Handle redirect
      const safeRedirectPath = redirectPath && redirectPath.startsWith('/') ? redirectPath : '/dashboard'
      if (process.client) {
        sessionStorage.removeItem('postLoginRedirect')
      }
      await navigateTo(safeRedirectPath, { replace: true })
      
      return {
        token: tokenData.access_token,
        user: user.value!
      } as AuthResponse
    } catch (error: any) {
      console.error('Login error:', error)
      throw error
    }
  }

  const register = async (data: RegisterRequest, redirectPath?: string) => {
    try {
      const response = await api.post<AuthResponse>('/auth/register', data)
      
      // Store token and user info
      token.value = response.token
      user.value = response.user
      
      // Handle redirect
      const safeRedirectPath = redirectPath && redirectPath.startsWith('/') ? redirectPath : '/dashboard'
      if (process.client) {
        sessionStorage.removeItem('postLoginRedirect')
      }
      await navigateTo(safeRedirectPath, { replace: true })
      
      return response
    } catch (error: any) {
      console.error('Registration error:', error)
      throw error
    }
  }

  const logout = async () => {
    // Optionally call Keycloak logout endpoint
    if (refreshToken.value && process.client) {
      try {
        const keycloakUrl = config.public.keycloak.url
        const realm = config.public.keycloak.realm
        const logoutEndpoint = `${keycloakUrl}/realms/${realm}/protocol/openid-connect/logout`
        const clientId = config.public.keycloak.clientId

        const formData = new URLSearchParams()
        formData.append('client_id', clientId)
        formData.append('refresh_token', refreshToken.value)

        await fetch(logoutEndpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: formData.toString()
        }).catch(() => {
          // Ignore logout errors
        })
      } catch (error) {
        // Ignore logout errors
        console.error('Logout error:', error)
      }
    }

    token.value = null
    refreshToken.value = null
    user.value = null
    
    // Navigate to login page
    await navigateTo('/login', { replace: true })
  }

  const updateToken = async () => {
    if (!refreshToken.value) {
      return false
    }

    try {
      const tokenEndpoint = getKeycloakTokenEndpoint()
      const clientId = config.public.keycloak.clientId

      // Call Keycloak token endpoint with refresh_token grant
      const formData = new URLSearchParams()
      formData.append('grant_type', 'refresh_token')
      formData.append('client_id', clientId)
      formData.append('refresh_token', refreshToken.value)

      const response = await fetch(tokenEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData.toString()
      })

      if (!response.ok) {
        // Refresh token expired or invalid, clear tokens
        token.value = null
        refreshToken.value = null
        user.value = null
        return false
      }

      const tokenData: KeycloakTokenResponse = await response.json()
      
      // Update tokens
      token.value = tokenData.access_token
      if (tokenData.refresh_token) {
        refreshToken.value = tokenData.refresh_token
      }
      
      // Update user profile if needed
      await loadUserProfile()
      
      return true
    } catch (error) {
      console.error('Token refresh failed:', error)
      // Clear tokens on error
      token.value = null
      refreshToken.value = null
      user.value = null
      return false
    }
  }

  const loadUserProfile = async () => {
    // User profile is loaded from the login/register response
    // No additional API call needed
    if (!user.value && token.value) {
      // If we have a token but no user, try to decode it
      try {
        const tokenParts = token.value.split('.')
        if (tokenParts.length === 3) {
          const payload = JSON.parse(atob(tokenParts[1]))
          user.value = {
            id: payload.sub || payload.id || '',
            email: payload.email || '',
            displayName: payload.name || payload.displayName || payload.preferred_username || '',
            username: payload.preferred_username || payload.username,
            preferredCurrency: payload.preferredCurrency,
            locale: payload.locale,
            timezone: payload.timezone,
            createdAt: payload.iat 
              ? new Date(payload.iat * 1000).toISOString() 
              : new Date().toISOString()
          } as User
        }
      } catch (tokenErr) {
        console.error('Failed to extract user info from token:', tokenErr)
      }
    }
  }

  const getCurrentUserId = (): string | null => {
    return user.value?.id || null
  }

  const isAuthenticated = computed(() => {
    return !!token.value && !!user.value
  })

  return {
    user: readonly(user),
    token: readonly(token),
    isAuthenticated,
    login,
    register,
    logout,
    updateToken,
    loadUserProfile,
    getCurrentUserId
  }
}
