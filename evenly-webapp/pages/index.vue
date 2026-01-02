<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900 relative">
    <LanguageSwitcher />
    <div class="text-center space-y-8 max-w-md w-full">
      <!-- Boarding Image -->
      <div class="flex justify-center">
        <img
          src="/images/boarding.png"
          alt="Evenly"
          class="w-full max-w-lg h-auto object-contain"
        />
      </div>

      <!-- Title -->
      <div class="space-y-2">
        <h1 class="text-4xl font-bold text-white">{{ t('landing.title') }}</h1>
        <p class="text-slate-400 text-lg">{{ t('landing.tagline') }}</p>
      </div>

      <!-- Description -->
      <p class="text-slate-300 leading-relaxed">
        {{ t('landing.description') }}
      </p>

      <!-- CTA Buttons -->
      <div class="space-y-3 pt-4">
        <NuxtLink
          to="/register"
          class="btn btn-green w-full py-4 text-center"
        >
          {{ t('landing.getStarted') }}
        </NuxtLink>
        <NuxtLink
          to="/login"
          class="block w-full bg-slate-800 text-white font-medium py-4 rounded-xl border border-slate-700 hover:bg-slate-700 transition-colors text-center"
        >
          {{ t('landing.signIn') }}
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false
})

const { t } = useI18n()
const { isAuthenticated } = useAuth()

// Redirect if already authenticated
watchEffect(() => {
  if (isAuthenticated.value) {
    navigateTo('/dashboard')
  }
})
</script>
