import type { Payment, PaymentStatus, PaginatedResponse, CreatePaymentRequest, SettlementScope } from '~/types/api'
import { useApi } from '~/utils/api'

export interface PaymentFilters {
  startDate?: string
  endDate?: string
  status?: PaymentStatus
  settlementScope?: SettlementScope
  page?: number
  size?: number
  sort?: string // Format: "property,DIRECTION" e.g. "effectiveDate,DESC"
}

export const usePayments = () => {
  const api = useApi()
  const payments = ref<Payment[]>([])
  const pageInfo = ref<{ number: number; size: number; totalElements: number; totalPages: number } | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchPayments = async (workspaceId: string, filters?: PaymentFilters, append = false) => {
    loading.value = true
    error.value = null
    try {
      // endpoints.json: GET /api/workspaces/{id}/payments?page=0&size=5&sort=effectiveDate,DESC
      const queryParams = new URLSearchParams()
      if (filters?.startDate) queryParams.append('startDate', filters.startDate)
      if (filters?.endDate) queryParams.append('endDate', filters.endDate)
      if (filters?.status) queryParams.append('status', filters.status)
      if (filters?.settlementScope) queryParams.append('settlementScope', filters.settlementScope)
      if (filters?.page !== undefined) queryParams.append('page', String(filters.page))
      if (filters?.size !== undefined) queryParams.append('size', String(filters.size))
      if (filters?.sort) queryParams.append('sort', filters.sort)

      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/payments${query ? `?${query}` : ''}`
      const response = await api.get<PaginatedResponse<Payment>>(path)
      
      if (append) {
        // Append new payments for pagination
        payments.value = [...payments.value, ...response.data]
      } else {
        // Replace payments for new search/filter
        payments.value = response.data
      }
      pageInfo.value = response.page
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

  const createPayment = async (workspaceId: string, request: CreatePaymentRequest) => {
    loading.value = true
    error.value = null
    try {
      const payment = await api.post<Payment>(`/api/workspaces/${workspaceId}/pay`, request)
      return payment
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const updatePayment = async (workspaceId: string, paymentId: string, request: Partial<CreatePaymentRequest>) => {
    loading.value = true
    error.value = null
    try {
      const payment = await api.put<Payment>(`/api/workspaces/${workspaceId}/payments/${paymentId}`, request)
      const index = payments.value.findIndex(p => p.id === paymentId)
      if (index !== -1) {
        payments.value[index] = payment
      }
      return payment
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const deletePayment = async (workspaceId: string, paymentId: string) => {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/api/workspaces/${workspaceId}/payments/${paymentId}`)
      payments.value = payments.value.filter(p => p.id !== paymentId)
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

  const loadMorePayments = async (workspaceId: string, filters?: PaymentFilters) => {
    if (!pageInfo.value) return
    
    const nextPage = pageInfo.value.number + 1
    if (nextPage >= pageInfo.value.totalPages) return
    
    const nextFilters = {
      ...filters,
      page: nextPage
    }
    
    await fetchPayments(workspaceId, nextFilters, true)
  }

  return {
    payments: readonly(payments),
    pageInfo: readonly(pageInfo),
    loading: readonly(loading),
    error: readonly(error),
    fetchPayments,
    loadMorePayments,
    getPayment,
    createPayment,
    updatePayment,
    deletePayment,
    clearPayments
  }
}
