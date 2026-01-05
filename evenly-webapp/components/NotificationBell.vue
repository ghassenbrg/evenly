<template>
  <button
    @click="navigateToNotifications"
    class="relative flex items-center justify-center w-9 h-9 rounded-lg bg-slate-800 hover:bg-slate-700 active:bg-slate-600 transition-colors focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-slate-900"
    :aria-label="t('notifications.bellAriaLabel')"
  >
    <svg
      class="w-5 h-5 text-slate-300"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      aria-hidden="true"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
      />
    </svg>
    
    <!-- Badge for unread count -->
    <span
      v-if="unreadCount > 0"
      class="absolute -top-1 -right-1 flex items-center justify-center min-w-[18px] h-[18px] px-1 text-xs font-semibold text-white bg-emerald-500 rounded-full"
      :aria-label="t('notifications.unreadCountAriaLabel', { count: unreadCount })"
    >
      <span class="sr-only">{{ t('notifications.unreadCountAriaLabel', { count: unreadCount }) }}</span>
      <span aria-hidden="true">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </span>
  </button>
</template>

<script setup lang="ts">
const { t } = useI18n()
const notifications = useNotifications()
// unreadCount is already a computed from the composable
const unreadCount = notifications.unreadCount

// Start polling when component mounts
onMounted(() => {
  notifications.startPolling()
  // Also fetch on window focus
  if (process.client) {
    window.addEventListener('focus', notifications.fetchUnreadCount)
  }
})

// Stop polling when component unmounts
onUnmounted(() => {
  notifications.stopPolling()
  if (process.client) {
    window.removeEventListener('focus', notifications.fetchUnreadCount)
  }
})

// Refetch on route change
const route = useRoute()
watch(() => route.path, () => {
  notifications.fetchUnreadCount()
})

const navigateToNotifications = () => {
  navigateTo('/notifications')
}
</script>

