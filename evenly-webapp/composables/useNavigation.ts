/**
 * Navigation composable for handling back button logic and main page detection
 */

// Main pages are those directly accessible from the bottom navigation bar
const MAIN_PAGES = ['/dashboard', '/expenses', '/history', '/settings'] as const

export const useNavigation = () => {
  const route = useRoute()
  const router = useRouter()

  /**
   * Check if the current route is a main page
   * A main page is one that:
   * 1. Has meta.isMainPage = true, OR
   * 2. Matches one of the MAIN_PAGES paths exactly
   */
  const isMainPage = computed(() => {
    // Check route meta first (preferred method)
    if (route.meta?.isMainPage === true) {
      return true
    }
    
    // Fallback to path matching
    return MAIN_PAGES.includes(route.path as typeof MAIN_PAGES[number])
  })

  /**
   * Navigate back with fallback logic
   * - Uses router.back() if history exists
   * - Falls back to a logical parent page or dashboard if no history
   */
  const goBack = async () => {
    // Try to go back in history first
    // router.back() will navigate to the previous route if available
    if (process.client) {
      // Check if we have browser history
      if (window.history.length > 1) {
        router.back()
        // Note: If router.back() doesn't navigate (e.g., no previous route),
        // the user will stay on the current page. This is acceptable behavior.
      } else {
        // No history available - use fallback
        const fallbackRoute = getFallbackRoute()
        await navigateTo(fallbackRoute)
      }
    } else {
      // Server-side: always use fallback
      const fallbackRoute = getFallbackRoute()
      await navigateTo(fallbackRoute)
    }
  }

  /**
   * Determine the fallback route when no history exists
   * Returns a logical parent page based on the current route structure
   */
  const getFallbackRoute = (): string => {
    const path = route.path

    // Wallets routes -> wallets index
    if (path.startsWith('/wallets')) {
      return '/wallets'
    }

    // Notifications -> dashboard (most common entry point)
    if (path === '/notifications') {
      return '/dashboard'
    }

    // Subscriptions/Transactions -> dashboard
    if (path === '/subscriptions' || path === '/transactions') {
      return '/dashboard'
    }

    // Default fallback to dashboard
    return '/dashboard'
  }

  return {
    isMainPage,
    goBack,
    getFallbackRoute
  }
}

