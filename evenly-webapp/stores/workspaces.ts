import { defineStore } from 'pinia'
import type { Workspace, CreateWorkspaceRequest, UpdateWorkspaceSettingsRequest } from '~/types/api'
import { useApi } from '~/utils/api'

export const useWorkspacesStore = defineStore('workspaces', () => {
  const api = useApi()
  const workspaces = ref<Workspace[]>([])
  const activeWorkspaceId = ref<string | null>(null)

  const activeWorkspace = computed(() => 
    workspaces.value.find(w => w.id === activeWorkspaceId.value) || null
  )

  const fetchWorkspaces = async () => {
    workspaces.value = await api.get<Workspace[]>('/api/workspaces')
    // Sort workspaces: personal first, then others
    workspaces.value.sort((a, b) => {
      if (a.isPersonal && !b.isPersonal) return -1
      if (!a.isPersonal && b.isPersonal) return 1
      return 0
    })
    // Default to personal workspace on first load, or first workspace if no personal exists
    if (workspaces.value.length > 0 && !activeWorkspaceId.value) {
      const personalWorkspace = workspaces.value.find(w => w.isPersonal)
      activeWorkspaceId.value = personalWorkspace?.id || workspaces.value[0].id
    }
  }

  const isPersonalWorkspace = (workspaceId: string | null) => {
    if (!workspaceId) return false
    const workspace = workspaces.value.find(w => w.id === workspaceId)
    return workspace?.isPersonal ?? false
  }

  const canModifyWorkspace = (workspaceId: string | null) => {
    return !isPersonalWorkspace(workspaceId)
  }

  const createWorkspace = async (data: CreateWorkspaceRequest) => {
    const workspace = await api.post<Workspace>('/api/workspaces', data)
    workspaces.value.push(workspace)
    activeWorkspaceId.value = workspace.id
    return workspace
  }

  const updateWorkspaceSettings = async (id: string, data: UpdateWorkspaceSettingsRequest) => {
    if (isPersonalWorkspace(id)) {
      throw new Error('Cannot modify personal workspace settings')
    }
    const workspace = await api.put<Workspace>(`/api/workspaces/${id}/settings`, data)
    const index = workspaces.value.findIndex(w => w.id === id)
    if (index !== -1) {
      workspaces.value[index] = workspace
    }
    return workspace
  }

  const setActiveWorkspace = (id: string) => {
    if (workspaces.value.some(w => w.id === id)) {
      activeWorkspaceId.value = id
    }
  }

  return {
    workspaces: readonly(workspaces),
    activeWorkspaceId: readonly(activeWorkspaceId),
    activeWorkspace,
    fetchWorkspaces,
    createWorkspace,
    updateWorkspaceSettings,
    setActiveWorkspace,
    isPersonalWorkspace,
    canModifyWorkspace
  }
})

