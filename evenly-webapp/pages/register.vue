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
        <h1 class="text-3xl font-bold text-white">{{ t('auth.register.title') }}</h1>
        <p class="text-slate-400">{{ t('auth.register.subtitle') }}</p>
      </div>

      <!-- Register Button -->
      <button
        @click="handleRegister"
        :disabled="loading"
        class="btn btn-green w-full py-3"
      >
        <span v-if="!loading">{{ t('auth.register.submit') }}</span>
        <span v-else>{{ t('auth.register.submitting') }}</span>
      </button>

      <!-- Login Link -->
      <div class="text-center">
        <p class="text-slate-400">
          {{ t('auth.register.hasAccount') }}
          <NuxtLink to="/login" class="text-emerald-500 hover:text-emerald-400 font-medium">
            {{ t('auth.register.signIn') }}
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

const { $keycloak } = useNuxtApp()
const loading = ref(false)
const { t } = useI18n()

const handleRegister = async () => {
  try {
    loading.value = true
    if (!$keycloak) {
      throw new Error('Keycloak not initialized')
    }
    
    // Redirect to Keycloak registration
    await $keycloak.register({
      redirectUri: window.location.origin + '/keycloak-callback'
    })
  } catch (err: any) {
    console.error('Registration error:', err)
    loading.value = false
  }
}
</script>

