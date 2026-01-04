import type {
  PageTransactionDto,
  Pageable,
  Transaction,
  TransactionDto,
  TransactionRequest,
  TransactionType
} from '~/types/pockito'
import { useApi } from '~/utils/api'
import { usePockitoWallets } from './usePockitoWallets'

interface TransactionFilters {
  walletId?: string
  startDate?: string
  endDate?: string
  transactionType?: TransactionType
}

export const usePockitoTransactions = () => {
  const config = useRuntimeConfig()
  const api = useApi(config.public.pockitoApiBase || config.public.apiBase)
  const walletsComposable = usePockitoWallets()

  const transactions = useState<TransactionDto[]>('pockito:transactions', () => [])
  const pageable = useState<PageTransactionDto | null>('pockito:transactions:page', () => null)
  const currentTransaction = useState<TransactionDto | null>('pockito:transactions:current', () => null)
  const loading = useState<boolean>('pockito:transactions:loading', () => false)
  const error = useState<Error | null>('pockito:transactions:error', () => null)
  const currentFilters = useState<TransactionFilters>('pockito:transactions:filters', () => ({}))

  const begin = () => {
    loading.value = true
    error.value = null
  }

  const end = () => {
    loading.value = false
  }

  const buildQuery = (pageable: Pageable, filters?: TransactionFilters) => {
    const params = new URLSearchParams()
    if (pageable.page !== undefined) params.set('page', pageable.page.toString())
    if (pageable.size !== undefined) params.set('size', pageable.size.toString())
    pageable.sort?.forEach(sort => params.append('sort', sort))

    if (filters?.walletId) params.set('walletId', filters.walletId)
    if (filters?.startDate) params.set('startDate', filters.startDate)
    if (filters?.endDate) params.set('endDate', filters.endDate)
    if (filters?.transactionType) params.set('transactionType', filters.transactionType)

    const query = params.toString()
    return query ? `/api/transactions?${query}` : '/api/transactions'
  }

  const loadFirstPage = async (page: Pageable, filters?: TransactionFilters) => {
    begin()
    try {
      currentFilters.value = { ...(filters || {}) }
      const path = buildQuery(page, filters)
      const response = await api.get<PageTransactionDto>(path)
      transactions.value = [...(response.content || [])]
      pageable.value = response
      return response
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const loadNextPage = async () => {
    if (!pageable.value) return
    const nextPage = (pageable.value.number || 0) + 1
    const size = pageable.value.size || 10
    const sort = pageable.value.sort?.sorted ? pageable.value.sort : undefined
    const page: Pageable = { page: nextPage, size, sort: ['effectiveDate,desc'] }

    begin()
    try {
      const path = buildQuery(page, currentFilters.value)
      const response = await api.get<PageTransactionDto>(path)
      const merged = [...transactions.value, ...(response.content || [])]
      transactions.value = merged
      pageable.value = { ...response, content: merged }
      return response
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const matchesFilters = (tx: Transaction) => {
    const filters = currentFilters.value
    if (filters.walletId) {
      const walletIds = [tx.walletFromId, tx.walletToId].filter(Boolean)
      if (!walletIds.includes(filters.walletId)) return false
    }
    if (filters.transactionType && tx.transactionType !== filters.transactionType) {
      return false
    }
    if (filters.startDate && tx.effectiveDate < filters.startDate) return false
    if (filters.endDate && tx.effectiveDate > filters.endDate) return false
    return true
  }

  const createTransaction = async (request: TransactionRequest) => {
    begin()
    try {
      const created = await api.post<Transaction>('/api/transactions', request)
      if (matchesFilters(created)) {
        transactions.value = [created as TransactionDto, ...transactions.value]
        if (pageable.value) {
          pageable.value = { ...pageable.value, content: transactions.value }
        }
      }
      [created.walletFromId, created.walletToId]
        .filter(Boolean)
        .forEach(id => walletsComposable.refreshWalletSilently(id as string))
      return created
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const updateTransaction = async (transactionId: string, request: TransactionRequest) => {
    begin()
    try {
      const updated = await api.put<Transaction>(`/api/transactions/${transactionId}`, request)
      const idx = transactions.value.findIndex(t => t.id === updated.id)
      if (idx >= 0) {
        const next = [...transactions.value]
        next[idx] = updated as TransactionDto
        transactions.value = next
        if (pageable.value) {
          pageable.value = { ...pageable.value, content: next }
        }
      } else if (matchesFilters(updated)) {
        transactions.value = [updated as TransactionDto, ...transactions.value]
      }
      if (currentTransaction.value?.id === updated.id) {
        currentTransaction.value = updated as TransactionDto
      }
      [updated.walletFromId, updated.walletToId]
        .filter(Boolean)
        .forEach(id => walletsComposable.refreshWalletSilently(id as string))
      return updated
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const deleteTransaction = async (transactionId: string) => {
    const existing = transactions.value.find(t => t.id === transactionId)
    begin()
    try {
      await api.delete(`/api/transactions/${transactionId}`)
      transactions.value = transactions.value.filter(t => t.id !== transactionId)
      if (pageable.value) {
        pageable.value = { ...pageable.value, content: transactions.value }
      }
      if (currentTransaction.value?.id === transactionId) {
        currentTransaction.value = null
      }
      if (existing) {
        [existing.walletFromId, existing.walletToId]
          .filter(Boolean)
          .forEach(id => walletsComposable.refreshWalletSilently(id as string))
      }
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const loadTransactionById = async (transactionId: string) => {
    begin()
    try {
      const tx = await api.get<Transaction>(`/api/transactions/${transactionId}`)
      currentTransaction.value = tx as TransactionDto
      const idx = transactions.value.findIndex(t => t.id === tx.id)
      if (idx >= 0) {
        const next = [...transactions.value]
        next[idx] = tx as TransactionDto
        transactions.value = next
      } else {
        transactions.value = [tx as TransactionDto, ...transactions.value]
      }
      return tx
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const resetTransactions = () => {
    transactions.value = []
    pageable.value = null
    currentTransaction.value = null
  }

  return {
    transactions: readonly(transactions),
    pageable: readonly(pageable),
    currentTransaction: readonly(currentTransaction),
    loading: readonly(loading),
    error: readonly(error),
    loadFirstPage,
    loadNextPage,
    createTransaction,
    updateTransaction,
    deleteTransaction,
    loadTransactionById,
    resetTransactions
  }
}
