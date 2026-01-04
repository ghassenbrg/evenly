<template>
  <div class="p-4 space-y-4">
    <!-- Loading State -->
    <div v-if="loading && payments.length === 0" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error && payments.length === 0" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || t('payments.loadFailed') }}</p>
      <button
        @click="loadPayments"
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
      
      <!-- Loading More Indicator -->
      <div v-if="loading && payments.length > 0" class="flex justify-center pt-4 pb-4">
        <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-emerald-500"></div>
      </div>
      
      <!-- End of List -->
      <div v-else-if="displayedGroups.length > 0 && !hasMore" class="text-center py-8">
        <p class="text-sm text-white/40">{{ t('common.noMoreItems') }}</p>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
  isMainPage: true
})

import { useWorkspacesStore } from '~/stores/workspaces'
import { usePayments, type PaymentFilters } from '~/composables/usePayments'
import type { Payment } from '~/types/api'

const { t, locale } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace, activeWorkspaceId } = storeToRefs(workspacesStore)
const { payments, loading, error, fetchPayments, clearPayments } = usePayments()

const visibleCount = ref(10)
const itemsPerPage = 10

const loadPayments = async () => {
  if (!activeWorkspaceId.value) return
  
  const filters: PaymentFilters = {
    sortBy: 'createdAt',
    sortOrder: 'desc'
  }
  
  await fetchPayments(activeWorkspaceId.value, filters)
}

// Sort payments by date (newest first)
const sortedPayments = computed(() => {
  return [...payments.value].sort((a, b) => {
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
})

// Group payments by day
const groupedPayments = computed(() => {
  const groups: Record<string, Payment[]> = {}
  
  sortedPayments.value.forEach(payment => {
    const date = new Date(payment.createdAt)
    const dateKey = date.toISOString().split('T')[0]
    
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    groups[dateKey].push(payment as Payment)
  })
  
  return groups
})

// Format day label (Today, Yesterday, or date)
const formatDayLabel = (dateISO: string): string => {
  const date = new Date(dateISO)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const paymentDate = new Date(date)
  paymentDate.setHours(0, 0, 0, 0)
  
  const diffTime = today.getTime() - paymentDate.getTime()
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

// Get displayed groups (lazy loaded)
const displayedGroups = computed(() => {
  let totalCount = 0
  const result: typeof paymentGroups.value = []
  
  for (const group of paymentGroups.value) {
    if (totalCount >= visibleCount.value) break
    
    const remaining = visibleCount.value - totalCount
    if (group.payments.length <= remaining) {
      result.push(group)
      totalCount += group.payments.length
    } else {
      result.push({
        ...group,
        payments: group.payments.slice(0, remaining)
      })
      totalCount += remaining
    }
  }
  
  return result
})

// Check if there are more payments to load
const hasMore = computed(() => {
  const totalPayments = sortedPayments.value.length
  return visibleCount.value < totalPayments
})

// Load more payments
const loadMore = () => {
  visibleCount.value += itemsPerPage
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

const handlePaymentClick = (paymentId: string) => {
  // TODO: Navigate to payment detail page or open payment modal
  console.log('Payment clicked:', paymentId)
}

// Watch for workspace changes and refetch payments
watch(activeWorkspaceId, () => {
  clearPayments()
  visibleCount.value = itemsPerPage
  loadPayments()
}, { immediate: true })

onMounted(() => {
  if (!activeWorkspace.value) {
    workspacesStore.fetchWorkspaces().then(() => {
      loadPayments()
    })
  } else {
    loadPayments()
  }
})
</script>
