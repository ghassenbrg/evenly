<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900">
    <div class="w-full max-w-md space-y-8">
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-white">Join Workspace</h1>
        <p class="text-slate-400">Placeholder page</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  layout: false
})
</script>

