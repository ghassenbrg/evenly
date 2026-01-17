<template>
  <div class="p-4 space-y-4">
    <!-- Error State -->
    <div v-if="error" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || t('common.loadDashboardFailed') }}</p>
      <button
        @click="() => loadDashboard()"
        class="mt-2 text-sm text-red-400 hover:text-red-300 underline"
      >
        {{ t('common.retry') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <DashboardBalanceCard
        :balance-summary="balanceSummary"
        :is-personal="activeWorkspace?.isPersonal || false"
        :workspace-id="activeWorkspaceId || undefined"
        :loading="balanceLoading"
        @settle-up="showSettleUp = true"
        @add-expense="showCreateExpenseSheet = true"
      />
      
      <DashboardSettleUpSheet
        v-if="activeWorkspaceId"
        v-model="showSettleUp"
        :workspace-id="activeWorkspaceId"
        @settled="loadDashboard()"
        @open-payment="handleOpenPayment"
      />
      
      <DashboardPaymentSheet
        v-if="activeWorkspaceId && selectedBalance"
        v-model="showPaymentSheet"
        :workspace-id="activeWorkspaceId"
        :balance="selectedBalance"
        :current-user-balance="selectedCurrentUserBalance"
        :current-user-total-balance="summary?.total"
        @payment-completed="handlePaymentCompleted"
      />
      
      <DashboardExpenseSnapshotCard
        :items="expenseSnapshotItems"
        :others-count="othersCount"
        :others-percent="othersPercent"
        :others-color="othersColor"
        :loading="expenseSnapshotLoading"
        @open-all-categories="handleOpenAllCategories"
      />
      
      <DashboardCategoriesBreakdownCard
        :items="categoryItems"
        :total-categories="categories.length"
        :loading="categoriesLoading"
        @open-all-categories="handleOpenAllCategories"
      />
      
      <DashboardAllCategoriesSnapshotSheet
        v-if="activeWorkspaceId"
        v-model="showAllCategoriesSheet"
        :workspace-id="activeWorkspaceId"
        :start-date="allCategoriesStartDate"
        :end-date="allCategoriesEndDate"
      />
      
      <DashboardRecentExpensesCard
        :expenses="recentExpenses as Expense[]"
        :loading="recentExpensesLoading"
        @open-expense="handleExpenseClick"
      />
      
      <ExpensesCreateExpenseSheet
        v-if="activeWorkspaceId"
        v-model="showCreateExpenseSheet"
        :workspace-id="activeWorkspaceId"
        @expense-created="handleExpenseCreated"
      />
      
      <ExpensesEditExpenseSheet
        v-if="activeWorkspaceId"
        v-model="showEditExpenseSheet"
        :workspace-id="activeWorkspaceId"
        :expense="selectedExpense"
        @expense-updated="handleExpenseUpdated"
        @expense-deleted="handleExpenseDeleted"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { endOfLocalDay, startOfLocalDay, toDateOnly } from '~/utils/date'
import { useWorkspacesStore } from '~/stores/workspaces'
import { useAnalytics } from '~/composables/useAnalytics'
import { useCategories } from '~/composables/useCategories'
import { useFormatting } from '~/composables/useFormatting'
import { useCategoryColor } from '~/composables/useCategoryColor'
import ExpensesCreateExpenseSheet from '~/components/expenses/CreateExpenseSheet.vue'
import ExpensesEditExpenseSheet from '~/components/expenses/EditExpenseSheet.vue'
import type { Expense, Balance } from '~/types/api'

definePageMeta({
  middleware: 'auth',
  scrollToTop: false,
  isMainPage: true
})

const { t } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace, activeWorkspaceId } = storeToRefs(workspacesStore)
const { summary, balanceSummary, categoryAnalytics, expenseSnapshot, recentExpenses, loading, error, fetchSummary, fetchCategoryAnalytics, fetchRecentExpenses, clear } = useAnalytics()
const { categories, loading: categoriesLoading, fetchCategories } = useCategories()
const { formatCurrency } = useFormatting()
const { colorToGradient } = useCategoryColor()

// Track individual loading states - initialize as true for initial load
const balanceLoading = ref(true)
const expenseSnapshotLoading = ref(true)
const recentExpensesLoading = ref(true)

const showSettleUp = ref(false)
const showPaymentSheet = ref(false)
const showAllCategoriesSheet = ref(false)
const showCreateExpenseSheet = ref(false)
const showEditExpenseSheet = ref(false)
const selectedBalance = ref<Balance | null>(null)
const selectedCurrentUserBalance = ref<Balance | null>(null)
const selectedExpense = ref<Expense | null>(null)

// Track date range for the all categories sheet (from the card's period selector)
const allCategoriesStartDate = ref<string | undefined>(undefined)
const allCategoriesEndDate = ref<string | undefined>(undefined)

const handleOpenAllCategories = (startDate?: string, endDate?: string) => {
  allCategoriesStartDate.value = startDate
  allCategoriesEndDate.value = endDate
  showAllCategoriesSheet.value = true
}

const handleOpenPayment = (balance: Balance, currentUserBalance?: Balance | null) => {
  selectedBalance.value = balance
  selectedCurrentUserBalance.value = currentUserBalance || null
  showPaymentSheet.value = true
}

const handlePaymentCompleted = () => {
  // Reload dashboard data after payment
  loadDashboard()
  // Emit settled event to SettleUpSheet if it's open
  // (The @settled listener will also call loadDashboard, but that's fine - it's idempotent)
}

const handleExpenseCreated = () => {
  // Reload dashboard data after expense creation
  loadDashboard()
}

const handleExpenseClick = (expenseId: string) => {
  const expense = recentExpenses.value.find(e => e.id === expenseId)
  if (expense) {
    selectedExpense.value = expense as Expense
    showEditExpenseSheet.value = true
  }
}

const handleExpenseUpdated = () => {
  // Reload dashboard data after expense update
  loadDashboard()
}

const handleExpenseDeleted = () => {
  // Reload dashboard data after expense delete
  loadDashboard()
}

const getDateRange = (period: 'month' | 'week' | 'all' | 'custom', customRange?: { start: string | null; end: string | null }) => {
  const now = new Date()
  let start: Date
  let end: Date = endOfLocalDay(now)

  switch (period) {
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
      if (customRange?.start && customRange?.end) {
        start = startOfLocalDay(customRange.start)
        end = endOfLocalDay(customRange.end)
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

const loadDashboard = async (period: 'month' | 'week' | 'all' | 'custom' = 'month', customRange?: { start: string | null; end: string | null }) => {
  if (!activeWorkspaceId.value) return
  
  const { start, end } = getDateRange(period, customRange)
  
  // Load data with individual loading states
  balanceLoading.value = true
  expenseSnapshotLoading.value = true
  recentExpensesLoading.value = true
  
  try {
    await Promise.all([
      fetchSummary(activeWorkspaceId.value, start, end).finally(() => { balanceLoading.value = false }),
      fetchCategoryAnalytics(activeWorkspaceId.value, start, end, 4).finally(() => { expenseSnapshotLoading.value = false }),
      fetchRecentExpenses(activeWorkspaceId.value, 5).finally(() => { recentExpensesLoading.value = false }),
      fetchCategories()
    ])
  } catch (err) {
    balanceLoading.value = false
    expenseSnapshotLoading.value = false
    recentExpensesLoading.value = false
  }
}


const expenseSnapshotItems = computed(() => {
  // Use expenseSnapshot data directly if available (preferred)
  if (expenseSnapshot.value && expenseSnapshot.value.data.length > 0) {
    return expenseSnapshot.value.data
      .filter(item => item.categoryId !== null)
      .slice(0, 4)
      .map(item => ({
        key: item.categoryId || 'others',
        label: item.categoryName || t('common.unknown'),
        percent: item.spentPercentage || 0,
        count: item.expensesCount || 0,
        color: colorToGradient(item.categoryColor || '#64748b'),
        iconClass: item.categoryIcon || 'fa-solid fa-ellipsis'
      }))
  }
  
  // Fallback to categoryAnalytics if expenseSnapshot is not available
  if (!categoryAnalytics.value.length) return []
  
  const total = categoryAnalytics.value.reduce((sum, item) => sum + item.total, 0)
  const topItems = categoryAnalytics.value
    .slice(0, 4)
    .map(item => ({
      key: item.categoryId,
      label: item.category?.name || t('common.unknown'),
      percent: total > 0 ? Math.round((item.total / total) * 100) : 0,
      count: item.count,
      color: colorToGradient(item.category?.color || '#64748b'),
      iconClass: item.category?.icon || 'fa-solid fa-ellipsis'
    }))
  
  return topItems
})

const othersCount = computed(() => {
  if (categoryAnalytics.value.length <= 4) return 0
  return categoryAnalytics.value.slice(4).reduce((sum, item) => sum + item.count, 0)
})

const othersPercent = computed(() => {
  if (!categoryAnalytics.value.length) return 0
  const total = categoryAnalytics.value.reduce((sum, item) => sum + item.total, 0)
  const topTotal = categoryAnalytics.value.slice(0, 4).reduce((sum, item) => sum + item.total, 0)
  return total > 0 ? Math.round(((total - topTotal) / total) * 100) : 0
})

const othersColor = 'linear-gradient(135deg, #64748b 0%, #475569 100%)'

const categoryItems = computed(() => {
  // Use expenseSnapshot data directly if available (preferred)
  if (expenseSnapshot.value && expenseSnapshot.value.data.length > 0) {
    return expenseSnapshot.value.data
      .filter(item => item.categoryId !== null)
      .sort((a, b) => (b.totalAmount || 0) - (a.totalAmount || 0))
      .slice(0, 4)
      .map(item => ({
        id: item.categoryId || '',
        name: item.categoryName || t('common.unknown'),
        iconClass: item.categoryIcon || 'fa-solid fa-ellipsis',
        expenseCount: item.expensesCount || 0,
        totalAmount: item.totalAmount || 0,
        accent: getAccentFromColor(item.categoryColor || '#64748b') as 'green' | 'rose' | 'sky' | 'indigo',
        color: item.categoryColor || '#64748b'
      }))
  }
  
  // Fallback to categoryAnalytics
  return [...categoryAnalytics.value]
    .sort((a, b) => b.total - a.total)
    .slice(0, 4)
    .map(item => ({
      id: item.categoryId,
      name: item.category?.name || t('common.unknown'),
      iconClass: item.category?.icon || 'fa-solid fa-ellipsis',
      expenseCount: item.count,
      totalAmount: item.total,
      accent: getAccentFromColor(item.category?.color || '#64748b') as 'green' | 'rose' | 'sky' | 'indigo',
      color: item.category?.color || '#64748b'
    }))
})

const getAccentFromColor = (color: string): 'green' | 'rose' | 'sky' | 'indigo' => {
  if (color.includes('10b981') || color.includes('059669')) return 'green'
  if (color.includes('f43f5e') || color.includes('e11d48')) return 'rose'
  if (color.includes('0ea5e9') || color.includes('0284c7')) return 'sky'
  if (color.includes('6366f1') || color.includes('4f46e5')) return 'indigo'
  return 'green'
}

// Watch for workspace changes and reload dashboard
watch(activeWorkspaceId, (newWorkspaceId, oldWorkspaceId) => {
  // Only reload if workspace actually changed and is valid
  if (!newWorkspaceId) return
  if (newWorkspaceId === oldWorkspaceId) return
  
  // Set loading states before clearing to show skeletons
  balanceLoading.value = true
  expenseSnapshotLoading.value = true
  recentExpensesLoading.value = true
  clear()
  loadDashboard()
}, { immediate: false })

// Initial load on mount
onMounted(() => {
  if (!activeWorkspace.value) {
    workspacesStore.fetchWorkspaces().then(() => {
      // Ensure activeWorkspaceId is set and load dashboard
      if (activeWorkspaceId.value) {
        loadDashboard()
      }
    }).catch((err) => {
      console.error('Failed to fetch workspaces:', err)
    })
  } else if (activeWorkspaceId.value) {
    // Load dashboard if workspace is already available on mount
    loadDashboard()
  }
})
</script>
