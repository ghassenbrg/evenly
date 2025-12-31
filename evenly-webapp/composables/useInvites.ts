import type { Invite, CreateInviteRequest } from '~/types/api'
import { useApi } from '~/utils/api'

export const useInvites = () => {
  const api = useApi()
  const workspacesStore = useWorkspacesStore()

  const createInvite = async (workspaceId: string, data: CreateInviteRequest) => {
    if (workspacesStore.isPersonalWorkspace(workspaceId)) {
      throw new Error('Cannot create invites for personal workspace')
    }
    return await api.post<Invite>(`/api/workspaces/${workspaceId}/invites`, data)
  }

  const joinInvite = async (code: string) => {
    return await api.post(`/api/invites/join`, { code })
  }

  return {
    createInvite,
    joinInvite
  }
}

