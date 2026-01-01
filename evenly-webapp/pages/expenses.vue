<template>
  <div class="p-4">
    <div class="flex flex-col items-center justify-center py-12 text-center">
      <p class="text-slate-400">{{ t('placeholders.expenses') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

const { t } = useI18n()
</script>
