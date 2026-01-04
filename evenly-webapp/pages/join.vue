<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900">
    <div class="w-full max-w-md space-y-8">
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-white">{{ t('join.title') || 'Join Workspace' }}</h1>
        <p class="text-slate-400">{{ t('join.description') || 'Enter an invitation code to join a workspace' }}</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex items-center justify-center py-12">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
      </div>

      <!-- Success State -->
      <div v-else-if="success" class="bg-emerald-500/10 border border-emerald-500/20 rounded-xl p-6 text-center">
        <svg class="w-12 h-12 text-emerald-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        <h2 class="text-xl font-semibold text-white mb-2">{{ t('join.success') || 'Successfully joined!' }}</h2>
        <p class="text-slate-300 mb-4">{{ t('join.redirecting') || 'Redirecting to workspace...' }}</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMessage" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
        <p class="text-red-400 text-sm">{{ errorMessage }}</p>
        <button
          @click="errorMessage = ''"
          class="mt-3 text-sm text-red-400 hover:text-red-300 underline"
        >
          {{ t('common.tryAgain') || 'Try again' }}
        </button>
      </div>

      <!-- Form -->
      <form v-else @submit.prevent="handleJoin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('join.inviteCode') || 'Invitation Code' }}</label>
          <input
            v-model="code"
            type="text"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 font-mono text-center text-lg"
            :placeholder="t('join.codePlaceholder') || 'Enter code'"
            autofocus
          />
        </div>
        <button
          type="submit"
          :disabled="loading || !code"
          class="btn btn-green w-full py-3"
        >
          <span v-if="!loading">{{ t('join.submit') || 'Join Workspace' }}</span>
          <span v-else>{{ t('join.joining') || 'Joining...' }}</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  layout: false
})

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const { joinInvite } = useInvites()
const workspacesStore = useWorkspacesStore()
const { success: showSuccess, error: showError } = useToast()

const code = ref('')
const loading = ref(false)
const success = ref(false)
const errorMessage = ref('')

// Check for code in query parameter
onMounted(async () => {
  const queryCode = route.query.code as string | undefined
  if (queryCode) {
    code.value = queryCode
    await handleJoin()
  }
})

const handleJoin = async () => {
  if (!code.value.trim()) {
    errorMessage.value = t('join.codeRequired') || 'Please enter an invitation code'
    return
  }

  try {
    loading.value = true
    errorMessage.value = ''
    
    await joinInvite(code.value.trim())
    
    // Refresh workspaces to include the new one
    await workspacesStore.fetchWorkspaces()
    
    success.value = true
    showSuccess(t('join.success') || 'Successfully joined workspace!')
    
    // Redirect to dashboard after a short delay
    setTimeout(() => {
      router.push('/dashboard')
    }, 1500)
  } catch (err: any) {
    errorMessage.value = err.message || t('join.failed') || 'Failed to join workspace. Please check the code and try again.'
    showError(errorMessage.value)
  } finally {
    loading.value = false
  }
}
</script>

