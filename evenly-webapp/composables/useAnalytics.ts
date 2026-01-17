import type { BalanceSummary, ExpenseSnapshotResponse, ExpenseSnapshotItem, Expense, SettlementScope } from '~/types/api'
import { useApi } from '~/utils/api'

// Shared state - singleton pattern (like useToast)
const balanceSummary = ref<BalanceSummary | null>(null)
const expenseSnapshot = ref<ExpenseSnapshotResponse | null>(null)
const recentExpenses = ref<Expense[]>([])
const loading = ref(false)
const error = ref<Error | null>(null)

export const useAnalytics = () => {
  const api = useApi()

  // For backward compatibility with existing UI
  const summary = computed(() => {
    if (!balanceSummary.value) return null
    return {
      total: balanceSummary.value.totalAmount,
      sharedBudgetUsage: {
        limit: balanceSummary.value.budgetLimit,
        spent: balanceSummary.value.totalAmount,
        percentage: balanceSummary.value.spentPercentage,
        warning: balanceSummary.value.spentPercentage >= 80
      }
    }
  })

  const categoryAnalytics = computed(() => {
    if (!expenseSnapshot.value) return []
    return expenseSnapshot.value.data.map(item => ({
      categoryId: item.categoryId || '',
      total: item.totalAmount,
      count: item.expensesCount || 0,
      category: item.categoryId ? {
        id: item.categoryId,
        name: item.categoryName,
        icon: item.categoryIcon,
        color: item.categoryColor, // Include categoryColor from API
        workspaceId: '',
        slug: '',
        isActive: true,
        sortOrder: 0,
        createdAt: '',
        updatedAt: ''
      } : undefined
    }))
  })

  const fetchSummary = async (workspaceId: string, startDate?: string, endDate?: string) => {
    loading.value = true
    error.value = null
    try {
      // endpoints.json: GET /api/workspaces/{id}/analytics/balance-summary
      const queryParams = new URLSearchParams()
      if (startDate) queryParams.append('startDate', startDate)
      if (endDate) queryParams.append('endDate', endDate)
      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/analytics/balance-summary${query ? `?${query}` : ''}`
      balanceSummary.value = await api.get<BalanceSummary>(path)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchCategoryAnalytics = async (
    workspaceId: string,
    startDate?: string,
    endDate?: string,
    size?: number,
    settlementScope?: SettlementScope
  ) => {
    loading.value = true
    error.value = null
    try {
      // endpoints.json: GET /api/workspaces/{id}/analytics/expenses-snapshot
      const queryParams = new URLSearchParams()
      if (startDate) queryParams.append('startDate', startDate)
      if (endDate) queryParams.append('endDate', endDate)
      if (settlementScope) queryParams.append('settlementScope', settlementScope)
      // If size is 0 or undefined, don't add it (return all)
      // If size is > 0, add it as a query parameter
      if (size !== undefined && size > 0) {
        queryParams.append('size', size.toString())
      }
      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/analytics/expenses-snapshot${query ? `?${query}` : ''}`
      expenseSnapshot.value = await api.get<ExpenseSnapshotResponse>(path)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchRecentExpenses = async (workspaceId: string, limit: number = 5) => {
    loading.value = true
    error.value = null
    try {
      // endpoints.json: GET /api/workspaces/{id}/analytics/recent-expenses?size=3
      recentExpenses.value = await api.get<Expense[]>(`/api/workspaces/${workspaceId}/analytics/recent-expenses?size=${limit}`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clear = () => {
    balanceSummary.value = null
    expenseSnapshot.value = null
    recentExpenses.value = []
    error.value = null
  }

  return {
    // Backward compatibility
    summary: readonly(summary),
    categoryAnalytics: readonly(categoryAnalytics),
    // New API contract fields
    balanceSummary: readonly(balanceSummary),
    expenseSnapshot: readonly(expenseSnapshot),
    recentExpenses: readonly(recentExpenses),
    loading: readonly(loading),
    error: readonly(error),
    fetchSummary,
    fetchCategoryAnalytics,
    fetchRecentExpenses,
    clear
  }
}
