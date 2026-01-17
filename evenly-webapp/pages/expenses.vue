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

      <div class="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-white/10 bg-slate-900/40 px-3 py-2">
        <label class="flex items-center gap-2 text-xs font-medium text-white/70">
          <input
            v-model="includeSettled"
            type="checkbox"
            class="h-4 w-4 rounded border-slate-600 bg-slate-800 text-emerald-500 focus:ring-emerald-500"
          />
          {{ t('expenses.includeSettled') }}
        </label>
        <button
          type="button"
          @click="handleSettlePeriod"
          :disabled="!canSettlePeriod || settling"
          class="inline-flex items-center gap-2 rounded-lg bg-amber-500/20 px-3 py-1.5 text-xs font-semibold text-amber-200 transition-colors hover:bg-amber-500/30 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11V7a4 4 0 00-8 0v4m1 0h14a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2z" />
          </svg>
          <span>{{ t('expenses.markSettled') }}</span>
        </button>
      </div>
      
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

    <!-- Edit Expense Sheet -->
    <ExpensesEditExpenseSheet
      v-if="activeWorkspaceId"
      v-model="showEditExpenseSheet"
      :workspace-id="activeWorkspaceId"
      :expense="selectedExpense"
      @expense-updated="handleExpenseUpdated"
      @expense-deleted="handleExpenseDeleted"
    />

    <!-- Create Expense Sheet -->
    <ExpensesCreateExpenseSheet
      v-if="activeWorkspaceId"
      v-model="showCreateExpenseSheet"
      :workspace-id="activeWorkspaceId"
      @expense-created="handleExpenseCreated"
    />

    <!-- Floating Action Button -->
    <button
      v-if="activeWorkspaceId"
      @click="showCreateExpenseSheet = true"
      class="fixed bottom-24 right-4 z-40 h-16 w-16 rounded-full bg-emerald-500 text-slate-900 font-bold hover:bg-emerald-400 active:bg-emerald-600 active:scale-95 transition-all shadow-xl shadow-emerald-500/40 flex items-center justify-center touch-manipulation mb-safe"
      :aria-label="t('expenses.addExpense') || 'Add expense'"
    >
      <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { endOfLocalDay, startOfLocalDay, toDateOnly } from '~/utils/date'
import { useWorkspacesStore } from '~/stores/workspaces'
import { useExpenses, type ExpenseFilters } from '~/composables/useExpenses'
import { useExpensesSummary, type ExpenseSummaryFilters } from '~/composables/useExpensesSummary'
import Skeleton from '~/components/Skeleton.vue'
import ExpensesCreateExpenseSheet from '~/components/expenses/CreateExpenseSheet.vue'
import type { Expense } from '~/types/api'
import { useSettlements } from '~/composables/useSettlements'
import { useToast } from '~/composables/useToast'

definePageMeta({
  middleware: 'auth',
  isMainPage: true
})

const { t, locale } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace, activeWorkspaceId } = storeToRefs(workspacesStore)
const { expenses, pageInfo, loading, error, fetchExpenses, loadMoreExpenses, clearExpenses } = useExpenses()
const { summary: expensesSummary, loading: summaryLoading, fetchExpensesSummary, clearSummary } = useExpensesSummary()
const { createSettlement, loading: settling } = useSettlements()
const { success, error: showError } = useToast()

const selectedPeriod = ref<'month' | 'week' | 'all' | 'custom'>('month')
const customDateRange = ref<{ start: string | null; end: string | null }>({ start: null, end: null })
const includeSettled = ref(false)

const pageSize = 10

const getDateRange = () => {
  const now = new Date()
  let start: Date
  let end: Date = endOfLocalDay(now)

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
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth() - 2, 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      break
    case 'custom':
      if (customDateRange.value.start && customDateRange.value.end) {
        start = startOfLocalDay(customDateRange.value.start)
        end = endOfLocalDay(customDateRange.value.end)
      } else {
        start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
        end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
      }
      break
    default:
      start = startOfLocalDay(new Date(now.getFullYear(), now.getMonth(), 1))
      end = endOfLocalDay(new Date(now.getFullYear(), now.getMonth() + 1, 0))
  }

  return {
    start: toDateOnly(start),
    end: toDateOnly(end)
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
    status: includeSettled.value ? undefined : 'ACTIVE'
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
    const dateKey = toDateOnly(expense.effectiveDate)
    
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
    status: includeSettled.value ? undefined : 'ACTIVE'
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

const selectedExpense = ref<Expense | null>(null)
const showEditExpenseSheet = ref(false)
const showCreateExpenseSheet = ref(false)

const handleExpenseClick = (expenseId: string) => {
  const expense = expenses.value.find(e => e.id === expenseId)
  if (expense) {
    selectedExpense.value = expense as Expense
    showEditExpenseSheet.value = true
  }
}

const handleExpenseUpdated = () => {
  // Reload expenses and summary after update
  loadExpenses(true)
}

const handleExpenseDeleted = () => {
  // Reload expenses and summary after delete
  loadExpenses(true)
}

const handleExpenseCreated = () => {
  // Reload expenses and summary after create
  loadExpenses(true)
}

const canSettlePeriod = computed(() => {
  if (!activeWorkspaceId.value) return false
  if (selectedPeriod.value === 'all') return false
  const { start, end } = getDateRange()
  return Boolean(start && end)
})

const handleSettlePeriod = async () => {
  if (!activeWorkspaceId.value) return
  if (selectedPeriod.value === 'all') {
    showError(t('expenses.settlementPeriodRequired'))
    return
  }
  const { start, end } = getDateRange()
  if (!start || !end) {
    showError(t('expenses.settlementPeriodRequired'))
    return
  }
  if (!confirm(t('expenses.settlementConfirm'))) return

  try {
    await createSettlement(activeWorkspaceId.value, {
      startDate: start,
      endDate: end
    })
    success(t('expenses.settlementCreated'))
    loadExpenses(true)
  } catch (err: any) {
    showError(err.message || t('expenses.settlementFailed'))
  }
}

watch(activeWorkspaceId, (newId) => {
  if (newId) {
    loadExpenses(true)
  }
})

watch(includeSettled, () => {
  if (activeWorkspaceId.value) {
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
