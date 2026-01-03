import type { Settlement, CreateSettlementRequest } from '~/types/api'
import { useApi } from '~/utils/api'

export const useSettlements = () => {
  const api = useApi()
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const createSettlement = async (workspaceId: string, request?: CreateSettlementRequest) => {
    loading.value = true
    error.value = null
    try {
      const settlement = await api.post<Settlement>(`/api/workspaces/${workspaceId}/settlements`, request || {})
      return settlement
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchSettlements = async (workspaceId: string) => {
    loading.value = true
    error.value = null
    try {
      const settlements = await api.get<Settlement[]>(`/api/workspaces/${workspaceId}/settlements`)
      return settlements
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    loading: readonly(loading),
    error: readonly(error),
    createSettlement,
    fetchSettlements
  }
}

