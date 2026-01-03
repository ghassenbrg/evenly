import { defineStore } from 'pinia'
import type { Workspace, CreateWorkspaceRequest, UpdateWorkspaceSettingsRequest } from '~/types/api'
import { useApi } from '~/utils/api'
import { useCookie } from '#imports'

export const useWorkspacesStore = defineStore('workspaces', () => {
  const api = useApi()
  const workspaces = ref<Workspace[]>([])
  
  // Persist active workspace ID in cookies
  const activeWorkspaceIdCookie = useCookie<string | null>('activeWorkspaceId', {
    default: () => null,
    maxAge: 60 * 60 * 24 * 365 // 1 year
  })
  
  const activeWorkspaceId = ref<string | null>(activeWorkspaceIdCookie.value)

  const activeWorkspace = computed(() => 
    workspaces.value.find(w => w.id === activeWorkspaceId.value) || null
  )
  
  // Save active workspace ID to cookie whenever it changes
  watch(activeWorkspaceId, (newId) => {
    activeWorkspaceIdCookie.value = newId
  })

  const fetchWorkspaces = async () => {
    workspaces.value = await api.get<Workspace[]>('/api/workspaces')
    // Sort workspaces: personal first, then others
    workspaces.value.sort((a, b) => {
      if (a.isPersonal && !b.isPersonal) return -1
      if (!a.isPersonal && b.isPersonal) return 1
      return 0
    })
    
    // Validate and set active workspace
    if (workspaces.value.length > 0) {
      const savedWorkspaceId = activeWorkspaceIdCookie.value
      
      // Check if saved workspace still exists
      const savedWorkspaceExists = savedWorkspaceId && workspaces.value.some(w => w.id === savedWorkspaceId)
      
      if (savedWorkspaceExists) {
        // Use saved workspace
        activeWorkspaceId.value = savedWorkspaceId
      } else {
        // Default to personal workspace, or first workspace if no personal exists
        const personalWorkspace = workspaces.value.find(w => w.isPersonal)
        activeWorkspaceId.value = personalWorkspace?.id || workspaces.value[0].id
      }
    } else {
      // No workspaces available
      activeWorkspaceId.value = null
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
      // Cookie is automatically updated via watch
    } else {
      // If workspace doesn't exist, fallback to personal or first workspace
      const personalWorkspace = workspaces.value.find(w => w.isPersonal)
      activeWorkspaceId.value = personalWorkspace?.id || workspaces.value[0]?.id || null
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

