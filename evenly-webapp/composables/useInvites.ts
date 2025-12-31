import type { Invite, CreateInviteRequest } from '~/types/api'
import { useApi } from '~/utils/api'

export const useInvites = () => {
  const api = useApi()

  const createInvite = async (workspaceId: string, data: CreateInviteRequest) => {
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

