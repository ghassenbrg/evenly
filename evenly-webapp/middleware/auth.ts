export default defineNuxtRouteMiddleware((to, from) => {
  const { isAuthenticated } = useAuth()
  
  if (!isAuthenticated.value) {
    const redirect = to.fullPath
    return navigateTo({
      path: '/login',
      query: redirect ? { redirect } : undefined
    })
  }
})
