import type { SettleUpResponse } from '~/types/api'
import { useApi } from '~/utils/api'

export const useSettleUp = () => {
  const api = useApi()
  const settleUpData = ref<SettleUpResponse | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchSettleUp = async (workspaceId: string) => {
    loading.value = true
    error.value = null
    try {
      settleUpData.value = await api.get<SettleUpResponse>(`/api/workspaces/${workspaceId}/settle-up`)
      return settleUpData.value
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clear = () => {
    settleUpData.value = null
    error.value = null
  }

  return {
    settleUpData: readonly(settleUpData),
    loading: readonly(loading),
    error: readonly(error),
    fetchSettleUp,
    clear
  }
}


