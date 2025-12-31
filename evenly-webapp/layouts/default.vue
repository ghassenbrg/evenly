<template>
  <div class="min-h-screen flex flex-col bg-slate-900 text-white">
    <!-- Header -->
    <header class="sticky top-0 z-40 bg-slate-900/95 backdrop-blur-sm border-b border-slate-800 pt-safe">
      <div class="flex items-center justify-between px-4 h-14">
        <h1 class="text-lg font-semibold">{{ pageTitle }}</h1>
        <WorkspaceSwitch v-if="showWorkspaceSwitch" />
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 overflow-y-auto pb-20">
      <slot />
    </main>

    <!-- Bottom Tab Bar -->
    <nav class="fixed bottom-0 left-0 right-0 bg-slate-900/95 backdrop-blur-sm border-t border-slate-800 pb-safe z-30">
      <div class="flex items-center justify-around h-16">
        <NuxtLink
          to="/dashboard"
          class="flex flex-col items-center justify-center flex-1 h-full transition-colors"
          :class="isActive('/dashboard') ? 'text-emerald-500' : 'text-slate-400'"
        >
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
          <span class="text-xs">Dashboard</span>
        </NuxtLink>

        <NuxtLink
          to="/expenses"
          class="flex flex-col items-center justify-center flex-1 h-full transition-colors"
          :class="isActive('/expenses') ? 'text-emerald-500' : 'text-slate-400'"
        >
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <span class="text-xs">Expenses</span>
        </NuxtLink>

        <NuxtLink
          to="/history"
          class="flex flex-col items-center justify-center flex-1 h-full transition-colors"
          :class="isActive('/history') ? 'text-emerald-500' : 'text-slate-400'"
        >
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span class="text-xs">History</span>
        </NuxtLink>

        <NuxtLink
          to="/settings"
          class="flex flex-col items-center justify-center flex-1 h-full transition-colors"
          :class="isActive('/settings') ? 'text-emerald-500' : 'text-slate-400'"
        >
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span class="text-xs">Settings</span>
        </NuxtLink>
      </div>
    </nav>

  </div>
</template>

<script setup lang="ts">
const route = useRoute()

const showWorkspaceSwitch = computed(() => {
  return route.path !== '/login' && route.path !== '/register' && route.path !== '/' && route.path !== '/join'
})

// Get page title from route
const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/dashboard': 'Dashboard',
    '/expenses': 'Expenses',
    '/history': 'History',
    '/settings': 'Settings'
  }
  return titles[route.path] || 'Evenly'
})

const isActive = (path: string) => {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<script lang="ts">
export default {
  name: 'DefaultLayout'
}
</script>

<style scoped>
.pt-safe {
  padding-top: env(safe-area-inset-top);
}

.pb-safe {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
