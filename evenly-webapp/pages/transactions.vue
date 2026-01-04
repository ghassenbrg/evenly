<template>
  <div class="p-4 space-y-4">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-emerald-500/20 flex items-center justify-center">
          <svg class="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
        </div>
        <div>
          <h2 class="text-lg font-semibold text-white">{{ t('transactions.title') || 'Transactions' }}</h2>
          <p v-if="transactions.length > 0" class="text-sm text-white/60 mt-0.5">
            {{ transactions.length }} {{ transactions.length === 1 ? 'transaction' : 'transactions' }}
          </p>
        </div>
      </div>
      <button
        class="h-10 px-4 rounded-xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 transition-colors flex items-center gap-2 shadow-lg shadow-emerald-500/20"
        @click="openSheet()"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        {{ t('transactions.add') || 'Add' }}
      </button>
    </div>

    <!-- Quick Stats Summary -->
    <div v-if="transactions.length > 0 && !loading" class="grid grid-cols-3 gap-3">
      <div class="rounded-xl bg-gradient-to-br from-emerald-500/10 to-emerald-600/5 border border-emerald-500/20 p-3">
        <div class="flex items-center gap-2 mb-2">
          <div class="w-6 h-6 rounded-lg bg-emerald-500/20 flex items-center justify-center">
            <svg class="w-3.5 h-3.5 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          </div>
          <p class="text-xs text-emerald-300/80 font-medium">{{ t('transactions.stats.income') || 'Income' }}</p>
        </div>
        <p class="text-base font-bold text-emerald-400">{{ formatCurrency(stats.income, stats.currency) }}</p>
      </div>
      <div class="rounded-xl bg-gradient-to-br from-red-500/10 to-red-600/5 border border-red-500/20 p-3">
        <div class="flex items-center gap-2 mb-2">
          <div class="w-6 h-6 rounded-lg bg-red-500/20 flex items-center justify-center">
            <svg class="w-3.5 h-3.5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" />
            </svg>
          </div>
          <p class="text-xs text-red-300/80 font-medium">{{ t('transactions.stats.expense') || 'Expense' }}</p>
        </div>
        <p class="text-base font-bold text-red-400">{{ formatCurrency(stats.expense, stats.currency) }}</p>
      </div>
      <div class="rounded-xl bg-gradient-to-br from-slate-800/50 to-slate-900/50 border border-slate-700/50 p-3">
        <div class="flex items-center gap-2 mb-2">
          <div class="w-6 h-6 rounded-lg bg-slate-700/50 flex items-center justify-center">
            <svg class="w-3.5 h-3.5" :class="stats.net >= 0 ? 'text-emerald-400' : 'text-red-400'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
          <p class="text-xs text-white/60 font-medium">{{ t('transactions.stats.net') || 'Net' }}</p>
        </div>
        <p class="text-base font-bold" :class="stats.net >= 0 ? 'text-emerald-400' : 'text-red-400'">
          {{ formatCurrency(Math.abs(stats.net), stats.currency) }}
        </p>
      </div>
    </div>

    <!-- Filters -->
    <div class="flex gap-2">
      <div class="flex-1 relative">
        <select
          v-model="filters.walletId"
          class="w-full bg-slate-800/50 border border-slate-700/50 rounded-xl px-3 py-2.5 pl-10 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 transition-all"
          @change="reload"
        >
          <option value="">{{ t('transactions.filters.allWallets') || 'All Wallets' }}</option>
          <option v-for="wallet in wallets" :key="wallet.id" :value="wallet.id">
            {{ wallet.name }}
          </option>
        </select>
        <svg class="w-4 h-4 text-white/40 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
        </svg>
      </div>
      <div class="flex-1 relative">
        <select
          v-model="filters.transactionType"
          class="w-full bg-slate-800/50 border border-slate-700/50 rounded-xl px-3 py-2.5 pl-10 text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 transition-all"
          @change="reload"
        >
          <option value="">{{ t('transactions.filters.allTypes') || 'All Types' }}</option>
          <option v-for="type in transactionTypes" :key="type" :value="type">
            {{ type }}
          </option>
        </select>
        <svg class="w-4 h-4 text-white/40 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
        </svg>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading && transactions.length === 0" class="flex flex-col items-center justify-center py-16">
      <div class="animate-spin rounded-full h-10 w-10 border-2 border-emerald-500 border-t-transparent mb-4"></div>
      <p class="text-sm text-white/60">{{ t('common.loading') }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="transactions.length === 0" class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 border border-slate-800 p-8 text-center">
      <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-slate-800/50 flex items-center justify-center">
        <svg class="w-8 h-8 text-white/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
      </div>
      <h3 class="text-base font-semibold text-white mb-2">{{ t('transactions.empty') || 'No transactions yet' }}</h3>
      <p class="text-sm text-white/60 mb-4">{{ t('transactions.emptyDescription') || 'Add your first transaction to start tracking.' }}</p>
      <button
        class="h-10 px-4 rounded-xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 transition-colors"
        @click="openSheet()"
      >
        {{ t('transactions.add') || 'Add Transaction' }}
      </button>
    </div>

    <!-- Transactions List -->
    <div v-else class="space-y-2">
      <div
        v-for="tx in transactions"
        :key="tx.id"
        class="bg-white/5 hover:bg-white/8 rounded-lg transition-colors group relative"
      >
        <button
          type="button"
          @click="openSheet(tx)"
          class="w-full flex items-center justify-between py-3.5 px-2 pr-28 sm:pr-20"
        >
          <!-- Left Icon -->
          <div
            class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
            :style="{
              'background': tx.transactionType === 'INCOME' 
                ? 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
                : tx.transactionType === 'EXPENSE'
                ? 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)'
                : 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)'
            }"
          >
            <svg
              v-if="tx.transactionType === 'INCOME'"
              class="w-5 h-5 text-white/80"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
            <svg
              v-else-if="tx.transactionType === 'EXPENSE'"
              class="w-5 h-5 text-white/80"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" />
            </svg>
            <svg
              v-else
              class="w-5 h-5 text-white/80"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
            </svg>
          </div>

          <!-- Middle Text Block -->
          <div class="flex-1 min-w-0 px-3 text-left">
            <div class="text-base font-medium text-white/90 text-left truncate">{{ tx.categoryName || tx.subscriptionName || t('common.other') }}</div>
            <div class="text-sm text-white/55 mt-0.5 text-left">
              {{ formatDate(tx.effectiveDate) }}
            </div>
            <div v-if="tx.note" class="text-xs text-white/50 mt-1 text-left truncate">
              {{ tx.note }}
            </div>
          </div>

          <!-- Right Amount -->
          <div class="text-base font-semibold text-white/85 flex-shrink-0 mr-24 sm:mr-0">
            <span v-if="tx.transactionType === 'EXPENSE'">-</span>
            <span v-else-if="tx.transactionType === 'INCOME'">+</span>
            {{ formatCurrency(tx.amount, tx.walletFromCurrency || tx.walletToCurrency || 'USD') }}
          </div>
        </button>

        <!-- Action Buttons - Right Side (Mobile-first: larger, always visible) -->
        <div class="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1.5 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
          <button
            class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-slate-700/50 hover:bg-slate-600/50 active:bg-slate-600 text-white/80 hover:text-white transition-colors flex items-center justify-center touch-manipulation shadow-lg"
            @click.stop="openSheet(tx)"
            :title="t('common.edit') || 'Edit'"
          >
            <svg class="w-5 h-5 sm:w-4 sm:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </button>
          <button
            class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-red-500/20 hover:bg-red-500/30 active:bg-red-500/40 text-red-400 hover:text-red-300 transition-colors flex items-center justify-center touch-manipulation shadow-lg"
            @click.stop="confirmDeleteTransaction(tx.id)"
            :title="t('common.delete') || 'Delete'"
          >
            <svg class="w-5 h-5 sm:w-4 sm:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Load More -->
    <div v-if="hasMore" class="flex justify-center pt-2">
      <button
        class="px-4 py-2 text-sm text-emerald-400 hover:text-emerald-300 font-medium transition-colors"
        @click="loadNextPage"
      >
        {{ t('common.loadMore') || 'Load more' }}
        <svg class="w-4 h-4 inline-block ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
    </div>

    <BottomSheet v-model="showSheet" :title="sheetTitle">
      <TransactionForm :transaction="selectedTransaction" @saved="onSaved" @cancelled="closeSheet" />
    </BottomSheet>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

