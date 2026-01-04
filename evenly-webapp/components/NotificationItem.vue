<template>
  <div
    @click="handleClick"
    class="flex items-start space-x-3 p-3 rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors cursor-pointer"
    :class="{ 'opacity-60': notification.read }"
  >
    <!-- Unread Indicator -->
    <div class="flex-shrink-0 pt-1">
      <div
        v-if="!notification.read"
        class="w-2 h-2 rounded-full bg-emerald-500"
        aria-hidden="true"
      ></div>
      <div
        v-else
        class="w-2 h-2 rounded-full bg-transparent"
        aria-hidden="true"
      ></div>
    </div>

    <!-- Content -->
    <div class="flex-1 min-w-0">
      <div class="flex items-start justify-between">
        <div class="flex-1 min-w-0">
          <h4 class="text-sm font-medium text-white truncate">{{ notification.title }}</h4>
          <p class="text-xs text-slate-400 mt-1 line-clamp-2">{{ notification.message }}</p>
        </div>
      </div>
      
      <!-- Workspace and Time -->
      <div class="flex items-center justify-between mt-2">
        <span
          v-if="notification.workspace"
          class="text-xs text-slate-500"
        >
          {{ notification.workspace.name }}
        </span>
        <span
          v-else
          class="text-xs text-slate-500"
        >
          {{ t('notifications.unknownWorkspace') }}
        </span>
        <span class="text-xs text-slate-500">
          {{ formatTime(notification.createdAt) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Notification } from '~/types/api'
import { useFormatting } from '~/composables/useFormatting'

const props = defineProps<{
  notification: Notification
}>()

const emit = defineEmits<{
  read: [notificationId: string, workspaceId: string]
}>()

const { t } = useI18n()
const { formatRelativeTime } = useFormatting()

const formatTime = (dateString: string): string => {
  return formatRelativeTime(new Date(dateString))
}

const handleClick = () => {
  if (!props.notification.read) {
    emit('read', props.notification.id, props.notification.workspaceId)
  }
}
</script>

