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

      <!-- Login Button -->
      <button
        @click="handleLogin"
        :disabled="loading"
        class="btn btn-green w-full py-3"
      >
        <span v-if="!loading">{{ t('auth.login.submit') }}</span>
        <span v-else>{{ t('auth.login.submitting') }}</span>
      </button>

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

const { login } = useAuth()
const { success } = useToast()
const loading = ref(false)

const { t } = useI18n()

const handleLogin = async () => {
  try {
    loading.value = true
    // Redirect to Keycloak login
    await login()
  } catch (err: any) {
    console.error('Login error:', err)
    loading.value = false
  }
}
</script>

