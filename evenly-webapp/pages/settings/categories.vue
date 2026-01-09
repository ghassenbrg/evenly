<template>
  <div class="p-4 space-y-6">
    <!-- Header -->
    <div class="flex items-center gap-4">
      <button
        @click="router.back()"
        class="p-2 text-slate-400 hover:text-white transition-colors"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
      <h1 class="text-2xl font-bold text-white">{{ t('settings.categories.title') }}</h1>
    </div>

    <!-- Description -->
    <p class="text-slate-400 text-sm">{{ t('settings.categories.description') }}</p>

    <!-- Categories Management Component -->
    <SettingsCategoriesManagement />
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  isMainPage: false
})

const { t } = useI18n()
const router = useRouter()
</script>
