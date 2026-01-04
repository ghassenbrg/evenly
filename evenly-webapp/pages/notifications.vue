<template>
  <div class="p-4 space-y-4">
    <!-- Header with Mark All as Read -->
    <div v-if="notifications.length > 0 && hasUnread" class="flex items-center justify-between">
      <h2 class="text-lg font-semibold">{{ t('notifications.title') }}</h2>
      <button
        @click="handleMarkAllAsRead"
        class="text-sm text-emerald-500 hover:text-emerald-400 transition-colors"
        :disabled="markingAllAsRead"
      >
        {{ markingAllAsRead ? t('notifications.markingAllAsRead') : t('notifications.markAllAsRead') }}
      </button>
    </div>
    <div v-else-if="notifications.length > 0" class="flex items-center justify-between">
      <h2 class="text-lg font-semibold">{{ t('notifications.title') }}</h2>
    </div>

    <!-- Loading State -->
    <div v-if="loading && notifications.length === 0" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error && notifications.length === 0" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4">
      <p class="text-red-400 text-sm">{{ error.message || t('notifications.loadFailed') }}</p>
      <button
        @click="loadNotifications"
        class="mt-2 text-sm text-red-400 hover:text-red-300 underline"
      >
        {{ t('common.retry') }}
      </button>
    </div>

    <!-- Content -->
    <template v-else>
      <!-- Notifications List -->
      <div v-if="displayedGroups.length > 0" class="space-y-6">
        <template v-for="(group, groupIndex) in displayedGroups" :key="group.date">
          <!-- Day Header -->
          <div class="mb-1">
            <h3 class="text-xs font-medium text-white/50">{{ group.label }}</h3>
          </div>
          
          <!-- Notifications for this day -->
          <div class="space-y-2">
            <template v-for="(notification, notificationIndex) in group.notifications" :key="notification.id">
              <NotificationItem
                :notification="notification"
                @read="handleMarkAsRead"
              />
            </template>
          </div>
        </template>
      </div>

      <!-- Empty State -->
      <div v-else-if="!loading" class="text-center py-12">
        <div class="flex flex-col items-center space-y-4">
          <div class="w-16 h-16 rounded-full bg-slate-800 flex items-center justify-center">
            <svg class="w-8 h-8 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
          </div>
          <p class="text-sm text-white/40">{{ t('notifications.noNotifications') }}</p>
        </div>
      </div>
      
      <!-- Load More Trigger (for infinite scroll) -->
      <div
        v-if="hasMore && !loading"
        ref="loadMoreTrigger"
        class="flex justify-center pt-4 pb-4"
      >
        <div class="text-sm text-white/40">{{ t('notifications.loadingMore') }}</div>
      </div>
      
      <!-- Loading More Indicator -->
      <div v-if="loading && notifications.length > 0" class="flex justify-center pt-4 pb-4">
        <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-emerald-500"></div>
      </div>
      
      <!-- End of List -->
      <div v-else-if="displayedGroups.length > 0 && !hasMore" class="text-center py-8">
        <p class="text-sm text-white/40">{{ t('common.noMoreItems') }}</p>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

import { useNotifications } from '~/composables/useNotifications'
import type { Notification } from '~/types/api'

const { t, locale } = useI18n()
const notifications = useNotifications()
const notificationList = notifications.notifications
const loading = notifications.loading
const error = notifications.error
const hasMore = notifications.hasMore

const markingAllAsRead = ref(false)

// Start polling when page mounts
onMounted(() => {
  notifications.startPolling()
  loadNotifications()
})

// Stop polling when page unmounts
onUnmounted(() => {
  notifications.stopPolling()
})

// Refetch notifications when returning to page
const route = useRoute()
watch(() => route.path, (newPath) => {
  if (newPath === '/notifications') {
    notifications.fetchNotifications(true)
    notifications.fetchUnreadCount()
  }
})

const loadNotifications = async () => {
  await notifications.fetchNotifications(true)
}

// Sort notifications by date (newest first)
const sortedNotifications = computed(() => {
  return [...notificationList.value].sort((a, b) => {
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
})

// Group notifications by day
const groupedNotifications = computed(() => {
  const groups: Record<string, Notification[]> = {}
  
  sortedNotifications.value.forEach(notification => {
    const date = new Date(notification.createdAt)
    const dateKey = date.toISOString().split('T')[0]
    
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    groups[dateKey].push(notification)
  })
  
  return groups
})

// Format day label (Today, Yesterday, or date)
const formatDayLabel = (dateISO: string): string => {
  const date = new Date(dateISO)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const notificationDate = new Date(date)
  notificationDate.setHours(0, 0, 0, 0)
  
  const diffTime = today.getTime() - notificationDate.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  const currentLocale = locale.value === 'ja' ? 'ja-JP' : 'en-US'
  
  if (diffDays === 0) {
    return t('common.today')
  } else if (diffDays === 1) {
    return t('common.yesterday')
  } else if (diffDays < 7) {
    return date.toLocaleDateString(currentLocale, { weekday: 'long' })
  } else {
    return date.toLocaleDateString(currentLocale, { month: 'long', day: 'numeric', year: 'numeric' })
  }
}

// Get grouped notifications with labels
const notificationGroups = computed(() => {
  return Object.entries(groupedNotifications.value)
    .map(([date, notifications]) => ({
      date,
      label: formatDayLabel(date),
      notifications
    }))
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
})

// Get displayed groups (for pagination)
const displayedGroups = computed(() => {
  return notificationGroups.value
})

// Check if there are unread notifications
const hasUnread = computed(() => {
  return sortedNotifications.value.some(n => !n.read)
})

// Get unique workspace IDs for mark all as read
const workspaceIds = computed(() => {
  return [...new Set(sortedNotifications.value.map(n => n.workspaceId))]
})

const handleMarkAsRead = async (notificationId: string, workspaceId: string) => {
  try {
    await notifications.markAsRead(notificationId, workspaceId)
  } catch (err) {
    console.error('Failed to mark notification as read:', err)
  }
}

const handleMarkAllAsRead = async () => {
  if (markingAllAsRead.value) return
  
  markingAllAsRead.value = true
  try {
    // Mark all as read for each workspace
    for (const workspaceId of workspaceIds.value) {
      await notifications.markAllAsRead(workspaceId)
    }
  } catch (err) {
    console.error('Failed to mark all notifications as read:', err)
  } finally {
    markingAllAsRead.value = false
  }
}

const loadMoreTrigger = ref<HTMLElement | null>(null)

// Infinite scroll with Intersection Observer
if (process.client) {
  onMounted(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore.value && !loading.value) {
          notifications.loadMore()
        }
      },
      { rootMargin: '100px' }
    )
    
    watch(loadMoreTrigger, (el) => {
      if (el) {
        observer.observe(el)
      }
    }, { immediate: true })
    
    onUnmounted(() => {
      if (loadMoreTrigger.value) {
        observer.unobserve(loadMoreTrigger.value)
      }
      observer.disconnect()
    })
  })
}
</script>

