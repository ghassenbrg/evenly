import type { User, Currency, Country } from '~/types/pockito'
import { useApi } from '~/utils/api'

export const usePockitoUsers = () => {
  const config = useRuntimeConfig()
  const api = useApi(config.public.pockitoApiBase || config.public.apiBase)
  const currentUser = useState<User | null>('pockito:user', () => null)
  const loading = useState<boolean>('pockito:user:loading', () => false)
  const error = useState<Error | null>('pockito:user:error', () => null)

  const begin = () => {
    loading.value = true
    error.value = null
  }
  const end = () => {
    loading.value = false
  }

  const getOrCreateCurrentUser = async () => {
    begin()
    try {
      const user = await api.get<User>('/api/users/me')
      currentUser.value = user
      return user
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const getUserByUsername = async (username: string) => {
    begin()
    try {
      return await api.get<User>(`/api/users/${username}`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const checkUserExists = async (username: string) => {
    begin()
    try {
      await api.get<void>(`/api/users/${username}/exists`)
      return true
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const updateUserCurrency = async (username: string, currencyCode: Currency) => {
    begin()
    try {
      const user = await api.put<User>(`/api/users/${username}/currency?currencyCode=${currencyCode}`)
      if (currentUser.value?.username === user.username) {
        currentUser.value = user
      }
      return user
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  const updateUserCountry = async (username: string, countryCode: Country) => {
    begin()
    try {
      const user = await api.put<User>(`/api/users/${username}/country?countryCode=${countryCode}`)
      if (currentUser.value?.username === user.username) {
        currentUser.value = user
      }
      return user
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      end()
    }
  }

  return {
    currentUser: readonly(currentUser),
    loading: readonly(loading),
    error: readonly(error),
    getOrCreateCurrentUser,
    getUserByUsername,
    checkUserExists,
    updateUserCurrency,
    updateUserCountry
  }
}
