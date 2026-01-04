import type { Notification, UnreadCountResponse, PaginatedNotifications } from '~/types/api'
import { useApi } from '~/utils/api'

export const useNotifications = () => {
  const api = useApi()
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

  const fetchUnreadCount = async () => {
    try {
      const response = await api.get<UnreadCountResponse>('/api/notifications/unread-count')
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
      const response = await api.get<PaginatedNotifications>(
        `/api/notifications?page=${page.value}&pageSize=${pageSize.value}`
      )
      
      if (reset) {
        notifications.value = response.items
      } else {
        notifications.value = [...notifications.value, ...response.items]
      }
      
      total.value = response.total
      hasMore.value = response.hasMore
      page.value = response.page
      
      // Update unread count after fetching notifications
      await fetchUnreadCount()
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

  const markAsRead = async (notificationId: string, workspaceId: string) => {
    try {
      await api.post(`/api/workspaces/${workspaceId}/notifications/${notificationId}/read`)
      
      // Update local state
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.read = true
      }
      
      // Update unread count
      if (unreadCount.value > 0) {
        unreadCount.value -= 1
      }
    } catch (err) {
      console.error('Failed to mark notification as read:', err)
      throw err
    }
  }

  const markAllAsRead = async (workspaceId: string) => {
    try {
      await api.post(`/api/workspaces/${workspaceId}/notifications/read-all`)
      
      // Update local state
      notifications.value.forEach(n => {
        if (n.workspaceId === workspaceId) {
          n.read = true
        }
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
    unreadCount: readonly(unreadCount),
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

