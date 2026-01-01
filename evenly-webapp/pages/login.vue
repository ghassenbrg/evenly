<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900">
    <LanguageSwitcher />
    <div class="w-full max-w-md space-y-8">
      <!-- Logo -->
      <div class="flex justify-center">
        <div class="w-20 h-20 rounded-2xl bg-emerald-500 flex items-center justify-center">
          <span class="text-4xl text-white">¥</span>
        </div>
      </div>

      <!-- Title -->
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-white">{{ t('auth.login.title') }}</h1>
        <p class="text-slate-400">{{ t('auth.login.subtitle') }}</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('auth.login.emailOrUsername') }}</label>
          <input
            v-model="username"
            type="text"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            :placeholder="t('auth.login.emailOrUsernamePlaceholder')"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('auth.login.password') }}</label>
          <input
            v-model="password"
            type="password"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            :placeholder="t('auth.login.passwordPlaceholder')"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="btn btn-green w-full py-3"
        >
          <span v-if="!loading">{{ t('auth.login.submit') }}</span>
          <span v-else>{{ t('auth.login.submitting') }}</span>
        </button>
      </form>

      <!-- Register Link -->
      <div class="text-center">
        <p class="text-slate-400">
          {{ t('auth.login.noAccount') }}
          <NuxtLink to="/register" class="text-emerald-500 hover:text-emerald-400 font-medium">
            {{ t('auth.login.signUp') }}
          </NuxtLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'guest',
  layout: false
})

const authStore = useAuthStore()
const { success, error } = useToast()
const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)

const { t } = useI18n()

const handleLogin = async () => {
  try {
    loading.value = true
    await authStore.loginUser(username.value, password.value)
    success(t('auth.login.welcomeBack'))
    
    // Wait for next tick and a small delay to ensure token cookie is set
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 50))
    
    // Pre-load workspaces
    const workspacesStore = useWorkspacesStore()
    await workspacesStore.fetchWorkspaces()
    
    // Use replace to avoid stacking pages in history
    await navigateTo('/dashboard', { replace: true })
  } catch (err: any) {
    console.error('Login error:', err)
    let errorMessage = t('auth.login.invalidCredentials')
    
    // Try to extract error message from various possible structures
    if (err?.message) {
      errorMessage = err.message
    } else if (err?.error) {
      // Handle case where error.error is an object with message property
      if (typeof err.error === 'object' && err.error.message) {
        errorMessage = err.error.message
      } else if (typeof err.error === 'object' && err.error.error) {
        errorMessage = err.error.error
      } else if (typeof err.error === 'string') {
        errorMessage = err.error
      }
    }
    
    console.log('Showing error toast with message:', errorMessage)
    error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