import BottomSheet from '~/components/BottomSheet.vue'
import TransactionForm from '~/components/pockito/TransactionForm.vue'
import { usePockitoTransactions } from '~/composables/usePockitoTransactions'
import { usePockitoWallets } from '~/composables/usePockitoWallets'
import { TransactionType } from '~/types/pockito'
import { useWorkspacesStore } from '~/stores/workspaces'

const { t } = useI18n()
const { formatCurrency, formatDate } = useFormatting()
const toast = useToast()
const { transactions, pageable, loadFirstPage, loadNextPage, deleteTransaction, loading, error } = usePockitoTransactions()
const { wallets, loadWallets } = usePockitoWallets()
const transactionTypes = Object.values(TransactionType)

// Calculate quick stats (using first wallet's currency or USD as default)
const stats = computed(() => {
  const defaultCurrency = wallets.value[0]?.currency || 'USD'
  
  // Group by currency and calculate totals
  const incomeByCurrency = new Map<string, number>()
  const expenseByCurrency = new Map<string, number>()
  
  transactions.value.forEach(tx => {
    const currency = tx.walletFromCurrency || tx.walletToCurrency || defaultCurrency
    if (tx.transactionType === 'INCOME') {
      incomeByCurrency.set(currency, (incomeByCurrency.get(currency) || 0) + tx.amount)
    } else if (tx.transactionType === 'EXPENSE') {
      expenseByCurrency.set(currency, (expenseByCurrency.get(currency) || 0) + tx.amount)
    }
  })
  
  // Use the most common currency or default
  const allCurrencies = [...new Set([
    ...Array.from(incomeByCurrency.keys()),
    ...Array.from(expenseByCurrency.keys())
  ])]
  const statsCurrency = allCurrencies.length > 0 ? allCurrencies[0] : defaultCurrency
  
  const income = incomeByCurrency.get(statsCurrency) || 0
  const expense = expenseByCurrency.get(statsCurrency) || 0
  
  return {
    income,
    expense,
    net: income - expense,
    currency: statsCurrency
  }
})

