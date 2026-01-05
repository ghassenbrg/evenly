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
