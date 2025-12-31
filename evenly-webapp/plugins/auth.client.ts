export default defineNuxtPlugin(async () => {
  const { isAuthenticated } = useAuth()
  
  // If authenticated, fetch workspaces on app initialization
  if (isAuthenticated.value) {
    try {
      const workspacesStore = useWorkspacesStore()
      // Fetch workspaces - will only set activeWorkspaceId if workspaces exist
      await workspacesStore.fetchWorkspaces()
    } catch (error) {
      // Silently fail - user might not have workspaces yet or token might be invalid
      console.error('Failed to fetch workspaces on init:', error)
    }
  }
})

