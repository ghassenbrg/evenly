<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 py-12 bg-slate-900">
    <div class="text-center space-y-8 max-w-md w-full">
      <!-- Logo/Icon -->
      <div class="flex justify-center">
        <div class="w-20 h-20 rounded-2xl bg-emerald-500 flex items-center justify-center">
          <span class="text-4xl text-white">¥</span>
        </div>
      </div>

      <!-- Title -->
      <div class="space-y-2">
        <h1 class="text-4xl font-bold text-white">Evenly</h1>
        <p class="text-slate-400 text-lg">Split expenses fairly</p>
      </div>

      <!-- Description -->
      <p class="text-slate-300 leading-relaxed">
        Track shared expenses, split bills, and settle up with friends and roommates.
      </p>

      <!-- CTA Buttons -->
      <div class="space-y-3 pt-4">
        <NuxtLink
          to="/register"
          class="block w-full bg-emerald-500 text-white font-semibold py-4 rounded-xl hover:bg-emerald-600 transition-colors text-center"
        >
          Get Started
        </NuxtLink>
        <NuxtLink
          to="/login"
          class="block w-full bg-slate-800 text-white font-medium py-4 rounded-xl border border-slate-700 hover:bg-slate-700 transition-colors text-center"
        >
          Sign In
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false
})

const { isAuthenticated } = useAuth()

// Redirect if already authenticated
watchEffect(() => {
  if (isAuthenticated.value) {
    navigateTo('/dashboard')
  }
})
</script>
