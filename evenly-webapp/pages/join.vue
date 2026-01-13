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
      <form v-else @submit.prevent="inputMethod === 'link' ? handleLinkInput() : handleJoin()" class="space-y-4">
        <!-- Input Method Toggle -->
        <div class="flex gap-2 p-1 bg-slate-800 rounded-xl">
          <button
            type="button"
            @click="inputMethod = 'code'; code = ''; linkInput = ''; errorMessage = ''"
            :class="inputMethod === 'code' ? 'bg-emerald-500 text-white' : 'text-slate-400'"
            class="flex-1 py-2 px-4 rounded-lg font-medium transition-colors"
          >
            {{ t('join.useCode') || 'Use Code' }}
          </button>
          <button
            type="button"
            @click="inputMethod = 'link'; code = ''; linkInput = ''; errorMessage = ''"
            :class="inputMethod === 'link' ? 'bg-emerald-500 text-white' : 'text-slate-400'"
            class="flex-1 py-2 px-4 rounded-lg font-medium transition-colors"
          >
            {{ t('join.useLink') || 'Use Link' }}
          </button>
        </div>

        <!-- Code Input -->
        <div v-if="inputMethod === 'code'">
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

        <!-- Link Input -->
        <div v-else>
          <label class="block text-sm font-medium text-slate-300 mb-2">{{ t('join.inviteLink') || 'Invitation Link' }}</label>
          <input
            v-model="linkInput"
            type="text"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 text-sm"
            :placeholder="t('join.linkPlaceholder') || 'Paste invitation link'"
            autofocus
          />
          <p class="mt-2 text-xs text-slate-400">{{ t('join.linkHint') || 'Paste the full invitation link here' }}</p>
        </div>

        <button
          type="submit"
          :disabled="loading || (inputMethod === 'code' ? !code : !linkInput)"
          class="btn btn-green w-full py-3"
        >
          <span v-if="!loading">
            {{ inputMethod === 'link' ? (t('join.extractCode') || 'Extract Code') : (t('join.submit') || 'Join Workspace') }}
          </span>
          <span v-else>{{ t('join.joining') || 'Joining...' }}</span>
        </button>

        <!-- Not authenticated message -->
        <div v-if="!isAuthenticated" class="bg-slate-800/50 border border-slate-700 rounded-xl p-4 text-center">
          <p class="text-slate-300 text-sm mb-3">{{ t('join.loginRequired') || 'You need to be logged in to join a workspace' }}</p>
          <div class="flex gap-2">
            <NuxtLink
              :to="{ path: '/login', query: { redirect: route.fullPath } }"
              class="flex-1 bg-emerald-500 hover:bg-emerald-600 text-white font-medium py-2 px-4 rounded-lg transition-colors"
            >
              {{ t('auth.login.title') || 'Login' }}
            </NuxtLink>
            <NuxtLink
              to="/register"
              class="flex-1 bg-slate-700 hover:bg-slate-600 text-white font-medium py-2 px-4 rounded-lg transition-colors"
            >
              {{ t('auth.register.title') || 'Sign Up' }}
            </NuxtLink>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false
})

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const { joinInvite } = useInvites()
const workspacesStore = useWorkspacesStore()
const { success: showSuccess, error: showError } = useToast()
const { isAuthenticated } = useAuth()

const code = ref('')
const linkInput = ref('')
const inputMethod = ref<'code' | 'link'>('code')
const loading = ref(false)
const success = ref(false)
const errorMessage = ref('')

// Extract code from URL or link
const extractCodeFromLink = (link: string): string | null => {
  try {
    const url = new URL(link)
    return url.searchParams.get('code') || null
  } catch {
    // If it's not a full URL, try to extract code from path
    const match = link.match(/[?&]code=([^&]+)/)
    return match ? match[1] : null
  }
}

// Check for code in query parameter on mount
onMounted(async () => {
  const queryCode = route.query.code as string | undefined
  if (queryCode) {
    code.value = queryCode
    inputMethod.value = 'code'
    // If authenticated, auto-join. Otherwise, wait for user to login
    if (isAuthenticated.value) {
      await handleJoin()
    }
  }
})

// Watch for authentication changes - if user logs in while on join page, try to join
watch(isAuthenticated, async (authenticated) => {
  if (authenticated && code.value && !loading.value && !success.value) {
    await handleJoin()
  }
})

const handleLinkInput = () => {
  if (!linkInput.value.trim()) return
  
  const extractedCode = extractCodeFromLink(linkInput.value.trim())
  if (extractedCode) {
    code.value = extractedCode
    linkInput.value = ''
    inputMethod.value = 'code'
  } else {
    errorMessage.value = t('join.invalidLink') || 'Invalid link. Please check the link and try again.'
    showError(errorMessage.value)
  }
}

const handleJoin = async () => {
  // Check authentication first
  if (!isAuthenticated.value) {
    // Redirect to login with join URL as redirect
    const joinUrl = route.fullPath
    return router.push({
      path: '/login',
      query: { redirect: joinUrl }
    })
  }

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

