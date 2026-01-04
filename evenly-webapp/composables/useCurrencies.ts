import type { Currency } from '~/types/api'
import { useApi } from '~/utils/api'

export const useCurrencies = () => {
  const api = useApi()
  const currencies = ref<Currency[]>([])
  const loading = ref(false)

  const fetchCurrencies = async () => {
    if (currencies.value.length > 0) {
      return currencies.value
    }
    
    loading.value = true
    try {
      currencies.value = await api.get<Currency[]>('/api/currencies')
      return currencies.value
    } finally {
      loading.value = false
    }
  }

  return {
    currencies: readonly(currencies),
    loading: readonly(loading),
    fetchCurrencies
  }
}

