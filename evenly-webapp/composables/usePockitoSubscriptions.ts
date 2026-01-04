import type {
  Subscription,
  SubscriptionRequest,
  PaySubscriptionRequest,
  Transaction
} from '~/types/pockito'
import { useApi } from '~/utils/api'
import { usePockitoWallets } from './usePockitoWallets'

export const usePockitoSubscriptions = () => {
  const config = useRuntimeConfig()
  const api = useApi(config.public.pockitoApiBase || config.public.apiBase)
  const walletsComposable = usePockitoWallets()

  const subscriptions = useState<Subscription[] | null>('pockito:subscriptions', () => null)
  const currentSubscription = useState<Subscription | null>('pockito:subscriptions:current', () => null)
  const loading = useState<boolean>('pockito:subscriptions:loading', () => false)
  const error = useState<Error | null>('pockito:subscriptions:error', () => null)

  const begin = () => {
    loading.value = true
    error.value = null
  }
  const end = () => {
    loading.value = false
  }

  const loadSubscriptions = async () => {
    begin()
    try {
      const list = await api.get<Subscription[]>('/api/subscriptions')
      subscriptions.value = [...list]
      if (currentSubscription.value) {
        const updated = list.find(s => s.id === currentSubscription.value?.id) ?? null
        currentSubscription.value = updated
      }
      return list
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const loadSubscription = async (subscriptionId: string) => {
    begin()
    try {
      const sub = await api.get<Subscription>(`/api/subscriptions/${subscriptionId}`)
      currentSubscription.value = sub
      const list = subscriptions.value ?? []
      const idx = list.findIndex(s => s.id === sub.id)
      if (idx >= 0) {
        const next = [...list]
        next[idx] = sub
        subscriptions.value = next
      } else {
        subscriptions.value = [sub, ...list]
      }
      return sub
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const createSubscription = async (request: SubscriptionRequest) => {
    begin()
    try {
      const created = await api.post<Subscription>('/api/subscriptions', request)
      subscriptions.value = [created, ...(subscriptions.value || [])]
      return created
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const updateSubscription = async (subscriptionId: string, request: SubscriptionRequest) => {
    begin()
    try {
      const updated = await api.put<Subscription>(`/api/subscriptions/${subscriptionId}`, request)
      const list = subscriptions.value ?? []
      const idx = list.findIndex(s => s.id === updated.id)
      if (idx >= 0) {
        const next = [...list]
        next[idx] = updated
        subscriptions.value = next
      }
      if (currentSubscription.value?.id === updated.id) {
        currentSubscription.value = updated
      }
      return updated
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const deleteSubscription = async (subscriptionId: string) => {
    begin()
    try {
      await api.delete(`/api/subscriptions/${subscriptionId}`)
      subscriptions.value = (subscriptions.value || []).filter(s => s.id !== subscriptionId)
      if (currentSubscription.value?.id === subscriptionId) {
        currentSubscription.value = null
      }
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const paySubscription = async (subscriptionId: string, request: PaySubscriptionRequest) => {
    begin()
    try {
      const tx = await api.post<Transaction>(`/api/subscriptions/${subscriptionId}/pay`, request)
      // Refresh subscription and affected wallet silently
      try {
        await loadSubscription(subscriptionId)
      } catch {
        // ignore refresh errors
      }
      if (tx && tx.walletFromId) {
        walletsComposable.refreshWalletSilently(tx.walletFromId)
      }
      return tx
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  return {
    subscriptions: readonly(subscriptions),
    currentSubscription: readonly(currentSubscription),
    loading: readonly(loading),
    error: readonly(error),
    loadSubscriptions,
    loadSubscription,
    createSubscription,
    updateSubscription,
    deleteSubscription,
    paySubscription
  }
}
