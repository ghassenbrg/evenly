<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900">
    <LanguageSwitcher />
    <div class="w-full max-w-md space-y-8">
      <!-- Boarding Image -->
      <div class="flex justify-center">
        <img
          src="/images/boarding.png"
          alt="Evenly"
          class="w-full max-w-sm h-auto object-contain"
        />
      </div>

      <!-- Title -->
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-white">{{ t('auth.login.title') }}</h1>
        <p class="text-slate-400">{{ t('auth.login.subtitle') }}</p>
      </div>

      <!-- Login Form -->
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label for="username" class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('auth.login.username') || 'Username/Email' }}
          </label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            required
            autocomplete="username"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            :placeholder="t('auth.login.usernamePlaceholder') || 'Enter your username or email'"
            :disabled="loading"
          />
        </div>

        <div>
          <label for="password" class="block text-sm font-medium text-slate-300 mb-2">
            {{ t('auth.login.password') || 'Password' }}
          </label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            required
            autocomplete="current-password"
            class="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            :placeholder="t('auth.login.passwordPlaceholder') || 'Enter your password'"
            :disabled="loading"
          />
        </div>

        <div v-if="error" class="text-red-400 text-sm">
          {{ error }}
        </div>

        <!-- Login Button -->
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
import type { LoginRequest } from '~/types/api'

definePageMeta({
  middleware: 'guest',
  layout: false
})

const { login } = useAuth()
const { error: showError } = useToast()
const loading = ref(false)
const route = useRoute()
const error = ref<string | null>(null)

const { t } = useI18n()

const form = ref<LoginRequest>({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    loading.value = true
    error.value = null
    
    const redirectQuery = typeof route.query.redirect === 'string' ? route.query.redirect : null
    await login(form.value, redirectQuery || undefined)
  } catch (err: any) {
    console.error('Login error:', err)
    error.value = err.message || err.error?.message || t('auth.login.error') || 'Login failed. Please check your credentials.'
    showError(error.value)
    loading.value = false
  }
}
</script>
