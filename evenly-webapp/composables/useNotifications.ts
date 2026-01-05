import type { Notification } from '~/types/api'
import { useApi } from '~/utils/api'

// Shared state - singleton pattern (like useToast)
const unreadCount = ref<number>(0)
const notifications = ref<Notification[]>([])
const loading = ref(false)
const error = ref<Error | null>(null)
const total = ref(0)
const hasMore = ref(false)
const page = ref(1)
const pageSize = ref(20)

// Poll interval for unread count (45 seconds)
let pollInterval: ReturnType<typeof setInterval> | null = null

export const useNotifications = () => {
  const api = useApi()

  const fetchUnreadCount = async () => {
    try {
      // endpoints.json: GET /api/notifications/unread-count returns { data: { count: number } }
      // useApi transforms it to { unreadCount: number }
      const response = await api.get<{ unreadCount: number }>('/api/notifications/unread-count')
      unreadCount.value = response.unreadCount
    } catch (err) {
      console.error('Failed to fetch unread count:', err)
      // Don't throw - we don't want to break the UI if this fails
    }
  }

  const fetchNotifications = async (reset = false) => {
    if (reset) {
      page.value = 1
      notifications.value = []
    }

    loading.value = true
    error.value = null
    try {
      // endpoints.json structure: { data: Notification[], unreadCount: number }
      // Note: endpoints.json doesn't support pagination, so we get all notifications
      const response = await api.get<{ data: Notification[], unreadCount: number }>('/api/notifications')
      
      const notificationsList = response.data || []
      const responseUnreadCount = response.unreadCount || 0
      
      if (reset) {
        notifications.value = notificationsList
      } else {
        notifications.value = [...notifications.value, ...notificationsList]
      }
      
      total.value = notificationsList.length
      hasMore.value = false // endpoints.json doesn't support pagination
      unreadCount.value = responseUnreadCount
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const loadMore = async () => {
    if (!hasMore.value || loading.value) return
    page.value += 1
    await fetchNotifications(false)
  }

  const markAsRead = async (notificationId: string) => {
    try {
      // Find notification before marking as read to check if it was unread
      const notification = notifications.value.find(n => n.id === notificationId)
      const wasUnread = notification && !notification.read
      
      // endpoints.json uses /api/notifications/{id}/mark-as-read (no workspaceId in path)
      await api.post(`/api/notifications/${notificationId}/mark-as-read`)
      
      // Update local state
      if (notification) {
        notification.read = true
      }
      
      // Decrement unread count if notification was previously unread
      if (wasUnread && unreadCount.value > 0) {
        unreadCount.value = unreadCount.value - 1
      }
    } catch (err) {
      console.error('Failed to mark notification as read:', err)
      throw err
    }
  }

  const markAllAsRead = async () => {
    try {
      // endpoints.json uses /api/notifications/mark-all-as-read (no workspaceId in path)
      await api.post('/api/notifications/mark-all-as-read')
      
      // Update local state - mark all notifications as read
      notifications.value.forEach(n => {
        n.read = true
      })
      
      // Reset unread count
      unreadCount.value = 0
    } catch (err) {
      console.error('Failed to mark all notifications as read:', err)
      throw err
    }
  }

  const startPolling = () => {
    if (pollInterval) return
    
    // Initial fetch
    fetchUnreadCount()
    
    // Poll every 45 seconds
    pollInterval = setInterval(() => {
      fetchUnreadCount()
    }, 45000)
  }

  const stopPolling = () => {
    if (pollInterval) {
      clearInterval(pollInterval)
      pollInterval = null
    }
  }

  const clear = () => {
    notifications.value = []
    error.value = null
    total.value = 0
    hasMore.value = false
    page.value = 1
  }

  return {
    // Return computed to ensure reactivity
    unreadCount: computed(() => unreadCount.value),
    notifications: readonly(notifications),
    loading: readonly(loading),
    error: readonly(error),
    total: readonly(total),
    hasMore: readonly(hasMore),
    fetchUnreadCount,
    fetchNotifications,
    loadMore,
    markAsRead,
    markAllAsRead,
    startPolling,
    stopPolling,
    clear
  }
}

