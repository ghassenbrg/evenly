import type { AnalyticsSummary, CategoryAnalytics, Expense } from '~/types/api'
import { useApi } from '~/utils/api'

export const useAnalytics = () => {
  const api = useApi()
  const summary = ref<AnalyticsSummary | null>(null)
  const categoryAnalytics = ref<CategoryAnalytics[]>([])
  const recentExpenses = ref<Expense[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchSummary = async (workspaceId: string, startDate?: string, endDate?: string) => {
    loading.value = true
    error.value = null
    try {
      const queryParams = new URLSearchParams()
      if (startDate) queryParams.append('startDate', startDate)
      if (endDate) queryParams.append('endDate', endDate)
      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/analytics/summary${query ? `?${query}` : ''}`
      summary.value = await api.get<AnalyticsSummary>(path)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchCategoryAnalytics = async (workspaceId: string, startDate?: string, endDate?: string) => {
    loading.value = true
    error.value = null
    try {
      const queryParams = new URLSearchParams()
      if (startDate) queryParams.append('startDate', startDate)
      if (endDate) queryParams.append('endDate', endDate)
      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/analytics/categories${query ? `?${query}` : ''}`
      categoryAnalytics.value = await api.get<CategoryAnalytics[]>(path)
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
      recentExpenses.value = await api.get<Expense[]>(`/api/workspaces/${workspaceId}/analytics/recent?limit=${limit}`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clear = () => {
    summary.value = null
    categoryAnalytics.value = []
    recentExpenses.value = []
    error.value = null
  }

  return {
    summary: readonly(summary),
    categoryAnalytics: readonly(categoryAnalytics),
    recentExpenses: readonly(recentExpenses),
    loading: readonly(loading),
    error: readonly(error),
    fetchSummary,
    fetchCategoryAnalytics,
    fetchRecentExpenses,
    clear
  }
}

