import type { Plugin } from '#app'

declare module '#app' {
  interface NuxtApp {
    $toast: {
      show: (message: string, type?: 'success' | 'error' | 'info', duration?: number) => string
      success: (message: string, duration?: number) => string
      error: (message: string, duration?: number) => string
      info: (message: string, duration?: number) => string
      remove: (id: string) => void
    }
  }
}

