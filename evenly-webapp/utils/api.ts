import { useRuntimeConfig, useCookie, navigateTo } from '#imports'

export interface ApiError {
  error?: string
  message?: string
  status?: number
}

export const useApi = () => {
  const config = useRuntimeConfig()
  const token = useCookie<string | null>('token', { default: () => null })

  const request = async <T = any>(path: string, options: RequestInit = {}): Promise<T> => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string> || {})
    }
    if (token.value) {
      headers.Authorization = `Bearer ${token.value}`
    }

    try {
      const res = await fetch(`${config.public.apiBase}${path}`, { ...options, headers })
      
      if (res.status === 401) {
        token.value = null
        await navigateTo('/login')
        throw new Error('Unauthorized')
      }

      if (!res.ok) {
        const error: ApiError = await res.json().catch(() => ({}))
        error.status = res.status
        throw error
      }

      return res.status === 204 ? null as T : await res.json()
    } catch (err) {
      if (err instanceof Error && err.message === 'Unauthorized') {
        throw err
      }
      throw err
    }
  }

  const get = <T = any>(path: string) => request<T>(path, { method: 'GET' })
  const post = <T = any>(path: string, body?: any) => {
    const options: RequestInit = { method: 'POST' }
    if (body !== undefined && body !== null) {
      options.body = JSON.stringify(body)
    }
    return request<T>(path, options)
  }
  const put = <T = any>(path: string, body?: any) => request<T>(path, {
    method: 'PUT',
    body: body ? JSON.stringify(body) : undefined
  })
  const del = <T = any>(path: string) => request<T>(path, { method: 'DELETE' })

  return { request, get, post, put, delete: del, token }
}
