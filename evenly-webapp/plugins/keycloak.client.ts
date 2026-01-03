import Keycloak from 'keycloak-js'

export default defineNuxtPlugin(async () => {
  const config = useRuntimeConfig()
  const token = useCookie<string | null>('token', { default: () => null })
  const user = useCookie<any | null>('user', { default: () => null })

  const keycloak = new Keycloak({
    url: config.public.keycloak.url,
    realm: config.public.keycloak.realm,
    clientId: config.public.keycloak.clientId
  })

  // Initialize Keycloak
  try {
    // Check if we're on the callback page
    const isCallback = window.location.pathname === '/keycloak-callback'
    
    // Initialize Keycloak with redirect-based flow only (no iframes)
    // This avoids CSP errors from Keycloak's frame-ancestors policy
    // Note: Even with checkLoginIframe: false, check-sso might attempt iframe checks
    // CSP errors are expected and won't break functionality since we use redirects
    let authenticated = false
    try {
      authenticated = await keycloak.init({
        onLoad: isCallback ? 'login-required' : 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false, // Disable iframe-based login checks
        enableLogging: false,
        redirectUri: window.location.origin + '/keycloak-callback'
      })
      
      // Immediately clear hash after Keycloak processes it to prevent Vue Router warnings
      // Vue Router tries to parse hash fragments as CSS selectors during navigation
      if (isCallback && window.location.hash) {
        window.history.replaceState(null, '', window.location.pathname + window.location.search)
      }
    } catch (initError: any) {
      // Handle CSP or other initialization errors gracefully
      // If it's a CSP error, it's expected when Keycloak tries to use iframes
      // The app will still work with redirect-based flows
      if (initError?.message?.includes('Content Security Policy') || 
          initError?.message?.includes('frame-ancestors')) {
        console.warn('Keycloak CSP warning (expected when iframes are disabled):', initError.message)
        // Try to check if user is already authenticated via token in storage
        if (keycloak.token && keycloak.authenticated) {
          authenticated = true
        }
      } else if (initError?.error === 'invalid_redirect_uri' || 
                 initError?.message?.includes('Invalid parameter: redirect_uri') ||
                 initError?.error_description?.includes('redirect_uri')) {
        console.error('Keycloak redirect URI error:', initError)
        console.error('Please configure the following redirect URIs in Keycloak client settings:')
        console.error(`  - ${window.location.origin}/keycloak-callback`)
        console.error(`  - ${window.location.origin}/* (or use wildcard)`)
        // Don't set authenticated if redirect URI is invalid
        authenticated = false
      } else {
        console.error('Keycloak initialization error:', initError)
      }
    }

    if (authenticated) {
      // User is authenticated, store token and user info
      token.value = keycloak.token || null
      
      // Load user profile - will be handled by useAuth composable
      // Don't load here to avoid CORS issues, let the composable handle it with token fallback

      // Set up token refresh
      keycloak.onTokenExpired = () => {
        keycloak.updateToken(30).then((refreshed) => {
          if (refreshed) {
            token.value = keycloak.token || null
          }
        }).catch(() => {
          // Token refresh failed, logout
          keycloak.logout({ redirectUri: window.location.origin + '/login' })
        })
      }
    }
  } catch (error) {
    console.error('Keycloak initialization error:', error)
  }

  return {
    provide: {
      keycloak
    }
  }
})

