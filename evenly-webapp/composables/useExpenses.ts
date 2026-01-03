import type { Expense, CreateExpenseRequest, UpdateExpenseRequest } from '~/types/api'
import { useApi } from '~/utils/api'

export interface ExpenseFilters {
  startDate?: string
  endDate?: string
  categoryId?: string
  status?: 'ACTIVE' | 'SETTLED'
  limit?: number
  offset?: number
}

export const useExpenses = () => {
  const api = useApi()
  const expenses = ref<Expense[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchExpenses = async (workspaceId: string, filters?: ExpenseFilters) => {
    loading.value = true
    error.value = null
    try {
      const queryParams = new URLSearchParams()
      if (filters?.startDate) queryParams.append('startDate', filters.startDate)
      if (filters?.endDate) queryParams.append('endDate', filters.endDate)
      if (filters?.categoryId) queryParams.append('categoryId', filters.categoryId)
      if (filters?.status) queryParams.append('status', filters.status)
      if (filters?.limit) queryParams.append('limit', String(filters.limit))
      if (filters?.offset) queryParams.append('offset', String(filters.offset))

      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/expenses${query ? `?${query}` : ''}`
      expenses.value = await api.get<Expense[]>(path)
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

  return {
    expenses: readonly(expenses),
    loading: readonly(loading),
    error: readonly(error),
    fetchExpenses,
    getExpense,
    createExpense,
    updateExpense,
    deleteExpense,
    clearExpenses
  }
}

