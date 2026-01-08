<template>
  <div class="p-4 space-y-4 pb-safe">
    <!-- Header -->
    <div class="flex items-center justify-between mb-2">
      <div class="flex items-center gap-3">
        <div class="w-12 h-12 rounded-2xl bg-emerald-500/20 flex items-center justify-center">
          <svg class="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </div>
        <div>
          <h2 class="text-xl font-bold text-white">{{ t('subscriptions.title') || 'Subscriptions' }}</h2>
          <p v-if="subscriptions && subscriptions.length > 0" class="text-sm text-white/60 mt-0.5">
            {{ subscriptions.length }} {{ subscriptions.length === 1 ? 'subscription' : 'subscriptions' }}
          </p>
        </div>
      </div>
      <button
        class="h-12 w-12 rounded-2xl bg-emerald-500 text-slate-900 font-semibold hover:bg-emerald-400 active:bg-emerald-600 active:scale-95 transition-all flex items-center justify-center shadow-lg shadow-emerald-500/20 touch-manipulation"
        @click="openCreate"
        :aria-label="t('subscriptions.add') || 'Add subscription'"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
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
      <div v-else class="space-y-3">
        <div
          v-for="sub in sortedSubscriptions"
          :key="sub.id"
          class="bg-gradient-to-br from-slate-800/50 to-slate-900/50 rounded-2xl transition-all active:scale-[0.98] touch-manipulation"
          :class="{
            'border-2 border-red-500/50 hover:border-red-500/70': sub.isActive && sub.nextDueDate && isOverdue(sub),
            'border-2 border-yellow-500/50 hover:border-yellow-500/70': sub.isActive && sub.nextDueDate && isDueSoon(sub) && !isOverdue(sub),
            'border border-slate-700/50 hover:border-slate-600/50': !(sub.isActive && sub.nextDueDate && (isOverdue(sub) || isDueSoon(sub)))
          }"
        >
          <button
            type="button"
            @click="openEdit(sub)"
            class="w-full flex items-center gap-4 p-4"
          >
            <!-- Left Icon -->
            <div
              class="w-14 h-14 rounded-2xl flex items-center justify-center flex-shrink-0 shadow-lg"
              style="background: linear-gradient(135deg, #10b981 0%, #059669 100%)"
            >
              <svg class="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            </div>

            <!-- Middle Text Block -->
            <div class="flex-1 min-w-0 text-left">
              <div class="flex items-center gap-2 mb-1 flex-wrap">
                <h3 class="text-lg font-semibold text-white truncate">{{ sub.name }}</h3>
                <span
                  :class="['text-xs px-2 py-0.5 rounded-full font-medium flex-shrink-0', sub.isActive ? 'bg-emerald-500/20 text-emerald-300' : 'bg-slate-700/50 text-white/60']"
                >
                  {{ sub.isActive ? (t('subscriptions.active') || 'Active') : (t('subscriptions.inactive') || 'Inactive') }}
                </span>
                <!-- Due Date Warning Indicators -->
                <span
                  v-if="sub.isActive && sub.nextDueDate && isOverdue(sub)"
                  class="text-xs px-2 py-0.5 rounded-full font-medium flex-shrink-0 bg-red-500/20 text-red-400 flex items-center gap-1"
                  :title="t('subscriptions.overdue') || 'Overdue'"
                >
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                  {{ t('subscriptions.overdue') || 'Overdue' }}
                </span>
                <span
                  v-else-if="sub.isActive && sub.nextDueDate && isDueSoon(sub)"
                  class="text-xs px-2 py-0.5 rounded-full font-medium flex-shrink-0 bg-yellow-500/20 text-yellow-400 flex items-center gap-1"
                  :title="t('subscriptions.dueSoon') || 'Due Soon'"
                >
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                  {{ t('subscriptions.dueSoon') || 'Due Soon' }}
                </span>
              </div>
              <p class="text-xl font-bold text-white mb-0.5">
                {{ formatCurrency(sub.amount, sub.currency) }}
              </p>
              <div class="flex items-center gap-2 text-sm text-white/60">
                <span>{{ formatFrequency(sub) }}</span>
                <span v-if="sub.nextDueDate" :class="{'text-red-400 font-semibold': isOverdue(sub), 'text-yellow-400 font-semibold': isDueSoon(sub)}">
                  · {{ formatDate(sub.nextDueDate) }}
                </span>
              </div>
            </div>

            <!-- Right Arrow -->
            <div class="flex-shrink-0">
              <svg class="w-6 h-6 text-white/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </button>

          <!-- Action Buttons - Bottom Row (Mobile-first: always visible) -->
          <div class="flex items-center gap-2 px-4 pb-4 border-t border-slate-700/50 pt-3">
            <button
              class="flex-1 h-11 rounded-xl bg-slate-700/50 hover:bg-slate-600/50 active:bg-slate-600 text-white/90 hover:text-white transition-all active:scale-95 flex items-center justify-center gap-2 touch-manipulation font-medium text-sm"
              @click.stop="openEdit(sub)"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              {{ t('common.edit') || 'Edit' }}
            </button>
            <button
              v-if="sub.isActive"
              class="flex-1 h-11 rounded-xl bg-emerald-500/20 hover:bg-emerald-500/30 active:bg-emerald-500/40 text-emerald-400 hover:text-emerald-300 transition-all active:scale-95 flex items-center justify-center gap-2 touch-manipulation font-medium text-sm"
              @click.stop="openPay(sub)"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v2a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              {{ t('subscriptions.payNow') || 'Pay' }}
            </button>
            <button
              class="h-11 w-11 rounded-xl bg-red-500/20 hover:bg-red-500/30 active:bg-red-500/40 text-red-400 hover:text-red-300 transition-all active:scale-95 flex items-center justify-center touch-manipulation"
              @click.stop="confirmDelete(sub.id)"
              :title="t('common.delete') || 'Delete'"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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

