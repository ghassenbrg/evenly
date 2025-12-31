import type { WorkspaceMember } from '~/types/api'
import { useApi } from '~/utils/api'

export const useWorkspaceMembers = () => {
  const api = useApi()
  const members = ref<WorkspaceMember[]>([])
  const loading = ref(false)

  const fetchMembers = async (workspaceId: string) => {
    loading.value = true
    try {
      members.value = await api.get<WorkspaceMember[]>(`/api/workspaces/${workspaceId}/members`)
    } finally {
      loading.value = false
    }
  }

  return {
    members: readonly(members),
    loading: readonly(loading),
    fetchMembers
  }
}

