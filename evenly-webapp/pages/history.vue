<template>
  <div class="p-4 space-y-4">
    <!-- Sort and Period Dropdowns -->
    <div class="flex items-center justify-between gap-3">
      <SortDropdown
        v-model:sort-by="sortBy"
        v-model:direction="sortDirection"
        @sort-change="handleSortChange"
      />
      <PeriodDropdown
        v-model="selectedPeriod"
        v-model:range="customDateRange"
        v-model:settlement-scope="settlementScope"
        @period-change="handlePeriodChange"
        @settlement-change="handleSettlementChange"
      />
    </div>

    <!-- Loading State with Skeleton -->
    <div v-if="loading && payments.length === 0" class="space-y-2">
      <Skeleton v-for="i in 5" :key="i" variant="list-item" />
    </div>

    <!-- Error State -->
    <div v-else-if="error && payments.length === 0" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || t('payments.loadFailed') }}</p>
      <button
        @click="loadPayments(true)"
        class="mt-2 text-sm text-red-400 hover:text-red-300 underline"
      >
        {{ t('common.retry') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <!-- Payments List by Day -->
      <div v-if="displayedGroups.length > 0" class="space-y-6">
        <template v-for="(group, groupIndex) in displayedGroups" :key="group.date">
          <!-- Day Header -->
          <div class="mb-1">
            <h3 class="text-xs font-medium text-white/50">{{ group.label }}</h3>
          </div>
          
          <!-- Payments for this day -->
          <div class="space-y-2">
            <template v-for="(payment, paymentIndex) in group.payments" :key="payment.id">
              <HistoryPaymentItem
                :payment="payment as Payment"
                @click="handlePaymentClick"
              />
            </template>
          </div>
        </template>
      </div>

      <!-- Empty State -->
      <div v-else-if="!loading" class="text-center py-12">
        <div class="flex flex-col items-center space-y-4">
          <div class="w-16 h-16 rounded-full bg-slate-800 flex items-center justify-center">
            <svg class="w-8 h-8 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p class="text-sm text-white/40">{{ t('payments.noPayments') }}</p>
        </div>
      </div>
      
      <!-- Load More Trigger (for infinite scroll) -->
      <div
        v-if="hasMore && !loading"
        ref="loadMoreTrigger"
        class="flex justify-center pt-4 pb-4"
      >
        <div class="text-sm text-white/40">{{ t('payments.loadingMore') }}</div>
      </div>
      
      <!-- Loading More Indicator with Skeleton -->
      <div v-if="loading && payments.length > 0" class="space-y-2 pt-4">
        <Skeleton v-for="i in 3" :key="i" variant="list-item" />
      </div>
      
      <!-- End of List -->
      <div v-else-if="displayedGroups.length > 0 && !hasMore" class="text-center py-8">
        <p class="text-sm text-white/40">{{ t('common.noMoreItems') }}</p>
      </div>
    </template>

    <!-- Edit Payment Sheet -->
    <HistoryEditPaymentSheet
      v-if="activeWorkspaceId"
      v-model="showEditPaymentSheet"
      :workspace-id="activeWorkspaceId"
      :payment="selectedPayment"
      @payment-updated="handlePaymentUpdated"
      @payment-deleted="handlePaymentDeleted"
    />
  </div>
</template>

<script setup lang="ts">
import { endOfLocalDay, startOfLocalDay, toDateOnly } from '~/utils/date'
import { useWorkspacesStore } from '~/stores/workspaces'
import { usePayments, type PaymentFilters } from '~/composables/usePayments'
import type { Payment, SettlementScope } from '~/types/api'

definePageMeta({
  middleware: 'auth',
  isMainPage: true
})

const { t, locale } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace, activeWorkspaceId } = storeToRefs(workspacesStore)
const { payments, pageInfo, loading, error, fetchPayments, loadMorePayments, clearPayments } = usePayments()

// Period dropdown state - default to "all"
const selectedPeriod = ref<'month' | 'week' | 'all' | 'custom'>('all')
const customDateRange = ref<{ start: string | null; end: string | null }>({ start: null, end: null })
const settlementScope = ref<SettlementScope>('ALL')

// Sort dropdown state
const sortBy = ref<'effectiveDate' | 'amount'>('effectiveDate')
const sortDirection = ref<'ASC' | 'DESC'>('DESC')

const pageSize = 10

const getDateRange = () => {
  const now = new Date()
  let start: Date | undefined
  let end: Date | undefined

  switch (selectedPeriod.value) {
    case 'month':
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      break
    case 'week':
      const dayOfWeek = now.getDay()
      const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), diff))
      end = endOfLocalDay(now)
      break
    case 'all':
      // For "all", don't set date filters (show all payments)
      return { start: undefined, end: undefined }
    case 'custom':
      if (customDateRange.value.start && customDateRange.value.end) {
        start = startOfLocalDay(customDateRange.value.start)
        end = endOfLocalDay(customDateRange.value.end)
      } else {
        return { start: undefined, end: undefined }
      }
      break
    default:
      return { start: undefined, end: undefined }
  }

  return {
    start: start ? toDateOnly(start) : undefined,
    end: end ? toDateOnly(end) : undefined
  }
}

