<template>
  <div class="fixed top-4 right-4 z-50 pt-safe pr-safe">
    <div class="relative">
      <button
        @click="showMenu = !showMenu"
        class="flex items-center space-x-2 px-3 py-2 rounded-lg bg-slate-800/90 backdrop-blur-sm border border-slate-700 hover:bg-slate-700 transition-colors"
      >
        <svg class="w-4 h-4 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
        </svg>
        <span class="text-sm font-medium text-slate-300">{{ t('settings.' + currentLocale.toLowerCase()) }}</span>
        <svg class="w-4 h-4 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      <!-- Dropdown Menu -->
      <Transition name="fade">
        <div
          v-if="showMenu"
          class="absolute top-full right-0 mt-2 w-40 bg-slate-800 rounded-xl shadow-lg border border-slate-700 z-50"
          @click.stop
        >
          <div class="py-2">
            <button
              v-for="loc in availableLocales"
              :key="loc.code"
              @click="setLocale(loc.code)"
              :class="currentLocale === loc.code ? 'bg-slate-700/50 text-white' : 'text-slate-300 hover:bg-slate-700'"
              class="w-full text-left px-4 py-2 transition-colors flex items-center justify-between"
            >
              <span>{{ t(`settings.${loc.code}`) || loc.name }}</span>
              <svg
                v-if="currentLocale === loc.code"
                class="w-5 h-5 text-emerald-500 flex-shrink-0 ml-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </button>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
const { locale, locales, setLocale, t } = useI18n()
const showMenu = ref(false)

const currentLocale = computed(() => locale.value)
const availableLocales = computed(() => locales.value)

// Close menu when clicking outside
onMounted(() => {
  const handleClickOutside = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    if (!target.closest('.relative')) {
      showMenu.value = false
    }
  }
  document.addEventListener('click', handleClickOutside)
  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
  })
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.pt-safe {
  padding-top: env(safe-area-inset-top);
}

.pr-safe {
  padding-right: env(safe-area-inset-right);
}
</style>

