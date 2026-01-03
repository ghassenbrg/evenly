import type { Balance } from '~/types/api'
import { useApi } from '~/utils/api'

export const useBalance = () => {
  const api = useApi()
  const balances = ref<Balance[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchBalances = async (workspaceId: string) => {
    loading.value = true
    error.value = null
    try {
      balances.value = await api.get<Balance[]>(`/api/workspaces/${workspaceId}/balance`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clear = () => {
    balances.value = []
    error.value = null
  }

  return {
    balances: readonly(balances),
    loading: readonly(loading),
    error: readonly(error),
    fetchBalances,
    clear
  }
}

