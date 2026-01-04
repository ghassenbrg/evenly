import type {
  Wallet,
  WalletList,
  WalletRequest,
  WalletType,
  ReorderWalletsRequest
} from '~/types/pockito'
import { useApi } from '~/utils/api'

interface WalletFilters {
  type?: WalletType
}

export const usePockitoWallets = () => {
  const config = useRuntimeConfig()
  const api = useApi(config.public.pockitoApiBase || config.public.apiBase)
  const wallets = useState<Wallet[]>('pockito:wallets', () => [])
  const currentWallet = useState<Wallet | null>('pockito:currentWallet', () => null)
  const loading = useState<boolean>('pockito:wallets:loading', () => false)
  const error = useState<Error | null>('pockito:wallets:error', () => null)

  const begin = () => {
    loading.value = true
    error.value = null
  }

  const end = () => {
    loading.value = false
  }

  const syncCurrentFromList = (list: Wallet[]) => {
    if (currentWallet.value) {
      const updated = list.find(w => w.id === currentWallet.value?.id)
      currentWallet.value = updated ?? currentWallet.value
    } else {
      const defaultWallet = list.find(w => w.isDefault)
      if (defaultWallet) {
        currentWallet.value = defaultWallet
      }
    }
  }

  const loadWallets = async (filters?: WalletFilters) => {
    begin()
    try {
      let path = '/api/wallets'
      if (filters?.type) {
        path = `/api/wallets/type/${filters.type}`
      }
      const response = await api.get<WalletList>(path)
      wallets.value = [...(response.wallets || [])]
      syncCurrentFromList(wallets.value)
      return wallets.value
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const loadWallet = async (walletId: string) => {
    begin()
    try {
      const wallet = await api.get<Wallet>(`/api/wallets/${walletId}`)
      const existing = wallets.value
      const idx = existing.findIndex(w => w.id === wallet.id)
      if (idx >= 0) {
        existing[idx] = wallet
        wallets.value = [...existing]
      } else {
        wallets.value = [wallet, ...existing]
      }
      currentWallet.value = wallet
      return wallet
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const createWallet = async (request: WalletRequest) => {
    begin()
    try {
      const created = await api.post<Wallet>('/api/wallets', request)
      wallets.value = [created, ...wallets.value]
      if (created.isDefault) {
        currentWallet.value = created
      }
      return created
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const updateWallet = async (walletId: string, request: WalletRequest) => {
    begin()
    try {
      const updated = await api.put<Wallet>(`/api/wallets/${walletId}`, request)
      const idx = wallets.value.findIndex(w => w.id === updated.id)
      if (idx >= 0) {
        const next = [...wallets.value]
        next[idx] = updated
        wallets.value = next
      }
      if (currentWallet.value?.id === updated.id) {
        currentWallet.value = updated
      }
      return updated
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const deleteWallet = async (walletId: string) => {
    begin()
    try {
      await api.delete(`/api/wallets/${walletId}`)
      wallets.value = wallets.value.filter(w => w.id !== walletId)
      if (currentWallet.value?.id === walletId) {
        currentWallet.value = null
      }
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const setDefaultWallet = async (walletId: string) => {
    begin()
    try {
      const updated = await api.post<Wallet>(`/api/wallets/${walletId}/set-default`, {})
      wallets.value = wallets.value.map(w => ({ ...w, isDefault: w.id === updated.id }))
      currentWallet.value = updated
      return updated
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const reorderWallets = async (request: ReorderWalletsRequest) => {
    begin()
    try {
      await api.post<void>('/api/wallets/reorder', request)
      const orderMap = new Map(request.walletIds.map((id, idx) => [id, idx] as const))
      wallets.value = [...wallets.value].sort((a, b) => {
        const posA = orderMap.get(a.id) ?? a.orderPosition ?? 0
        const posB = orderMap.get(b.id) ?? b.orderPosition ?? 0
        return posA - posB
      })
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const refreshWallet = async (walletId: string) => {
    try {
      const wallet = await api.get<Wallet>(`/api/wallets/${walletId}`)
      const idx = wallets.value.findIndex(w => w.id === wallet.id)
      if (idx >= 0) {
        const next = [...wallets.value]
        next[idx] = wallet
        wallets.value = next
      } else {
        wallets.value = [wallet, ...wallets.value]
      }
      if (currentWallet.value?.id === wallet.id) {
        currentWallet.value = wallet
      }
      return wallet
    } catch (err) {
      error.value = err as Error
      throw err
    }
  }

  const refreshWalletSilently = async (walletId: string) => {
    try {
      await refreshWallet(walletId)
    } catch {
      // Ignore refresh errors to avoid interrupting flows
    }
  }

  return {
    wallets: readonly(wallets),
    currentWallet: readonly(currentWallet),
    loading: readonly(loading),
    error: readonly(error),
    loadWallets,
    loadWallet,
    createWallet,
    updateWallet,
    deleteWallet,
    setDefaultWallet,
    reorderWallets,
    refreshWallet,
    refreshWalletSilently
  }
}
