import type { WorkspaceMember } from '~/types/api'
import { useApi } from '~/utils/api'

export const useWorkspaceMembers = () => {
  const api = useApi()
  const members = ref<WorkspaceMember[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetchMembers = async (workspaceId: string) => {
    loading.value = true
    error.value = null
    try {
      members.value = await api.get<WorkspaceMember[]>(`/api/workspaces/${workspaceId}/members`)
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearMembers = () => {
    members.value = []
    error.value = null
  }

  return {
    members: readonly(members),
    loading: readonly(loading),
    error: readonly(error),
    fetchMembers,
    clearMembers
  }
}

