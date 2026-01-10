<template>
  <div class="p-4 space-y-6">
    <!-- Search Input -->
    <div class="relative">
      <input
        v-model="searchQuery"
        type="text"
        :placeholder="t('settings.searchPlaceholder') || 'Search settings...'"
        class="w-full px-4 py-3 pl-10 bg-slate-800 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 text-base"
      />
      <svg
        class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
      <button
        v-if="searchQuery"
        @click="searchQuery = ''"
        class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white transition-colors"
        aria-label="Clear search"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Language Section -->
    <div
      v-if="shouldShowSection('language')"
      class="bg-slate-800 rounded-2xl p-4 space-y-4"
    >
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

    <!-- Pockito Features -->
    <div
      v-if="shouldShowSection('pockito')"
      class="bg-slate-800 rounded-2xl p-4 space-y-3"
    >
      <h2 class="text-white font-semibold text-lg">{{ t('settings.pockito.title') }}</h2>
      <p class="text-slate-400 text-sm">{{ t('settings.pockito.description') }}</p>
      <NuxtLink
        to="/settings/pockito"
        class="w-full bg-white/5 border border-slate-700 hover:border-emerald-500 text-white font-medium py-3 rounded-xl text-center transition-colors flex items-center justify-center gap-2"
      >
        <img src="/icons/pockito-icon.svg" alt="Pockito" class="w-5 h-5" />
        {{ t('settings.pockito.explore') }}
      </NuxtLink>
    </div>

    <!-- Categories Management -->
    <div
      v-if="shouldShowSection('categories')"
      class="bg-slate-800 rounded-2xl p-4 space-y-3"
    >
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
    <div
      v-if="shouldShowSection('account')"
      class="bg-slate-800 rounded-2xl p-4 space-y-4"
    >
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

    <!-- Empty State -->
    <div
      v-if="searchQuery && !hasVisibleSections"
      class="text-center py-12"
    >
      <div class="flex flex-col items-center space-y-4">
        <div class="w-16 h-16 rounded-full bg-slate-800 flex items-center justify-center">
          <svg class="w-8 h-8 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        <p class="text-sm text-slate-400">{{ t('settings.noResults') || 'No settings found matching your search' }}</p>
      </div>
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

const searchQuery = ref('')

// Section data for filtering (computed to be reactive)
const sections = computed(() => ({
  language: {
    title: t('settings.language'),
    description: t('settings.languageDescription'),
    keywords: ['language', 'lang', 'locale', '言語', '言語設定']
  },
  pockito: {
    title: t('settings.pockito.title'),
    description: t('settings.pockito.description'),
    keywords: ['pockito', 'wallet', 'transaction', 'subscription', 'money', 'finance', 'ウォレット', '取引', 'サブスクリプション']
  },
  categories: {
    title: t('settings.categories.title'),
    description: t('settings.categories.description'),
    keywords: ['category', 'categories', 'カテゴリ']
  },
  account: {
    title: t('account.title'),
    description: currentUser.value ? `${currentUser.value.displayName} ${currentUser.value.email}` : '',
    keywords: ['account', 'profile', 'user', 'logout', 'アカウント', 'プロフィール', 'ログアウト']
  }
}))

const shouldShowSection = (sectionKey: 'language' | 'pockito' | 'categories' | 'account') => {
  if (!searchQuery.value.trim()) {
    return true
  }
  
  const query = searchQuery.value.toLowerCase().trim()
  const section = sections.value[sectionKey]
  
  // Check title
  if (section.title.toLowerCase().includes(query)) {
    return true
  }
  
  // Check description
  if (section.description.toLowerCase().includes(query)) {
    return true
  }
  
  // Check keywords
  if (section.keywords.some(keyword => keyword.toLowerCase().includes(query))) {
    return true
  }
  
  return false
}

const hasVisibleSections = computed(() => {
  return shouldShowSection('language') ||
         shouldShowSection('pockito') ||
         shouldShowSection('categories') ||
         shouldShowSection('account')
})

const handleLogout = async () => {
  await authStore.logout()
}
</script>
