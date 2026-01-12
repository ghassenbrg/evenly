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
          <h4 class="text-sm font-medium text-white truncate">{{ getNotificationTitle(notification.type) }}</h4>
          <p class="text-xs text-slate-400 mt-1 line-clamp-2">{{ notification.content }}</p>
        </div>
      </div>
      
      <!-- Workspace and Time -->
      <div class="flex items-center justify-between mt-2">
        <span 
          class="text-xs font-medium px-2 py-0.5 rounded-md"
          :class="isActiveWorkspace(notification.workspaceId) 
            ? 'text-emerald-400 bg-emerald-500/10' 
            : 'text-blue-400 bg-blue-500/10'"
        >
          {{ getWorkspaceName(notification.workspaceId) }}
        </span>
        <span class="text-xs text-slate-500">
          {{ formatTime(notification.timestamp) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Notification } from '~/types/api'
import { useFormatting } from '~/composables/useFormatting'
import { useWorkspacesStore } from '~/stores/workspaces'
import { storeToRefs } from 'pinia'

const props = defineProps<{
  notification: Notification
}>()

const emit = defineEmits<{
  read: [notificationId: string]
}>()

const { t } = useI18n()
const { formatRelativeTime } = useFormatting()
const workspacesStore = useWorkspacesStore()
const { activeWorkspaceId } = storeToRefs(workspacesStore)

const formatTime = (dateString: string): string => {
  return formatRelativeTime(new Date(dateString))
}

const getNotificationTitle = (type: string): string => {
  const titles: Record<string, string> = {
    message: t('notifications.types.message'),
    alert: t('notifications.types.alert'),
    reminder: t('notifications.types.reminder')
  }
  return titles[type] || type
}

const getWorkspaceName = (workspaceId: string): string => {
  const workspace = workspacesStore.workspaces.find(w => w.id === workspaceId)
  return workspace?.name || workspaceId || t('notifications.unknownWorkspace')
}

const isActiveWorkspace = (workspaceId: string): boolean => {
  return activeWorkspaceId.value === workspaceId
}

const handleClick = () => {
  if (!props.notification.read) {
    emit('read', props.notification.id)
  }
}
</script>