const sortedSubscriptions = computed(() => {
  if (!subscriptions?.value) return []
  
  return [...subscriptions.value].sort((a, b) => {
    // First, separate active and inactive
    if (a.isActive !== b.isActive) {
      // Active subscriptions come first
      return a.isActive ? -1 : 1
    }
    
    // Within the same active/inactive group, sort by nextDueDate ascending
    const dateA = a.nextDueDate ? new Date(a.nextDueDate).getTime() : Number.MAX_SAFE_INTEGER
    const dateB = b.nextDueDate ? new Date(b.nextDueDate).getTime() : Number.MAX_SAFE_INTEGER
    
    // If both have dates, sort ascending (earliest first)
    // If one doesn't have a date, it goes to the end
    if (dateA === Number.MAX_SAFE_INTEGER && dateB === Number.MAX_SAFE_INTEGER) {
      return 0 // Both missing dates, maintain order
    }
    if (dateA === Number.MAX_SAFE_INTEGER) return 1 // A missing, goes after B
    if (dateB === Number.MAX_SAFE_INTEGER) return -1 // B missing, goes after A
    
    return dateA - dateB // Ascending order (earliest first)
  })
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

const isOverdue = (sub: any): boolean => {
  if (!sub.nextDueDate || !sub.isActive) return false
  const dueDate = new Date(sub.nextDueDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  dueDate.setHours(0, 0, 0, 0)
  return dueDate < today
}

const isDueSoon = (sub: any): boolean => {
  if (!sub.nextDueDate || !sub.isActive || isOverdue(sub)) return false
  const dueDate = new Date(sub.nextDueDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  dueDate.setHours(0, 0, 0, 0)
  const diffTime = dueDate.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays >= 0 && diffDays <= 3
}

const workspacesStore = useWorkspacesStore()
watch(
  () => workspacesStore.activeWorkspaceId,
  async () => {
    await load()
  }
)
</script>
