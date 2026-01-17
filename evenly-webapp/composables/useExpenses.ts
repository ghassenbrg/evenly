import type { Expense, CreateExpenseRequest, UpdateExpenseRequest, PaginatedResponse, SettlementScope } from '~/types/api'
import { useApi } from '~/utils/api'

export interface ExpenseFilters {
  startDate?: string
  endDate?: string
  categoryId?: string
  settlementScope?: SettlementScope
  page?: number
  size?: number
  sort?: string // Format: "property,DIRECTION" e.g. "effectiveDate,DESC"
}

export const useExpenses = () => {
  const api = useApi()
  const expenses = ref<Expense[]>([])
  const pageInfo = ref<{ number: number; size: number; totalElements: number; totalPages: number } | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchExpenses = async (workspaceId: string, filters?: ExpenseFilters, append = false) => {
    loading.value = true
    error.value = null
    try {
      // endpoints.json: GET /api/workspaces/{id}/expenses?page=0&size=5&sort=effectiveDate,DESC
      const queryParams = new URLSearchParams()
      if (filters?.startDate) queryParams.append('startDate', filters.startDate)
      if (filters?.endDate) queryParams.append('endDate', filters.endDate)
      if (filters?.categoryId) queryParams.append('categoryId', filters.categoryId)
      if (filters?.settlementScope) queryParams.append('settlementScope', filters.settlementScope)
      if (filters?.page !== undefined) queryParams.append('page', String(filters.page))
      if (filters?.size !== undefined) queryParams.append('size', String(filters.size))
      if (filters?.sort) queryParams.append('sort', filters.sort)

      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/expenses${query ? `?${query}` : ''}`
      const response = await api.get<PaginatedResponse<Expense>>(path)
      
      if (append) {
        // Append new expenses for pagination
        expenses.value = [...expenses.value, ...response.data]
      } else {
        // Replace expenses for new search/filter
        expenses.value = response.data
      }
      pageInfo.value = response.page
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const getExpense = async (workspaceId: string, expenseId: string) => {
    loading.value = true
    error.value = null
    try {
      return await api.get<Expense>(`/api/workspaces/${workspaceId}/expenses/${expenseId}`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const createExpense = async (workspaceId: string, data: CreateExpenseRequest) => {
    loading.value = true
    error.value = null
    try {
      const expense = await api.post<Expense>(`/api/workspaces/${workspaceId}/expenses`, {
        ...data,
        workspaceId
      })
      expenses.value.unshift(expense)
      return expense
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateExpense = async (workspaceId: string, expenseId: string, data: UpdateExpenseRequest) => {
    loading.value = true
    error.value = null
    try {
      const expense = await api.put<Expense>(`/api/workspaces/${workspaceId}/expenses/${expenseId}`, data)
      const index = expenses.value.findIndex(e => e.id === expenseId)
      if (index !== -1) {
        expenses.value[index] = expense
      }
      return expense
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteExpense = async (workspaceId: string, expenseId: string) => {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/api/workspaces/${workspaceId}/expenses/${expenseId}`)
      expenses.value = expenses.value.filter(e => e.id !== expenseId)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearExpenses = () => {
    expenses.value = []
    error.value = null
  }

  const loadMoreExpenses = async (workspaceId: string, filters?: ExpenseFilters) => {
    if (!pageInfo.value) return
    
    const nextPage = pageInfo.value.number + 1
    if (nextPage >= pageInfo.value.totalPages) return
    
    const nextFilters = {
      ...filters,
      page: nextPage
    }
    
    await fetchExpenses(workspaceId, nextFilters, true)
  }

  return {
    expenses: readonly(expenses),
    pageInfo: readonly(pageInfo),
    loading: readonly(loading),
    error: readonly(error),
    fetchExpenses,
    loadMoreExpenses,
    getExpense,
    createExpense,
    updateExpense,
    deleteExpense,
    clearExpenses
  }
}
