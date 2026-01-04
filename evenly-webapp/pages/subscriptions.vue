<template>
  <div class="p-4 space-y-4">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-emerald-500/20 flex items-center justify-center">
          <svg class="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </div>
        <div>
          <h2 class="text-lg font-semibold text-white">{{ t('subscriptions.title') || 'Subscriptions' }}</h2>
          <p v-if="subscriptions && subscriptions.length > 0" class="text-sm text-white/60 mt-0.5">
            {{ subscriptions.length }} {{ subscriptions.length === 1 ? 'subscription' : 'subscriptions' }}
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
        {{ t('subscriptions.add') || 'Add' }}
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading && !subscriptions?.length" class="flex flex-col items-center justify-center py-16">
      <div class="animate-spin rounded-full h-10 w-10 border-2 border-emerald-500 border-t-transparent mb-4"></div>
      <p class="text-sm text-white/60">{{ t('common.loading') }}</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error && !subscriptions?.length" class="rounded-2xl bg-red-500/10 border border-red-500/20 p-6">
      <div class="flex items-start gap-3">
        <svg class="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div class="flex-1">
          <p class="text-red-400 font-medium mb-1">{{ t('subscriptions.loadFailed') }}</p>
          <p class="text-red-300/80 text-sm mb-3">{{ error?.message || 'Unable to load subscriptions' }}</p>
          <button
            class="text-sm text-red-400 hover:text-red-300 font-medium underline"
            @click="load"
          >
            {{ t('common.retry') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div v-else>
      <!-- Monthly Totals Summary -->
      <div v-if="totals.length" class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 border border-slate-800 p-4 mb-4">
        <p class="text-sm font-medium text-white/70 mb-3">{{ t('subscriptions.monthlyTotals') || 'Monthly total' }}</p>
        <div class="flex flex-wrap gap-2">
          <span
            v-for="item in totals"
            :key="item.currency"
            class="px-3 py-1.5 rounded-full bg-emerald-500/20 text-emerald-300 font-semibold text-sm"
          >
            {{ formatCurrency(item.total, item.currency) }}
          </span>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!subscriptions || subscriptions.length === 0" class="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 border border-slate-800 p-8 text-center">
        <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-slate-800/50 flex items-center justify-center">
          <svg class="w-8 h-8 text-white/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </div>
        <h3 class="text-base font-semibold text-white mb-2">{{ t('subscriptions.empty') || 'No subscriptions yet' }}</h3>
        <p class="text-sm text-white/60 mb-4">{{ t('subscriptions.emptyDescription') || 'Create your first subscription to track recurring payments.' }}</p>
        <button
          class="h-10 px-4 rounded-xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 transition-colors"
          @click="openCreate"
        >
          {{ t('subscriptions.add') || 'Add Subscription' }}
        </button>
      </div>

      <!-- Subscriptions List -->
      <div v-else class="space-y-2">
        <div
          v-for="sub in subscriptions"
          :key="sub.id"
          class="bg-white/5 hover:bg-white/8 rounded-lg transition-colors group relative"
        >
          <button
            type="button"
            @click="openEdit(sub)"
            class="w-full flex items-center justify-between py-3.5 px-2 pr-28 sm:pr-20"
          >
            <!-- Left Icon -->
            <div
              class="w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 shadow-lg"
              style="background: linear-gradient(135deg, #10b981 0%, #059669 100%)"
            >
              <svg class="w-5 h-5 text-white/80" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            </div>

            <!-- Middle Text Block -->
            <div class="flex-1 min-w-0 px-3 text-left">
              <div class="flex items-center gap-2 mb-0.5">
                <div class="text-base font-medium text-white/90 text-left truncate">{{ sub.name }}</div>
                <span
                  :class="['text-xs px-1.5 py-0.5 rounded-full font-medium flex-shrink-0', sub.isActive ? 'bg-emerald-500/20 text-emerald-300' : 'bg-slate-700/50 text-white/60']"
                >
                  {{ sub.isActive ? (t('subscriptions.active') || 'Active') : (t('subscriptions.inactive') || 'Inactive') }}
                </span>
              </div>
              <div class="text-sm text-white/55 mt-0.5 text-left">
                {{ formatFrequency(sub) }}
                <span v-if="sub.nextDueDate"> · {{ formatDate(sub.nextDueDate) }}</span>
              </div>
              <div v-if="sub.description" class="text-xs text-white/50 mt-1 text-left truncate">
                {{ sub.description }}
              </div>
            </div>

            <!-- Right Amount -->
            <div class="text-base font-semibold text-white/85 flex-shrink-0 mr-20 sm:mr-0">
              {{ formatCurrency(sub.amount, sub.currency) }}
            </div>
          </button>

          <!-- Action Buttons - Right Side (Mobile-first: larger, always visible) -->
          <div class="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1.5 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
            <button
              class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-slate-700/50 hover:bg-slate-600/50 active:bg-slate-600 text-white/80 hover:text-white transition-colors flex items-center justify-center touch-manipulation shadow-lg"
              @click.stop="openEdit(sub)"
              :title="t('common.edit') || 'Edit'"
            >
              <svg class="w-5 h-5 sm:w-4 sm:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button
              v-if="sub.isActive"
              class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-emerald-500/20 hover:bg-emerald-500/30 active:bg-emerald-500/40 text-emerald-400 hover:text-emerald-300 transition-colors flex items-center justify-center touch-manipulation shadow-lg"
              @click.stop="openPay(sub)"
              :title="t('subscriptions.payNow') || 'Pay'"
            >
              <svg class="w-5 h-5 sm:w-4 sm:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v2a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </button>
            <button
              class="h-10 w-10 sm:h-8 sm:w-8 rounded-lg bg-red-500/20 hover:bg-red-500/30 active:bg-red-500/40 text-red-400 hover:text-red-300 transition-colors flex items-center justify-center touch-manipulation shadow-lg"
              @click.stop="confirmDelete(sub.id)"
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

    <BottomSheet v-model="showForm" :title="formTitle">
      <SubscriptionForm :subscription="selectedSubscription" @saved="onSaved" @cancelled="closeForm" />
    </BottomSheet>

    <BottomSheet v-model="showPay" :title="t('subscriptions.pay.title') || 'Pay subscription'">
      <PaySubscriptionForm
        v-if="selectedSubscription"
        :subscription="selectedSubscription"
        @completed="onPaid"
        @cancelled="closePay"
      />
    </BottomSheet>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

import BottomSheet from '~/components/BottomSheet.vue'
import SubscriptionForm from '~/components/pockito/SubscriptionForm.vue'
import PaySubscriptionForm from '~/components/pockito/PaySubscriptionForm.vue'
import { usePockitoSubscriptions } from '~/composables/usePockitoSubscriptions'
import { useWorkspacesStore } from '~/stores/workspaces'

const { t } = useI18n()
const { formatCurrency, formatDate } = useFormatting()
const toast = useToast()
const {
  subscriptions,
  loadSubscriptions,
  deleteSubscription,
  loading,
  error
} = usePockitoSubscriptions()

const showForm = ref(false)
const showPay = ref(false)
const selectedSubscription = ref(null as any)

const formTitle = computed(() =>
  selectedSubscription.value ? t('subscriptions.editSubscription') || 'Edit subscription' : t('subscriptions.newSubscription') || 'New subscription'
)

const totals = computed(() => {
  if (!subscriptions?.value) return []
  const map = new Map<string, number>()
  subscriptions.value.forEach((sub) => {
    if (sub.monthlyEquivalentAmount !== undefined) {
      const current = map.get(sub.currency) || 0
      map.set(sub.currency, current + (sub.monthlyEquivalentAmount || 0))
    }
  })
  return Array.from(map.entries()).map(([currency, total]) => ({ currency, total }))
})

const load = async () => {
  await loadSubscriptions()
}

onMounted(async () => {
  await load()
})

const openCreate = () => {
  selectedSubscription.value = null
  showForm.value = true
}

const openEdit = (sub: any) => {
  selectedSubscription.value = sub
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
}

const onSaved = async () => {
  showForm.value = false
  await load()
}

const openPay = (sub: any) => {
  selectedSubscription.value = sub
  showPay.value = true
}

const closePay = () => {
  showPay.value = false
}

const onPaid = async () => {
  showPay.value = false
  await load()
}

const confirmDelete = async (id: string) => {
  if (confirm(t('subscriptions.confirmDelete') || 'Delete this subscription?')) {
    try {
      await deleteSubscription(id)
      toast.success(t('subscriptions.deleteSuccess') || 'Subscription deleted')
    } catch (err) {
      toast.error((err as Error)?.message || t('subscriptions.deleteFailed') || 'Failed to delete subscription')
    }
  }
}

const formatFrequency = (sub: any) => {
  if (sub.interval === 1) {
    return sub.frequency
  }
  return `${t('subscriptions.every') || 'Every'} ${sub.interval} ${sub.frequency.toLowerCase()}`
}

const workspacesStore = useWorkspacesStore()
watch(
  () => workspacesStore.activeWorkspaceId,
  async () => {
    await load()
  }
)
</script>
