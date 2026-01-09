<template>
  <div class="p-4 space-y-6">
    <!-- Language Section -->
    <div class="bg-slate-800 rounded-2xl p-4 space-y-4">
      <h2 class="text-white font-semibold text-lg">{{ t('settings.language') }}</h2>
      <p class="text-slate-400 text-sm">{{ t('settings.languageDescription') }}</p>
      <div class="grid gap-3 grid-cols-2">
        <button
          v-for="loc in availableLocales"
          :key="loc.code"
          @click="setLocale(loc.code)"
          :class="currentLocale === loc.code ? 'bg-emerald-500 text-white' : 'bg-slate-700 text-slate-300 hover:bg-slate-600'"
          class="px-4 py-3 rounded-xl font-medium transition-colors"
        >
          {{ t(`settings.${loc.code}`) || loc.name }}
        </button>
      </div>
    </div>

    <!-- Money features -->
    <div class="bg-slate-800 rounded-2xl p-4 space-y-3">
      <h2 class="text-white font-semibold text-lg">{{ t('settings.money') }}</h2>
      <p class="text-slate-400 text-sm">{{ t('settings.moneyDescription') }}</p>
      <div class="grid gap-3 grid-cols-1">
        <NuxtLink
          to="/wallets"
          class="w-full bg-white/5 border border-slate-700 hover:border-emerald-500 text-white font-medium py-3 rounded-xl text-center transition-colors"
        >
          {{ t('wallets.title') }}
        </NuxtLink>
        <NuxtLink
          to="/transactions"
          class="w-full bg-white/5 border border-slate-700 hover:border-emerald-500 text-white font-medium py-3 rounded-xl text-center transition-colors"
        >
          {{ t('transactions.title') }}
        </NuxtLink>
        <NuxtLink
          to="/subscriptions"
          class="w-full bg-white/5 border border-slate-700 hover:border-emerald-500 text-white font-medium py-3 rounded-xl text-center transition-colors"
        >
          {{ t('subscriptions.title') }}
        </NuxtLink>
      </div>
    </div>

    <!-- Categories Management -->
    <div class="bg-slate-800 rounded-2xl p-4 space-y-3">
      <h2 class="text-white font-semibold text-lg">{{ t('settings.categories.title') }}</h2>
      <p class="text-slate-400 text-sm">{{ t('settings.categories.description') }}</p>
      <NuxtLink
        to="/settings/categories"
        class="w-full bg-white/5 border border-slate-700 hover:border-emerald-500 text-white font-medium py-3 rounded-xl text-center transition-colors flex items-center justify-center gap-2"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        {{ t('settings.categories.manage') }}
      </NuxtLink>
    </div>

    <!-- Account Section -->
    <div class="bg-slate-800 rounded-2xl p-4 space-y-4">
      <h2 class="text-white font-semibold text-lg">{{ t('account.title') }}</h2>
      <div v-if="currentUser" class="space-y-2">
        <div>
          <p class="text-slate-400 text-sm">{{ t('account.name') }}</p>
          <p class="text-white font-medium">{{ currentUser.displayName }}</p>
        </div>
        <div>
          <p class="text-slate-400 text-sm">{{ t('account.email') }}</p>
          <p class="text-white font-medium">{{ currentUser.email }}</p>
        </div>
      </div>
      <button
        @click="handleLogout"
        class="w-full bg-red-500 hover:bg-red-600 text-white font-medium py-3 rounded-xl transition-colors"
      >
        {{ t('auth.logout') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  isMainPage: true
})

const { locale, locales, setLocale, t } = useI18n()
const authStore = useAuthStore()
const { currentUser } = storeToRefs(authStore)
const router = useRouter()

const currentLocale = computed(() => locale.value)
const availableLocales = computed(() => locales.value)

const handleLogout = async () => {
  await authStore.logout()
}
</script>