const filters = reactive<{ walletId?: string; transactionType?: TransactionType | '' }>({
  walletId: '',
  transactionType: ''
})

const showSheet = ref(false)
const selectedTransaction = ref(null as any)
const sheetTitle = computed(() =>
  selectedTransaction.value ? t('transactions.editTransaction') || 'Edit transaction' : t('transactions.newTransaction') || 'New transaction'
)

const load = async () => {
  await loadWallets()
  await loadFirstPage({ page: 0, size: 10, sort: ['effectiveDate,desc'] }, {
    walletId: filters.walletId || undefined,
    transactionType: (filters.transactionType as TransactionType) || undefined
  })
}

onMounted(async () => {
  await load()
})

const reload = async () => {
  await loadFirstPage({ page: 0, size: 10, sort: ['effectiveDate,desc'] }, {
    walletId: filters.walletId || undefined,
    transactionType: (filters.transactionType as TransactionType) || undefined
  })
}

const openSheet = (tx?: any) => {
  selectedTransaction.value = tx || null
  showSheet.value = true
}

const closeSheet = () => {
  showSheet.value = false
  selectedTransaction.value = null
}

const onSaved = async () => {
  showSheet.value = false
  await reload()
}

const confirmDeleteTransaction = async (transactionId: string) => {
  if (confirm(t('transactions.confirmDelete') || 'Delete this transaction?')) {
    try {
      await deleteTransaction(transactionId)
      toast.success(t('transactions.deleteSuccess') || 'Transaction deleted')
      await reload()
    } catch (err) {
      toast.error((err as Error)?.message || t('transactions.deleteFailed') || 'Failed to delete transaction')
    }
  }
}

const hasMore = computed(() => {
  if (!pageable.value) return false
  return (pageable.value.number || 0) < (pageable.value.totalPages || 1) - 1
})

const workspacesStore = useWorkspacesStore()
watch(
  () => workspacesStore.activeWorkspaceId,
  async () => {
    await reload()
  }
)
</script>
