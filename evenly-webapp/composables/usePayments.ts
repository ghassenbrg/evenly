import type { Payment, PaymentStatus, PaginatedResponse, CreatePaymentRequest } from '~/types/api'
import { useApi } from '~/utils/api'

export interface PaymentFilters {
  startDate?: string
  endDate?: string
  status?: PaymentStatus
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

  const fetchPayments = async (workspaceId: string, filters?: PaymentFilters) => {
    loading.value = true
    error.value = null
    try {
      // endpoints.json: GET /api/workspaces/{id}/payments?page=0&size=5&sort=effectiveDate,DESC
      const queryParams = new URLSearchParams()
      if (filters?.startDate) queryParams.append('startDate', filters.startDate)
      if (filters?.endDate) queryParams.append('endDate', filters.endDate)
      if (filters?.status) queryParams.append('status', filters.status)
      if (filters?.page !== undefined) queryParams.append('page', String(filters.page))
      if (filters?.size !== undefined) queryParams.append('size', String(filters.size))
      if (filters?.sort) queryParams.append('sort', filters.sort)

      const query = queryParams.toString()
      const path = `/api/workspaces/${workspaceId}/payments${query ? `?${query}` : ''}`
      const response = await api.get<PaginatedResponse<Payment>>(path)
      payments.value = response.data
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

  const clearPayments = () => {
    payments.value = []
    error.value = null
  }

  return {
    payments: readonly(payments),
    pageInfo: readonly(pageInfo),
    loading: readonly(loading),
    error: readonly(error),
    fetchPayments,
    getPayment,
    createPayment,
    clearPayments
  }
}

