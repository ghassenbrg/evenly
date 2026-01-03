import type { User } from '~/types/api'
import { useCookie } from '#imports'

export const useAuth = () => {
  const token = useCookie<string | null>('token', { default: () => null })
  const user = useCookie<User | null>('user', { default: () => null })
  const { $keycloak } = useNuxtApp()

  const login = async (redirectUri?: string) => {
    if (!$keycloak) {
      throw new Error('Keycloak not initialized')
    }
    
    const callbackUri = redirectUri || window.location.origin + '/keycloak-callback'
    try {
      await $keycloak.login({
        redirectUri: callbackUri
      })
    } catch (error: any) {
      if (error?.error === 'invalid_redirect_uri' || 
          error?.message?.includes('Invalid parameter: redirect_uri')) {
        console.error('Keycloak redirect URI not configured. Please add the following to Keycloak client settings:')
        console.error(`  Valid Redirect URIs: ${callbackUri}`)
        console.error(`  Web Origins: ${window.location.origin}`)
        throw new Error(`Redirect URI not configured in Keycloak. Please add: ${callbackUri}`)
      }
      throw error
    }
  }

  const logout = async () => {
    if (!$keycloak) {
      token.value = null
      user.value = null
      return
    }
    
    await $keycloak.logout({
      redirectUri: window.location.origin
    })
    
    token.value = null
    user.value = null
  }

  const updateToken = async () => {
    if (!$keycloak || !$keycloak.authenticated) {
      return false
    }
    
    try {
      const refreshed = await $keycloak.updateToken(30)
      if (refreshed) {
        token.value = $keycloak.token || null
        
        // Update user profile if token was refreshed
        if ($keycloak.authenticated) {
          await loadUserProfile()
        }
      }
      return refreshed
    } catch (error) {
      console.error('Token refresh failed:', error)
      await logout()
      return false
    }
  }

  const loadUserProfile = async () => {
    if (!$keycloak || !$keycloak.authenticated) {
      return
    }
    
    try {
      // Try to load user profile from Keycloak
      const userProfile = await $keycloak.loadUserProfile()
      user.value = {
        id: userProfile.id || '',
        email: userProfile.email || '',
        displayName: `${userProfile.firstName || ''} ${userProfile.lastName || ''}`.trim() || userProfile.username || '',
        username: userProfile.username,
        createdAt: userProfile.createdTimestamp 
          ? new Date(userProfile.createdTimestamp).toISOString() 
          : new Date().toISOString()
      } as User
    } catch (err: any) {
      // If profile loading fails (CORS, 401, etc.), try to extract info from token
      console.warn('Failed to load user profile from Keycloak, using token info:', err)
      
      try {
        // Decode token to get user info (JWT token contains user info)
        if ($keycloak.token) {
          const tokenParts = $keycloak.token.split('.')
          if (tokenParts.length === 3) {
            const payload = JSON.parse(atob(tokenParts[1]))
            user.value = {
              id: payload.sub || '',
              email: payload.email || '',
              displayName: payload.name || `${payload.given_name || ''} ${payload.family_name || ''}`.trim() || payload.preferred_username || '',
              username: payload.preferred_username || payload.preferred_username,
              createdAt: payload.iat 
                ? new Date(payload.iat * 1000).toISOString() 
                : new Date().toISOString()
            } as User
          }
        }
      } catch (tokenErr) {
        console.error('Failed to extract user info from token:', tokenErr)
        // Keep existing user value if available
      }
    }
  }

  const isAuthenticated = computed(() => {
    if (process.client && $keycloak) {
      return $keycloak.authenticated || false
    }
    return !!token.value && !!user.value
  })

  // Watch for Keycloak authentication state changes
  if (process.client && $keycloak) {
    // Only watch if keycloak is initialized
    watch(() => $keycloak?.authenticated, async (authenticated) => {
      if (authenticated && $keycloak) {
        token.value = $keycloak.token || null
        // Wait a bit for token to be fully available
        await nextTick()
        await loadUserProfile()
        
        // Set up token refresh
        $keycloak.onTokenExpired = () => {
          updateToken()
        }
      } else {
        token.value = null
        user.value = null
      }
    }, { immediate: false }) // Don't run immediately, wait for plugin initialization
  }

  return {
    user: readonly(user),
    token: readonly(token),
    isAuthenticated,
    login,
    logout,
    updateToken,
    loadUserProfile
  }
}

