<template>
  <div class="p-4 space-y-4">
    <!-- Loading State -->
    <div v-if="loading && expenses.length === 0" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error && expenses.length === 0" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || 'Failed to load expenses' }}</p>
      <button
        @click="loadExpenses"
        class="mt-2 text-sm text-red-400 hover:text-red-300 underline"
      >
        Retry
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <ExpensesTotalCard 
        :expenses="expenses as Expense[]" 
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
      
      <!-- Loading More Indicator -->
      <div v-if="loading && expenses.length > 0" class="flex justify-center pt-4 pb-4">
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
  middleware: 'auth'
})

import { useWorkspacesStore } from '~/stores/workspaces'
import { useExpenses, type ExpenseFilters } from '~/composables/useExpenses'
import type { Expense } from '~/types/api'

const { t, locale } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace, activeWorkspaceId } = storeToRefs(workspacesStore)
const { expenses, loading, error, fetchExpenses, clearExpenses } = useExpenses()

const selectedPeriod = ref<'month' | 'week' | 'all' | 'custom'>('month')
const customDateRange = ref<{ start: string | null; end: string | null }>({ start: null, end: null })
const visibleCount = ref(10)
const itemsPerPage = 10

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

const loadExpenses = async () => {
  if (!activeWorkspaceId.value) return
  
  const { start, end } = getDateRange()
  const filters: ExpenseFilters = {
    startDate: start,
    endDate: end,
    status: 'ACTIVE'
  }
  
  await fetchExpenses(activeWorkspaceId.value, filters)
}

const handlePeriodChange = (period: 'month' | 'week' | 'all' | 'custom', dateRange?: { start: string | null; end: string | null }) => {
  selectedPeriod.value = period
  if (dateRange) {
    customDateRange.value = dateRange
  }
  visibleCount.value = itemsPerPage
  loadExpenses()
}

// Sort expenses by date (newest first)
const sortedExpenses = computed(() => {
  return [...expenses.value].sort((a, b) => {
    return new Date(b.date).getTime() - new Date(a.date).getTime()
  })
})

// Group expenses by day
const groupedExpenses = computed(() => {
  const groups: Record<string, Expense[]> = {}
  
  sortedExpenses.value.forEach(expense => {
    const date = new Date(expense.date)
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

// Get displayed groups (lazy loaded)
const displayedGroups = computed(() => {
  let totalCount = 0
  const result: typeof expenseGroups.value = []
  
  for (const group of expenseGroups.value) {
    if (totalCount >= visibleCount.value) break
    
    const remaining = visibleCount.value - totalCount
    if (group.expenses.length <= remaining) {
      result.push(group)
      totalCount += group.expenses.length
    } else {
      result.push({
        ...group,
        expenses: group.expenses.slice(0, remaining)
      })
      totalCount += remaining
    }
  }
  
  return result
})

// Check if there are more expenses to load
const hasMore = computed(() => {
  const totalExpenses = sortedExpenses.value.length
  return visibleCount.value < totalExpenses
})

// Load more expenses
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

const handleExpenseClick = (expenseId: string) => {
  // TODO: Navigate to expense detail page or open expense modal
  console.log('Expense clicked:', expenseId)
}

watch(activeWorkspaceId, () => {
  clearExpenses()
  visibleCount.value = itemsPerPage
  loadExpenses()
}, { immediate: true })

onMounted(() => {
  if (!activeWorkspace.value) {
    workspacesStore.fetchWorkspaces().then(() => {
      loadExpenses()
    })
  } else {
    loadExpenses()
  }
})
</script>
