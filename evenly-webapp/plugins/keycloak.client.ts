// Keycloak plugin is disabled - using REST-based authentication instead
// This file is kept for reference but is no longer actively used
// The backend handles Keycloak authentication via POST /auth/login and POST /auth/register

export default defineNuxtPlugin(async () => {
  // Provide a minimal mock Keycloak object for backward compatibility
  // Components should use useAuth() composable instead
  const keycloak = {
    authenticated: false,
    token: null,
    idToken: null,
    tokenParsed: null,
    idTokenParsed: null,
    login: () => Promise.reject(new Error('Keycloak is disabled. Use REST-based authentication.')),
    logout: () => Promise.reject(new Error('Keycloak is disabled. Use REST-based authentication.')),
    register: () => Promise.reject(new Error('Keycloak is disabled. Use REST-based authentication.')),
    updateToken: () => Promise.resolve(false),
    onTokenExpired: null as any
  }

  return {
    provide: {
      keycloak
    }
  }
})
