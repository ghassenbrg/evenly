import { useRuntimeConfig, useCookie } from '#imports'

export const useApi = () => {
  const config = useRuntimeConfig()
  const token = useCookie('token')

  const request = async (path: string, options: RequestInit = {}) => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string> || {})
    }
    if (token.value) {
      headers.Authorization = `Bearer ${token.value}`
    }
    const res = await fetch(`${config.public.apiBase}${path}`, { ...options, headers })
    if (!res.ok) {
      const error = await res.json().catch(() => ({}))
      throw new Error(error.error || 'Request failed')
    }
    return res.status === 204 ? null : res.json()
  }

  return { request }
}
