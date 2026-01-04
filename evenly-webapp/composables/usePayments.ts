import type { Payment, PaymentStatus, PaymentType } from '~/types/api'
import { useApi } from '~/utils/api'

export interface PaymentFilters {
  startDate?: string
  endDate?: string
  status?: PaymentStatus
  type?: PaymentType
  limit?: number
  offset?: number
  sortBy?: 'createdAt' | 'amount'
  sortOrder?: 'asc' | 'desc'
}

export const usePayments = () => {
  const api = useApi()
  const payments = ref<Payment[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchPayments = async (workspaceId: string, filters?: PaymentFilters) => {
    loading.value = true
    error.value = null
    try {
      const queryParams = new URLSearchParams()
      if (filters?.startDate) queryParams.append('startDate', filters.startDate)
      if (filters?.endDate) queryParams.append('endDate', filters.endDate)
      if (filters?.status) queryParams.append('status', filters.status)
      if (filters?.type) queryParams.append('type', filters.type)
      if (filters?.limit) queryParams.append('limit', String(filters.limit))
      if (filters?.offset) queryParams.append('offset', String(filters.offset))
      if (filters?.sortBy) queryParams.append('sortBy', filters.sortBy)
      if (filters?.sortOrder) queryParams.append('sortOrder', filters.sortOrder)

      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/payments${query ? `?${query}` : ''}`
      payments.value = await api.get<Payment[]>(path)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const getPayment = async (workspaceId: string, paymentId: string) => {
    loading.value = true
    error.value = null
    try {
      return await api.get<Payment>(`/api/workspaces/${workspaceId}/payments/${paymentId}`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearPayments = () => {
    payments.value = []
    error.value = null
  }

  return {
    payments: readonly(payments),
    loading: readonly(loading),
    error: readonly(error),
    fetchPayments,
    getPayment,
    clearPayments
  }
}

