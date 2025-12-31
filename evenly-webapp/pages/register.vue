<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900">
    <div class="w-full max-w-md space-y-8">
      <!-- Logo -->
      <div class="flex justify-center">
        <div class="w-20 h-20 rounded-2xl bg-emerald-500 flex items-center justify-center">
          <span class="text-4xl text-white">¥</span>
        </div>
      </div>

      <!-- Title -->
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-white">Create Account</h1>
        <p class="text-slate-400">Get started with Evenly</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleRegister" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">Display Name</label>
          <input
            v-model="displayName"
            type="text"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            placeholder="John Doe"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">Email</label>
          <input
            v-model="email"
            type="email"
            required
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            placeholder="you@example.com"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-2">Password</label>
          <input
            v-model="password"
            type="password"
            required
            minlength="6"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            placeholder="••••••••"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-emerald-500 text-white font-semibold py-3 rounded-xl hover:bg-emerald-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="!loading">Create Account</span>
          <span v-else>Creating account...</span>
        </button>
      </form>

      <!-- Login Link -->
      <div class="text-center">
        <p class="text-slate-400">
          Already have an account?
          <NuxtLink to="/login" class="text-emerald-500 hover:text-emerald-400 font-medium">
            Sign in
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

const displayName = ref('')
const email = ref('')
const password = ref('')
const loading = ref(false)

const handleRegister = async () => {
  try {
    loading.value = true
    await authStore.registerUser(email.value, password.value, displayName.value)
    success('Account created!')
    
    // Wait for next tick and a small delay to ensure token cookie is set
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 50))
    
    // Pre-load workspaces
    const workspacesStore = useWorkspacesStore()
    await workspacesStore.fetchWorkspaces()
    
    await router.push('/dashboard')
  } catch (err: any) {
    error(err.message || 'Registration failed')
  } finally {
    loading.value = false
  }
}
</script>

