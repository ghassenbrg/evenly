<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-900">
    <div class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500 mx-auto mb-4"></div>
      <p class="text-white">{{ t('auth.login.authenticating') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false
})

const { loadUserProfile, isAuthenticated } = useAuth()
const { success } = useToast()
const router = useRouter()
const { t } = useI18n()

onMounted(async () => {
  try {
    // Wait for Keycloak plugin to initialize and process the callback
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Wait for next tick to ensure Keycloak is ready
    await nextTick()
    
    // Try to load user profile (will use token fallback if CORS fails)
    await loadUserProfile()
    
    // Wait a bit more to ensure everything is set
    await new Promise(resolve => setTimeout(resolve, 200))
    
    if (isAuthenticated.value) {
      success(t('auth.login.welcomeBack'))
      
      // Pre-load workspaces
      const workspacesStore = useWorkspacesStore()
      await workspacesStore.fetchWorkspaces()
      
      // Redirect to dashboard
      await navigateTo('/dashboard', { replace: true })
    } else {
      // Not authenticated, redirect to login
      await navigateTo('/login', { replace: true })
    }
  } catch (error) {
    console.error('Keycloak callback error:', error)
    await navigateTo('/login', { replace: true })
  }
})
</script>