const loadPayments = async (reset = true) => {
  if (!activeWorkspaceId.value) return
  
  if (reset) {
    clearPayments()
  }
  
  const { start, end } = getDateRange()
  if (selectedPeriod.value === 'custom' && (!start || !end)) {
    return
  }
  const filters: PaymentFilters = {
    page: reset ? 0 : (pageInfo.value?.number ?? 0),
    size: pageSize,
    sort: `${sortBy.value},${sortDirection.value}`,
    startDate: start,
    endDate: end,
    settlementScope: settlementScope.value
  }
  
  await fetchPayments(activeWorkspaceId.value, filters, !reset)
}

const handleSortChange = (newSortBy: 'effectiveDate' | 'amount', newDirection: 'ASC' | 'DESC') => {
  sortBy.value = newSortBy
  sortDirection.value = newDirection
  loadPayments(true)
}

const handlePeriodChange = (period: 'month' | 'week' | 'all' | 'custom', dateRange?: { start: string | null; end: string | null }) => {
  selectedPeriod.value = period
  
  // For custom period, only proceed if we have a valid range with both start and end
  if (period === 'custom') {
    if (dateRange && dateRange.start && dateRange.end) {
      customDateRange.value = dateRange
      loadPayments(true)
    }
    // If no valid range, just update the selected period but don't load data
    return
  }
  
  // For non-custom periods, proceed immediately
  if (dateRange) {
    customDateRange.value = dateRange
  }
  loadPayments(true)
}

const handleSettlementChange = (scope: SettlementScope) => {
  settlementScope.value = scope
  loadPayments(true)
}

// Sort payments by date (newest first) - using effectiveDate from API
const sortedPayments = computed(() => {
  return [...payments.value].sort((a, b) => {
    return new Date(b.effectiveDate).getTime() - new Date(a.effectiveDate).getTime()
  })
})

// Group payments by day
const groupedPayments = computed(() => {
  const groups: Record<string, Payment[]> = {}
  
  sortedPayments.value.forEach(payment => {
    const dateKey = toDateOnly(payment.effectiveDate)
    
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    groups[dateKey].push(payment as Payment)
  })
  
  return groups
})

// Format day label (Today, Yesterday, or date)
const formatDayLabel = (dateISO: string): string => {
  const date = startOfLocalDay(dateISO)
  const today = startOfLocalDay(new Date())
  
  const diffTime = today.getTime() - date.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  const currentLocale = locale.value === 'ja' ? 'ja-JP' : 'en-US'
  
  if (diffDays === 0) {
    return t('common.today')
  } else if (diffDays === 1) {
    return t('common.yesterday')
  } else if (diffDays < 7) {
    return date.toLocaleDateString(currentLocale, { weekday: 'long' })
  } else {
    return date.toLocaleDateString(currentLocale, { month: 'long', day: 'numeric', year: 'numeric' })
  }
}

// Get grouped payments with labels
const paymentGroups = computed(() => {
  return Object.entries(groupedPayments.value)
    .map(([date, payments]) => ({
      date,
      label: formatDayLabel(date),
      payments
    }))
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
})

// Get displayed groups (show all loaded payments)
const displayedGroups = computed(() => {
  return paymentGroups.value
})

// Check if there are more payments to load from API
const hasMore = computed(() => {
  if (!pageInfo.value) return false
  return pageInfo.value.number + 1 < pageInfo.value.totalPages
})

// Load more payments from API
const loadMore = async () => {
  if (!activeWorkspaceId.value || !hasMore.value || loading.value) return
  
  const { start, end } = getDateRange()
  if (selectedPeriod.value === 'custom' && (!start || !end)) {
    return
  }
  const filters: PaymentFilters = {
    page: (pageInfo.value?.number ?? 0) + 1,
    size: pageSize,
    sort: `${sortBy.value},${sortDirection.value}`,
    startDate: start,
    endDate: end,
    settlementScope: settlementScope.value
  }
  
  await loadMorePayments(activeWorkspaceId.value, filters)
}

const loadMoreTrigger = ref<HTMLElement | null>(null)

// Infinite scroll with Intersection Observer
if (process.client) {
  onMounted(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore.value && !loading.value) {
          loadMore()
        }
      },
      { rootMargin: '100px' }
    )
    
    watch(loadMoreTrigger, (el) => {
      if (el) {
        observer.observe(el)
      }
    }, { immediate: true })
    
    onUnmounted(() => {
      if (loadMoreTrigger.value) {
        observer.unobserve(loadMoreTrigger.value)
      }
      observer.disconnect()
    })
  })
}

const selectedPayment = ref<Payment | null>(null)
const showEditPaymentSheet = ref(false)

const handlePaymentClick = (paymentId: string) => {
  const payment = payments.value.find(p => p.id === paymentId)
  if (payment) {
    selectedPayment.value = payment as Payment
    showEditPaymentSheet.value = true
  }
}

const handlePaymentUpdated = () => {
  // Reload payments after update
  loadPayments(true)
}

const handlePaymentDeleted = () => {
  // Reload payments after delete
  loadPayments(true)
}

// Watch for workspace changes and refetch payments (but not on initial mount)
watch(activeWorkspaceId, () => {
  if (activeWorkspaceId.value) {
    selectedPeriod.value = 'all'
    customDateRange.value = { start: null, end: null }
    loadPayments(true)
  }
})

onMounted(() => {
  if (!activeWorkspace.value) {
    workspacesStore.fetchWorkspaces().then(() => {
      loadPayments(true)
    })
  } else {
    loadPayments(true)
  }
})
</script>
