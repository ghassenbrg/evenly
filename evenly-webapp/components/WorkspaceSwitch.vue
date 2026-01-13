<template>
  <div class="relative">
    <button
      @click="showMenu = !showMenu"
      class="flex items-center space-x-2 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors"
    >
      <span class="text-sm font-medium max-w-[120px] truncate">{{ activeWorkspace?.isPersonal ? t('workspace.mySpace') : (activeWorkspace?.name || t('workspace.noWorkspace')) }}</span>
      <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <!-- Dropdown Menu -->
    <Transition name="fade">
      <div
        v-if="showMenu"
        class="absolute top-full right-0 mt-2 w-64 bg-slate-800 rounded-xl shadow-lg border border-slate-700 z-50"
        @click.stop
      >
        <div class="py-2">
          <div class="px-4 py-2 border-b border-slate-700">
            <p class="text-xs font-semibold text-slate-400 uppercase">{{ t('workspace.title') }}</p>
          </div>
          
          <div v-if="workspaces && workspaces.length > 0" class="max-h-64 overflow-y-auto">
            <button
              v-for="workspace in workspaces"
              :key="workspace.id"
              @click="selectWorkspace(workspace.id)"
              class="w-full text-left px-4 py-3 hover:bg-slate-700 transition-colors flex items-center justify-between"
              :class="workspace.id === activeWorkspaceId ? 'bg-slate-700/50' : ''"
            >
              <div class="flex-1 min-w-0">
                <div class="flex items-center space-x-2">
                  <p class="text-white font-medium truncate">{{ workspace.isPersonal ? t('workspace.mySpace') : workspace.name }}</p>
                  <span
                    v-if="workspace.isPersonal"
                    class="text-xs px-2 py-0.5 bg-slate-700 text-slate-300 rounded-full"
                    :title="t('workspace.personalTooltip')"
                  >
                    {{ t('workspace.personal') }}
                  </span>
                </div>
                <p class="text-xs text-slate-400">
                  {{ workspace.isPersonal ? t('workspace.personalDescription') : getSplitModeLabel(workspace.defaultSplitMode) }}
                </p>
              </div>
              <svg
                v-if="workspace.id === activeWorkspaceId"
                class="w-5 h-5 text-emerald-500 flex-shrink-0 ml-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </button>
          </div>
          
          <div class="px-4 py-2 border-t border-slate-700">
            <button
              @click="showCreateSheet = true"
              class="w-full text-left px-4 py-2 text-emerald-500 hover:bg-slate-700 rounded-lg transition-colors flex items-center space-x-2"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              <span>{{ t('workspace.create') }}</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Create Workspace Sheet -->
    <CreateWorkspaceSheet
      v-model="showCreateSheet"
      @created="handleWorkspaceCreated"
    />
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()
const workspacesStore = useWorkspacesStore()
const { activeWorkspace, activeWorkspaceId } = storeToRefs(workspacesStore)

// Access workspaces directly from store to ensure reactivity
const workspaces = computed(() => workspacesStore.workspaces)

const showMenu = ref(false)
const showCreateSheet = ref(false)

const selectWorkspace = (id: string) => {
  workspacesStore.setActiveWorkspace(id)
  showMenu.value = false
}

const handleWorkspaceCreated = () => {
  showCreateSheet.value = false
  showMenu.value = false
}

const getSplitModeLabel = (splitMode: 'EQUAL' | 'WEIGHTED'): string => {
  if (splitMode === 'EQUAL') {
    return t('workspace.splitModeEqual')
  } else if (splitMode === 'WEIGHTED') {
    return t('workspace.splitModeWeighted')
  }
  return splitMode
}

// Close menu when clicking outside
onMounted(() => {
  const handleClickOutside = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    if (!target.closest('.relative')) {
      showMenu.value = false
    }
  }
  document.addEventListener('click', handleClickOutside)
  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
  })
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
