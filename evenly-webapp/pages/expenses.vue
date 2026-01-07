<template>
  <div class="p-4 space-y-4">
    <!-- Loading State with Skeleton -->
    <div v-if="loading && expenses.length === 0" class="space-y-4">
      <!-- ExpensesTotalCard handles its own skeleton -->
      <ExpensesTotalCard 
        :summary="null"
        :summary-loading="true"
        v-model="selectedPeriod"
        v-model:range="customDateRange"
        @period-change="handlePeriodChange"
      />
      <div class="space-y-2">
        <Skeleton v-for="i in 5" :key="i" variant="expense-item" />
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error && expenses.length === 0" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || t('expenses.loadFailed') }}</p>
      <button
        @click="() => loadExpenses(true)"
        class="mt-2 text-sm text-red-400 hover:text-red-300 underline"
      >
        {{ t('common.retry') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <ExpensesTotalCard 
        :summary="expensesSummary ? { ...expensesSummary, linearChartData: [...expensesSummary.linearChartData] } : null"
        :summary-loading="summaryLoading"
        v-model="selectedPeriod"
        v-model:range="customDateRange"
        @period-change="handlePeriodChange"
      />
      
      <!-- Expenses List by Day -->
      <div v-if="displayedGroups.length > 0" class="space-y-6">
        <template v-for="(group, groupIndex) in displayedGroups" :key="group.date">
          <!-- Day Header -->
          <div class="mb-1">
            <h3 class="text-xs font-medium text-white/50">{{ group.label }}</h3>
          </div>
          
          <!-- Expenses for this day -->
          <div class="space-y-2">
            <template v-for="(expense, expenseIndex) in group.expenses" :key="expense.id">
              <ExpensesExpenseItem
                :expense="expense as Expense"
                @click="handleExpenseClick"
              />
            </template>
          </div>
        </template>
      </div>

      <!-- Empty State -->
      <div v-else-if="!loading" class="text-center py-12">
        <p class="text-sm text-white/40">{{ t('expenses.noExpenses') }}</p>
      </div>
      
      <!-- Load More Trigger (for infinite scroll) -->
      <div
        v-if="hasMore && !loading"
        ref="loadMoreTrigger"
        class="flex justify-center pt-4 pb-4"
      >
        <div class="text-sm text-white/40">{{ t('expenses.loadingMore') }}</div>
      </div>
      
      <!-- Loading More Indicator with Skeleton -->
      <div v-if="loading && expenses.length > 0" class="space-y-2 pt-4">
        <Skeleton v-for="i in 3" :key="i" variant="expense-item" />
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
import { useExpenses, type ExpenseFilters } from '~/composables/useExpenses'
import { useExpensesSummary, type ExpenseSummaryFilters } from '~/composables/useExpensesSummary'
import Skeleton from '~/components/Skeleton.vue'
import type { Expense } from '~/types/api'

const { t, locale } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace, activeWorkspaceId } = storeToRefs(workspacesStore)
const { expenses, pageInfo, loading, error, fetchExpenses, loadMoreExpenses, clearExpenses } = useExpenses()
const { summary: expensesSummary, loading: summaryLoading, fetchExpensesSummary, clearSummary } = useExpensesSummary()

const selectedPeriod = ref<'month' | 'week' | 'all' | 'custom'>('month')
const customDateRange = ref<{ start: string | null; end: string | null }>({ start: null, end: null })

const pageSize = 10

const getDateRange = () => {
  const now = new Date()
  let start: Date
  let end: Date = new Date(now)

  switch (selectedPeriod.value) {
    case 'month':
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      break
    case 'week':
      const dayOfWeek = now.getDay()
      const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
      start = new Date(now.getFullYear(), now.getMonth(), diff)
      start.setHours(0, 0, 0, 0)
      end = new Date(now)
      end.setHours(23, 59, 59, 999)
      break
    case 'all':
      start = new Date(now.getFullYear(), now.getMonth() - 2, 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      break
    case 'custom':
      if (customDateRange.value.start && customDateRange.value.end) {
        start = new Date(customDateRange.value.start)
        start.setHours(0, 0, 0, 0)
        end = new Date(customDateRange.value.end)
        end.setHours(23, 59, 59, 999)
      } else {
        start = new Date(now.getFullYear(), now.getMonth(), 1)
        end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
      }
      break
    default:
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
  }

  return {
    start: start.toISOString().split('T')[0],
    end: end.toISOString().split('T')[0]
  }
}

const loadExpenses = async (reset = true) => {
  if (!activeWorkspaceId.value) return
  
  if (reset) {
    clearExpenses()
    clearSummary()
  }
  
  const { start, end } = getDateRange()
  const filters: ExpenseFilters = {
    page: reset ? 0 : (pageInfo.value?.number ?? 0),
    size: pageSize,
    sort: 'effectiveDate,DESC',
    startDate: start,
    endDate: end,
    status: 'ACTIVE'
  }
  
  // Load expenses and summary in parallel
  await Promise.all([
    fetchExpenses(activeWorkspaceId.value, filters, !reset),
    fetchExpensesSummary(activeWorkspaceId.value, { startDate: start, endDate: end })
  ])
}

const handlePeriodChange = (period: 'month' | 'week' | 'all' | 'custom', dateRange?: { start: string | null; end: string | null }) => {
  selectedPeriod.value = period
  if (dateRange) {
    customDateRange.value = dateRange
  }
  loadExpenses(true)
}

// Expenses are already sorted by API, so use them directly
const sortedExpenses = computed(() => {
  return expenses.value
})

// Group expenses by day
const groupedExpenses = computed(() => {
  const groups: Record<string, Expense[]> = {}
  
  sortedExpenses.value.forEach(expense => {
    const date = new Date(expense.effectiveDate)
    const dateKey = date.toISOString().split('T')[0]
    
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    // Cast to Expense to handle readonly types from composable
    groups[dateKey].push(expense as Expense)
  })
  
  return groups
})

// Format day label (Today, Yesterday, or date)
const formatDayLabel = (dateISO: string): string => {
  const date = new Date(dateISO)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const expenseDate = new Date(date)
  expenseDate.setHours(0, 0, 0, 0)
  
  const diffTime = today.getTime() - expenseDate.getTime()
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

// Get grouped expenses with labels
const expenseGroups = computed(() => {
  return Object.entries(groupedExpenses.value)
    .map(([date, expenses]) => ({
      date,
      label: formatDayLabel(date),
      expenses
    }))
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
})

// Get displayed groups (show all loaded expenses)
const displayedGroups = computed(() => {
  return expenseGroups.value
})

// Check if there are more expenses to load from API
const hasMore = computed(() => {
  if (!pageInfo.value) return false
  return pageInfo.value.number + 1 < pageInfo.value.totalPages
})

// Load more expenses from API
const loadMore = async () => {
  if (!activeWorkspaceId.value || !hasMore.value || loading.value) return
  
  const { start, end } = getDateRange()
  const filters: ExpenseFilters = {
    page: (pageInfo.value?.number ?? 0) + 1,
    size: pageSize,
    sort: 'effectiveDate,DESC',
    startDate: start,
    endDate: end,
    status: 'ACTIVE'
  }
  
  await loadMoreExpenses(activeWorkspaceId.value, filters)
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

const handleExpenseClick = (expenseId: string) => {
  // TODO: Navigate to expense detail page or open expense modal
  console.log('Expense clicked:', expenseId)
}

watch(activeWorkspaceId, (newId) => {
  if (newId) {
    loadExpenses(true)
  }
})

onMounted(() => {
  if (!activeWorkspace.value) {
    workspacesStore.fetchWorkspaces().then(() => {
      if (activeWorkspaceId.value) {
        loadExpenses(true)
      }
    })
  } else if (activeWorkspaceId.value) {
    loadExpenses(true)
  }
})
</script>
