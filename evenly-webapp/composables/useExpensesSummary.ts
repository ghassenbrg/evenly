import type { ExpenseSummary } from '~/types/api'
import { useApi } from '~/utils/api'

export interface ExpenseSummaryFilters {
  startDate?: string
  endDate?: string
}

export const useExpensesSummary = () => {
  const api = useApi()
  const summary = ref<ExpenseSummary | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchExpensesSummary = async (workspaceId: string, filters?: ExpenseSummaryFilters) => {
    loading.value = true
    error.value = null
    try {
      const queryParams = new URLSearchParams()
      if (filters?.startDate) queryParams.append('startDate', filters.startDate)
      if (filters?.endDate) queryParams.append('endDate', filters.endDate)

      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/analytics/expenses-summary${query ? `?${query}` : ''}`
      const response = await api.get<ExpenseSummary>(path)
      summary.value = response
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearSummary = () => {
    summary.value = null
    error.value = null
  }

  return {
    summary: readonly(summary),
    loading: readonly(loading),
    error: readonly(error),
    fetchExpensesSummary,
    clearSummary
  }
}

