<template>
  <div class="p-4 space-y-6">
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
</script>
