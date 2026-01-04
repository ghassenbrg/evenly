<template>
  <div class="p-4 space-y-4">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-emerald-500/20 flex items-center justify-center">
          <svg class="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
          </svg>
        </div>
        <div>
          <h2 class="text-lg font-semibold text-white">{{ t('wallets.title') || 'Wallets' }}</h2>
          <p v-if="wallets.length > 0" class="text-sm text-white/60 mt-0.5">
            {{ wallets.length }} {{ wallets.length === 1 ? 'wallet' : 'wallets' }}
          </p>
        </div>
      </div>
      <button
        class="h-10 px-4 rounded-xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 transition-colors flex items-center gap-2 shadow-lg shadow-emerald-500/20"
        @click="openCreate"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        {{ t('wallets.add') || 'Add' }}
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading && wallets.length === 0" class="flex flex-col items-center justify-center py-16">
      <div class="animate-spin rounded-full h-10 w-10 border-2 border-emerald-500 border-t-transparent mb-4"></div>
      <p class="text-sm text-white/60">{{ t('common.loading') }}</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error && wallets.length === 0" class="rounded-2xl bg-red-500/10 border border-red-500/20 p-6">
      <div class="flex items-start gap-3">
        <svg class="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div class="flex-1">
          <p class="text-red-400 font-medium mb-1">{{ t('wallets.loadFailed') }}</p>
          <p class="text-red-300/80 text-sm mb-3">{{ error?.message || 'Unable to load wallets' }}</p>
          <button
            class="text-sm text-red-400 hover:text-red-300 font-medium underline"
            @click="load"
          >
            {{ t('common.retry') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="wallets.length === 0" class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 border border-slate-800 p-8 text-center">
      <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-slate-800/50 flex items-center justify-center">
        <svg class="w-8 h-8 text-white/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
        </svg>
      </div>
      <h3 class="text-base font-semibold text-white mb-2">{{ t('wallets.empty') || 'No wallets yet' }}</h3>
      <p class="text-sm text-white/60 mb-4">{{ t('wallets.emptyDescription') || 'Create your first wallet to start tracking your finances.' }}</p>
      <button
        class="h-10 px-4 rounded-xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 transition-colors"
        @click="openCreate"
      >
        {{ t('wallets.add') || 'Add Wallet' }}
      </button>
    </div>

    <!-- Wallets List -->
    <div v-else class="space-y-2">
      <div
        v-for="wallet in sortedWallets"
        :key="wallet.id"
        class="bg-white/5 hover:bg-white/8 rounded-lg transition-colors group relative"
      >
        <button
          type="button"
          @click="viewWallet(wallet.id)"
          class="w-full flex items-center justify-between py-3.5 px-2 pr-28 sm:pr-20"
        >
          <!-- Left Icon -->
          <div
            class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
            :style="wallet.color ? { background: wallet.color } : { background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)' }"
          >
            <svg class="w-5 h-5 text-white/80" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </div>

          <!-- Middle Text Block -->
          <div class="flex-1 min-w-0 px-3 text-left">
            <div class="flex items-center gap-2 mb-0.5">
              <div class="text-base font-medium text-white/90 text-left truncate">{{ wallet.name }}</div>
              <span
                v-if="wallet.isDefault"
                class="text-xs px-1.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-medium flex-shrink-0"
              >
                {{ t('wallets.default') || 'Default' }}
              </span>
            </div>
            <div class="text-sm text-white/55 mt-0.5 text-left">
              {{ wallet.currency }}
            </div>
            <div v-if="wallet.description" class="text-xs text-white/50 mt-1 text-left truncate">
              {{ wallet.description }}
            </div>
          </div>

          <!-- Right Amount -->
          <div class="text-base font-semibold text-white/85 flex-shrink-0 mr-20 sm:mr-0">
            {{ formatCurrency(wallet.balance, wallet.currency) }}
          </div>
        </button>

        <!-- Action Buttons - Right Side (Mobile-first: larger, always visible) -->
        <div class="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1.5 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
          <button
            class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-slate-700/50 hover:bg-slate-600/50 active:bg-slate-600 text-white/80 hover:text-white transition-colors flex items-center justify-center touch-manipulation shadow-lg"
            @click.stop="openEdit(wallet)"
            :title="t('common.edit') || 'Edit'"
          >
            <svg class="w-5 h-5 sm:w-4 sm:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </button>
          <button
            v-if="!wallet.isDefault"
            class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-slate-700/50 hover:bg-slate-600/50 active:bg-slate-600 text-white/80 hover:text-white transition-colors flex items-center justify-center touch-manipulation shadow-lg"
            @click.stop="setDefault(wallet.id)"
            :title="t('wallets.makeDefault') || 'Set default'"
          >
            <svg class="w-5 h-5 sm:w-4 sm:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </button>
          <button
            class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-red-500/20 hover:bg-red-500/30 active:bg-red-500/40 text-red-400 hover:text-red-300 transition-colors flex items-center justify-center touch-manipulation shadow-lg"
            @click.stop="confirmDelete(wallet.id)"
            :title="t('common.delete') || 'Delete'"
          >
            <svg class="w-5 h-5 sm:w-4 sm:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <BottomSheet v-model="showSheet" :title="sheetTitle">
      <WalletForm :wallet="selectedWallet" @saved="onSaved" @cancelled="closeSheet" />
    </BottomSheet>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

import BottomSheet from '~/components/BottomSheet.vue'
import WalletForm from '~/components/pockito/WalletForm.vue'
import { usePockitoWallets } from '~/composables/usePockitoWallets'
import { useWorkspacesStore } from '~/stores/workspaces'

const { t } = useI18n()
const { formatCurrency } = useFormatting()
const router = useRouter()
const toast = useToast()
const { wallets, loadWallets, loading, error, deleteWallet, setDefaultWallet } = usePockitoWallets()
const selectedWallet = ref(null as any)
const showSheet = ref(false)
const sheetTitle = computed(() => (selectedWallet.value ? t('wallets.editWallet') || 'Edit wallet' : t('wallets.newWallet') || 'New wallet'))

const load = async () => {
  await loadWallets()
}

onMounted(async () => {
  await load()
})

const sortedWallets = computed(() => {
  return [...wallets.value].sort((a, b) => {
    if (a.orderPosition == null && b.orderPosition == null) return 0
    if (a.orderPosition == null) return 1
    if (b.orderPosition == null) return -1
    return a.orderPosition - b.orderPosition
  })
})

const openCreate = () => {
  selectedWallet.value = null
  showSheet.value = true
}

const openEdit = (wallet: any) => {
  selectedWallet.value = wallet
  showSheet.value = true
}

const closeSheet = () => {
  showSheet.value = false
}

const onSaved = async () => {
  showSheet.value = false
  await load()
}

const confirmDelete = async (walletId: string) => {
  if (confirm(t('wallets.confirmDelete') || 'Delete this wallet?')) {
    try {
      await deleteWallet(walletId)
      toast.success(t('wallets.deleteSuccess') || 'Wallet deleted')
    } catch (err) {
      toast.error((err as Error)?.message || t('wallets.deleteFailed') || 'Failed to delete wallet')
    }
  }
}

const setDefault = async (walletId: string) => {
  try {
    await setDefaultWallet(walletId)
    toast.success(t('wallets.setDefaultSuccess') || 'Default wallet updated')
  } catch (err) {
    toast.error((err as Error)?.message || t('wallets.setDefaultFailed') || 'Failed to set default wallet')
  }
}

const viewWallet = (walletId: string) => {
  router.push(`/wallets/${walletId}`)
}

const workspacesStore = useWorkspacesStore()
watch(
  () => workspacesStore.activeWorkspaceId,
  async () => {
    await load()
  }
)
</script>
