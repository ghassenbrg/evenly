<template>
  <div class="p-4 space-y-6">
    <!-- Description -->
    <p class="text-slate-400 text-sm">{{ t('settings.pockito.description') }}</p>

    <!-- Features Grid -->
    <div class="space-y-4">
      <h2 class="text-white font-semibold text-lg">{{ t('settings.pockito.features') }}</h2>
      
      <!-- Wallets Feature Card -->
      <NuxtLink
        to="/wallets"
        class="block bg-gradient-to-br from-slate-800/50 to-slate-900/50 border border-slate-700/50 hover:border-emerald-500/50 rounded-2xl p-5 transition-all active:scale-[0.98]"
      >
        <div class="flex items-start gap-4">
          <div class="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center flex-shrink-0">
            <svg class="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="text-lg font-semibold text-white mb-1">{{ t('wallets.title') }}</h3>
            <p class="text-sm text-slate-400 mb-3">{{ t('settings.pockito.walletsDescription') }}</p>
            <div class="flex items-center text-emerald-400 text-sm font-medium">
              <span>Open Wallets</span>
              <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </div>
        </div>
      </NuxtLink>

      <!-- Transactions Feature Card -->
      <NuxtLink
        to="/transactions"
        class="block bg-gradient-to-br from-slate-800/50 to-slate-900/50 border border-slate-700/50 hover:border-emerald-500/50 rounded-2xl p-5 transition-all active:scale-[0.98]"
      >
        <div class="flex items-start gap-4">
          <div class="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center flex-shrink-0">
            <svg class="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="text-lg font-semibold text-white mb-1">{{ t('transactions.title') }}</h3>
            <p class="text-sm text-slate-400 mb-3">{{ t('settings.pockito.transactionsDescription') }}</p>
            <div class="flex items-center text-emerald-400 text-sm font-medium">
              <span>Open Transactions</span>
              <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </div>
        </div>
      </NuxtLink>

      <!-- Subscriptions Feature Card -->
      <NuxtLink
        to="/subscriptions"
        class="block bg-gradient-to-br from-slate-800/50 to-slate-900/50 border border-slate-700/50 hover:border-emerald-500/50 rounded-2xl p-5 transition-all active:scale-[0.98]"
      >
        <div class="flex items-start gap-4">
          <div class="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center flex-shrink-0">
            <svg class="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="text-lg font-semibold text-white mb-1">{{ t('subscriptions.title') }}</h3>
            <p class="text-sm text-slate-400 mb-3">{{ t('settings.pockito.subscriptionsDescription') }}</p>
            <div class="flex items-center text-emerald-400 text-sm font-medium">
              <span>Open Subscriptions</span>
              <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </div>
        </div>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  isMainPage: false
})

const { t } = useI18n()
</script>
