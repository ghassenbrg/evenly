<template>
  <div class="p-4 space-y-4">
    <div v-if="walletLoading && !wallet" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <div v-else-if="walletError && !wallet" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ walletError?.message || t('wallets.loadFailed') }}</p>
      <button class="text-red-400 underline text-sm mt-2" @click="loadWalletData">
        {{ t('common.retry') }}
      </button>
    </div>

    <template v-else-if="wallet">
      <!-- Wallet Header Card -->
      <div class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 border border-slate-800 p-6 space-y-4">
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-4 flex-1 min-w-0">
            <!-- Wallet Icon -->
            <div
              class="w-16 h-16 rounded-2xl flex items-center justify-center flex-shrink-0 shadow-lg"
              :style="wallet.color ? { background: wallet.color } : { background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)' }"
            >
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
              </svg>
            </div>

            <!-- Wallet Info -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <h2 class="text-xl font-semibold text-white truncate">{{ wallet.name }}</h2>
                <span
                  v-if="wallet.isDefault"
                  class="text-xs px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-medium flex-shrink-0"
                >
                  {{ t('wallets.default') || 'Default' }}
                </span>
              </div>
              <p class="text-2xl font-bold text-white mb-1">
                {{ formatCurrency(wallet.balance, wallet.currency) }}
              </p>
              <p v-if="wallet.description" class="text-sm text-white/60 truncate">
                {{ wallet.description }}
              </p>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex gap-2 pt-2 border-t border-slate-800">
          <button
            class="flex-1 h-10 px-4 rounded-xl border border-slate-700 text-white/80 hover:border-slate-600 hover:text-white hover:bg-white/5 transition-colors font-medium text-sm"
            @click="openEdit"
          >
            {{ t('common.edit') || 'Edit' }}
          </button>
          <button
            class="flex-1 h-10 px-4 rounded-xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 transition-colors flex items-center justify-center gap-2"
            @click="openTransaction()"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            {{ t('transactions.add') || 'Add transaction' }}
          </button>
        </div>
      </div>

      <!-- Transactions Section -->
      <div class="space-y-3">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-white">{{ t('transactions.title') || 'Transactions' }}</h3>
          <button
            v-if="hasMore"
            class="text-sm text-emerald-400 hover:text-emerald-300 font-medium transition-colors"
            @click="loadMore"
          >
            {{ t('common.loadMore') || 'Load more' }}
          </button>
        </div>

        <!-- Loading State -->
        <div v-if="transactions.length === 0 && transactionsLoading" class="flex flex-col items-center justify-center py-12">
          <div class="animate-spin rounded-full h-8 w-8 border-2 border-emerald-500 border-t-transparent mb-3"></div>
          <p class="text-sm text-white/60">{{ t('common.loading') }}</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="transactions.length === 0" class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 border border-slate-800 p-8 text-center">
          <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-slate-800/50 flex items-center justify-center">
            <svg class="w-8 h-8 text-white/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <h4 class="text-base font-semibold text-white mb-2">{{ t('transactions.empty') || 'No transactions yet' }}</h4>
          <p class="text-sm text-white/60 mb-4">{{ t('transactions.emptyDescription') || 'Add your first transaction to start tracking.' }}</p>
          <button
            class="h-10 px-4 rounded-xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 transition-colors"
            @click="openTransaction"
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
              @click="openTransaction(tx)"
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
                {{ formatCurrency(tx.amount, tx.walletFromCurrency || tx.walletToCurrency || wallet.currency) }}
              </div>
            </button>

            <!-- Action Buttons - Right Side (Mobile-first: larger, always visible) -->
            <div class="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1.5 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
              <button
                class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-slate-700/50 hover:bg-slate-600/50 active:bg-slate-600 text-white/80 hover:text-white transition-colors flex items-center justify-center touch-manipulation shadow-lg"
                @click.stop="openTransaction(tx)"
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
      </div>
    </template>

    <BottomSheet v-model="showWalletSheet" :title="t('wallets.editWallet') || 'Edit wallet'">
      <WalletForm :wallet="wallet" @saved="onWalletSaved" @cancelled="closeWalletSheet" />
    </BottomSheet>

    <BottomSheet v-model="showTransactionSheet" :title="transactionSheetTitle">
      <TransactionForm
        :transaction="selectedTransaction"
        :initial-wallet-from-id="wallet?.id"
        @saved="onTransactionSaved"
        @cancelled="closeTransactionSheet"
      />
    </BottomSheet>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

import BottomSheet from '~/components/BottomSheet.vue'
import WalletForm from '~/components/pockito/WalletForm.vue'
import TransactionForm from '~/components/pockito/TransactionForm.vue'
import { usePockitoWallets } from '~/composables/usePockitoWallets'
import { usePockitoTransactions } from '~/composables/usePockitoTransactions'
import { useWorkspacesStore } from '~/stores/workspaces'

const route = useRoute()
const { t } = useI18n()
const { formatCurrency, formatDate } = useFormatting()
const toast = useToast()
const walletId = computed(() => route.params.id as string)
const { loadWallet, currentWallet, loading: walletLoading, error: walletError } = usePockitoWallets()
const {
  transactions,
  pageable,
  loadFirstPage,
  loadNextPage,
  deleteTransaction,
  loading: transactionsLoading,
  error: transactionsError
} = usePockitoTransactions()

const wallet = computed(() => currentWallet.value)
const showWalletSheet = ref(false)
const showTransactionSheet = ref(false)
const selectedTransaction = ref(null as any)

const transactionSheetTitle = computed(() => {
  // Explicitly check for null/undefined to ensure "Add transaction" shows when creating
  if (selectedTransaction.value === null || selectedTransaction.value === undefined) {
    return t('transactions.newTransaction') || 'Add transaction'
  }
  return t('transactions.editTransaction') || 'Edit transaction'
})

const loadWalletData = async () => {
  try {
    await loadWallet(walletId.value)
  } catch (err) {
    console.error('Failed to load wallet:', err)
    toast.error((err as Error)?.message || t('wallets.loadFailed') || 'Failed to load wallet')
  }
}

const loadTransactions = async () => {
  try {
    await loadFirstPage({ page: 0, size: 10, sort: ['effectiveDate,desc'] }, { walletId: walletId.value })
  } catch (err) {
    console.error('Failed to load transactions:', err)
    toast.error((err as Error)?.message || t('transactions.loadFailed') || 'Failed to load transactions')
  }
}

onMounted(async () => {
  await loadWalletData()
  if (wallet.value) {
    await loadTransactions()
  }
})

const workspacesStore = useWorkspacesStore()
watch(
  () => workspacesStore.activeWorkspaceId,
  async () => {
    await loadWalletData()
    if (wallet.value) {
      await loadTransactions()
    }
  }
)

const openEdit = () => {
  showWalletSheet.value = true
}

const closeWalletSheet = () => {
  showWalletSheet.value = false
}

const onWalletSaved = async () => {
  showWalletSheet.value = false
  await loadWalletData()
}

const openTransaction = (tx?: any) => {
  // Check if tx is actually a transaction object (has an id property)
  // If it's an event object or undefined, treat it as creating new transaction
  if (tx && typeof tx === 'object' && 'id' in tx && tx.id) {
    // Editing existing transaction
    selectedTransaction.value = tx
  } else {
    // Creating new transaction - explicitly clear
    selectedTransaction.value = null
  }
  
  // Open the sheet - the computed title will react to selectedTransaction
  showTransactionSheet.value = true
}

const closeTransactionSheet = () => {
  showTransactionSheet.value = false
  selectedTransaction.value = null
}

const onTransactionSaved = async () => {
  showTransactionSheet.value = false
  await loadTransactions()
}

const confirmDeleteTransaction = async (transactionId: string) => {
  if (confirm(t('transactions.confirmDelete') || 'Delete this transaction?')) {
    try {
      await deleteTransaction(transactionId)
      toast.success(t('transactions.deleteSuccess') || 'Transaction deleted')
      await loadTransactions()
    } catch (err) {
      toast.error((err as Error)?.message || t('transactions.deleteFailed') || 'Failed to delete transaction')
    }
  }
}

const loadMore = async () => {
  await loadNextPage()
}

const hasMore = computed(() => {
  if (!pageable.value) return false
  return (pageable.value.number || 0) < (pageable.value.totalPages || 1) - 1
})


watch(
  () => route.params.id,
  async (newId) => {
    if (typeof newId === 'string') {
      try {
        await loadWallet(newId)
        if (currentWallet.value) {
          await loadFirstPage({ page: 0, size: 10, sort: ['effectiveDate,desc'] }, { walletId: newId })
        }
      } catch (err) {
        console.error('Failed to load wallet:', err)
        toast.error((err as Error)?.message || t('wallets.loadFailed') || 'Failed to load wallet')
      }
    }
  }
)
</script>
